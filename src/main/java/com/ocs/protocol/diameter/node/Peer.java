package com.ocs.protocol.diameter.node;

import java.net.*;
import java.util.*;

public class Peer
{
    private String host;
    private int port;
    private boolean secure;
    TransportProtocol transport_protocol;
    public Capability capabilities;
    
    public Peer(final InetAddress inetAddress) {
        this(inetAddress, TransportProtocol.tcp);
    }
    
    public Peer(final InetAddress inetAddress, final TransportProtocol transportProtocol) {
        this(inetAddress, 3868, transportProtocol);
    }
    
    public Peer(final InetAddress inetAddress, final int n) {
        this(inetAddress, n, TransportProtocol.tcp);
    }
    
    public Peer(final InetAddress inetAddress, final int port, final TransportProtocol transport_protocol) {
        super();
        this.host = inetAddress.getHostAddress();
        this.port = port;
        this.secure = false;
        this.transport_protocol = transport_protocol;
    }
    
    public Peer(final String s) throws EmptyHostNameException {
        this(s, 3868);
    }
    
    public Peer(final String s, final int n) throws EmptyHostNameException {
        this(s, n, TransportProtocol.tcp);
    }
    
    public Peer(final String s, final int port, final TransportProtocol transport_protocol) throws EmptyHostNameException {
        super();
        if (s.length() == 0) {
            throw new EmptyHostNameException();
        }
        this.host = new String(s);
        this.port = port;
        this.secure = false;
        this.transport_protocol = transport_protocol;
    }
    
    public Peer(final InetSocketAddress inetSocketAddress) {
        super();
        this.host = inetSocketAddress.getAddress().getHostAddress();
        this.port = inetSocketAddress.getPort();
        this.secure = false;
        this.transport_protocol = TransportProtocol.tcp;
    }
    
    public Peer(final URI uri) throws UnsupportedURIException {
        super();
        if (uri.getScheme() != null && !uri.getScheme().equals("aaa") && !uri.getScheme().equals("aaas")) {
            throw new UnsupportedURIException("Only aaa: schemes are supported");
        }
        if (uri.getUserInfo() != null) {
            throw new UnsupportedURIException("userinfo not supported in Diameter URIs");
        }
        if (uri.getPath() != null && !uri.getPath().equals("")) {
            throw new UnsupportedURIException("path not supported in Diameter URIs");
        }
        this.host = uri.getHost();
        this.port = uri.getPort();
        if (this.port == -1) {
            this.port = 3868;
        }
        this.secure = uri.getScheme().equals("aaas");
        this.transport_protocol = TransportProtocol.tcp;
    }
    
    public Peer(final Peer peer) {
        super();
        this.host = new String(peer.host);
        this.port = peer.port;
        this.secure = peer.secure;
        if (peer.capabilities != null) {
            this.capabilities = new Capability(peer.capabilities);
        }
        this.transport_protocol = peer.transport_protocol;
    }
    
    public URI uri() {
        try {
            return new URI(this.secure ? "aaas" : "aaa", null, this.host, this.port, null, null, null);
        }
        catch (URISyntaxException ex) {
            return null;
        }
    }
    
    public static Peer fromURIString(String substring) throws UnsupportedURIException {
        final int index = substring.indexOf(59);
        String substring2 = null;
        if (index != -1) {
            substring2 = substring.substring(index + 1);
            substring = substring.substring(0, index);
        }
        URI uri;
        try {
            uri = new URI(substring);
        }
        catch (URISyntaxException ex) {
            throw new UnsupportedURIException(ex);
        }
        final Peer peer = new Peer(uri);
        if (substring2 != null) {
            final StringTokenizer stringTokenizer = new StringTokenizer(substring2, ";");
            while (stringTokenizer.hasMoreTokens()) {
                final StringTokenizer stringTokenizer2 = new StringTokenizer(stringTokenizer.nextToken(), "=");
                if (!stringTokenizer2.hasMoreTokens()) {
                    continue;
                }
                if (!stringTokenizer2.nextToken().equals("transport")) {
                    continue;
                }
                if (!stringTokenizer2.hasMoreTokens()) {
                    continue;
                }
                final String nextToken = stringTokenizer2.nextToken();
                if (nextToken.equals("sctp")) {
                    peer.transport_protocol = TransportProtocol.sctp;
                }
                else {
                    if (!nextToken.equals("tcp")) {
                        throw new UnsupportedURIException("Unknown transport-protocol: " + nextToken);
                    }
                    peer.transport_protocol = TransportProtocol.tcp;
                }
            }
        }
        return peer;
    }
    
    public String host() {
        return this.host;
    }
    
    public void host(final String host) {
        this.host = host;
    }
    
    public int port() {
        return this.port;
    }
    
    public void port(final int port) {
        this.port = port;
    }
    
    public boolean secure() {
        return this.secure;
    }
    
    public void secure(final boolean secure) {
        this.secure = secure;
    }
    
    public TransportProtocol transportProtocol() {
        return this.transport_protocol;
    }
    
    public void transportProtocol(final TransportProtocol transport_protocol) {
        this.transport_protocol = transport_protocol;
    }
    
    public String toString() {
        return (this.secure ? "aaas" : "aaa") + "://" + this.host + ":" + new Integer(this.port).toString() + ((this.transport_protocol == TransportProtocol.tcp) ? "" : ";transport=sctp");
    }
    
    public int hashCode() {
        return this.port + this.host.hashCode();
    }
    
    public boolean equals(final Object o) {
        final Peer peer = (Peer)o;
        return this.port == peer.port && this.host.equals(peer.host);
    }
    
    public enum TransportProtocol
    {
        tcp, 
        sctp;
    }
}
