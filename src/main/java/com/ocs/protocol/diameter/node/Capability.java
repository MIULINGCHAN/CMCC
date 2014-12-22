package com.ocs.protocol.diameter.node;

import java.util.*;

public class Capability
{
    Set<Integer> supported_vendor;
    Set<Integer> auth_app;
    Set<Integer> acct_app;
    Set<VendorApplication> auth_vendor;
    Set<VendorApplication> acct_vendor;
    
    public Capability() {
        super();
        this.supported_vendor = new HashSet<Integer>();
        this.auth_app = new HashSet<Integer>();
        this.acct_app = new HashSet<Integer>();
        this.auth_vendor = new HashSet<VendorApplication>();
        this.acct_vendor = new HashSet<VendorApplication>();
    }
    
    public Capability(final Capability capability) {
        super();
        this.supported_vendor = new HashSet<Integer>();
        final Iterator<Integer> iterator = capability.supported_vendor.iterator();
        while (iterator.hasNext()) {
            this.supported_vendor.add(iterator.next());
        }
        this.auth_app = new HashSet<Integer>();
        final Iterator<Integer> iterator2 = capability.auth_app.iterator();
        while (iterator2.hasNext()) {
            this.auth_app.add(iterator2.next());
        }
        this.acct_app = new HashSet<Integer>();
        final Iterator<Integer> iterator3 = capability.acct_app.iterator();
        while (iterator3.hasNext()) {
            this.acct_app.add(iterator3.next());
        }
        this.auth_vendor = new HashSet<VendorApplication>();
        final Iterator<VendorApplication> iterator4 = capability.auth_vendor.iterator();
        while (iterator4.hasNext()) {
            this.auth_vendor.add(iterator4.next());
        }
        this.acct_vendor = new HashSet<VendorApplication>();
        final Iterator<VendorApplication> iterator5 = capability.acct_vendor.iterator();
        while (iterator5.hasNext()) {
            this.acct_vendor.add(iterator5.next());
        }
    }
    
    public boolean isSupportedVendor(final int n) {
        return this.supported_vendor.contains(n);
    }
    
    public boolean isAllowedAuthApp(final int n) {
        return this.auth_app.contains(n) || this.auth_app.contains(-1);
    }
    
    public boolean isAllowedAcctApp(final int n) {
        return this.acct_app.contains(n) || this.acct_app.contains(-1);
    }
    
    public boolean isAllowedAuthApp(final int n, final int n2) {
        return this.auth_vendor.contains(new VendorApplication(n, n2));
    }
    
    public boolean isAllowedAcctApp(final int n, final int n2) {
        return this.acct_vendor.contains(new VendorApplication(n, n2));
    }
    
    public void addSupportedVendor(final int n) {
        this.supported_vendor.add(n);
    }
    
    public void addAuthApp(final int n) {
        this.auth_app.add(n);
    }
    
    public void addAcctApp(final int n) {
        this.acct_app.add(n);
    }
    
    public void addVendorAuthApp(final int n, final int n2) {
        this.auth_vendor.add(new VendorApplication(n, n2));
    }
    
    public void addVendorAcctApp(final int n, final int n2) {
        this.acct_vendor.add(new VendorApplication(n, n2));
    }
    
    public boolean isEmpty() {
        return this.auth_app.isEmpty() && this.acct_app.isEmpty() && this.auth_vendor.isEmpty() && this.acct_vendor.isEmpty();
    }
    
    static Capability calculateIntersection(final Capability capability, final Capability capability2) {
        final Capability capability3 = new Capability();
        for (final Integer n : capability2.supported_vendor) {
            if (capability.isSupportedVendor(n)) {
                capability3.addSupportedVendor(n);
            }
        }
        for (final Integer n2 : capability2.auth_app) {
            if (n2 == -1 || capability.auth_app.contains(n2) || capability.auth_app.contains(-1)) {
                capability3.addAuthApp(n2);
            }
        }
        for (final Integer n3 : capability2.acct_app) {
            if (n3 == -1 || capability.acct_app.contains(n3) || capability.acct_app.contains(-1)) {
                capability3.addAcctApp(n3);
            }
        }
        for (final VendorApplication vendorApplication : capability2.auth_vendor) {
            if (capability.isAllowedAuthApp(vendorApplication.vendor_id, vendorApplication.application_id)) {
                capability3.addVendorAuthApp(vendorApplication.vendor_id, vendorApplication.application_id);
            }
        }
        for (final VendorApplication vendorApplication2 : capability2.acct_vendor) {
            if (capability.isAllowedAcctApp(vendorApplication2.vendor_id, vendorApplication2.application_id)) {
                capability3.addVendorAcctApp(vendorApplication2.vendor_id, vendorApplication2.application_id);
            }
        }
        return capability3;
    }
    
    static class VendorApplication
    {
        public int vendor_id;
        public int application_id;
        
        public VendorApplication(final int vendor_id, final int application_id) {
            super();
            this.vendor_id = vendor_id;
            this.application_id = application_id;
        }
        
        public int hashCode() {
            return this.vendor_id + this.application_id;
        }
        
        public boolean equals(final Object o) {
            return ((VendorApplication)o).vendor_id == this.vendor_id && ((VendorApplication)o).application_id == this.application_id;
        }
    }
}
