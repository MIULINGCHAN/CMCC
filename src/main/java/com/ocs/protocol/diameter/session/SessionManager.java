package com.ocs.protocol.diameter.session;

import java.util.logging.*;
import java.io.*;

import com.ocs.protocol.diameter.*;
import com.ocs.protocol.diameter.node.*;

import java.util.*;

public class SessionManager extends NodeManager
{
    private Map<String, SessionAndTimeout> map_session;
    private Peer[] peers;
    private Thread timer_thread;
    private long earliest_timeout;
    private boolean stop;
    Logger logger;
    
    public SessionManager(final NodeSettings nodeSettings, final Peer[] peers) throws InvalidSettingException {
        super(nodeSettings);
        if (nodeSettings.port() == 0) {
            throw new InvalidSettingException("If you have sessions then you must allow inbound connections");
        }
        this.map_session = new HashMap<String, SessionAndTimeout>();
        this.peers = peers;
        this.earliest_timeout = Long.MAX_VALUE;
        this.stop = false;
        this.logger = Logger.getLogger("com.ocs.protocol.diameter.session");
    }
    
    public void start() throws IOException, UnsupportedTransportProtocolException {
        this.logger.log(Level.FINE, "Starting session manager");
        super.start();
        (this.timer_thread = new TimerThread()).setDaemon(true);
        this.timer_thread.start();
        final Peer[] peers = this.peers;
        for (int length = peers.length, i = 0; i < length; ++i) {
            super.node().initiateConnection(peers[i], true);
        }
    }
    
    public void stop(final long n) {
        this.logger.log(Level.FINE, "Stopping session manager");
        super.stop(n);
        synchronized (this.map_session) {
            this.stop = true;
            this.map_session.notify();
        }
        try {
            this.timer_thread.join();
        }
        catch (InterruptedException ex) {}
        this.logger.log(Level.FINE, "Session manager stopped");
    }
    
    protected void handleRequest(final Message message, final ConnectionKey connectionKey, final Peer peer) {
        this.logger.log(Level.FINE, "Handling request, command_code=" + message.hdr.command_code);
        final Message mandatory_RFC3588 = new Message();
        mandatory_RFC3588.prepareResponse(message);
        final String sessionId = this.extractSessionId(message);
        if (sessionId == null) {
            this.logger.log(Level.FINE, "Cannot handle request - no Session-Id AVP in request");
            mandatory_RFC3588.add(new AVP_Unsigned32(268, 5005));
            this.node().addOurHostAndRealm(mandatory_RFC3588);
            mandatory_RFC3588.add(new AVP_Grouped(279, new AVP[] { new AVP_UTF8String(263, "") }));
            Utils.copyProxyInfo(message, mandatory_RFC3588);
            Utils.setMandatory_RFC3588(mandatory_RFC3588);
            try {
                this.answer(mandatory_RFC3588, connectionKey);
            }
            catch (NotAnAnswerException ex) {}
            return;
        }
        final Session session = this.findSession(sessionId);
        if (session == null) {
            this.logger.log(Level.FINE, "Cannot handle request - Session-Id '" + sessionId + " does not denote a known session");
            mandatory_RFC3588.add(new AVP_Unsigned32(268, 5002));
            this.node().addOurHostAndRealm(mandatory_RFC3588);
            Utils.copyProxyInfo(message, mandatory_RFC3588);
            Utils.setMandatory_RFC3588(mandatory_RFC3588);
            try {
                this.answer(mandatory_RFC3588, connectionKey);
            }
            catch (NotAnAnswerException ex2) {}
            return;
        }
        mandatory_RFC3588.add(new AVP_Unsigned32(268, session.handleRequest(message)));
        this.node().addOurHostAndRealm(mandatory_RFC3588);
        Utils.copyProxyInfo(message, mandatory_RFC3588);
        Utils.setMandatory_RFC3588(mandatory_RFC3588);
        try {
            this.answer(mandatory_RFC3588, connectionKey);
        }
        catch (NotAnAnswerException ex3) {}
    }
    
    protected void handleAnswer(final Message message, final ConnectionKey connectionKey, final Object o) {
        if (message != null) {
            this.logger.log(Level.FINE, "Handling answer, command_code=" + message.hdr.command_code);
        }
        else {
            this.logger.log(Level.FINE, "Handling non-answer");
        }
        final String sessionId = this.extractSessionId(message);
        this.logger.log(Level.FINEST, "session-id=" + sessionId);
        Session session;
        if (sessionId != null) {
            session = this.findSession(sessionId);
        }
        else {
            session = ((RequestState)o).session;
        }
        if (session == null) {
            this.logger.log(Level.FINE, "Session '" + sessionId + "' not found");
            return;
        }
        this.logger.log(Level.FINE, "Found session, dispatching (non-)answer to it");
        if (message != null) {
            session.handleAnswer(message, ((RequestState)o).state);
        }
        else {
            session.handleNonAnswer(((RequestState)o).command_code, ((RequestState)o).state);
        }
    }
    
