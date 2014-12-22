package com.ocs.protocol.diameter;

class packunpack
{
    public static final void pack8(final byte[] array, final int n, final byte b) {
        array[n] = b;
    }
    
    public static final void pack16(final byte[] array, final int n, final int n2) {
        array[n + 0] = (byte)(n2 >> 8 & 0xFF);
        array[n + 1] = (byte)(n2 & 0xFF);
    }
    
    public static final void pack32(final byte[] array, final int n, final int n2) {
        array[n + 0] = (byte)(n2 >> 24 & 0xFF);
        array[n + 1] = (byte)(n2 >> 16 & 0xFF);
        array[n + 2] = (byte)(n2 >> 8 & 0xFF);
        array[n + 3] = (byte)(n2 & 0xFF);
    }
    
    public static final void pack64(final byte[] array, final int n, final long n2) {
        array[n + 0] = (byte)(n2 >> 56 & 0xFFL);
        array[n + 1] = (byte)(n2 >> 48 & 0xFFL);
        array[n + 2] = (byte)(n2 >> 40 & 0xFFL);
        array[n + 3] = (byte)(n2 >> 32 & 0xFFL);
        array[n + 4] = (byte)(n2 >> 24 & 0xFFL);
        array[n + 5] = (byte)(n2 >> 16 & 0xFFL);
        array[n + 6] = (byte)(n2 >> 8 & 0xFFL);
        array[n + 7] = (byte)(n2 & 0xFFL);
    }
    
    public static final byte unpack8(final byte[] array, final int n) {
        return array[n];
    }
    
    public static final int unpack32(final byte[] array, final int n) {
        return (array[n + 0] & 0xFF) << 24 | (array[n + 1] & 0xFF) << 16 | (array[n + 2] & 0xFF) << 8 | (array[n + 3] & 0xFF);
    }
    
    public static final int unpack16(final byte[] array, final int n) {
        return (array[n + 0] & 0xFF) << 8 | (array[n + 1] & 0xFF);
    }
    
    public static final long unpack64(final byte[] array, final int n) {
        return (array[n + 0] & 0xFF) << 56 | (array[n + 1] & 0xFF) << 48 | (array[n + 2] & 0xFF) << 40 | (array[n + 3] & 0xFF) << 32 | (array[n + 4] & 0xFF) << 24 | (array[n + 5] & 0xFF) << 16 | (array[n + 6] & 0xFF) << 8 | (array[n + 7] & 0xFF);
    }
}
