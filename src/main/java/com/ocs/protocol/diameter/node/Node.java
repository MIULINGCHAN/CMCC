package com.ocs.protocol.diameter.node;

import java.util.logging.*;
import java.io.*;
import java.net.*;
import java.lang.reflect.*;
import java.util.*;
import com.ocs.protocol.diameter.*;

public class Node
{
    private MessageDispatcher message_dispatcher;
    private ConnectionListener connection_listener;
    private NodeSettings settings;
    private NodeValidator node_validator;
    private NodeState node_state;
    private Thread reconnect_thread;
    private boolean please_stop;
    private long shutdown_deadline;
    private Map<ConnectionKey, Connection> map_key_conn;
    private Set<Peer> persistent_peers;
    private Logger logger;
    private Object obj_conn_wait;
    private NodeImplementation tcp_node;
    private NodeImplementation sctp_node;
    
    public Node(final MessageDispatcher messageDispatcher, final ConnectionListener connectionListener, final NodeSettings nodeSettings) {
        this(messageDispatcher, connectionListener, nodeSettings, null);
    }
    
    public Node(final MessageDispatcher messageDispatcher, final ConnectionListener connectionListener, final NodeSettings settings, final NodeValidator nodeValidator) {
        super();
        this.message_dispatcher = ((messageDispatcher == null) ? new DefaultMessageDispatcher() : messageDispatcher);
        this.connection_listener = ((connectionListener == null) ? new DefaultConnectionListener() : connectionListener);
        this.settings = settings;
        this.node_validator = ((nodeValidator == null) ? new DefaultNodeValidator() : nodeValidator);
        this.node_state = new NodeState();
        this.logger = Logger.getLogger("com.ocs.protocol.diameter.node");
        this.obj_conn_wait = new Object();
        this.tcp_node = null;
        this.sctp_node = null;
    }
    
    public void start() throws IOException, UnsupportedTransportProtocolException {
        this.logger.log(Level.INFO, "Starting Diameter node");
        this.please_stop = false;
        this.prepare();
        if (this.tcp_node != null) {
            this.tcp_node.start(); //开启select thread
        }
        if (this.sctp_node != null) {
            this.sctp_node.start();
        }
        (this.reconnect_thread = new ReconnectThread()).setDaemon(true);
        this.reconnect_thread.start();
        this.logger.log(Level.INFO, "Diameter node started");
    }
    
    public void stop() {
        this.stop(0L);
    }
    
    public void stop(final long n) {
        this.logger.log(Level.INFO, "Stopping Diameter node");
        this.shutdown_deadline = System.currentTimeMillis() + n;
        if (this.tcp_node != null) {
            this.tcp_node.initiateStop(this.shutdown_deadline);
        }
        if (this.sctp_node != null) {
            this.sctp_node.initiateStop(this.shutdown_deadline);
        }
        synchronized (this.map_key_conn) {
            this.please_stop = true;
            final Iterator<Map.Entry<ConnectionKey, Connection>> iterator = this.map_key_conn.entrySet().iterator();
            while (iterator.hasNext()) {
                final Connection connection = iterator.next().getValue();
                switch (connection.state) {
                    case connecting:
                    case connected_in:
                    case connected_out: {
                        this.logger.log(Level.FINE, "Closing connection to " + connection.host_id + " because we are shutting down");
                        iterator.remove();
                        connection.node_impl.closeConnection(connection);
                    }
                    case tls: {
                        continue;
                    }
                    case ready: {
                        this.initiateConnectionClose(connection, 0);
                    }
                    case closing: {
                        continue;
                    }
				default:
					break;
                }
            }
        }
        if (this.tcp_node != null) {
            this.tcp_node.wakeup();
        }
        if (this.sctp_node != null) {
            this.sctp_node.wakeup();
        }
        synchronized (this.map_key_conn) {
            this.map_key_conn.notify();
        }
        try {
            if (this.tcp_node != null) {
                this.tcp_node.join();
            }
            if (this.sctp_node != null) {
                this.sctp_node.join();
            }
            this.reconnect_thread.join();
        }
        catch (InterruptedException ex) {}
        this.reconnect_thread = null;
        synchronized (this.map_key_conn) {
            final Iterator<Map.Entry<ConnectionKey, Connection>> iterator2 = this.map_key_conn.entrySet().iterator();
            while (iterator2.hasNext()) {
                this.closeConnection(iterator2.next().getValue());
            }
        }
        synchronized (this.obj_conn_wait) {
            this.obj_conn_wait.notifyAll();
        }
        this.map_key_conn = null;
        this.persistent_peers = null;
        if (this.tcp_node != null) {
            this.tcp_node.closeIO();
        }
        if (this.sctp_node != null) {
            this.sctp_node.closeIO();
        }
        this.logger.log(Level.INFO, "Diameter node stopped");
    }
    
    private boolean anyReadyConnection() {
        synchronized (this.map_key_conn) {
            final Iterator<Map.Entry<ConnectionKey, Connection>> iterator = this.map_key_conn.entrySet().iterator();
            while (iterator.hasNext()) {
                if (iterator.next().getValue().state == Connection.State.ready) {
                    return true;
                }
            }
        }
        return false;
    }
    
    public void waitForConnection() throws InterruptedException {
        synchronized (this.obj_conn_wait) {
            while (!this.anyReadyConnection()) {
                this.obj_conn_wait.wait();
            }
        }
    }
    
