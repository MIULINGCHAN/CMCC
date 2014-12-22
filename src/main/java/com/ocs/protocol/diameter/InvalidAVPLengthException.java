package com.ocs.protocol.diameter;

@SuppressWarnings("serial")
public class InvalidAVPLengthException extends Exception
{
    public AVP avp;
    
    public InvalidAVPLengthException(final AVP avp) {
        super();
        this.avp = new AVP(avp);
    }
}
