package com.ocs.protocol.diameter.node;

import dk.i1.sctp.AssociationId;

import java.util.*;
import java.net.*;

class SCTPConnection extends Connection
{
    private LinkedList<byte[]> queued_messages;
    private SCTPNode node_impl;
    AssociationId assoc_id;
    boolean closed;
    short sac_inbound_streams;
    short sac_outbound_streams;
    short out_stream_index;
    
    SCTPConnection(final SCTPNode node_impl, final long n, final long n2) {
        super(node_impl, n, n2);
        this.queued_messages = new LinkedList<byte[]>();
        this.node_impl = node_impl;
        this.closed = false;
        this.sac_inbound_streams = 0;
        this.sac_outbound_streams = 0;
        this.out_stream_index = 0;
    }
    
    short nextOutStream() {
        final short out_stream_index = this.out_stream_index;
        this.out_stream_index = (short)((this.out_stream_index + 1) % this.sac_outbound_streams);
        return out_stream_index;
    }
    
    InetAddress toInetAddress() {
        final Iterator<InetAddress> iterator = this.getLocalAddresses().iterator();
        if (iterator.hasNext()) {
            return iterator.next();
        }
        return null;
    }
    
    void sendMessage(final byte[] array) {
        this.node_impl.sendMessage(this, array);
    }
    
    Object getRelevantNodeAuthInfo() {
        return new RelevantSCTPAuthInfo(this.node_impl.sctp_socket, this.assoc_id);
    }
    
    Collection<InetAddress> getLocalAddresses() {
        try {
            return (Collection<InetAddress>)this.node_impl.sctp_socket.getLocalInetAddresses(this.assoc_id);
        }
        catch (SocketException ex) {
            return null;
        }
    }
    
    Peer toPeer() {
        try {
            return new Peer(this.toInetAddress(), this.node_impl.sctp_socket.getPeerInetPort(this.assoc_id));
        }
        catch (SocketException ex) {
            return null;
        }
    }
    
    void queueMessage(final byte[] array) {
        this.queued_messages.addLast(array);
    }
    
    byte[] peekFirstQueuedMessage() {
        return this.queued_messages.peek();
    }
    
    void removeFirstQueuedMessage() {
        this.queued_messages.poll();
    }
}
