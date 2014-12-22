package com.ocs.protocol.diameter.node;

import java.nio.channels.*;
import java.net.*;
import java.util.*;

class TCPConnection extends Connection
{
    TCPNode node_impl;
    SocketChannel channel;
    ConnectionBuffers connection_buffers;
    
    public TCPConnection(final TCPNode node_impl, final long n, final long n2) {
        super(node_impl, n, n2);
        this.node_impl = node_impl;
        this.connection_buffers = new NormalConnectionBuffers();
    }
    
    void makeSpaceInNetInBuffer() {
        this.connection_buffers.makeSpaceInNetInBuffer();
    }
    
    void makeSpaceInAppOutBuffer(final int n) {
        this.connection_buffers.makeSpaceInAppOutBuffer(n);
    }
    
    void consumeAppInBuffer(final int n) {
        this.connection_buffers.consumeAppInBuffer(n);
    }
    
    void consumeNetOutBuffer(final int n) {
        this.connection_buffers.consumeNetOutBuffer(n);
    }
    
    boolean hasNetOutput() {
        return this.connection_buffers.netOutBuffer().position() != 0;
    }
    
    void processNetInBuffer() {
        this.connection_buffers.processNetInBuffer();
    }
    
    void processAppOutBuffer() {
        this.connection_buffers.processAppOutBuffer();
    }
    
    InetAddress toInetAddress() {
        return ((InetSocketAddress)this.channel.socket().getRemoteSocketAddress()).getAddress();
    }
    
    void sendMessage(final byte[] array) {
        this.node_impl.sendMessage(this, array);
    }
    
    Object getRelevantNodeAuthInfo() {
        return this.channel;
    }
    
    Collection<InetAddress> getLocalAddresses() {
        final ArrayList<InetAddress> list = new ArrayList<InetAddress>();
        list.add(this.channel.socket().getLocalAddress());
        return list;
    }
    
    Peer toPeer() {
        return new Peer(this.toInetAddress(), this.channel.socket().getPort());
    }
}
