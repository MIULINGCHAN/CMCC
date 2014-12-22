package com.ocs.protocol.diameter;

import java.nio.*;

public class AVP_Float64 extends AVP
{
    public AVP_Float64(final AVP avp) throws InvalidAVPLengthException {
        super(avp);
        if (avp.queryPayloadSize() != 4) {
            throw new InvalidAVPLengthException(avp);
        }
    }
    
    public AVP_Float64(final int n, final double n2) {
        super(n, double2byte(n2));
    }
    
    public AVP_Float64(final int n, final int n2, final double n3) {
        super(n, n2, double2byte(n3));
    }
    
    public void setValue(final double n) {
        this.setPayload(double2byte(n));
    }
    
    public double queryValue() {
        final byte[] queryPayload = this.queryPayload();
        final ByteBuffer allocate = ByteBuffer.allocate(4);
        allocate.order(ByteOrder.BIG_ENDIAN);
        allocate.put(queryPayload);
        allocate.rewind();
        return allocate.getDouble();
    }
    
    private static final byte[] double2byte(final double n) {
        final ByteBuffer allocate = ByteBuffer.allocate(4);
        allocate.order(ByteOrder.BIG_ENDIAN);
        allocate.putDouble(n);
        allocate.rewind();
        final byte[] array = new byte[4];
        allocate.get(array);
        return array;
    }
}
