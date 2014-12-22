package com.ocs.protocol.diameter.node;

public class DefaultNodeValidator implements NodeValidator
{
    public AuthenticationResult authenticateNode(final String s, final Object o) {
        final AuthenticationResult authenticationResult = new AuthenticationResult();
        authenticationResult.known = true;
        return authenticationResult;
    }
    
    public Capability authorizeNode(final String s, final NodeSettings nodeSettings, final Capability capability) {
        return Capability.calculateIntersection(nodeSettings.capabilities(), capability);
    }
}
