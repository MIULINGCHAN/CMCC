package com.ocs.protocol.diameter.node;

import com.ocs.protocol.diameter.*;

@SuppressWarnings("serial")
class InvalidAVPValueException extends Exception
{
    public AVP avp;
    
    public InvalidAVPValueException(final AVP avp) {
        super();
        this.avp = avp;
    }
}
