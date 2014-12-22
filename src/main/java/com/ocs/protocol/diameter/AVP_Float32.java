package com.ocs.protocol.diameter;

import java.nio.*;

public class AVP_Float32 extends AVP
{
    public AVP_Float32(final AVP avp) throws InvalidAVPLengthException {
        super(avp);
        if (avp.queryPayloadSize() != 4) {
            throw new InvalidAVPLengthException(avp);
        }
    }
    
    public AVP_Float32(final int n, final float n2) {
        super(n, float2byte(n2));
    }
    
    public AVP_Float32(final int n, final int n2, final float n3) {
        super(n, n2, float2byte(n3));
    }
    
    public void setValue(final float n) {
        this.setPayload(float2byte(n));
    }
    
    public float queryValue() {
        final byte[] queryPayload = this.queryPayload();
        final ByteBuffer allocate = ByteBuffer.allocate(4);
        allocate.order(ByteOrder.BIG_ENDIAN);
        allocate.put(queryPayload);
        allocate.rewind();
        return allocate.getFloat();
    }
    
    private static final byte[] float2byte(final float n) {
        final ByteBuffer allocate = ByteBuffer.allocate(4);
        allocate.order(ByteOrder.BIG_ENDIAN);
        allocate.putFloat(n);
        allocate.rewind();
        final byte[] array = new byte[4];
        allocate.get(array);
        return array;
    }
}
