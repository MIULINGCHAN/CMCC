package com.ocs.protocol.diameter.node;

@SuppressWarnings("serial")
public class UnsupportedURIException extends Exception
{
    public UnsupportedURIException(final String s) {
        super(s);
    }
    
    public UnsupportedURIException(final Throwable t) {
        super(t);
    }
}
