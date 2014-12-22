package com.ocs.protocol.diameter.node;

public class NodeSettings
{
    private String host_id;
    private String realm;
    private int vendor_id;
    private Capability capabilities;
    private int port;
    private String product_name;
    private int firmware_revision;
    private long watchdog_interval;
    private long idle_close_timeout;
    private Boolean use_tcp;
    private Boolean use_sctp;
    
    public NodeSettings(final String host_id, final String realm, final int vendor_id, final Capability capabilities, final int port, final String product_name, final int firmware_revision) throws InvalidSettingException {
        super();
        if (host_id == null) {
            throw new InvalidSettingException("null host_id");
        }
        final int index = host_id.indexOf(46);
        if (index == -1) {
            throw new InvalidSettingException("host_id must contains at least 2 dots");
        }
        if (host_id.indexOf(46, index + 1) == -1) {
            throw new InvalidSettingException("host_id must contains at least 2 dots");
        }
        this.host_id = host_id;
        if (realm.indexOf(46) == -1) {
            throw new InvalidSettingException("realm must contain at least 1 dot");
        }
        this.realm = realm;
        if (vendor_id == 0) {
            throw new InvalidSettingException("vendor_id must not be non-zero. (It must be your IANA-assigned \"SMI Network Management Private Enterprise Code\". See http://www.iana.org/assignments/enterprise-numbers)");
        }
        this.vendor_id = vendor_id;
        if (capabilities.isEmpty()) {
            throw new InvalidSettingException("Capabilities must be non-empty");
        }
        this.capabilities = capabilities;
        if (port < 0 || port > 65535) {
            throw new InvalidSettingException("listen-port must be 0..65535");
        }
        this.port = port;
        if (product_name == null) {
            throw new InvalidSettingException("product-name cannot be null");
        }
        this.product_name = product_name;
        this.firmware_revision = firmware_revision;
        this.watchdog_interval = 30000L;
        this.idle_close_timeout = 604800000L;
    }
    
    public String hostId() {
        return this.host_id;
    }
    
    public String realm() {
        return this.realm;
    }
    
    public int vendorId() {
        return this.vendor_id;
    }
    
    public Capability capabilities() {
        return this.capabilities;
    }
    
    public int port() {
        return this.port;
    }
    
    public String productName() {
        return this.product_name;
    }
    
    public int firmwareRevision() {
        return this.firmware_revision;
    }
    
    public long watchdogInterval() {
        return this.watchdog_interval;
    }
    
    public void setWatchdogInterval(final long watchdog_interval) throws InvalidSettingException {
        if (watchdog_interval < 6000L) {
            throw new InvalidSettingException("watchdog interval must be at least 6 seconds. RFC3539 section 3.4.1 item 1");
        }
        this.watchdog_interval = watchdog_interval;
    }
    
    public long idleTimeout() {
        return this.idle_close_timeout;
    }
    
    public void setIdleTimeout(final long idle_close_timeout) throws InvalidSettingException {
        if (idle_close_timeout < 0L) {
            throw new InvalidSettingException("idle timeout cannot be negative");
        }
        this.idle_close_timeout = idle_close_timeout;
    }
    
    public Boolean useTCP() {
        return this.use_tcp;
    }
    
    public void setUseTCP(final Boolean use_tcp) {
        this.use_tcp = use_tcp;
    }
    
    public Boolean useSCTP() {
        return this.use_sctp;
    }
    
    public void setUseSCTP(final Boolean use_sctp) {
        this.use_sctp = use_sctp;
    }
}
