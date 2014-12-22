package com.ocs.protocol.diameter;

@SuppressWarnings("serial")
public class InvalidAddressTypeException extends Exception
{
    public AVP avp;
    
    public InvalidAddressTypeException(final AVP avp) {
        super();
        this.avp = new AVP(avp);
    }
}
