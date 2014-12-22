package com.ocs.protocol.diameter.session;

import java.util.logging.*;

import com.ocs.protocol.diameter.node.*;
import com.ocs.protocol.diameter.*;

public class AASession extends BaseSession
{
    private static Logger logger;
    
    public AASession(final int n, final SessionManager sessionManager) {
        super(n, sessionManager);
    }
    
    public void handleAnswer(final Message message, final Object o) {
        switch (message.hdr.command_code) {
            case 265: {
                this.handleAAA(message);
                break;
            }
            default: {
                super.handleAnswer(message, o);
                break;
            }
        }
    }
    
    public void handleNonAnswer(final int n, final Object o) {
        switch (n) {
            case 265: {
                if (this.authInProgress()) {
                    this.authFailed(null);
                    break;
                }
                AASession.logger.log(Level.INFO, "Got a non-answer AA for session '" + this.sessionId() + "' when no reauth was progress.");
                break;
            }
            default: {
                super.handleNonAnswer(n, o);
                break;
            }
        }
    }
    
    public void handleAAA(final Message message) {
        AASession.logger.log(Level.FINER, "Handling AAA");
        if (!this.authInProgress()) {
            return;
        }
        this.authInProgress(false);
        if (this.state() == State.discon) {
            return;
        }
        final int resultCode = BaseSession.getResultCode(message);
        switch (resultCode) {
            case 2001: {
                if (this.processAAAInfo(message)) {
                    this.authSuccessful(message);
                    break;
                }
                this.closeSession(message, 3);
                break;
            }
            case 1001: {
                this.sendAAR();
                break;
            }
            case 5003: {
                AASession.logger.log(Level.INFO, "Authorization for session " + this.sessionId() + " rejected, closing session");
                if (this.state() == State.pending) {
                    this.closeSession(message, 3);
                    break;
                }
                this.closeSession(message, 6);
                break;
            }
            default: {
                AASession.logger.log(Level.INFO, "AAR failed, result_code=" + resultCode);
                this.closeSession(message, 3);
                break;
            }
        }
    }
    
    protected void startAuth() {
        this.sendAAR();
    }
    
    protected void startReauth() {
        this.sendAAR();
    }
    
    private final void sendAAR() {
        AASession.logger.log(Level.FINE, "Considering sending AAR for " + this.sessionId());
        if (this.authInProgress()) {
            return;
        }
        AASession.logger.log(Level.FINE, "Sending AAR for " + this.sessionId());
        this.authInProgress(true);
        final Message mandatory_RFC3588 = new Message();
        mandatory_RFC3588.hdr.setRequest(true);
        mandatory_RFC3588.hdr.setProxiable(true);
        mandatory_RFC3588.hdr.application_id = this.authAppId();
        mandatory_RFC3588.hdr.command_code = 265;
        this.collectAARInfo(mandatory_RFC3588);
        Utils.setMandatory_RFC3588(mandatory_RFC3588);
        try {
            this.sessionManager().sendRequest(mandatory_RFC3588, this, null);
        }
        catch (NotARequestException ex2) {}
        catch (NotRoutableException ex) {
            AASession.logger.log(Level.INFO, "Could not send AAR for session " + this.sessionId(), ex);
            this.authFailed(null);
        }
    }
    
    protected void collectAARInfo(final Message message) {
        this.addCommonStuff(message);
        message.add(new AVP_Unsigned32(258, this.authAppId()));
    }
    
    protected boolean processAAAInfo(final Message message) {
        AASession.logger.log(Level.FINE, "Processing AAA info");
        try {
            long n = 0L;
            final AVP find = message.find(291);
            if (find != null) {
                n = new AVP_Unsigned32(find).queryValue() * 1000;
            }
            long n2 = 0L;
            final AVP find2 = message.find(276);
            if (find2 != null) {
                n2 = new AVP_Unsigned32(find2).queryValue() * 1000;
            }
            final AVP find3 = message.find(27);
            if (find3 != null) {
                this.updateSessionTimeout(new AVP_Unsigned32(find3).queryValue());
            }
            final AVP find4 = message.find(277);
            if (find4 != null) {
                this.stateMaintained(new AVP_Unsigned32(find4).queryValue() == 0);
            }
            final long currentTimeMillis = System.currentTimeMillis();
            AASession.logger.log(Level.FINER, "Session " + this.sessionId() + ": now=" + currentTimeMillis + "  auth_lifetime=" + n + " auth_grace_period=" + n2);
            this.session_auth_timers.updateTimers(currentTimeMillis, n, n2);
            AASession.logger.log(Level.FINER, "getNextReauthTime=" + this.session_auth_timers.getNextReauthTime() + " getMaxTimeout=" + this.session_auth_timers.getMaxTimeout());
        }
        catch (InvalidAVPLengthException ex) {
            return false;
        }
        return true;
    }
    
    static {
        AASession.logger = Logger.getLogger("com.ocs.protocol.diameter.session.AASession");
    }
}
