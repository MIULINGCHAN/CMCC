package com.ocs.protocol.diameter;

@SuppressWarnings("unused")
public class AVP
{
    byte[] payload;
    public int code;
    private int flags;
    public int vendor_id;
    private static final int avp_flag_vendor = 128;
    private static final int avp_flag_mandatory = 64;
    private static final int avp_flag_private = 32;
    
    public AVP() {
        super();
    }
    
    public AVP(final AVP avp) {
        super();
        this.payload = new byte[avp.payload.length];
        System.arraycopy(avp.payload, 0, this.payload, 0, avp.payload.length);
        this.code = avp.code;
        this.flags = avp.flags;
        this.vendor_id = avp.vendor_id;
    }
    
    public AVP(final int n, final byte[] array) {
        this(n, 0, array);
    }
    
    public AVP(final int code, final int vendor_id, final byte[] payload) {
        super();
        this.code = code;
        this.vendor_id = vendor_id;
        this.payload = payload;
    }
    
    static final int decodeSize(final byte[] array, final int n, final int n2) {
        if (n2 < 8) {
            return 0;
        }
        final int unpack32 = packunpack.unpack32(array, n + 4);
        final int n3 = unpack32 >> 24 & 0xFF;
        final int n4 = unpack32 & 0xFFFFFF;
        final int n5 = n4 + 3 & 0xFFFFFFFC;
        if ((n3 & 0x80) != 0x0) {
            if (n4 < 12) {
                return 0;
            }
        }
        else if (n4 < 8) {
            return 0;
        }
        return n5;
    }
    
    boolean decode(final byte[] array, final int n, final int n2) {
        if (n2 < 8) {
            return false;
        }
        int n3 = 0;
        this.code = packunpack.unpack32(array, n + n3);
        n3 += 4;
        final int unpack32 = packunpack.unpack32(array, n + n3);
        n3 += 4;
        this.flags = (unpack32 >> 24 & 0xFF);
        int n4 = unpack32 & 0xFFFFFF;
        if (n2 != (n4 + 3 & 0xFFFFFFFC)) {
            return false;
        }
        n4 -= 8;
        if ((this.flags & 0x80) != 0x0) {
            if (n4 < 4) {
                return false;
            }
            this.vendor_id = packunpack.unpack32(array, n + n3);
            n3 += 4;
            n4 -= 4;
        }
        else {
            this.vendor_id = 0;
        }
        this.setPayload(array, n + n3, n4);
        return true;
    }
    
    int encodeSize() {
        int n = 8;
        if (this.vendor_id != 0) {
            n += 4;
        }
        return n + (this.payload.length + 3 & 0xFFFFFFFC);
    }
    
    int encode(final byte[] array, final int n) {
        int n2 = 8;
        if (this.vendor_id != 0) {
            n2 += 4;
        }
        final int n3 = n2 + this.payload.length;
        final int flags = this.flags;
        int n4;
        if (this.vendor_id != 0) {
            n4 = (flags | 0x80);
        }
        else {
            n4 = (flags & 0xFFFFFF7F);
        }
        int n5 = 0;
        packunpack.pack32(array, n + n5, this.code);
        n5 += 4;
        packunpack.pack32(array, n + n5, n3 | n4 << 24);
        n5 += 4;
        if (this.vendor_id != 0) {
            packunpack.pack32(array, n + n5, this.vendor_id);
            n5 += 4;
        }
        System.arraycopy(this.payload, 0, array, n + n5, this.payload.length);
        return this.encodeSize();
    }
    
    byte[] encode() {
        int n = 8;
        if (this.vendor_id != 0) {
            n += 4;
        }
        final int n2 = n + this.payload.length;
        final int flags = this.flags;
        int n3;
        if (this.vendor_id != 0) {
            n3 = (flags | 0x80);
        }
        else {
            n3 = (flags & 0xFFFFFF7F);
        }
        final byte[] array = new byte[this.encodeSize()];
        int n4 = 0;
        packunpack.pack32(array, n4, this.code);
        n4 += 4;
        packunpack.pack32(array, n4, n2 | n3 << 24);
        n4 += 4;
        if (this.vendor_id != 0) {
            packunpack.pack32(array, n4, this.vendor_id);
            n4 += 4;
        }
        System.arraycopy(this.payload, 0, array, n4, this.payload.length);
        return array;
    }
    
    byte[] queryPayload() {
        final byte[] array = new byte[this.payload.length];
        System.arraycopy(this.payload, 0, array, 0, this.payload.length);
        return array;
    }
    
    int queryPayloadSize() {
        return this.payload.length;
    }
    
    void setPayload(final byte[] array) {
        this.setPayload(array, 0, array.length);
    }
    
    void setPayload(final byte[] array, final int n, final int n2) {
        final byte[] payload = new byte[n2];
        System.arraycopy(array, n, payload, 0, n2);
        this.payload = payload;
    }
    
    public boolean isVendorSpecific() {
        return this.vendor_id != 0;
    }
    
    public boolean isMandatory() {
        return (this.flags & 0x40) != 0x0;
    }
    
    public boolean isPrivate() {
        return (this.flags & 0x20) != 0x0;
    }
    
    public void setMandatory(final boolean b) {
        if (b) {
            this.flags |= 0x40;
        }
        else {
            this.flags &= 0xFFFFFFBF;
        }
    }
    
    public void setPrivate(final boolean b) {
        if (b) {
            this.flags |= 0x20;
        }
        else {
            this.flags &= 0xFFFFFFDF;
        }
    }
    
    public AVP setM() {
        this.flags |= 0x40;
        return this;
    }
    
    void inline_shallow_replace(final AVP avp) {
        this.payload = avp.payload;
        this.code = avp.code;
        this.flags = avp.flags;
        this.vendor_id = avp.vendor_id;
    }
}
