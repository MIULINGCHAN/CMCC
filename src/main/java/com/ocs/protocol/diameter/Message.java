package com.ocs.protocol.diameter;

import java.util.*;

public class Message
{
    public MessageHeader hdr;
    private ArrayList<AVP> avp;
    
    public Message() {
        super();
        this.hdr = new MessageHeader();
        this.avp = new ArrayList<AVP>();
    }
    
    public Message(final MessageHeader messageHeader) {
        super();
        this.hdr = new MessageHeader(messageHeader);
        this.avp = new ArrayList<AVP>();
    }
    
    public Message(final Message message) {
        this(message.hdr);
        final Iterator<AVP> iterator = message.avp.iterator();
        while (iterator.hasNext()) {
            this.avp.add(new AVP(iterator.next()));
        }
    }
    
    public int encodeSize() {
        int n = 0 + this.hdr.encodeSize();
        final Iterator<AVP> iterator = this.avp.iterator();
        while (iterator.hasNext()) {
            n += iterator.next().encodeSize();
        }
        return n;
    }
    
    public void encode(final byte[] array) {
        final int encodeSize = this.encodeSize();
        final int n = 0;
        int n2 = n + this.hdr.encode(array, n, encodeSize);
        final Iterator<AVP> iterator = this.avp.iterator();
        while (iterator.hasNext()) {
            n2 += iterator.next().encode(array, n2);
        }
    }
    
    public byte[] encode() {
        final int encodeSize = this.encodeSize();
        final byte[] array = new byte[encodeSize];
        final int n = 0;
        int n2 = n + this.hdr.encode(array, n, encodeSize);
        final Iterator<AVP> iterator = this.avp.iterator();
        while (iterator.hasNext()) {
            n2 += iterator.next().encode(array, n2);
        }
        return array;
    }
    
    public static int decodeSize(final byte[] array, final int n) {
        final int unpack32 = packunpack.unpack32(array, n);
        final int n2 = unpack32 >> 24 & 0xFF;
        final int n3 = unpack32 & 0xFFFFFF;
        if (n2 != 1 || n3 < 20 || n3 % 4 != 0) {
            return 4;
        }
        return n3;
    }
    
    public decode_status decode(final byte[] array) {
        return this.decode(array, 0, array.length);
    }
    
    public decode_status decode(final byte[] array, int n, final int n2) {
        if (n2 < 1) {
            return decode_status.not_enough;
        }
        if (packunpack.unpack8(array, n) != 1) {
            return decode_status.garbage;
        }
        if (n2 < 4) {
            return decode_status.not_enough;
        }
        final int decodeSize = decodeSize(array, n);
        if ((decodeSize & 0x3) != 0x0) {
            return decode_status.garbage;
        }
        if (decodeSize < 20) {
            return decode_status.garbage;
        }
        if (n2 < 20) {
            return decode_status.not_enough;
        }
        if (decodeSize == -1) {
            return decode_status.garbage;
        }
        if (n2 < decodeSize) {
            return decode_status.not_enough;
        }
        this.hdr.decode(array, n);
        if (this.hdr.version != 1) {
            return decode_status.garbage;
        }
        n += 20;
        int i = n2 - 20;
        final ArrayList<AVP> avp = new ArrayList<AVP>(i / 16);
        while (i > 0) {
            if (i < 8) {
                return decode_status.garbage;
            }
            final int decodeSize2 = AVP.decodeSize(array, n, i);
            if (decodeSize2 == 0) {
                return decode_status.garbage;
            }
            if (decodeSize2 > i) {
                return decode_status.garbage;
            }
            final AVP avp2 = new AVP();
            if (!avp2.decode(array, n, decodeSize2)) {
                return decode_status.garbage;
            }
            avp.add(avp2);
            n += decodeSize2;
            i -= decodeSize2;
        }
        if (i != 0) {
            return decode_status.garbage;
        }
        this.avp = (ArrayList<AVP>)avp;
        return decode_status.decoded;
    }
    
    public int size() {
        return this.avp.size();
    }
    
    public void ensureCapacity(final int n) {
        this.avp.ensureCapacity(n);
    }
    
    public AVP get(final int n) {
        return new AVP(this.avp.get(n));
    }
    
    public void clear() {
        this.avp.clear();
    }
    
    public void add(final AVP avp) {
        this.avp.add(avp);
    }
    
    public void add(final int n, final AVP avp) {
        this.avp.add(n, avp);
    }
    
    public void remove(final int n) {
        this.avp.remove(n);
    }
    
    public Iterable<AVP> avps() {
        return this.avp;
    }
    
    public Iterator<AVP> iterator() {
        return this.avp.iterator();
    }
    
    public Iterator<AVP> iterator(final int n) {
        return this.iterator(n, 0);
    }
    
    public Iterator<AVP> iterator(final int n, final int n2) {
        return new AVPIterator(this.avp.listIterator(), n, n2);
    }
    
    public void prepareResponse(final Message message) {
        this.hdr.prepareResponse(message.hdr);
    }
    
    public void prepareAnswer(final Message message) {
        this.prepareResponse(message);
    }
    
    public Iterable<AVP> subset(final int n) {
        return this.subset(n, 0);
    }
    
    public Iterable<AVP> subset(final int n, final int n2) {
        return new Subset(this, n, n2);
    }
    
    public AVP find(final int n) {
        return this.find(n, 0);
    }
    
    public AVP find(final int n, final int n2) {
        for (final AVP avp : this.avp) {
            if (avp.code == n && avp.vendor_id == n2) {
                return avp;
            }
        }
        return null;
    }
    
    int find_first(final int n) {
        int n2 = 0;
        final Iterator<AVP> iterator = this.avp.iterator();
        while (iterator.hasNext()) {
            if (iterator.next().code == n) {
                return n2;
            }
            ++n2;
        }
        return -1;
    }
    
    int count(final int n) {
        int n2 = 0;
        final Iterator<AVP> iterator = this.avp.iterator();
        while (iterator.hasNext()) {
            if (iterator.next().code == n) {
                ++n2;
            }
        }
        return n2;
    }
    
    public enum decode_status
    {
        decoded, 
        not_enough, 
        garbage;
    }
    
    private class AVPIterator implements Iterator<AVP>
    {
        private ListIterator<AVP> i;
        private int code;
        private int vendor_id;
        
        AVPIterator(final ListIterator<AVP> i, final int code, final int vendor_id) {
            super();
            this.i = i;
            this.code = code;
            this.vendor_id = vendor_id;
        }
        
        public void remove() {
            this.i.remove();
        }
        
        public boolean hasNext() {
            while (this.i.hasNext()) {
                final AVP avp = this.i.next();
                if (avp.code == this.code && (this.vendor_id == 0 || avp.vendor_id == this.vendor_id)) {
                    this.i.previous();
                    return true;
                }
            }
            return false;
        }
        
        public AVP next() {
            return this.i.next();
        }
    }
    
    private class Subset implements Iterable<AVP>
    {
        Message msg;
        int code;
        int vendor_id;
        
        Subset(final Message msg, final int code, final int vendor_id) {
            super();
            this.msg = msg;
            this.code = code;
            this.vendor_id = vendor_id;
        }
        
        public Iterator<AVP> iterator() {
            return this.msg.iterator(this.code, this.vendor_id);
        }
    }
}
