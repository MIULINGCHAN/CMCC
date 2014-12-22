package com.ocs.protocol.diameter;

public class AVP_OctetString extends AVP
{
    public AVP_OctetString(final AVP avp) {
        super(avp);
    }
    
    public AVP_OctetString(final int n, final byte[] array) {
        super(n, array);
    }
    
    public AVP_OctetString(final int n, final int n2, final byte[] array) {
        super(n, n2, array);
    }
    
    public byte[] queryValue() {
        return this.queryPayload();
    }
    
    public void setValue(final byte[] array) {
        this.setPayload(array, 0, array.length);
    }
}