    public void waitForConnection(final long n) throws InterruptedException {
        final long n2 = System.currentTimeMillis() + n;
        synchronized (this.obj_conn_wait) {
            for (long n3 = System.currentTimeMillis(); !this.anyReadyConnection() && n3 < n2; n3 = System.currentTimeMillis()) {
                this.obj_conn_wait.wait(n2 - n3);
            }
        }
    }
    
    public ConnectionKey findConnection(final Peer peer) {
        this.logger.log(Level.FINER, "Finding '" + peer.host() + "'");
        synchronized (this.map_key_conn) {
            final Iterator<Map.Entry<ConnectionKey, Connection>> iterator = this.map_key_conn.entrySet().iterator();
            while (iterator.hasNext()) {
                final Connection connection = iterator.next().getValue();
                if (connection.state != Connection.State.ready) {
                    continue;
                }
                if (connection.peer != null && connection.peer.equals(peer)) {
                    return connection.key;
                }
            }
            this.logger.log(Level.FINER, peer.host() + " NOT found");
            return null;
        }
    }
    
    public boolean isConnectionKeyValid(final ConnectionKey connectionKey) {
        synchronized (this.map_key_conn) {
            return this.map_key_conn.get(connectionKey) != null;
        }
    }
    
    public Peer connectionKey2Peer(final ConnectionKey connectionKey) {
        synchronized (this.map_key_conn) {
            final Connection connection = this.map_key_conn.get(connectionKey);
            if (connection != null) {
                return connection.peer;
            }
            return null;
        }
    }
    
    public InetAddress connectionKey2InetAddress(final ConnectionKey connectionKey) {
        synchronized (this.map_key_conn) {
            final Connection connection = this.map_key_conn.get(connectionKey);
            if (connection != null) {
                return connection.toInetAddress();
            }
            return null;
        }
    }
    
    public int nextHopByHopIdentifier(final ConnectionKey connectionKey) throws StaleConnectionException {
        synchronized (this.map_key_conn) {
            final Connection connection = this.map_key_conn.get(connectionKey);
            if (connection == null) {
                throw new StaleConnectionException();
            }
            return connection.nextHopByHopIdentifier();
        }
    }
    
    public void sendMessage(final Message message, final ConnectionKey connectionKey) throws StaleConnectionException {
        synchronized (this.map_key_conn) {
            final Connection connection = this.map_key_conn.get(connectionKey);
            if (connection == null) {
                throw new StaleConnectionException();
            }
            if (connection.state != Connection.State.ready) {
                throw new StaleConnectionException();
            }
            this.sendMessage(message, connection);
        }
    }
    
    private void sendMessage(final Message message, final Connection connection) {
        this.logger.log(Level.FINER, "command=" + message.hdr.command_code + ", to=" + ((connection.peer != null) ? connection.peer.toString() : connection.host_id));
        final byte[] encode = message.encode();
        if (this.logger.isLoggable(Level.FINEST)) {
            this.hexDump(Level.FINEST, "Raw packet encoded", encode, 0, encode.length);
        }
        connection.sendMessage(encode);
    }
    
    public void initiateConnection(final Peer peer, final boolean b) {
        if (b) {
            synchronized (this.persistent_peers) {
                this.persistent_peers.add(new Peer(peer));
            }
        }
        synchronized (this.map_key_conn) {
            final Iterator<Map.Entry<ConnectionKey, Connection>> iterator = this.map_key_conn.entrySet().iterator();
            while (iterator.hasNext()) {
                final Connection connection = iterator.next().getValue();
                if (connection.peer != null && connection.peer.equals(peer)) {
                    return;
                }
            }
            this.logger.log(Level.INFO, "Initiating connection to '" + peer.host() + "' port " + peer.port());
            NodeImplementation nodeImplementation = null;
            switch (peer.transportProtocol()) {
                case tcp: {
                    nodeImplementation = this.tcp_node;
                    break;
                }
                case sctp: {
                    nodeImplementation = this.sctp_node;
                    break;
                }
            }
            if (nodeImplementation != null) {
                final Connection connection2 = nodeImplementation.newConnection(this.settings.watchdogInterval(), this.settings.idleTimeout());
                connection2.host_id = peer.host();
                connection2.peer = peer;
                if (nodeImplementation.initiateConnection(connection2, peer)) {
                    this.map_key_conn.put(connection2.key, connection2);
                }
            }
        }
    }
    
    private static Boolean getUseOption(final Boolean b, final String s, final Boolean b2) {
        if (b != null) {
            return b;
        }
        final String property = System.getProperty(s);
        if (property != null && property.equals("true")) {
            return true;
        }
        if (property != null && property.equals("false")) {
            return false;
        }
        if (property != null && property.equals("maybe")) {
            return null;
        }
        return b2;
    }
    
    private NodeImplementation instantiateNodeImplementation(final Level level, final String s) {
        ClassLoader classLoader = this.getClass().getClassLoader();
        if (classLoader == null) {
            classLoader = ClassLoader.getSystemClassLoader();
        }
        try {
            final Class<?> loadClass = classLoader.loadClass(s);
            Constructor<?> constructor;
            try {
                constructor = loadClass.getConstructor(this.getClass(), this.settings.getClass(), this.logger.getClass());
            }
            catch (NoSuchMethodException ex) {
                this.logger.log(level, "Could not find constructor for " + s, ex);
                return null;
            }
            catch (NoClassDefFoundError noClassDefFoundError) {
                if (level != Level.FINE) {
                    this.logger.log(level, "Could not find constructor for " + s, noClassDefFoundError);
                }
                else {
                    this.logger.log(level, "Could not find constructor for " + s);
                }
                return null;
            }
            catch (UnsatisfiedLinkError unsatisfiedLinkError) {
                this.logger.log(level, "Could not find constructor for " + s, unsatisfiedLinkError);
                return null;
            }
            if (constructor == null) {
                return null;
            }
            try {
                return (NodeImplementation)constructor.newInstance(this, this.settings, this.logger);
            }
            catch (InstantiationException ex3) {
                return null;
            }
            catch (IllegalAccessException ex4) {
                return null;
            }
            catch (InvocationTargetException ex5) {
                return null;
            }
            catch (UnsatisfiedLinkError unsatisfiedLinkError2) {
                this.logger.log(level, "Could not construct a " + s, unsatisfiedLinkError2);
                return null;
            }
        }
        catch (ClassNotFoundException ex2) {
            this.logger.log(level, "class " + s + " not found/loaded", ex2);
            return null;
        }
    }
    
