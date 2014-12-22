package com.ocs.protocol.diameter;

public class MessageHeader
{
    byte version;
    private byte command_flags;
    public int command_code;
    public int application_id;
    public int hop_by_hop_identifier;
    public int end_to_end_identifier;
    public static final byte command_flag_request_bit = Byte.MIN_VALUE;
    public static final byte command_flag_proxiable_bit = 64;
    public static final byte command_flag_error_bit = 32;
    public static final byte command_flag_retransmit_bit = 16;
    
    public boolean isRequest() {
        return (this.command_flags & 0xFFFFFF80) != 0x0;
    }
    
    public boolean isProxiable() {
        return (this.command_flags & 0x40) != 0x0;
    }
    
    public boolean isError() {
        return (this.command_flags & 0x20) != 0x0;
    }
    
    public boolean isRetransmit() {
        return (this.command_flags & 0x10) != 0x0;
    }
    
    public void setRequest(final boolean b) {
        if (b) {
            this.command_flags |= 0xFFFFFF80;
        }
        else {
            this.command_flags &= 0x7F;
        }
    }
    
    public void setProxiable(final boolean b) {
        if (b) {
            this.command_flags |= 0x40;
        }
        else {
            this.command_flags &= 0xFFFFFFBF;
        }
    }
    
    public void setError(final boolean b) {
        if (b) {
            this.command_flags |= 0x20;
        }
        else {
            this.command_flags &= 0xFFFFFFDF;
        }
    }
    
    public void setRetransmit(final boolean b) {
        if (b) {
            this.command_flags |= 0x10;
        }
        else {
            this.command_flags &= 0xFFFFFFEF;
        }
    }
    
    public MessageHeader() {
        super();
        this.version = 1;
    }
    
    public MessageHeader(final MessageHeader messageHeader) {
        super();
        this.version = messageHeader.version;
        this.command_flags = messageHeader.command_flags;
        this.command_code = messageHeader.command_code;
        this.application_id = messageHeader.application_id;
        this.hop_by_hop_identifier = messageHeader.hop_by_hop_identifier;
        this.end_to_end_identifier = messageHeader.end_to_end_identifier;
    }
    
    int encodeSize() {
        return 20;
    }

    
    /**
    目测n是array中的偏移，n2是整个message的字节长度
    */
    int encode(final byte[] array, final int n, final int n2) { 
        packunpack.pack32(array, n + 0, n2);
        packunpack.pack8(array, n + 0, this.version);
        packunpack.pack32(array, n + 4, this.command_code);
        packunpack.pack8(array, n + 4, this.command_flags);
        packunpack.pack32(array, n + 8, this.application_id);
        packunpack.pack32(array, n + 12, this.hop_by_hop_identifier);
        packunpack.pack32(array, n + 16, this.end_to_end_identifier);
        return 20;
    }
    
    void decode(final byte[] array, final int n) {
        this.version = packunpack.unpack8(array, n + 0);
        this.command_flags = packunpack.unpack8(array, n + 4);
        this.command_code = (packunpack.unpack32(array, n + 4) & 0xFFFFFF);
        this.application_id = packunpack.unpack32(array, n + 8);
        this.hop_by_hop_identifier = packunpack.unpack32(array, n + 12);
        this.end_to_end_identifier = packunpack.unpack32(array, n + 16);
    }
    
    public void prepareResponse(final MessageHeader messageHeader) {
        this.command_flags = (byte)(messageHeader.command_flags & 0x40);
        this.command_code = messageHeader.command_code;
        this.application_id = messageHeader.application_id;
        this.hop_by_hop_identifier = messageHeader.hop_by_hop_identifier;
        this.end_to_end_identifier = messageHeader.end_to_end_identifier;
    }
    
    public void prepareAnswer(final MessageHeader messageHeader) {
        this.prepareResponse(messageHeader);
    }
}
