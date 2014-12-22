package com.ocs.protocol.diameter;

public class AVP_Integer64 extends AVP
{
    public AVP_Integer64(final AVP avp) throws InvalidAVPLengthException {
        super(avp);
        if (avp.queryPayloadSize() != 8) {
            throw new InvalidAVPLengthException(avp);
        }
    }
    
    public AVP_Integer64(final int n, final long n2) {
        super(n, long2byte(n2));
    }
    
    public AVP_Integer64(final int n, final int n2, final long n3) {
        super(n, n2, long2byte(n3));
    }
    
    public long queryValue() {
        return packunpack.unpack64(this.payload, 0);
    }
    
    public void setValue(final long n) {
        packunpack.pack64(this.payload, 0, n);
    }
    
    private static final byte[] long2byte(final long n) {
        final byte[] array = new byte[8];
        packunpack.pack64(array, 0, n);
        return array;
    }
}