    private NodeImplementation loadTransportProtocol(final Boolean b, final String s, final Boolean b2, final String s2, final String s3) throws IOException, UnsupportedTransportProtocolException {
        final Boolean useOption = getUseOption(b, s, b2);
        NodeImplementation instantiateNodeImplementation = null;
        if (useOption == null || useOption) {
            instantiateNodeImplementation = this.instantiateNodeImplementation((useOption != null) ? Level.INFO : Level.FINE, s2);
            if (instantiateNodeImplementation != null) {
                instantiateNodeImplementation.openIO(); //开启selector 开启serverSocketChannel,并将socket bind到该channel上
            }
            else if (useOption != null) {
                throw new UnsupportedTransportProtocolException(s3 + " support could not be loaded");
            }
        }
        this.logger.log(Level.INFO, s3 + " support was " + ((instantiateNodeImplementation != null) ? "loaded" : "not loaded"));
        return instantiateNodeImplementation;
    }
    
    private void prepare() throws IOException, UnsupportedTransportProtocolException {
        this.tcp_node = this.loadTransportProtocol(this.settings.useTCP(), "com.ocs.protocol.diameter.node.use_tcp", true, "com.ocs.protocol.diameter.node.TCPNode", "TCP");
//        this.sctp_node = this.loadTransportProtocol(this.settings.useSCTP(), "com.ocs.protocol.diameter.node.use_sctp", null, "com.ocs.protocol.diameter.node.SCTPNode", "SCTP");
        if (this.tcp_node == null && this.sctp_node == null) {
            this.logger.log(Level.WARNING, "No transport protocol classes could be loaded. The stack is running but without have any connectivity");
        }
        this.map_key_conn = new HashMap<ConnectionKey, Connection>();
        this.persistent_peers = new HashSet<Peer>();
    }
    
    long calcNextTimeout(final NodeImplementation nodeImplementation) {
        long shutdown_deadline = -1L;
        synchronized (this.map_key_conn) {
            final Iterator<Map.Entry<ConnectionKey, Connection>> iterator = this.map_key_conn.entrySet().iterator();
            while (iterator.hasNext()) {
                final Connection connection = iterator.next().getValue();
                if (connection.node_impl != nodeImplementation) {
                    continue;
                }
                final long calcNextTimeout = connection.timers.calcNextTimeout(connection.state == Connection.State.ready);
                if (shutdown_deadline != -1L && calcNextTimeout >= shutdown_deadline) {
                    continue;
                }
                shutdown_deadline = calcNextTimeout;
            }
        }
        if (this.please_stop && this.shutdown_deadline < shutdown_deadline) {
            shutdown_deadline = this.shutdown_deadline;
        }
        return shutdown_deadline;
    }
    
    void runTimers(final NodeImplementation nodeImplementation) {
        synchronized (this.map_key_conn) {
            final Iterator<Map.Entry<ConnectionKey, Connection>> iterator = this.map_key_conn.entrySet().iterator();
            while (iterator.hasNext()) {
                final Connection connection = iterator.next().getValue();
                if (connection.node_impl != nodeImplementation) {
                    continue;
                }
                switch (connection.timers.calcAction(connection.state == Connection.State.ready)) {
                    case none: {
                    	//正常测试是执行这个分支
                    	continue;
                    }
                    case disconnect_no_cer: {
                        this.logger.log(Level.WARNING, "Disconnecting due to no CER/CEA");
                        this.closeConnection(connection);
                        continue;
                    }
                    case disconnect_idle: {
                        this.logger.log(Level.WARNING, "Disconnecting due to idle");
                        this.initiateConnectionClose(connection, 1);
                        continue;
                    }
                    case disconnect_no_dw: {
                        this.logger.log(Level.WARNING, "Disconnecting due to no DWA");
                        this.closeConnection(connection);
                        continue;
                    }
                    case dwr: {
                        this.sendDWR(connection);
                        continue;
                    }
                }
            }
        }
    }
    
    void logRawDecodedPacket(final byte[] array, final int n, final int n2) {
        this.hexDump(Level.FINEST, "Raw packet decoded", array, n, n2);
    }
    
    void logGarbagePacket(final Connection connection, final byte[] array, final int n, final int n2) {
        this.hexDump(Level.WARNING, "Garbage from " + connection.host_id, array, n, n2);
    }
    
