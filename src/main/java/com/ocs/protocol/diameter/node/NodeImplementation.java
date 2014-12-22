package com.ocs.protocol.diameter.node;

import java.util.logging.*;
import java.io.*;
import com.ocs.protocol.diameter.*;

abstract class NodeImplementation
{
    private Node node;
    protected NodeSettings settings;
    protected Logger logger;
    
    NodeImplementation(final Node node, final NodeSettings settings, final Logger logger) {
        super();
        this.node = node;
        this.settings = settings;
        this.logger = logger;
    }
    
    abstract void openIO() throws IOException;
    
    abstract void start();
    
    abstract void wakeup();
    
    abstract void initiateStop(final long p0);
    
    abstract void join();
    
    abstract void closeIO();
    
    abstract boolean initiateConnection(final Connection p0, final Peer p1);
    
    abstract void close(final Connection p0, final boolean p1);
    
    abstract Connection newConnection(final long p0, final long p1);
    
    boolean anyOpenConnections() {
        return this.node.anyOpenConnections(this);
    }
    
    void registerInboundConnection(final Connection connection) {
        this.node.registerInboundConnection(connection);
    }
    
    void unregisterConnection(final Connection connection) {
        this.node.unregisterConnection(connection);
    }
    
    long calcNextTimeout() {
        return this.node.calcNextTimeout(this);
    }
    
    void closeConnection(final Connection connection) {
        this.node.closeConnection(connection);
    }
    
    void closeConnection(final Connection connection, final boolean b) {
        this.node.closeConnection(connection, b);
    }
    
    boolean handleMessage(final Message message, final Connection connection) {
        return this.node.handleMessage(message, connection);
    }
    
    void runTimers() {
        this.node.runTimers(this);
    }
    
    void logRawDecodedPacket(final byte[] array, final int n, final int n2) {
        this.node.logRawDecodedPacket(array, n, n2);
    }
    
    void logGarbagePacket(final Connection connection, final byte[] array, final int n, final int n2) {
        this.node.logGarbagePacket(connection, array, n, n2);
    }
    
    Object getLockObject() {
        return this.node.getLockObject();
    }
    
    void initiateCER(final Connection connection) {
        this.node.initiateCER(connection);
    }
}
