package com.ocs.protocol.diameter.node;

@SuppressWarnings("serial")
public class UnsupportedTransportProtocolException extends Exception
{
    public UnsupportedTransportProtocolException(final String s) {
        super(s);
    }
    
    public UnsupportedTransportProtocolException(final String s, final Throwable t) {
        super(s, t);
    }
}