    void hexDump(final Level level, final String s, final byte[] array, final int n, int n2) {
        if (!this.logger.isLoggable(level)) {
            return;
        }
        if (n2 > 1024) {
            n2 = 1024;
        }
        final StringBuffer sb = new StringBuffer(s.length() + 1 + n2 * 3 + (n2 / 16 + 1) * 15);
        sb.append(s + "\n");
        for (int i = 0; i < n2; i += 16) {
            sb.append(String.format("%04X ", new Integer(i)));
            for (int j = i; j < i + 16; ++j) {
                if (j % 4 == 0) {
                    sb.append(' ');
                }
                if (j < n2) {
                    sb.append(String.format("%02X", array[n + j]));
                }
                else {
                    sb.append("  ");
                }
            }
            sb.append("     ");
            for (int n3 = i; n3 < i + 16 && n3 < n2; ++n3) {
                final byte b = array[n + n3];
                if (b >= 32 && b < 127) {
                    sb.append((char)b);
                }
                else {
                    sb.append('.');
                }
            }
            sb.append('\n');
        }
        if (n2 > 1024) {
            sb.append("...\n");
        }
        this.logger.log(level, sb.toString());
    }
    
    void closeConnection(final Connection connection) {
        this.closeConnection(connection, false);
    }
    
    void closeConnection(final Connection connection, final boolean b) {
        if (connection.state == Connection.State.closed) {
            return;
        }
        this.logger.log(Level.INFO, "Closing connection to " + ((connection.peer != null) ? connection.peer.toString() : connection.host_id));
        synchronized (this.map_key_conn) {
            connection.node_impl.close(connection, b);
            this.map_key_conn.remove(connection.key);
            connection.state = Connection.State.closed;
        }
        this.connection_listener.handle(connection.key, connection.peer, false);
    }
    
    private void initiateConnectionClose(final Connection connection, final int n) {
        if (connection.state != Connection.State.ready) {
            return;
        }
        this.sendDPR(connection, n);
        connection.state = Connection.State.closing;
    }
    
    boolean handleMessage(final Message message, final Connection connection) {
        if (this.logger.isLoggable(Level.FINE)) {
            this.logger.log(Level.FINE, "command_code=" + message.hdr.command_code + " application_id=" + message.hdr.application_id + " connection_state=" + connection.state);
        }
        connection.timers.markActivity();
        if (connection.state == Connection.State.connected_in) {
            if (!message.hdr.isRequest() || message.hdr.command_code != 257 || message.hdr.application_id != 0) {
                this.logger.log(Level.WARNING, "Got something that wasn't a CER");
                return false;
            }
            connection.timers.markRealActivity();
            return this.handleCER(message, connection);
        }
        else if (connection.state == Connection.State.connected_out) {
            if (message.hdr.isRequest() || message.hdr.command_code != 257 || message.hdr.application_id != 0) {
                this.logger.log(Level.WARNING, "Got something that wasn't a CEA");
                return false;
            }
            connection.timers.markRealActivity();
            return this.handleCEA(message, connection);
        }
        else {
            switch (message.hdr.command_code) {
                case 257: {
                    this.logger.log(Level.WARNING, "Got CER from " + connection.host_id + " after initial capability-exchange");
                    return false;
                }
                case 280: {
                    if (message.hdr.isRequest()) {
                        return this.handleDWR(message, connection);
                    }
                    return this.handleDWA(message, connection);
                }
                case 282: {
                    if (message.hdr.isRequest()) {
                        return this.handleDPR(message, connection);
                    }
                    return this.handleDPA(message, connection);
                }
                default: {
                    connection.timers.markRealActivity();
                    if (message.hdr.isRequest()) {
                        if (this.isLoopedMessage(message)) {
                            this.rejectLoopedRequest(message, connection);
                            return true;
                        }
                        if (!this.isAllowedApplication(message, connection.peer)) {
                            this.rejectDisallowedRequest(message, connection);
                            return true;
                        }
                    }
                    return this.message_dispatcher.handle(message, connection.key, connection.peer) || !message.hdr.isRequest() || this.handleUnknownRequest(message, connection);
                }
            }
        }
    }
    
    private boolean isLoopedMessage(final Message message) {
        final Iterator<AVP> iterator = message.subset(282).iterator();
        while (iterator.hasNext()) {
            if (new AVP_UTF8String(iterator.next()).queryValue().equals(this.settings.hostId())) {
                return true;
            }
        }
        return false;
    }
    
    private void rejectLoopedRequest(final Message message, final Connection connection) {
        this.logger.log(Level.WARNING, "Rejecting looped request from " + connection.peer.host() + " (command=" + message.hdr.command_code + ").");
        this.rejectRequest(message, connection, 3005);
    }
    
    public boolean isAllowedApplication(final Message message, final Peer peer) {
        try {
            final AVP find = message.find(258);
            if (find != null) {
                final int queryValue = new AVP_Unsigned32(find).queryValue();
                if (this.logger.isLoggable(Level.FINE)) {
                    this.logger.log(Level.FINE, "auth-application-id=" + queryValue);
                }
                return peer.capabilities.isAllowedAuthApp(queryValue);
            }
            final AVP find2 = message.find(259);
            if (find2 != null) {
                final int queryValue2 = new AVP_Unsigned32(find2).queryValue();
                if (this.logger.isLoggable(Level.FINE)) {
                    this.logger.log(Level.FINE, "acct-application-id=" + queryValue2);
                }
                return peer.capabilities.isAllowedAcctApp(queryValue2);
            }
            final AVP find3 = message.find(260);
            if (find3 != null) {
                final AVP[] queryAVPs = new AVP_Grouped(find3).queryAVPs();
                if (queryAVPs.length == 2 && queryAVPs[0].code == 266) {
                    final int queryValue3 = new AVP_Unsigned32(queryAVPs[0]).queryValue();
                    final int queryValue4 = new AVP_Unsigned32(queryAVPs[1]).queryValue();
                    if (this.logger.isLoggable(Level.FINE)) {
                        this.logger.log(Level.FINE, "vendor-id=" + queryValue3 + ", app=" + queryValue4);
                    }
                    if (queryAVPs[1].code == 258) {
                        return peer.capabilities.isAllowedAuthApp(queryValue3, queryValue4);
                    }
                    if (queryAVPs[1].code == 259) {
                        return peer.capabilities.isAllowedAcctApp(queryValue3, queryValue4);
                    }
                }
                return false;
            }
            this.logger.log(Level.WARNING, "No auth-app-id, acct-app-id nor vendor-app in packet");
        }
        catch (InvalidAVPLengthException ex) {
            this.logger.log(Level.INFO, "Encountered invalid AVP length while looking at application-id", ex);
        }
        return false;
    }
    
