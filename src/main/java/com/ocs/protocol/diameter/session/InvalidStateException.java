package com.ocs.protocol.diameter.session;

@SuppressWarnings("serial")
public class InvalidStateException extends Exception
{
    public InvalidStateException() {
        super();
    }
    
    public InvalidStateException(final String s) {
        super(s);
    }
    
    public InvalidStateException(final Throwable t) {
        super(t);
    }
}
