package com.ocs.protocol.diameter.node;

public interface ConnectionListener
{
    void handle(ConnectionKey p0, Peer p1, boolean p2);
}