    private void rejectDisallowedRequest(final Message message, final Connection connection) {
        this.logger.log(Level.WARNING, "Rejecting request  from " + connection.peer.host() + " (command=" + message.hdr.command_code + ") because it is not allowed.");
        this.rejectRequest(message, connection, 3007);
    }
    
    private void rejectRequest(final Message message, final Connection connection, final int n) {
        final Message mandatory_RFC3588 = new Message();
        mandatory_RFC3588.prepareResponse(message);
        if (n >= 3000 && n <= 3999) {
            mandatory_RFC3588.hdr.setError(true);
        }
        mandatory_RFC3588.add(new AVP_Unsigned32(268, n));
        this.addOurHostAndRealm(mandatory_RFC3588);
        Utils.copyProxyInfo(message, mandatory_RFC3588);
        Utils.setMandatory_RFC3588(mandatory_RFC3588);
        this.sendMessage(mandatory_RFC3588, connection);
    }
    
    public void addOurHostAndRealm(final Message message) {
        message.add(new AVP_UTF8String(264, this.settings.hostId()));
        message.add(new AVP_UTF8String(296, this.settings.realm()));
    }
    
    public int nextEndToEndIdentifier() {
        return this.node_state.nextEndToEndIdentifier();
    }
    
    public String makeNewSessionId() {
        return this.makeNewSessionId(null);
    }
    
    public String makeNewSessionId(final String s) {
        final String string = this.settings.hostId() + ";" + this.node_state.nextSessionId_second_part();
        if (s == null) {
            return string;
        }
        return string + ";" + s;
    }
    
    public int stateId() {
        return this.node_state.stateId();
    }
    
    private boolean doElection(final String s) {
//        final int compareTo = this.settings.hostId().compareTo(s);
//        if (compareTo == 0) {
//            this.logger.log(Level.WARNING, "Got CER with host-id=" + s + ". Suspecting this is a connection from ourselves.");
//            return false;
//        }
//        final boolean b = compareTo > 0;
//        synchronized (this.map_key_conn) {
//            final Iterator<Map.Entry<ConnectionKey, Connection>> iterator = this.map_key_conn.entrySet().iterator();
//            while (iterator.hasNext()) {
//                final Connection connection = iterator.next().getValue();
//                if (connection.host_id != null && connection.host_id.equals(s) && connection.state == Connection.State.ready) {
//                    this.logger.log(Level.INFO, "New connection to a peer we already have a connection to (" + s + ")");
//                    if (b) {
//                        this.closeConnection(connection);
//                        return true;
//                    }
//                    return false;
//                }
//            }
//        }
//        return true;
    	
    	//为了在单机测试，这里先不检查同一个IP断掉连接的情况
    	return true;
    }
    
    private boolean handleCER(final Message message, final Connection connection) {
        this.logger.log(Level.FINE, "CER received from " + connection.host_id);
        final AVP find = message.find(264);
        if (find == null) {
            this.logger.log(Level.FINE, "CER from " + connection.host_id + " is missing the Origin-Host_id AVP. Rejecting.");
            final Message mandatory_RFC3588 = new Message();
            mandatory_RFC3588.prepareResponse(message);
            mandatory_RFC3588.add(new AVP_Unsigned32(268, 5005));
            this.addOurHostAndRealm(mandatory_RFC3588);
            mandatory_RFC3588.add(new AVP_FailedAVP(new AVP_UTF8String(264, "")));
            Utils.setMandatory_RFC3588(mandatory_RFC3588);
            this.sendMessage(mandatory_RFC3588, connection);
            return false;
        }
        final String queryValue = new AVP_UTF8String(find).queryValue();
        this.logger.log(Level.FINER, "Peer's origin-host-id is " + queryValue);
        final NodeValidator.AuthenticationResult authenticateNode = this.node_validator.authenticateNode(queryValue, connection.getRelevantNodeAuthInfo());
        if (authenticateNode == null || !authenticateNode.known) {
            this.logger.log(Level.FINE, "We do not know " + connection.host_id + " Rejecting.");
            final Message mandatory_RFC2 = new Message();
            mandatory_RFC2.prepareResponse(message);
            if (authenticateNode != null && authenticateNode.result_code != null) {
                mandatory_RFC2.add(new AVP_Unsigned32(268, authenticateNode.result_code));
            }
            else {
                mandatory_RFC2.add(new AVP_Unsigned32(268, 3010));
            }
            this.addOurHostAndRealm(mandatory_RFC2);
            if (authenticateNode != null && authenticateNode.error_message != null) {
                mandatory_RFC2.add(new AVP_UTF8String(281, authenticateNode.error_message));
            }
            Utils.setMandatory_RFC3588(mandatory_RFC2);
            this.sendMessage(mandatory_RFC2, connection);
            return false;
        }
        if (!this.doElection(queryValue)) {
            this.logger.log(Level.FINE, "CER from " + connection.host_id + " lost the election. Rejecting.");
            final Message mandatory_RFC3 = new Message();
            mandatory_RFC3.prepareResponse(message);
            mandatory_RFC3.add(new AVP_Unsigned32(268, 4003));
            this.addOurHostAndRealm(mandatory_RFC3);
            Utils.setMandatory_RFC3588(mandatory_RFC3);
            this.sendMessage(mandatory_RFC3, connection);
            return false;
        }
        (connection.peer = connection.toPeer()).host(queryValue);
        connection.host_id = queryValue;
        if (this.handleCEx(message, connection)) {
            final Message mandatory_RFC4 = new Message();
            mandatory_RFC4.prepareResponse(message);
            mandatory_RFC4.add(new AVP_Unsigned32(268, 2001));
            this.addCEStuff(mandatory_RFC4, connection.peer.capabilities, connection);
            this.logger.log(Level.INFO, "Connection to " + connection.peer.toString() + " is now ready");
            Utils.setMandatory_RFC3588(mandatory_RFC4);
            this.sendMessage(mandatory_RFC4, connection);
            connection.state = Connection.State.ready;
            this.connection_listener.handle(connection.key, connection.peer, true);
            synchronized (this.obj_conn_wait) {
                this.obj_conn_wait.notifyAll();
            }
            return true;
        }
        return false;
    }
    
