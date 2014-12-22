package com.ocs.protocol.diameter.session;

public class SessionAuthTimers
{
    private long latest_auth_time;
    private long next_reauth_time;
    private long auth_timeout;
    
    public void updateTimers(final long latest_auth_time, final long n, final long n2) {
        this.latest_auth_time = latest_auth_time;
        if (n != 0L) {
            this.auth_timeout = this.latest_auth_time + n + n2;
            if (n2 != 0L) {
                this.next_reauth_time = this.latest_auth_time + n;
            }
            else {
                this.next_reauth_time = Math.max(latest_auth_time + n / 2L, this.auth_timeout - 10L);
            }
        }
        else {
            this.next_reauth_time = Long.MAX_VALUE;
            this.auth_timeout = Long.MAX_VALUE;
        }
    }
    
    public long getNextReauthTime() {
        return this.next_reauth_time;
    }
    
    public long getMaxTimeout() {
        return this.auth_timeout;
    }
}
