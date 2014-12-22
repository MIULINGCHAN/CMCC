package com.ocs.protocol.diameter.session;

import java.util.*;
import java.util.logging.*;

import com.ocs.protocol.diameter.node.*;
import com.ocs.protocol.diameter.*;

public class ACHandler
{
    private BaseSession base_session;
    private long subsession_sequencer;
    private int accounting_record_number;
    public String acct_multi_session_id;
    public Integer acct_application_id;
    private Map<Long, SubSession> subsessions;
    
    public ACHandler(final BaseSession base_session) {
        super();
        this.base_session = base_session;
        this.accounting_record_number = 0;
        this.subsessions = new HashMap<Long, SubSession>();
        this.subsession_sequencer = 0L;
        this.subsessions.put(this.subsession_sequencer, new SubSession(this.subsession_sequencer++));
    }
    
    public long calcNextTimeout() {
        long min = Long.MAX_VALUE;
        final Iterator<Map.Entry<Long, SubSession>> iterator = this.subsessions.entrySet().iterator();
        while (iterator.hasNext()) {
            min = Math.min(min, iterator.next().getValue().next_interim);
        }
        return min;
    }
    
    public void handleTimeout() {
        final long currentTimeMillis = System.currentTimeMillis();
        final Iterator<Map.Entry<Long, SubSession>> iterator = this.subsessions.entrySet().iterator();
        while (iterator.hasNext()) {
            final SubSession subSession = iterator.next().getValue();
            if (subSession.next_interim <= currentTimeMillis) {
                this.sendInterim(subSession);
            }
        }
    }
    
    public long createSubSession() {
        final SubSession subSession = new SubSession(this.subsession_sequencer++);
        this.subsessions.put(subSession.subsession_id, subSession);
        return subSession.subsession_id;
    }
    
    public SubSession subSession(final long n) {
        return this.subsessions.get(n);
    }
    
    public void startSubSession(final long n) {
        if (n == 0L) {
            return;
        }
        final SubSession subSession = this.subSession(n);
        if (subSession == null) {
            return;
        }
        if (subSession.start_sent) {
            return;
        }
        this.sendStart(subSession);
    }
    
    public void stopSubSession(final long n) {
        if (n == 0L) {
            return;
        }
        final SubSession subSession = this.subSession(n);
        if (subSession == null) {
            return;
        }
        this.sendStop(subSession);
        this.subsessions.remove(subSession.subsession_id);
    }
    
    public void startSession() {
        final SubSession subSession = this.subSession(0L);
        if (subSession.start_sent) {
            return;
        }
        this.sendStart(subSession);
    }
    
    public void stopSession() {
        for (final Map.Entry<Long, SubSession> entry : this.subsessions.entrySet()) {
            if (entry.getValue().subsession_id == 0L) {
                continue;
            }
            this.sendStop(entry.getValue());
        }
        this.sendStop(this.subSession(0L));
        this.subsessions.clear();
    }
    
    public void sendEvent() {
        this.sendEvent(0L, null);
    }
    
    public void sendEvent(final AVP[] array) {
        this.sendEvent(0L, array);
    }
    
    public void sendEvent(final long n) {
        this.sendEvent(n, null);
    }
    
    public void sendEvent(final long n, final AVP[] array) {
        final SubSession subSession = this.subSession(0L);
        if (subSession == null) {
            return;
        }
        this.sendEvent(subSession, array);
    }
    
    private void sendStart(final SubSession subSession) {
        this.sendACR(this.makeACR(subSession, 2));
        if (subSession.interim_interval != Long.MAX_VALUE) {
            subSession.next_interim = System.currentTimeMillis() + subSession.interim_interval;
        }
        else {
            subSession.next_interim = Long.MAX_VALUE;
        }
    }
    
    private void sendInterim(final SubSession subSession) {
        this.sendACR(this.makeACR(subSession, 3));
        if (subSession.interim_interval != Long.MAX_VALUE) {
            subSession.next_interim = System.currentTimeMillis() + subSession.interim_interval;
        }
        else {
            subSession.next_interim = Long.MAX_VALUE;
        }
    }
    
    private void sendStop(final SubSession subSession) {
        this.sendACR(this.makeACR(subSession, 4));
    }
    
