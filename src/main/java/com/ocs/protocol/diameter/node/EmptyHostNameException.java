package com.ocs.protocol.diameter.node;

@SuppressWarnings("serial")
public class EmptyHostNameException extends Exception
{
    public EmptyHostNameException() {
        super();
    }
    
    public EmptyHostNameException(final String s) {
        super(s);
    }
    
    public EmptyHostNameException(final Throwable t) {
        super(t);
    }
}
