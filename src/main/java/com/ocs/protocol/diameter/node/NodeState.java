package com.ocs.protocol.diameter.node;

import java.util.*;

class NodeState
{
    private int state_id;
    private int end_to_end_identifier;
    private int session_id_high;
    private int session_id_low;
    
    NodeState() {
        super();
        final int n = (int)(System.currentTimeMillis() / 1000L);
        this.state_id = n;
        this.end_to_end_identifier = (n << 20 | (new Random().nextInt() & 0xFFFFF));
        this.session_id_high = n;
        this.session_id_low = 0;
    }
    
    public int stateId() {
        return this.state_id;
    }
    
    public synchronized int nextEndToEndIdentifier() {
        final int end_to_end_identifier = this.end_to_end_identifier;
        ++this.end_to_end_identifier;
        return end_to_end_identifier;
    }
    
    synchronized String nextSessionId_second_part() {
        final int session_id_low = this.session_id_low;
        final int session_id_high = this.session_id_high;
        ++this.session_id_low;
        if (this.session_id_low == 0) {
            ++this.session_id_high;
        }
        return session_id_high + ";" + session_id_low;
    }
}
