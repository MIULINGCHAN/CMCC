package com.ocs.protocol.diameter.node;

import java.io.*;
import java.util.logging.*;

import com.ocs.protocol.diameter.*;

import dk.i1.sctp.*;

import java.util.*;
import java.net.*;

class SCTPNode extends NodeImplementation
{
    private Thread node_thread;
    SCTPSocket sctp_socket;
    private boolean please_stop;
    private long shutdown_deadline;
    private Map<AssociationId, SCTPConnection> map;
    private LinkedList<OutstandingConnection> outstanding_connections;
    private boolean any_queued_messages;
    
    public SCTPNode(final Node node, final NodeSettings nodeSettings, final Logger logger) {
        super(node, nodeSettings, logger);
        this.map = new HashMap<AssociationId, SCTPConnection>();
        this.outstanding_connections = new LinkedList<OutstandingConnection>();
        this.any_queued_messages = false;
    }
    
    void openIO() throws IOException {
        this.sctp_socket = (SCTPSocket)new OneToManySCTPSocket();
        final sctp_event_subscribe sctp_event_subscribe = new sctp_event_subscribe();
        sctp_event_subscribe.sctp_data_io_event = true;
        sctp_event_subscribe.sctp_association_event = true;
        this.sctp_socket.subscribeEvents(sctp_event_subscribe);
        if (this.settings.port() != 0) {
            this.sctp_socket.bind(this.settings.port());
            this.sctp_socket.listen();
        }
        else {
            this.sctp_socket.bind();
        }
    }
    
    void start() {
        this.logger.log(Level.FINEST, "Starting SCTP node");
        this.please_stop = false;
        (this.node_thread = new SelectThread()).setDaemon(true);
        this.node_thread.start();
        this.logger.log(Level.FINEST, "Started SCTP node");
    }
    
    void wakeup() {
        this.logger.log(Level.FINEST, "Waking up selector thread");
        try {
            this.sctp_socket.wakeup();
        }
        catch (SocketException ex) {
            this.logger.log(Level.WARNING, "Could not wake up SCTP service thread", ex);
        }
    }
    
    void initiateStop(final long shutdown_deadline) {
        this.logger.log(Level.FINEST, "Initiating stop of SCTP node");
        this.please_stop = true;
        this.shutdown_deadline = shutdown_deadline;
        this.logger.log(Level.FINEST, "Initiated stop of SCTP node");
    }
    
    void join() {
        this.logger.log(Level.FINEST, "Joining node_thread thread");
        try {
            this.node_thread.join();
        }
        catch (InterruptedException ex) {}
        this.node_thread = null;
        this.logger.log(Level.FINEST, "Selector thread joined");
    }
    
    void closeIO() {
        this.logger.log(Level.FINEST, "Closing SCTP socket");
        if (this.sctp_socket != null) {
            try {
                this.sctp_socket.close();
            }
            catch (SocketException ex) {
                this.logger.log(Level.WARNING, "Error closing SCTP socket", ex);
            }
        }
        this.sctp_socket = null;
        this.logger.log(Level.FINEST, "Closed SCTP socket");
    }
    
    private void processChunk(final SCTPChunk sctpChunk) {
        this.logger.log(Level.FINEST, "processChunk()...");
        if (sctpChunk instanceof SCTPData) {
            this.logger.log(Level.FINEST, "Data chunk received");
            final SCTPData sctpData = (SCTPData)sctpChunk;
            this.processDataChunk(this.map.get(sctpData.sndrcvinfo.sinfo_assoc_id), sctpData);
        }
        else if (sctpChunk instanceof SCTPNotification) {
            final SCTPNotification sctpNotification = (SCTPNotification)sctpChunk;
            this.logger.log(Level.FINEST, "Notification chunk received");
            this.processNotificationChunk(sctpNotification);
        }
        else {
            this.logger.log(Level.WARNING, "Received unknown SCTP chunk from SCTP socket:" + sctpChunk.toString());
        }
    }
    
    private void processDataChunk(final SCTPConnection sctpConnection, final SCTPData sctpData) {
        final int length = sctpData.getLength();
        final byte[] data = sctpData.getData();
        if (length < 4) {
            this.logGarbagePacket(sctpConnection, data, 0, length);
            this.closeConnection(sctpConnection, true);
            return;
        }
        final int decodeSize = Message.decodeSize(data, 0);
        if (length < decodeSize) {
            this.logGarbagePacket(sctpConnection, data, 0, length);
            this.closeConnection(sctpConnection, true);
            return;
        }
        final Message message = new Message();
        switch (message.decode(data, 0, decodeSize)) {
            case decoded: {
                this.logRawDecodedPacket(data, 0, decodeSize);
                if (!this.handleMessage(message, sctpConnection)) {
                    this.logger.log(Level.FINER, "handle error");
                    this.closeConnection(sctpConnection);
                    return;
                }
                break;
            }
            case garbage: {
                this.logGarbagePacket(sctpConnection, data, 0, decodeSize);
                this.closeConnection(sctpConnection, true);
            }
        }
    }
    
