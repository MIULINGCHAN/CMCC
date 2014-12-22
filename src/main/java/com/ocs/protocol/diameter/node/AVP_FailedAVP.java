package com.ocs.protocol.diameter.node;

import com.ocs.protocol.diameter.*;

class AVP_FailedAVP extends AVP_Grouped
{
    private static AVP[] wrap(final AVP avp) {
        return new AVP[] { avp };
    }
    
    public AVP_FailedAVP(final AVP avp) {
        super(279, wrap(avp));
    }
}