    private boolean handleCEA(final Message message, final Connection connection) {
        this.logger.log(Level.FINE, "CEA received from " + connection.host_id);
        final AVP find = message.find(268);
        if (find == null) {
            this.logger.log(Level.WARNING, "CEA from " + connection.host_id + " did not contain a Result-Code AVP. Dropping connection");
            return false;
        }
        int queryValue;
        try {
            queryValue = new AVP_Unsigned32(find).queryValue();
        }
        catch (InvalidAVPLengthException ex) {
            this.logger.log(Level.INFO, "CEA from " + connection.host_id + " contained an ill-formed Result-Code. Dropping connection");
            return false;
        }
        if (queryValue != 2001) {
            this.logger.log(Level.INFO, "CEA from " + connection.host_id + " was rejected with Result-Code " + queryValue + ". Dropping connection");
            return false;
        }
        final AVP find2 = message.find(264);
        if (find2 == null) {
            this.logger.log(Level.WARNING, "Peer did not include origin-host-id in CEA");
            return false;
        }
        final String queryValue2 = new AVP_UTF8String(find2).queryValue();
        this.logger.log(Level.FINER, "Node:Peer's origin-host-id is '" + queryValue2 + "'");
        (connection.peer = connection.toPeer()).host(queryValue2);
        connection.host_id = queryValue2;
        if (this.handleCEx(message, connection)) {
            connection.state = Connection.State.ready;
            this.logger.log(Level.INFO, "Connection to " + connection.peer.toString() + " is now ready");
            this.connection_listener.handle(connection.key, connection.peer, true);
            synchronized (this.obj_conn_wait) {
                this.obj_conn_wait.notifyAll();
            }
            return true;
        }
        return false;
    }
    