    private void sendEvent(final SubSession subSession, final AVP[] array) {
        final Message acr = this.makeACR(subSession, 1);
        if (array != null) {
            for (int length = array.length, i = 0; i < length; ++i) {
                acr.add(array[i]);
            }
        }
        this.sendACR(acr);
    }
    
    private Message makeACR(final SubSession subSession, final int n) {
        final Message mandatory_RFC3588 = new Message();
        mandatory_RFC3588.hdr.setRequest(true);
        mandatory_RFC3588.hdr.setProxiable(true);
        mandatory_RFC3588.hdr.application_id = this.base_session.authAppId();
        mandatory_RFC3588.hdr.command_code = 271;
        this.collectACRInfo(mandatory_RFC3588, subSession, n);
        Utils.setMandatory_RFC3588(mandatory_RFC3588);
        return mandatory_RFC3588;
    }
    
    public void handleACA(final Message message) {
        if (message == null) {
            return;
        }
        try {
            final Iterator<AVP> iterator = message.iterator(485);
            if (!iterator.hasNext()) {
                return;
            }
            final int queryValue = new AVP_Unsigned32(iterator.next()).queryValue();
            for (final Map.Entry<Long, SubSession> entry : this.subsessions.entrySet()) {
                if (entry.getValue().most_recent_record_number == queryValue) {
                    entry.getValue().most_recent_record_number = -1;
                }
            }
        }
        catch (InvalidAVPLengthException ex) {}
    }
    
    private void sendACR(final Message message) {
        try {
            this.base_session.sessionManager().sendRequest(message, this.base_session, null);
        }
        catch (NotARequestException ex2) {}
        catch (NotRoutableException ex) {
            this.base_session.sessionManager().logger.log(Level.INFO, "Could not send ACR for session " + this.base_session.sessionId() + " :" + ex.toString());
            this.handleACA(null);
        }
    }
    
    public void collectACRInfo(final Message message, final SubSession subSession, final int n) {
        this.base_session.addCommonStuff(message);
        message.add(new AVP_Unsigned32(480, n));
        ++this.accounting_record_number;
        message.add(new AVP_Unsigned32(485, this.accounting_record_number));
        subSession.most_recent_record_number = this.accounting_record_number;
        if (this.acct_application_id != null) {
            message.add(new AVP_Unsigned32(259, this.acct_application_id));
        }
        if (subSession.subsession_id != 0L) {
            message.add(new AVP_Unsigned64(287, subSession.subsession_id));
        }
        if (this.acct_multi_session_id != null) {
            message.add(new AVP_UTF8String(50, this.acct_multi_session_id));
        }
        if (subSession.interim_interval != Long.MAX_VALUE) {
            message.add(new AVP_Unsigned32(85, (int)(subSession.interim_interval / 1000L)));
        }
        message.add(new AVP_Time(55, (int)(System.currentTimeMillis() / 1000L)));
        if (n != 2) {
            if (subSession.acct_session_time != null) {
                message.add(new AVP_Unsigned32(46, (int)(subSession.acct_session_time / 1000L)));
            }
            if (subSession.acct_input_octets != null) {
                message.add(new AVP_Unsigned64(363, subSession.acct_input_octets));
            }
            if (subSession.acct_output_octets != null) {
                message.add(new AVP_Unsigned64(364, subSession.acct_output_octets));
            }
            if (subSession.acct_input_packets != null) {
                message.add(new AVP_Unsigned64(365, subSession.acct_input_packets));
            }
            if (subSession.acct_output_packets != null) {
                message.add(new AVP_Unsigned64(366, subSession.acct_output_packets));
            }
        }
    }
    
    public static class SubSession
    {
        final long subsession_id;
        boolean start_sent;
        public long interim_interval;
        long next_interim;
        int most_recent_record_number;
        public Long acct_session_time;
        public Long acct_input_octets;
        public Long acct_output_octets;
        public Long acct_input_packets;
        public Long acct_output_packets;
        
        SubSession(final long subsession_id) {
            super();
            this.subsession_id = subsession_id;
            this.interim_interval = Long.MAX_VALUE;
            this.next_interim = Long.MAX_VALUE;
            this.most_recent_record_number = -1;
        }
    }
}