    private void processNotificationChunk(final SCTPNotification sctpNotification) {
        switch (sctpNotification.type) {
            case SCTP_ASSOC_CHANGE: {
                this.logger.log(Level.FINEST, "Association-change notification received");
                final SCTPNotificationAssociationChange sctpNotificationAssociationChange = (SCTPNotificationAssociationChange)sctpNotification;
                final AssociationId sac_assoc_id = sctpNotificationAssociationChange.sac_assoc_id;
                if (sctpNotificationAssociationChange.sac_state == SCTPNotificationAssociationChange.State.SCTP_COMM_UP) {
                    this.logger.log(Level.FINE, "Got an association");
                    InetAddress inetAddress = null;
                    Collection peerInetAddresses;
                    int peerInetPort;
                    try {
                        peerInetAddresses = this.sctp_socket.getPeerInetAddresses(sac_assoc_id);
                        final Iterator<InetAddress> iterator = peerInetAddresses.iterator();
                        if (iterator.hasNext()) {
                            inetAddress = iterator.next();
                        }
                        peerInetPort = this.sctp_socket.getPeerInetPort(sac_assoc_id);
                    }
                    catch (SocketException ex) {
                        this.logger.log(Level.WARNING, "Caught SocketException while retrieving SCTP peer address", ex);
                        try {
                            this.sctp_socket.disconnect(sac_assoc_id, true);
                        }
                        catch (SocketException ex3) {}
                        return;
                    }
                    this.logger.log(Level.INFO, "Got an association connection from " + inetAddress.toString() + " port " + sac_assoc_id);
                    SCTPConnection conn = null;
                    if (!this.outstanding_connections.isEmpty()) {
                        final OutstandingConnection outstandingConnection = this.outstanding_connections.peek();
                        final Iterator<InetAddress> iterator2 = peerInetAddresses.iterator();
                        while (iterator2.hasNext()) {
                            if (iterator2.next().equals(outstandingConnection.address) && peerInetPort == outstandingConnection.peer.port()) {
                                this.outstanding_connections.removeFirst();
                                this.scheduleNextConnection();
                                conn = outstandingConnection.conn;
                                this.logger.log(Level.FINE, "Outstading connection to " + conn.host_id + " completed");
                            }
                        }
                    }
                    if (!this.please_stop) {
                        if (conn == null) {
                            conn = new SCTPConnection(this, this.settings.watchdogInterval(), this.settings.idleTimeout());
                            conn.host_id = inetAddress.toString();
                            conn.state = Connection.State.connected_in;
                            conn.assoc_id = sac_assoc_id;
                            conn.sac_inbound_streams = sctpNotificationAssociationChange.sac_inbound_streams;
                            conn.sac_outbound_streams = sctpNotificationAssociationChange.sac_outbound_streams;
                            this.map.put(sac_assoc_id, conn);
                            this.registerInboundConnection(conn);
                        }
                        else {
                            conn.state = Connection.State.connected_out;
                            conn.assoc_id = sac_assoc_id;
                            conn.sac_inbound_streams = sctpNotificationAssociationChange.sac_inbound_streams;
                            conn.sac_outbound_streams = sctpNotificationAssociationChange.sac_outbound_streams;
                            this.map.put(sac_assoc_id, conn);
                            this.initiateCER(conn);
                        }
                        try {
                            final sctp_paddrparams peerParameters = new sctp_paddrparams();
                            peerParameters.spp_assoc_id = sac_assoc_id;
                            peerParameters.spp_flags = 1;
                            peerParameters.spp_hbinterval = (int)conn.watchdogInterval() + 1000;
                            this.sctp_socket.setPeerParameters(peerParameters);
                        }
                        catch (SocketException ex4) {}
                    }
                    else {
                        try {
                            this.sctp_socket.disconnect(sac_assoc_id);
                        }
                        catch (SocketException ex5) {}
                    }
                    break;
                }
                if (sctpNotificationAssociationChange.sac_state != SCTPNotificationAssociationChange.State.SCTP_RESTART) {
                    final SCTPConnection sctpConnection = this.map.get(sac_assoc_id);
                    switch (sctpNotificationAssociationChange.sac_state) {
                        case SCTP_COMM_LOST: {
                            this.logger.log(Level.INFO, "Received sctp-comm-lost notification on association " + sac_assoc_id);
                            break;
                        }
                        case SCTP_SHUTDOWN_COMP: {
                            this.logger.log(Level.INFO, "Received sctp-shutdown-comp notification on association " + sac_assoc_id);
                            break;
                        }
                        case SCTP_CANT_STR_ASSOC: {
                            final OutstandingConnection outstandingConnection2 = this.outstanding_connections.peek();
                            if (outstandingConnection2 != null) {
                                this.logger.log(Level.INFO, "Connection to " + outstandingConnection2.conn.host_id + " failed.");
                                this.outstanding_connections.remove();
                                break;
                            }
                            this.logger.log(Level.WARNING, "Got a cant-start-association association-change-event but no outstanding connect operation was found");
                            break;
                        }
                    }
                    if (sctpConnection != null) {
                        sctpConnection.closed = true;
                        this.closeConnection(sctpConnection);
                    }
                    break;
                }
                this.logger.log(Level.INFO, "Received sctp-restart notification on association " + sac_assoc_id);
                final SCTPConnection sctpConnection2 = this.map.get(sac_assoc_id);
                if (sctpConnection2 != null) {
                    sctpConnection2.closed = true;
                    this.closeConnection(sctpConnection2);
                }
                if (this.please_stop) {
                    try {
                        this.sctp_socket.disconnect(sac_assoc_id);
                    }
                    catch (SocketException ex6) {}
                    return;
                }
                InetAddress inetAddress2 = null;
                try {
                    final Iterator iterator3 = this.sctp_socket.getPeerInetAddresses(sac_assoc_id).iterator();
                    if (iterator3.hasNext()) {
                        inetAddress2 = (InetAddress) iterator3.next();
                    }
                    this.sctp_socket.getPeerInetPort(sac_assoc_id);
                }
                catch (SocketException ex2) {
                    this.logger.log(Level.WARNING, "Caught SocketException while retrieving SCTP peer address", ex2);
                    try {
                        this.sctp_socket.disconnect(sac_assoc_id, true);
                    }
                    catch (SocketException ex7) {}
                    return;
                }
                this.logger.log(Level.INFO, "Got an restarted connection from " + inetAddress2.toString() + " port " + sac_assoc_id);
                final SCTPConnection sctpConnection3 = new SCTPConnection(this, this.settings.watchdogInterval(), this.settings.idleTimeout());
                sctpConnection3.host_id = inetAddress2.toString();
                sctpConnection3.state = Connection.State.connected_in;
                sctpConnection3.assoc_id = sac_assoc_id;
                sctpConnection3.sac_inbound_streams = sctpNotificationAssociationChange.sac_inbound_streams;
                sctpConnection3.sac_outbound_streams = sctpNotificationAssociationChange.sac_outbound_streams;
                this.map.put(sac_assoc_id, sctpConnection3);
                this.registerInboundConnection(sctpConnection3);
                try {
                    final sctp_paddrparams peerParameters2 = new sctp_paddrparams();
                    peerParameters2.spp_assoc_id = sac_assoc_id;
                    peerParameters2.spp_flags = 1;
                    peerParameters2.spp_hbinterval = (int)sctpConnection3.watchdogInterval() + 1000;
                    this.sctp_socket.setPeerParameters(peerParameters2);
                }
                catch (SocketException ex8) {}
                break;
            }
            case SCTP_SHUTDOWN_EVENT: {
                final SCTPConnection sctpConnection4 = this.map.get(((SCTPNotificationShutdownEvent)sctpNotification).sse_assoc_id);
                this.logger.log(Level.INFO, "Received shutdown event from" + sctpConnection4.host_id);
                this.closeConnection(sctpConnection4);
                break;
            }
            default: {
                this.logger.log(Level.WARNING, "Received unknown SCTP event (" + sctpNotification.type + ")" + sctpNotification.toString());
                break;
            }
        }
    }
    
