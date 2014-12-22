package com.ocs.protocol.diameter.session;

import com.ocs.protocol.diameter.*;

public interface Session
{
    String sessionId();
    
    int handleRequest(Message p0);
    
    void handleAnswer(Message p0, Object p1);
    
    void handleNonAnswer(int p0, Object p1);
    
    long calcNextTimeout();
    
    void handleTimeout();
}
