package com.ocs.protocol.diameter.node;

import java.util.*;
import java.security.*;

class ConnectionTimers
{
    long last_activity;
    long last_real_activity;
    long last_in_dw;
    boolean dw_outstanding;
    long cfg_watchdog_timer;
    long watchdog_timer_with_jitter;
    long cfg_idle_close_timeout;
    private static Random random;
    
    private static synchronized long generateJitter() {
        if (ConnectionTimers.random == null) {
            try {
                ConnectionTimers.random = SecureRandom.getInstance("SHA1PRNG");
            }
            catch (NoSuchAlgorithmException ex) {}
            if (ConnectionTimers.random == null) {
                ConnectionTimers.random = new Random();
            }
        }
        final byte[] array = new byte[2];
        ConnectionTimers.random.nextBytes(array);
        int n = array[0] * 256 + array[1];
        if (n < 0) {
            n += 65536;
        }
        int n2 = n % 4001;
        n2 -= 2000;
        return n2;
    }
    
    public ConnectionTimers(final long cfg_watchdog_timer, final long cfg_idle_close_timeout) {
        super();
        this.last_activity = System.currentTimeMillis();
        this.last_real_activity = System.currentTimeMillis();
        this.last_in_dw = System.currentTimeMillis();
        this.dw_outstanding = false;
        this.cfg_watchdog_timer = cfg_watchdog_timer;
        this.watchdog_timer_with_jitter = this.cfg_watchdog_timer + generateJitter();
        this.cfg_idle_close_timeout = cfg_idle_close_timeout;
    }
    
    public void markDWR() {
        this.last_in_dw = System.currentTimeMillis();
    }
    
    public void markDWA() {
        this.last_in_dw = System.currentTimeMillis();
        this.dw_outstanding = false;
    }
    
    public void markActivity() {
        this.last_activity = System.currentTimeMillis();
    }
    
    public void markCER() {
        this.last_activity = System.currentTimeMillis();
    }
    
    public void markRealActivity() {
        this.last_real_activity = this.last_activity;
    }
    
    public void markDWR_out() {
        this.dw_outstanding = true;
        this.last_activity = System.currentTimeMillis();
        this.watchdog_timer_with_jitter = this.cfg_watchdog_timer + generateJitter();
    }
    
    public long calcNextTimeout(final boolean b) {
        if (!b) {
            return this.last_activity + this.watchdog_timer_with_jitter;
        }
        long n;
        if (!this.dw_outstanding) {
            n = this.last_activity + this.watchdog_timer_with_jitter;
        }
        else {
            n = this.last_activity + this.watchdog_timer_with_jitter + this.cfg_watchdog_timer;
        }
        if (this.cfg_idle_close_timeout != 0L) {
            final long n2 = this.last_real_activity + this.cfg_idle_close_timeout;
            if (n2 < n) {
                return n2;
            }
        }
        return n;
    }
    
    public timer_action calcAction(final boolean b) {
        final long currentTimeMillis = System.currentTimeMillis();
        if (!b) {
            if (currentTimeMillis >= this.last_activity + this.watchdog_timer_with_jitter) {
                return timer_action.disconnect_no_cer;
            }
            return timer_action.none;
        }
        else {
            if (this.cfg_idle_close_timeout != 0L && currentTimeMillis >= this.last_real_activity + this.cfg_idle_close_timeout) {
                return timer_action.disconnect_idle;
            }
            if (currentTimeMillis >= this.last_activity + this.watchdog_timer_with_jitter) {
                if (!this.dw_outstanding) {
                    return timer_action.dwr;
                }
                if (currentTimeMillis >= this.last_activity + this.cfg_watchdog_timer + this.cfg_watchdog_timer) {
                    return timer_action.disconnect_no_dw;
                }
            }
            return timer_action.none;
        }
    }
    
    static {
        ConnectionTimers.random = null;
    }
    
    public enum timer_action
    {
        none, 
        disconnect_no_cer, 
        disconnect_idle, 
        disconnect_no_dw, 
        dwr;
    }
}
