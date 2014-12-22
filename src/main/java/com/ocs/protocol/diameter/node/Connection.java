package com.ocs.protocol.diameter.node;

import java.net.*;
import java.util.*;

abstract class Connection
{
    NodeImplementation node_impl;
    public Peer peer;
    public String host_id;
    public ConnectionTimers timers;
    public ConnectionKey key;
    private int hop_by_hop_identifier_seq;
    public State state;
    
    public Connection(final NodeImplementation node_impl, final long n, final long n2) {
        super();
        this.node_impl = node_impl;
        this.timers = new ConnectionTimers(n, n2);
        this.key = new ConnectionKey();
        this.hop_by_hop_identifier_seq = new Random().nextInt();
        this.state = State.connected_in;
    }
    
    public synchronized int nextHopByHopIdentifier() {
        return this.hop_by_hop_identifier_seq++;
    }
    
    abstract InetAddress toInetAddress();
    
    abstract void sendMessage(final byte[] p0);
    
    abstract Object getRelevantNodeAuthInfo();
    
    abstract Collection<InetAddress> getLocalAddresses();
    
    abstract Peer toPeer();
    
    long watchdogInterval() {
        return this.timers.cfg_watchdog_timer;
    }
    
    public enum State
    {
        connecting, 
        connected_in, 
        connected_out, 
        tls, 
        ready, 
        closing, 
        closed;
    }
}