    public void sendRequest(final Message message, final Session session, final Object state) throws NotRoutableException, NotARequestException {
        this.logger.log(Level.FINE, "Sending request (command_code=" + message.hdr.command_code + ") for session " + session.sessionId());
        final RequestState requestState = new RequestState();
        requestState.command_code = message.hdr.command_code;
        requestState.state = state;
        requestState.session = session;
        this.sendRequest(message, this.peers(message), requestState);
    }
    
    public Peer[] peers() {
        return this.peers;
    }
    
    public Peer[] peers(final Message message) {
        return this.peers;
    }
    
    public void register(final Session session) {
        final SessionAndTimeout sessionAndTimeout = new SessionAndTimeout(session);
        synchronized (this.map_session) {
            this.map_session.put(session.sessionId(), sessionAndTimeout);
            if (sessionAndTimeout.timeout < this.earliest_timeout) {
                this.map_session.notify();
            }
        }
    }
    
    public void unregister(final Session session) {
        this.logger.log(Level.FINE, "Unregistering session " + session.sessionId());
        synchronized (this.map_session) {
            final SessionAndTimeout sessionAndTimeout = this.map_session.get(session.sessionId());
            if (sessionAndTimeout != null) {
                sessionAndTimeout.deleted = true;
                if (this.earliest_timeout == Long.MAX_VALUE) {
                    this.map_session.notify();
                }
                return;
            }
        }
        this.logger.log(Level.WARNING, "Could not find session " + session.sessionId());
    }
    
    public void updateTimeouts(final Session session) {
        synchronized (this.map_session) {
            final SessionAndTimeout sessionAndTimeout = this.map_session.get(session.sessionId());
            if (sessionAndTimeout == null) {
                return;
            }
            sessionAndTimeout.timeout = session.calcNextTimeout();
            if (sessionAndTimeout.timeout < this.earliest_timeout) {
                this.map_session.notify();
            }
        }
    }
    
    private final Session findSession(final String s) {
        synchronized (this.map_session) {
            final SessionAndTimeout sessionAndTimeout = this.map_session.get(s);
            return (sessionAndTimeout != null && !sessionAndTimeout.deleted) ? sessionAndTimeout.session : null;
        }
    }
    
    private final String extractSessionId(final Message message) {
        if (message == null) {
            return null;
        }
        final Iterator<AVP> iterator = message.iterator(263);
        if (!iterator.hasNext()) {
            return null;
        }
        return new AVP_UTF8String(iterator.next()).queryValue();
    }
    
    private static class SessionAndTimeout
    {
        public Session session;
        public long timeout;
        public boolean deleted;
        
        public SessionAndTimeout(final Session session) {
            super();
            this.session = session;
            this.timeout = session.calcNextTimeout();
            this.deleted = false;
        }
    }
    
    private static class RequestState
    {
        public int command_code;
        public Object state;
        public Session session;
    }
    
    private class TimerThread extends Thread
    {
        public TimerThread() {
            super("SessionManager timer thread");
        }
        
        public void run() {
            synchronized (SessionManager.this.map_session) {
                while (!SessionManager.this.stop) {
                    final long currentTimeMillis = System.currentTimeMillis();
                    SessionManager.this.earliest_timeout = Long.MAX_VALUE;
                    final Iterator<Map.Entry<String, SessionAndTimeout>> iterator = SessionManager.this.map_session.entrySet().iterator();
                    while (iterator.hasNext()) {
                        final Map.Entry<String, SessionAndTimeout> entry = iterator.next();
                        if (entry.getValue().deleted) {
                            iterator.remove();
                        }
                        else {
                            final Session session = entry.getValue().session;
                            if (entry.getValue().timeout < currentTimeMillis) {
                                session.handleTimeout();
                                entry.getValue().timeout = session.calcNextTimeout();
                            }
                            SessionManager.this.earliest_timeout = Math.min(SessionManager.this.earliest_timeout, entry.getValue().timeout);
                        }
                    }
                    final long currentTimeMillis2 = System.currentTimeMillis();
                    try {
                        if (SessionManager.this.earliest_timeout <= currentTimeMillis2) {
                            continue;
                        }
                        if (SessionManager.this.earliest_timeout == Long.MAX_VALUE) {
                            SessionManager.this.map_session.wait();
                        }
                        else {
                            SessionManager.this.map_session.wait(SessionManager.this.earliest_timeout - currentTimeMillis2);
                        }
                    }
                    catch (InterruptedException ex) {}
                }
            }
        }
    }
}
