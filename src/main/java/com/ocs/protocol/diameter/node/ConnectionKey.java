package com.ocs.protocol.diameter.node;

public class ConnectionKey
{
    private static int i_seq;
    private int i;
    
    private static final synchronized int nextI() {
        return ConnectionKey.i_seq++;
    }
    
    public ConnectionKey() {
        super();
        this.i = nextI();
    }
    
    public int hashCode() {
        return this.i;
    }
    
    public boolean equals(final Object o) {
        return ((ConnectionKey)o).i == this.i;
    }
    
    static {
        ConnectionKey.i_seq = 0;
    }
}
