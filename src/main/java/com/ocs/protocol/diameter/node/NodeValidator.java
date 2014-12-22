package com.ocs.protocol.diameter.node;

public interface NodeValidator
{
    AuthenticationResult authenticateNode(String p0, Object p1);
    
    Capability authorizeNode(String p0, NodeSettings p1, Capability p2);
    
    public static class AuthenticationResult
    {
        public boolean known;
        public String error_message;
        public Integer result_code;
    }
}
