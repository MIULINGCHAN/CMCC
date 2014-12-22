package com.ocs.protocol.diameter;

import java.net.*;

@SuppressWarnings("unused")
public class AVP_Address extends AVP_OctetString
{
    public AVP_Address(final AVP avp) throws InvalidAVPLengthException, InvalidAddressTypeException {
        super(avp);
        if (avp.queryPayloadSize() < 2) {
            throw new InvalidAVPLengthException(avp);
        }
        final int unpack16 = packunpack.unpack16(this.payload, 0);
        if (unpack16 == 1) {
            if (avp.queryPayloadSize() != 6) {
                throw new InvalidAVPLengthException(avp);
            }
        }
        else {
            if (unpack16 != 2) {
                throw new InvalidAddressTypeException(avp);
            }
            if (avp.queryPayloadSize() != 18) {
                throw new InvalidAVPLengthException(avp);
            }
        }
    }
    
    public AVP_Address(final int n, final InetAddress inetAddress) {
        super(n, InetAddress2byte(inetAddress));
    }
    
    public AVP_Address(final int n, final int n2, final InetAddress inetAddress) {
        super(n, n2, InetAddress2byte(inetAddress));
    }
    
    public InetAddress queryAddress() throws InvalidAVPLengthException, InvalidAddressTypeException {
        if (this.queryPayloadSize() < 2) {
            throw new InvalidAVPLengthException(this);
        }
        final byte[] queryValue = this.queryValue();
        final int unpack16 = packunpack.unpack16(queryValue, 0);
        try {
            switch (unpack16) {
                case 1: {
                    if (this.queryPayloadSize() != 6) {
                        throw new InvalidAVPLengthException(this);
                    }
                    final byte[] array = new byte[4];
                    System.arraycopy(queryValue, 2, array, 0, 4);
                    return InetAddress.getByAddress(array);
                }
                case 2: {
                    if (this.queryPayloadSize() != 18) {
                        throw new InvalidAVPLengthException(this);
                    }
                    final byte[] array2 = new byte[16];
                    System.arraycopy(queryValue, 2, array2, 0, 16);
                    return InetAddress.getByAddress(array2);
                }
                default: {
                    throw new InvalidAddressTypeException(this);
                }
            }
        }
        catch (UnknownHostException ex) {
            return null;
        }
    }
    
    public void setAddress(final InetAddress inetAddress) {
        this.setValue(InetAddress2byte(inetAddress));
    }
    
    private static final byte[] InetAddress2byte(final InetAddress inetAddress) {
        final byte[] address = inetAddress.getAddress();
        int n;
        try {
            final Inet4Address inet4Address = (Inet4Address)inetAddress;
            n = 1;
        }
        catch (ClassCastException ex) {
            final Inet6Address inet6Address = (Inet6Address)inetAddress;
            n = 2;
        }
        final byte[] array = new byte[2 + address.length];
        packunpack.pack16(array, 0, n);
        System.arraycopy(address, 0, array, 2, address.length);
        return array;
    }
}
