package com.ocs.protocol.diameter.node;

@SuppressWarnings("serial")
public class NotRoutableException extends Exception
{
    public NotRoutableException() {
        super();
    }
    
    public NotRoutableException(final String s) {
        super(s);
    }
    
    public NotRoutableException(final Throwable t) {
        super(t);
    }
}
