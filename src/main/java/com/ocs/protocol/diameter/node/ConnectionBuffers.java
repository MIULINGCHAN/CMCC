package com.ocs.protocol.diameter.node;

import java.nio.*;

abstract class ConnectionBuffers
{
    abstract ByteBuffer netOutBuffer();
    
    abstract ByteBuffer netInBuffer();
    
    abstract ByteBuffer appInBuffer();
    
    abstract ByteBuffer appOutBuffer();
    
    abstract void processNetInBuffer();
    
    abstract void processAppOutBuffer();
    
    abstract void makeSpaceInNetInBuffer();
    
    abstract void makeSpaceInAppOutBuffer(final int p0);
    
    void consumeNetOutBuffer(final int n) {
        consume(this.netOutBuffer(), n);
    }
    
    void consumeAppInBuffer(final int n) {
        consume(this.appInBuffer(), n);
    }
    
    static ByteBuffer makeSpaceInBuffer(ByteBuffer byteBuffer, final int n) {
        if (byteBuffer.position() + n > byteBuffer.capacity()) {
            final int position = byteBuffer.position();
            final int n2 = byteBuffer.capacity() + n;
            final ByteBuffer allocate = ByteBuffer.allocate(n2 + (4096 - n2 % 4096));
            byteBuffer.flip();
            allocate.put(byteBuffer);
            allocate.position(position);
            byteBuffer = allocate;
        }
        return byteBuffer;
    }
    
    private static void consume(final ByteBuffer byteBuffer, final int n) {
        byteBuffer.limit(byteBuffer.position());
        byteBuffer.position(n);
        byteBuffer.compact();
    }
}
