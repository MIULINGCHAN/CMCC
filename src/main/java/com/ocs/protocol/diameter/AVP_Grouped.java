package com.ocs.protocol.diameter;

@SuppressWarnings("unused")
public class AVP_Grouped extends AVP
{
    public AVP_Grouped(final AVP avp) throws InvalidAVPLengthException {
        super(avp);
        int i = 0;
        final byte[] queryPayload = this.queryPayload();
        int decodeSize;
        for (int n = 0; i < queryPayload.length; i += decodeSize, ++n) {
            decodeSize = AVP.decodeSize(queryPayload, i, queryPayload.length - i);
            if (decodeSize == 0) {
                throw new InvalidAVPLengthException(avp);
            }
        }
        if (i > queryPayload.length) {
            throw new InvalidAVPLengthException(avp);
        }
    }
    
    public AVP_Grouped(final int n, final AVP... array) {
        super(n, avps2byte(array));
    }
    
    public AVP_Grouped(final int n, final int n2, final AVP... array) {
        super(n, n2, avps2byte(array));
    }
    
    public AVP[] queryAVPs() {
        int i;
        byte[] queryPayload;
        int n;
        int decodeSize;
        for (i = 0, queryPayload = this.queryPayload(), n = 0; i < queryPayload.length; i += decodeSize, ++n) {
            decodeSize = AVP.decodeSize(queryPayload, i, queryPayload.length - i);
            if (decodeSize == 0) {
                return null;
            }
        }
        final AVP[] array = new AVP[n];
        int decodeSize2;
        for (int j = 0, n2 = 0; j < queryPayload.length; j += decodeSize2, ++n2) {
            decodeSize2 = AVP.decodeSize(queryPayload, j, queryPayload.length - j);
            (array[n2] = new AVP()).decode(queryPayload, j, decodeSize2);
        }
        return array;
    }
    
    public void setAVPs(final AVP... array) {
        this.setPayload(avps2byte(array));
    }
    
    private static final byte[] avps2byte(final AVP[] array) {
        int n = 0;
        for (int length = array.length, i = 0; i < length; ++i) {
            n += array[i].encodeSize();
        }
        final byte[] array2 = new byte[n];
        int n2 = 0;
        for (int length2 = array.length, j = 0; j < length2; ++j) {
            n2 += array[j].encode(array2, n2);
        }
        return array2;
    }
}
