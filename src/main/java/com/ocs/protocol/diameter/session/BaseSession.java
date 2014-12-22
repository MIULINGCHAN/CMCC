package com.ocs.protocol.diameter.session;

import java.util.logging.*;

import com.ocs.protocol.diameter.node.*;
import com.ocs.protocol.diameter.*;

public abstract class BaseSession implements Session
{
    private SessionManager session_manager;
    private String session_id;
    private State state;
    private int auth_app_id;
    private int session_timeout;
    private boolean state_maintained;
    private long first_auth_time;
    protected SessionAuthTimers session_auth_timers;
    private boolean auth_in_progress;
    
    public BaseSession(final int auth_app_id, final SessionManager session_manager) {
        super();
        this.state = State.idle;
        this.auth_app_id = auth_app_id;
        this.session_manager = session_manager;
        this.session_auth_timers = new SessionAuthTimers();
        this.state_maintained = true;
    }
    
    public final SessionManager sessionManager() {
        return this.session_manager;
    }
    
    public final String sessionId() {
        return this.session_id;
    }
    
    public final State state() {
        return this.state;
    }
    
    public final int authAppId() {
        return this.auth_app_id;
    }
    
    public final boolean authInProgress() {
        return this.auth_in_progress;
    }
    
    protected final void authInProgress(final boolean auth_in_progress) {
        this.auth_in_progress = auth_in_progress;
    }
    
    public boolean stateMaintained() {
        return this.state_maintained;
    }
    
    protected void stateMaintained(final boolean state_maintained) {
        this.state_maintained = state_maintained;
    }
    
    public long firstAuthTime() {
        return this.first_auth_time;
    }
    
    public int handleRequest(final Message message) {
        switch (message.hdr.command_code) {
            case 258: {
                return this.handleRAR(message);
            }
            case 274: {
                return this.handleASR(message);
            }
            default: {
                return 3001;
            }
        }
    }
    
    public void handleAnswer(final Message message, final Object o) {
        switch (message.hdr.command_code) {
            case 275: {
                this.handleSTA(message);
                break;
            }
            default: {
                this.session_manager.logger.log(Level.WARNING, "Session '" + this.session_id + "' could not handle answer (command_code=" + message.hdr.command_code + ")");
                break;
            }
        }
    }
    
    public void handleNonAnswer(final int n, final Object o) {
        switch (n) {
            case 275: {
                this.handleSTA(null);
                break;
            }
            default: {
                this.session_manager.logger.log(Level.WARNING, "Session '" + this.session_id + "' could not handle non-answer (command_code=" + n + ")");
                break;
            }
        }
    }
    
    protected int handleRAR(final Message message) {
        if (!this.auth_in_progress) {
            this.startReauth();
        }
        return 2001;
    }
    
    protected int handleASR(final Message message) {
        if (this.state_maintained) {
            this.closeSession(4);
        }
        else {
            final State state = this.state;
            final State idle = State.idle;
            this.newStatePre(this.state, idle, message, 0);
            this.state = idle;
            this.session_manager.unregister(this);
            this.newStatePost(state, this.state, message, 0);
        }
        return 2001;
    }
    
    protected void authSuccessful(final Message message) {
        if (this.state() == State.pending) {
            this.first_auth_time = System.currentTimeMillis();
        }
        final State state = this.state;
        final State open = State.open;
        this.newStatePre(state, open, message, 0);
        this.newStatePost(state, this.state = open, message, 0);
        this.sessionManager().updateTimeouts(this);
    }
    
    protected void authFailed(final Message message) {
        this.auth_in_progress = false;
        this.session_manager.logger.log(Level.INFO, "Authentication/Authorization failed, closing session " + this.session_id);
        if (this.state() == State.pending) {
            this.closeSession(message, 4);
        }
        else {
            this.closeSession(message, 6);
        }
    }
    
    public void handleSTA(final Message message) {
        final State state = this.state;
        final State idle = State.idle;
        this.newStatePre(this.state, idle, message, 0);
        this.session_manager.unregister(this);
        this.newStatePost(state, this.state = idle, message, 0);
    }
    
    public long calcNextTimeout() {
        long n = Long.MAX_VALUE;
        if (this.state == State.open) {
            if (this.session_timeout != 0) {
                n = Math.min(n, this.first_auth_time + this.session_timeout * 1000);
            }
            if (!this.auth_in_progress) {
                n = Math.min(n, this.session_auth_timers.getNextReauthTime());
            }
            else {
                n = Math.min(n, this.session_auth_timers.getMaxTimeout());
            }
        }
        return n;
    }
    
