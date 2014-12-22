package com.ocs.protocol.diameter;

import java.io.*;

public class AVP_UTF8String extends AVP
{
    public AVP_UTF8String(final AVP avp) {
        super(avp);
    }
    
    public AVP_UTF8String(final int n, final String s) {
        super(n, string2byte(s));
    }
    
    public AVP_UTF8String(final int n, final int n2, final String s) {
        super(n, n2, string2byte(s));
    }
    
    public String queryValue() {
        try {
            return new String(this.queryPayload(), "UTF-8");
        }
        catch (UnsupportedEncodingException ex) {
            return null;
        }
    }
    
    public void setValue(final String s) {
        this.setPayload(string2byte(s));
    }
    
    private static final byte[] string2byte(final String s) {
        try {
            return s.getBytes("UTF-8");
        }
        catch (UnsupportedEncodingException ex) {
            return null;
        }
    }
}
