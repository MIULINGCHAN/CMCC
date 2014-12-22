package com.ocs.protocol.diameter.node;

import com.ocs.protocol.diameter.*;

class DefaultMessageDispatcher implements MessageDispatcher
{
    public boolean handle(final Message message, final ConnectionKey connectionKey, final Peer peer) {
        return false;
    }
}