    public void handleTimeout() {
        if (this.state == State.open) {
            final long currentTimeMillis = System.currentTimeMillis();
            if (this.session_timeout != 0 && currentTimeMillis >= this.first_auth_time + this.session_timeout * 1000) {
                this.session_manager.logger.log(Level.FINE, "Session-Timeout has expired, closing session");
                this.closeSession(null, 8);
                return;
            }
            if (currentTimeMillis >= this.session_auth_timers.getMaxTimeout()) {
                this.session_manager.logger.log(Level.FINE, "authorization-lifetime has expired, closing session");
                this.closeSession(null, 6);
                return;
            }
            if (currentTimeMillis >= this.session_auth_timers.getNextReauthTime()) {
                this.session_manager.logger.log(Level.FINE, "authorization-lifetime(+grace-period) has expired, sending re-authorization");
                this.startReauth();
                this.sessionManager().updateTimeouts(this);
            }
        }
    }
    
    public void newStatePre(final State state, final State state2, final Message message, final int n) {
    }
    
    public void newStatePost(final State state, final State state2, final Message message, final int n) {
    }
    
    public void openSession() throws InvalidStateException {
        if (this.state != State.idle) {
            throw new InvalidStateException("Session cannot be opened unless it is idle");
        }
        if (this.session_id != null) {
            throw new InvalidStateException("Sessions cannot be reused");
        }
        this.session_id = this.makeNewSessionId();
        final State pending = State.pending;
        this.newStatePre(this.state, pending, null, 0);
        this.session_manager.register(this);
        this.state = pending;
        this.newStatePost(State.idle, pending, null, 0);
        this.startAuth();
    }
    
    public void closeSession(final int n) {
        this.closeSession(null, n);
    }
    
    protected void closeSession(final Message message, final int n) {
        switch (this.state) {
            case idle: {}
            case pending: {
                this.newStatePre(State.pending, State.discon, message, n);
                this.sendSTR(n);
                this.state = State.discon;
                this.newStatePost(State.pending, this.state, message, n);
                break;
            }
            case open: {
                if (this.state_maintained) {
                    this.newStatePre(State.open, State.discon, message, n);
                    this.sendSTR(n);
                    this.state = State.discon;
                    this.newStatePost(State.open, this.state, message, n);
                    break;
                }
                this.newStatePre(State.open, State.idle, message, n);
                this.state = State.idle;
                this.session_manager.unregister(this);
                this.newStatePost(State.open, this.state, message, n);
                break;
            }
            case discon: {}
        }
    }
    
    protected abstract void startAuth();
    
    protected abstract void startReauth();
    
    protected void updateSessionTimeout(final int session_timeout) {
        this.session_timeout = session_timeout;
        this.session_manager.updateTimeouts(this);
    }
    
    private final void sendSTR(final int n) {
        this.session_manager.logger.log(Level.FINE, "Sending STR for session " + this.session_id);
        final Message mandatory_RFC3588 = new Message();
        mandatory_RFC3588.hdr.setRequest(true);
        mandatory_RFC3588.hdr.setProxiable(true);
        mandatory_RFC3588.hdr.application_id = this.authAppId();
        mandatory_RFC3588.hdr.command_code = 275;
        this.collectSTRInfo(mandatory_RFC3588, n);
        Utils.setMandatory_RFC3588(mandatory_RFC3588);
        try {
            this.session_manager.sendRequest(mandatory_RFC3588, this, null);
        }
        catch (NotARequestException ex) {}
        catch (NotRoutableException ex2) {
            this.handleSTA(null);
        }
    }
    
    protected void collectSTRInfo(final Message message, final int n) {
        this.addCommonStuff(message);
        message.add(new AVP_Unsigned32(258, this.authAppId()));
        message.add(new AVP_Unsigned32(295, n));
    }
    
    protected String getDestinationRealm() {
        return this.session_manager.settings().realm();
    }
    
    protected String getSessionIdOptionalPart() {
        return null;
    }
    
    protected static final int getResultCode(final Message message) {
        final AVP find = message.find(268);
        if (find != null) {
            try {
                return new AVP_Unsigned32(find).queryValue();
            }
            catch (InvalidAVPLengthException ex) {
                return -1;
            }
        }
        return -1;
    }
    
    public void addCommonStuff(final Message message) {
        message.add(new AVP_UTF8String(263, this.session_id));
        message.add(new AVP_UTF8String(264, this.session_manager.settings().hostId()));
        message.add(new AVP_UTF8String(296, this.session_manager.settings().realm()));
        message.add(new AVP_UTF8String(283, this.getDestinationRealm()));
    }
    
    private final String makeNewSessionId() {
        return this.session_manager.node().makeNewSessionId(this.getSessionIdOptionalPart());
    }
    
    public enum State
    {
        idle, 
        pending, 
        open, 
        discon;
    }
}
