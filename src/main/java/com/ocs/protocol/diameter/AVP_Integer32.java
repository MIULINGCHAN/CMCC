package com.ocs.protocol.diameter;

public class AVP_Integer32 extends AVP
{
    public AVP_Integer32(final AVP avp) throws InvalidAVPLengthException {
        super(avp);
        if (avp.queryPayloadSize() != 4) {
            throw new InvalidAVPLengthException(avp);
        }
    }
    
    public AVP_Integer32(final int n, final int n2) {
        super(n, int2byte(n2));
    }
    
    public AVP_Integer32(final int n, final int n2, final int n3) {
        super(n, n2, int2byte(n3));
    }
    
    public int queryValue() {
        return packunpack.unpack32(this.payload, 0);
    }
    
    public void setValue(final int n) {
        packunpack.pack32(this.payload, 0, n);
    }
    
    private static final byte[] int2byte(final int n) {
        final byte[] array = new byte[4];
        packunpack.pack32(array, 0, n);
        return array;
    }
}