    private boolean handleCEx(final Message message, final Connection connection) {
        this.logger.log(Level.FINER, "Processing CER/CEA");
        try {
            final Capability capability = new Capability();
            final Iterator<AVP> iterator = message.subset(265).iterator();
            while (iterator.hasNext()) {
                final int queryValue = new AVP_Unsigned32(iterator.next()).queryValue();
                this.logger.log(Level.FINEST, "peer supports vendor " + queryValue);
                capability.addSupportedVendor(queryValue);
            }
            final Iterator<AVP> iterator2 = message.subset(258).iterator();
            while (iterator2.hasNext()) {
                final int queryValue2 = new AVP_Unsigned32(iterator2.next()).queryValue();
                this.logger.log(Level.FINEST, "peer supports auth-app " + queryValue2);
                if (queryValue2 != 0) {
                    capability.addAuthApp(queryValue2);
                }
            }
            final Iterator<AVP> iterator3 = message.subset(259).iterator();
            while (iterator3.hasNext()) {
                final int queryValue3 = new AVP_Unsigned32(iterator3.next()).queryValue();
                this.logger.log(Level.FINEST, "peer supports acct-app " + queryValue3);
                if (queryValue3 != 0) {
                    capability.addAcctApp(queryValue3);
                }
            }
            for (final AVP avp : message.subset(260)) {
                final AVP[] queryAVPs = new AVP_Grouped(avp).queryAVPs();
                if (queryAVPs.length < 2 || queryAVPs[0].code != 266) {
                    throw new InvalidAVPValueException(avp);
                }
                final int queryValue4 = new AVP_Unsigned32(queryAVPs[0]).queryValue();
                final int queryValue5 = new AVP_Unsigned32(queryAVPs[1]).queryValue();
                if (queryAVPs[1].code == 258) {
                    capability.addVendorAuthApp(queryValue4, queryValue5);
                }
                else {
                    if (queryAVPs[1].code != 259) {
                        throw new InvalidAVPValueException(avp);
                    }
                    capability.addVendorAcctApp(queryValue4, queryValue5);
                }
            }
            final Capability authorizeNode = this.node_validator.authorizeNode(connection.host_id, this.settings, capability);
            if (this.logger.isLoggable(Level.FINEST)) {
                String s = "";
                final Iterator<Integer> iterator5 = authorizeNode.supported_vendor.iterator();
                while (iterator5.hasNext()) {
                    s = s + "  supported_vendor " + iterator5.next() + "\n";
                }
                final Iterator<Integer> iterator6 = authorizeNode.auth_app.iterator();
                while (iterator6.hasNext()) {
                    s = s + "  auth_app " + iterator6.next() + "\n";
                }
                final Iterator<Integer> iterator7 = authorizeNode.acct_app.iterator();
                while (iterator7.hasNext()) {
                    s = s + "  acct_app " + iterator7.next() + "\n";
                }
                for (final Capability.VendorApplication vendorApplication : authorizeNode.auth_vendor) {
                    s = s + "  vendor_auth_app: vendor " + vendorApplication.vendor_id + ", application " + vendorApplication.application_id + "\n";
                }
                for (final Capability.VendorApplication vendorApplication2 : authorizeNode.acct_vendor) {
                    s = s + "  vendor_acct_app: vendor " + vendorApplication2.vendor_id + ", application " + vendorApplication2.application_id + "\n";
                }
                this.logger.log(Level.FINEST, "Resulting capabilities:\n" + s);
            }
            if (authorizeNode.isEmpty()) {
                this.logger.log(Level.WARNING, "No application in common with " + connection.host_id);
                if (message.hdr.isRequest()) {
                    final Message mandatory_RFC3588 = new Message();
                    mandatory_RFC3588.prepareResponse(message);
                    mandatory_RFC3588.add(new AVP_Unsigned32(268, 5010));
                    this.addOurHostAndRealm(mandatory_RFC3588);
                    Utils.setMandatory_RFC3588(mandatory_RFC3588);
                    this.sendMessage(mandatory_RFC3588, connection);
                }
                return false;
            }
            connection.peer.capabilities = authorizeNode;
        }
        catch (InvalidAVPLengthException ex) {
            this.logger.log(Level.WARNING, "Invalid AVP in CER/CEA", ex);
            if (message.hdr.isRequest()) {
                final Message mandatory_RFC2 = new Message();
                mandatory_RFC2.prepareResponse(message);
                mandatory_RFC2.add(new AVP_Unsigned32(268, 5014));
                this.addOurHostAndRealm(mandatory_RFC2);
                mandatory_RFC2.add(new AVP_FailedAVP(ex.avp));
                Utils.setMandatory_RFC3588(mandatory_RFC2);
                this.sendMessage(mandatory_RFC2, connection);
            }
            return false;
        }
        catch (InvalidAVPValueException ex2) {
            this.logger.log(Level.WARNING, "Invalid AVP in CER/CEA", ex2);
            if (message.hdr.isRequest()) {
                final Message mandatory_RFC3 = new Message();
                mandatory_RFC3.prepareResponse(message);
                mandatory_RFC3.add(new AVP_Unsigned32(268, 5004));
                this.addOurHostAndRealm(mandatory_RFC3);
                mandatory_RFC3.add(new AVP_FailedAVP(ex2.avp));
                Utils.setMandatory_RFC3588(mandatory_RFC3);
                this.sendMessage(mandatory_RFC3, connection);
            }
            return false;
        }
        return true;
    }
    
    void initiateCER(final Connection connection) {
        this.sendCER(connection);
    }
    
    private void sendCER(final Connection connection) {
        this.logger.log(Level.FINE, "Sending CER to " + connection.host_id);
        final Message mandatory_RFC3588 = new Message();
        mandatory_RFC3588.hdr.setRequest(true);
        mandatory_RFC3588.hdr.command_code = 257;
        mandatory_RFC3588.hdr.application_id = 0;
        mandatory_RFC3588.hdr.hop_by_hop_identifier = connection.nextHopByHopIdentifier();
        mandatory_RFC3588.hdr.end_to_end_identifier = this.node_state.nextEndToEndIdentifier();
        this.addCEStuff(mandatory_RFC3588, this.settings.capabilities(), connection);
        Utils.setMandatory_RFC3588(mandatory_RFC3588);
        this.sendMessage(mandatory_RFC3588, connection);
    }
    
    private void addCEStuff(final Message message, final Capability capability, final Connection connection) {
        this.addOurHostAndRealm(message);
        final Iterator<InetAddress> iterator = connection.getLocalAddresses().iterator();
        while (iterator.hasNext()) {
            message.add(new AVP_Address(257, iterator.next()));
        }
        message.add(new AVP_Unsigned32(266, this.settings.vendorId()));
        message.add(new AVP_UTF8String(269, this.settings.productName()));
        message.add(new AVP_Unsigned32(278, this.node_state.stateId()));
        final Iterator<Integer> iterator2 = capability.supported_vendor.iterator();
        while (iterator2.hasNext()) {
            message.add(new AVP_Unsigned32(265, iterator2.next()));
        }
        final Iterator<Integer> iterator3 = capability.auth_app.iterator();
        while (iterator3.hasNext()) {
            message.add(new AVP_Unsigned32(258, iterator3.next()));
        }
        final Iterator<Integer> iterator4 = capability.acct_app.iterator();
        while (iterator4.hasNext()) {
            message.add(new AVP_Unsigned32(259, iterator4.next()));
        }
        for (final Capability.VendorApplication vendorApplication : capability.auth_vendor) {
            message.add(new AVP_Grouped(260, new AVP[] { new AVP_Unsigned32(266, vendorApplication.vendor_id), new AVP_Unsigned32(258, vendorApplication.application_id) }));
        }
        for (final Capability.VendorApplication vendorApplication2 : capability.acct_vendor) {
            message.add(new AVP_Grouped(260, new AVP[] { new AVP_Unsigned32(266, vendorApplication2.vendor_id), new AVP_Unsigned32(259, vendorApplication2.application_id) }));
        }
        if (this.settings.firmwareRevision() != 0) {
            message.add(new AVP_Unsigned32(267, this.settings.firmwareRevision()));
        }
    }
    
