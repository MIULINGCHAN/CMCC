package com.ocs.protocol.diameter.node;

import java.nio.*;

class NormalConnectionBuffers extends ConnectionBuffers
{
    private ByteBuffer in_buffer;
    private ByteBuffer out_buffer;
    
    NormalConnectionBuffers() {
        super();
        this.in_buffer = ByteBuffer.allocate(8192);
        this.out_buffer = ByteBuffer.allocate(8192);
    }
    
    ByteBuffer netOutBuffer() {
        return this.out_buffer;
    }
    
    ByteBuffer netInBuffer() {
        return this.in_buffer;
    }
    
    ByteBuffer appInBuffer() {
        return this.in_buffer;
    }
    
    ByteBuffer appOutBuffer() {
        return this.out_buffer;
    }
    
    void processNetInBuffer() {
    }
    
    void processAppOutBuffer() {
    }
    
    void makeSpaceInNetInBuffer() {
        this.in_buffer = ConnectionBuffers.makeSpaceInBuffer(this.in_buffer, 4096);
    }
    
    void makeSpaceInAppOutBuffer(final int n) {
        this.out_buffer = ConnectionBuffers.makeSpaceInBuffer(this.out_buffer, n);
    }
}
