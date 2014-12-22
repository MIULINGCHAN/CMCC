package com.ocs.protocol.diameter.node;

import dk.i1.sctp.AssociationId;
import dk.i1.sctp.SCTPSocket;

public class RelevantSCTPAuthInfo
{
    public SCTPSocket sctp_socket;
    public AssociationId assoc_id;
    
    RelevantSCTPAuthInfo(final SCTPSocket sctp_socket, final AssociationId assoc_id) {
        super();
        this.sctp_socket = sctp_socket;
        this.assoc_id = assoc_id;
    }
}