    private boolean handleDWR(final Message message, final Connection connection) {
        this.logger.log(Level.INFO, "DWR received from " + connection.host_id);
        connection.timers.markDWR();
        final Message mandatory_RFC3588 = new Message();
        mandatory_RFC3588.prepareResponse(message);
        mandatory_RFC3588.add(new AVP_Unsigned32(268, 2001));
        this.addOurHostAndRealm(mandatory_RFC3588);
        mandatory_RFC3588.add(new AVP_Unsigned32(278, this.node_state.stateId()));
        Utils.setMandatory_RFC3588(mandatory_RFC3588);
        this.sendMessage(mandatory_RFC3588, connection);
        return true;
    }
    
    private boolean handleDWA(final Message message, final Connection connection) {
        this.logger.log(Level.FINE, "DWA received from " + connection.host_id);
        connection.timers.markDWA();
        return true;
    }
    
    private boolean handleDPR(final Message message, final Connection connection) {
        this.logger.log(Level.FINE, "DPR received from " + connection.host_id);
        final Message mandatory_RFC3588 = new Message();
        mandatory_RFC3588.prepareResponse(message);
        mandatory_RFC3588.add(new AVP_Unsigned32(268, 2001));
        this.addOurHostAndRealm(mandatory_RFC3588);
        Utils.setMandatory_RFC3588(mandatory_RFC3588);
        this.sendMessage(mandatory_RFC3588, connection);
        return false;
    }
    
    private boolean handleDPA(final Message message, final Connection connection) {
        if (connection.state == Connection.State.closing) {
            this.logger.log(Level.INFO, "Got a DPA from " + connection.host_id);
        }
        else {
            this.logger.log(Level.WARNING, "Got a DPA. This is not expected");
        }
        return false;
    }
    
    private boolean handleUnknownRequest(final Message message, final Connection connection) {
        this.logger.log(Level.INFO, "Unknown request received from " + connection.host_id);
        this.rejectRequest(message, connection, 3002);
        return true;
    }
    
    private void sendDWR(final Connection connection) {
        this.logger.log(Level.FINE, "Sending DWR to " + connection.host_id);
        final Message mandatory_RFC3588 = new Message();
        mandatory_RFC3588.hdr.setRequest(true);
        mandatory_RFC3588.hdr.command_code = 280;
        mandatory_RFC3588.hdr.application_id = 0;
        mandatory_RFC3588.hdr.hop_by_hop_identifier = connection.nextHopByHopIdentifier();
        mandatory_RFC3588.hdr.end_to_end_identifier = this.node_state.nextEndToEndIdentifier();
        this.addOurHostAndRealm(mandatory_RFC3588);
        mandatory_RFC3588.add(new AVP_Unsigned32(278, this.node_state.stateId()));
        Utils.setMandatory_RFC3588(mandatory_RFC3588);
        this.sendMessage(mandatory_RFC3588, connection);
        connection.timers.markDWR_out();
    }
    
    private void sendDPR(final Connection connection, final int n) {
        this.logger.log(Level.FINE, "Sending DPR to " + connection.host_id);
        final Message mandatory_RFC3588 = new Message();
        mandatory_RFC3588.hdr.setRequest(true);
        mandatory_RFC3588.hdr.command_code = 282;
        mandatory_RFC3588.hdr.application_id = 0;
        mandatory_RFC3588.hdr.hop_by_hop_identifier = connection.nextHopByHopIdentifier();
        mandatory_RFC3588.hdr.end_to_end_identifier = this.node_state.nextEndToEndIdentifier();
        this.addOurHostAndRealm(mandatory_RFC3588);
        mandatory_RFC3588.add(new AVP_Unsigned32(273, n));
        Utils.setMandatory_RFC3588(mandatory_RFC3588);
        this.sendMessage(mandatory_RFC3588, connection);
    }
    
    boolean anyOpenConnections(final NodeImplementation nodeImplementation) {
        synchronized (this.map_key_conn) {
            final Iterator<Map.Entry<ConnectionKey, Connection>> iterator = this.map_key_conn.entrySet().iterator();
            while (iterator.hasNext()) {
                if (iterator.next().getValue().node_impl == nodeImplementation) {
                    return true;
                }
            }
        }
        return false;
    }
    
    void registerInboundConnection(final Connection connection) {
        synchronized (this.map_key_conn) {
            this.map_key_conn.put(connection.key, connection);
        }
    }
    
    void unregisterConnection(final Connection connection) {
        synchronized (this.map_key_conn) {
            this.map_key_conn.remove(connection.key);
        }
    }
    
    Object getLockObject() {
        return this.map_key_conn;
    }
    
    private class ReconnectThread extends Thread
    {
        public ReconnectThread() {
            super("Diameter node reconnect thread");
        }
        
        public void run() {
            while (true) {
                synchronized (Node.this.map_key_conn) {
                    if (Node.this.please_stop) {
                        return;
                    }
                    try {
                        Node.this.map_key_conn.wait(30000L); //守护线程,30s后判断如果不是please_stop = true,则重新建立连接
                    }
                    catch (InterruptedException ex) {}
                    if (Node.this.please_stop) {
                        return;
                    }
                }
                synchronized (Node.this.persistent_peers) {
                    final Iterator<Peer> iterator = Node.this.persistent_peers.iterator();
                    while (iterator.hasNext()) {
                        Node.this.initiateConnection(iterator.next(), false);
                    }
                }
            }
        }
    }
}
