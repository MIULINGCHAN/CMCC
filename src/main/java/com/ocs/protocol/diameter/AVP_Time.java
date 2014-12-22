package com.ocs.protocol.diameter;

import java.util.*;

@SuppressWarnings("unused")
public class AVP_Time extends AVP_Unsigned32
{
    private static final int seconds_between_1900_and_1970 = -2085978496;
    
    public AVP_Time(final AVP avp) throws InvalidAVPLengthException {
        super(avp);
    }
    
    public AVP_Time(final int n, final Date date) {
        this(n, 0, date);
    }
    
    public AVP_Time(final int n, final int n2, final Date date) {
        super(n, n2, (int)(date.getTime() / 1000L - 2085978496L));
    }
    
    public AVP_Time(final int n, final int n2) {
        this(n, 0, n2);
    }
    
    public AVP_Time(final int n, final int n2, final int n3) {
        super(n, n2, n3 - 2085978496);
    }
    
    public Date queryDate() {
        return new Date(super.queryValue() + 2085978496);
    }
    
    public int querySecondsSince1970() {
        return super.queryValue() + 2085978496;
    }
    
    public void setValue(final Date date) {
        super.setValue((int)(date.getTime() / 1000L - 2085978496L));
    }
}