    void sendMessage(final SCTPConnection sctpConnection, final byte[] array) {
        this.logger.log(Level.FINEST, "sendMessage():");
        try {
            final SCTPData sctpData = new SCTPData(array);
            sctpData.sndrcvinfo.sinfo_assoc_id = sctpConnection.assoc_id;
            sctpData.sndrcvinfo.sinfo_stream = sctpConnection.nextOutStream();
            this.sctp_socket.send(sctpData);
        }
        catch (SocketException ex) {}
        catch (WouldBlockException ex2) {
            sctpConnection.queueMessage(array);
            this.any_queued_messages = true;
            try {
                this.sctp_socket.wakeup();
            }
            catch (SocketException ex3) {}
        }
    }
    
    private boolean trySendQueuedMessages() {
        boolean b = false;
        final Iterator<Map.Entry<AssociationId, SCTPConnection>> iterator = this.map.entrySet().iterator();
        while (iterator.hasNext()) {
            final SCTPConnection sctpConnection = iterator.next().getValue();
            byte[] peekFirstQueuedMessage;
            while ((peekFirstQueuedMessage = sctpConnection.peekFirstQueuedMessage()) != null) {
                try {
                    final SCTPData sctpData = new SCTPData(peekFirstQueuedMessage);
                    sctpData.sndrcvinfo.sinfo_assoc_id = sctpConnection.assoc_id;
                    sctpData.sndrcvinfo.sinfo_stream = sctpConnection.nextOutStream();
                    this.sctp_socket.send(sctpData);
                    sctpConnection.removeFirstQueuedMessage();
                    continue;
                }
                catch (SocketException ex) {
                    b = true;
                }
                catch (WouldBlockException ex2) {
                    b = true;
                }
                break;
            }
        }
        return b;
    }
    
