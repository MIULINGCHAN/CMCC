package com.ocs.protocol.diameter.node;

import java.io.*;
import com.ocs.protocol.diameter.*;

public class SimpleSyncClient extends NodeManager
{
    private Peer[] peers;
    
    public SimpleSyncClient(final NodeSettings nodeSettings, final Peer[] peers) {
        super(nodeSettings);
        this.peers = peers;
    }
    
    public void start() throws IOException, UnsupportedTransportProtocolException {
        super.start();
        final Peer[] peers = this.peers;
        for (int length = peers.length, i = 0; i < length; ++i) {
            this.node().initiateConnection(peers[i], true);
        }
    }
    
    protected void handleAnswer(final Message answer, final ConnectionKey connectionKey, final Object o) {
        final SyncCall syncCall = (SyncCall)o;
        synchronized (syncCall) {
            syncCall.answer = answer;
            syncCall.answer_ready = true;
            syncCall.notify();
        }
    }
    
    public Message sendRequest(final Message message) {
        final SyncCall syncCall = new SyncCall();
        syncCall.answer_ready = false;
        syncCall.answer = null;
        try {
            this.sendRequest(message, this.peers, syncCall);
            synchronized (syncCall) {
                while (!syncCall.answer_ready) {
                    syncCall.wait();
                }
            }
        }
        catch (NotRoutableException ex) {
            System.out.println("SimpleSyncClient.sendRequest(): not routable");
        }
        catch (InterruptedException ex2) {}
        catch (NotARequestException ex3) {}
        return syncCall.answer;
    }
    
    private static class SyncCall
    {
        boolean answer_ready;
        Message answer;
    }
}
