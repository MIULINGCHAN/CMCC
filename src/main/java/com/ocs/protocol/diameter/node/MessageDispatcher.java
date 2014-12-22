package com.ocs.protocol.diameter.node;

import com.ocs.protocol.diameter.*;

public interface MessageDispatcher
{
    boolean handle(Message p0, ConnectionKey p1, Peer p2);
}