    boolean initiateConnection(final Connection connection, final Peer peer) {
        final OutstandingConnection outstandingConnection = new OutstandingConnection((SCTPConnection)connection, peer);
        final boolean empty = this.outstanding_connections.isEmpty();
        this.outstanding_connections.addLast(outstandingConnection);
        return !empty || this.scheduleNextConnection();
    }
    
    private boolean scheduleNextConnection() {
        while (!this.outstanding_connections.isEmpty()) {
            final OutstandingConnection outstandingConnection = this.outstanding_connections.peek();
            final Peer peer = outstandingConnection.peer;
            final SCTPConnection conn = outstandingConnection.conn;
            try {
                outstandingConnection.address = InetAddress.getByName(peer.host());
                this.sctp_socket.connect(new InetSocketAddress(outstandingConnection.address, peer.port()));
                conn.state = Connection.State.connecting;
                return true;
            }
            catch (IOException ex) {
                this.logger.log(Level.WARNING, "java.io.IOException caught while initiating connection to '" + peer.host() + "'.", ex);
                this.outstanding_connections.removeFirst();
                continue;
            }
        }
        return false;
    }
    
    void close(final Connection connection, final boolean b) {
        final SCTPConnection sctpConnection = (SCTPConnection)connection;
        if (!sctpConnection.closed) {
            this.logger.log(Level.FINEST, "Closing connection (SCTP) to " + sctpConnection.host_id);
            try {
                this.sctp_socket.disconnect(sctpConnection.assoc_id, b);
            }
            catch (IOException ex) {
                this.logger.log(Level.WARNING, "Error closing SCTP connection to " + sctpConnection.host_id, ex);
            }
        }
        this.map.remove(sctpConnection.assoc_id);
    }
    
    Connection newConnection(final long n, final long n2) {
        return new SCTPConnection(this, n, n2);
    }
    
    static {
        try {
            final OneToManySCTPSocket oneToManySCTPSocket = new OneToManySCTPSocket();
        }
        catch (SocketException ex) {}
    }
    
    private static class OutstandingConnection
    {
        SCTPConnection conn;
        Peer peer;
        InetAddress address;
        
        OutstandingConnection(final SCTPConnection conn, final Peer peer) {
            super();
            this.conn = conn;
            this.peer = peer;
        }
    }
    
    private class SelectThread extends Thread
    {
        public SelectThread() {
            super("DiameterNode thread (SCTP)");
        }
        
        public void run() {
            try {
                this.run_();
                SCTPNode.this.sctp_socket.close();
            }
            catch (IOException ex) {}
        }
        
        private void run_() throws IOException {
            SCTPNode.this.sctp_socket.configureBlocking(false);
            while (true) {
                final boolean b;
                synchronized (SCTPNode.this.getLockObject()) {
                    b = (SCTPNode.this.any_queued_messages && SCTPNode.this.trySendQueuedMessages());
                }
                if (SCTPNode.this.please_stop) {
                    if (System.currentTimeMillis() >= SCTPNode.this.shutdown_deadline) {
                        break;
                    }
                    if (!SCTPNode.this.anyOpenConnections()) {
                        break;
                    }
                }
                long n = SCTPNode.this.calcNextTimeout();
                if (b) {
                    if (n == -1L) {
                        n = 200L;
                    }
                    else {
                        n = Math.min(n, 200L);
                    }
                }
                SCTPChunk sctpChunk;
                if (n != -1L) {
                    final long currentTimeMillis = System.currentTimeMillis();
                    if (n > currentTimeMillis) {
                        sctpChunk = SCTPNode.this.sctp_socket.receive(n - currentTimeMillis);
                    }
                    else {
                        sctpChunk = SCTPNode.this.sctp_socket.receiveNow();
                    }
                }
                else {
                    sctpChunk = SCTPNode.this.sctp_socket.receive();
                }
                if (sctpChunk != null) {
                    SCTPNode.this.processChunk(sctpChunk);
                }
                SCTPNode.this.runTimers();
            }
        }
    }
}
