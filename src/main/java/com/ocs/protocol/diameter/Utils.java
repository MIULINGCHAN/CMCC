package com.ocs.protocol.diameter;

import java.util.*;

public final class Utils
{
    private static final int[] empty_array;
    public static final int[] rfc3588_mandatory_codes;
    public static final int[] rfc3588_grouped_avps;
    public static final int[] rfc4006_mandatory_codes;
    public static final int[] rfc4006_grouped_avps;
    public static final ABNFComponent[] abnf_cer;
    public static final ABNFComponent[] abnf_cea;
    public static final ABNFComponent[] abnf_dpr;
    public static final ABNFComponent[] abnf_dpa;
    public static final ABNFComponent[] abnf_dwr;
    public static final ABNFComponent[] abnf_dwa;
    public static final ABNFComponent[] abnf_rar;
    public static final ABNFComponent[] abnf_raa;
    public static final ABNFComponent[] abnf_str;
    public static final ABNFComponent[] abnf_sta;
    public static final ABNFComponent[] abnf_asr;
    public static final ABNFComponent[] abnf_asa;
    
    private static final boolean contains(final int[] array, final int n) {
        for (int length = array.length, i = 0; i < length; ++i) {
            if (array[i] == n) {
                return true;
            }
        }
        return false;
    }
    
    private static final boolean setMandatory(final AVP avp, final int[] array, final int[] array2) {
        boolean b = false;
        if (avp.vendor_id == 0 && contains(array2, avp.code)) {
            try {
                final AVP_Grouped avp_Grouped = new AVP_Grouped(avp);
                final AVP[] queryAVPs;
                final AVP[] avPs = queryAVPs = avp_Grouped.queryAVPs();
                for (int length = queryAVPs.length, i = 0; i < length; ++i) {
                    b = (setMandatory(queryAVPs[i], array, array2) || b);
                }
                boolean b2 = false;
                for (final AVP avp2 : avPs) {
                    b2 = (b2 || avp2.isMandatory());
                }
                if (b2 && !avp.isMandatory()) {
                    avp_Grouped.setMandatory(true);
                    b = true;
                }
                if (b) {
                    avp_Grouped.setAVPs(avPs);
                    avp.inline_shallow_replace(avp_Grouped);
                }
            }
            catch (InvalidAVPLengthException ex) {}
        }
        if (!avp.isMandatory() && avp.vendor_id == 0 && contains(array, avp.code)) {
            avp.setMandatory(true);
            b = true;
        }
        return b;
    }
    
    public static final void setMandatory(final Iterable<AVP> iterable, final int[] array, final int[] array2) {
        final Iterator<AVP> iterator = iterable.iterator();
        while (iterator.hasNext()) {
            setMandatory(iterator.next(), array, array2);
        }
    }
    
    public static final void setMandatory(final Iterable<AVP> iterable, final int[] array) {
        setMandatory(iterable, array, Utils.empty_array);
    }
    
    public static final void setMandatory(final Iterable<AVP> iterable, final Collection<Integer> collection) {
        for (final AVP avp : iterable) {
            if (collection.contains(avp.code) && avp.vendor_id == 0) {
                avp.setMandatory(true);
            }
        }
    }
    
    public static final void setMandatory(final Message message, final int[] array) {
        setMandatory(message.avps(), array, Utils.empty_array);
    }
    
    public static final void setMandatory(final Message message, final int[] array, final int[] array2) {
        setMandatory(message.avps(), array, array2);
    }
    
    public static final void setMandatory_RFC3588(final Iterable<AVP> iterable) {
        setMandatory(iterable, Utils.rfc3588_mandatory_codes, Utils.rfc3588_grouped_avps);
    }
    
    public static final void setMandatory_RFC3588(final Message message) {
        setMandatory(message.avps(), Utils.rfc3588_mandatory_codes, Utils.rfc3588_grouped_avps);
    }
    
    public static final void setMandatory_RFC4006(final Iterable<AVP> iterable) {
        setMandatory(iterable, Utils.rfc4006_mandatory_codes, Utils.rfc4006_grouped_avps);
    }
    
    public static final void setMandatory_RFC4006(final Message message) {
        setMandatory(message.avps(), Utils.rfc4006_mandatory_codes, Utils.rfc4006_grouped_avps);
    }
    
    public static final void copyProxyInfo(final Message message, final Message message2) {
        final Iterator<AVP> iterator = message.subset(284).iterator();
        while (iterator.hasNext()) {
            message2.add(new AVP(iterator.next()));
        }
    }
    
    public static final CheckABNFFailure checkABNF(final Message message, final ABNFComponent[] array) {
        boolean b = false;
        for (int i = 0; i < array.length; ++i) {
            if (array[i].code == -1) {
                b = true;
                break;
            }
        }
        for (int j = 0; j < array.length; ++j) {
            final int code = array[j].code;
            if (code != -1) {
                if (array[j].min_count != 0 || array[j].max_count != -1) {
                    final int count = message.count(code);
                    if (count < array[j].min_count) {
                        return new CheckABNFFailure(new AVP(code, new byte[0]), 5005, (array[j].min_count == 1) ? null : ("AVP must occur at least " + array[j].min_count + " times"));
                    }
                    if (array[j].max_count != -1 && count > array[j].max_count) {
                        AVP avp = null;
                        int n = 0;
                        for (final AVP avp2 : message.subset(code)) {
                            if (++n > array[j].max_count) {
                                avp = avp2;
                                break;
                            }
                        }
                        return new CheckABNFFailure(new AVP(avp), 5009, null);
                    }
                    if (array[j].fixed_position) {
                        final int find_first = message.find_first(array[j].code);
                        if (find_first != j) {
                            return new CheckABNFFailure(new AVP(message.get(find_first)), 5004, "AVP occurs at wrong position");
                        }
                    }
                }
            }
        }
        if (!b) {
            for (final AVP avp3 : message.avps()) {
                boolean b2 = false;
                for (int length = array.length, k = 0; k < length; ++k) {
                    if (avp3.code == array[k].code) {
                        b2 = true;
                        break;
                    }
                }
                if (!b2) {
                    return new CheckABNFFailure(new AVP(avp3), 5008, null);
                }
            }
        }
        return null;
    }
    
    static {
        empty_array = new int[0];
        rfc3588_mandatory_codes = new int[] { 483, 485, 480, 44, 287, 259, 85, 50, 291, 258, 276, 274, 277, 25, 293, 283, 273, 300, 55, 297, 298, 279, 257, 299, 272, 264, 296, 278, 280, 284, 33, 292, 261, 262, 268, 285, 282, 270, 263, 271, 27, 265, 295, 1, 266, 260 };
        rfc3588_grouped_avps = new int[] { 300, 297, 279, 284, 260 };
        rfc4006_mandatory_codes = new int[] { 412, 413, 414, 415, 416, 417, 418, 419, 420, 421, 454, 422, 423, 424, 426, 427, 425, 428, 429, 449, 430, 431, 453, 457, 456, 455, 432, 433, 434, 435, 436, 437, 438, 461, 439, 443, 444, 450, 452, 451, 445, 446, 447, 448 };
        rfc4006_grouped_avps = new int[] { 413, 423, 430, 431, 457, 456, 434, 437, 440, 443, 445, 446, 458 };
        abnf_cer = new ABNFComponent[] { new ABNFComponent(false, 1, 1, 264), new ABNFComponent(false, 1, 1, 296), new ABNFComponent(false, 1, -1, 257), new ABNFComponent(false, 1, 1, 266), new ABNFComponent(false, 1, 1, 269), new ABNFComponent(false, 0, 1, 278), new ABNFComponent(false, 0, -1, 265), new ABNFComponent(false, 0, -1, 258), new ABNFComponent(false, 0, -1, 299), new ABNFComponent(false, 0, -1, 259), new ABNFComponent(false, 0, -1, 260), new ABNFComponent(false, 0, 1, 267), new ABNFComponent(false, 0, -1, -1) };
        abnf_cea = new ABNFComponent[] { new ABNFComponent(false, 1, 1, 268), new ABNFComponent(false, 1, 1, 264), new ABNFComponent(false, 1, 1, 296), new ABNFComponent(false, 1, -1, 257), new ABNFComponent(false, 1, 1, 266), new ABNFComponent(false, 1, 1, 269), new ABNFComponent(false, 0, 1, 278), new ABNFComponent(false, 0, 1, 281), new ABNFComponent(false, 0, 1, 279), new ABNFComponent(false, 0, -1, 265), new ABNFComponent(false, 0, -1, 258), new ABNFComponent(false, 0, -1, 299), new ABNFComponent(false, 0, -1, 259), new ABNFComponent(false, 0, -1, 260), new ABNFComponent(false, 0, 1, 267), new ABNFComponent(false, 0, -1, -1) };
        abnf_dpr = new ABNFComponent[] { new ABNFComponent(false, 1, 1, 264), new ABNFComponent(false, 1, 1, 296), new ABNFComponent(false, 1, 1, 273) };
        abnf_dpa = new ABNFComponent[] { new ABNFComponent(false, 1, 1, 268), new ABNFComponent(false, 1, 1, 264), new ABNFComponent(false, 1, 1, 296), new ABNFComponent(false, 0, 1, 281), new ABNFComponent(false, 0, 1, 279) };
        abnf_dwr = new ABNFComponent[] { new ABNFComponent(false, 1, 1, 264), new ABNFComponent(false, 1, 1, 296), new ABNFComponent(false, 0, 1, 278) };
        abnf_dwa = new ABNFComponent[] { new ABNFComponent(false, 1, 1, 268), new ABNFComponent(false, 1, 1, 264), new ABNFComponent(false, 1, 1, 296), new ABNFComponent(false, 0, 1, 281), new ABNFComponent(false, 0, 1, 279), new ABNFComponent(false, 0, 1, 278) };
        abnf_rar = new ABNFComponent[] { new ABNFComponent(true, 1, 1, 263), new ABNFComponent(false, 1, 1, 264), new ABNFComponent(false, 1, 1, 296), new ABNFComponent(false, 1, 1, 283), new ABNFComponent(false, 1, 1, 293), new ABNFComponent(false, 0, 1, 258), new ABNFComponent(false, 0, 1, 285), new ABNFComponent(false, 0, -1, -1) };
        abnf_raa = new ABNFComponent[] { new ABNFComponent(true, 1, 1, 263), new ABNFComponent(false, 1, 1, 268), new ABNFComponent(false, 1, 1, 264), new ABNFComponent(false, 1, 1, 296), new ABNFComponent(false, 0, -1, -1) };
        abnf_str = new ABNFComponent[] { new ABNFComponent(true, 1, 1, 263), new ABNFComponent(false, 1, 1, 264), new ABNFComponent(false, 1, 1, 296), new ABNFComponent(false, 1, 1, 283), new ABNFComponent(false, 1, 1, 258), new ABNFComponent(false, 1, 1, 295), new ABNFComponent(false, 0, 1, 1), new ABNFComponent(false, 0, 1, 293), new ABNFComponent(false, 0, -1, 25), new ABNFComponent(false, 0, 1, 278), new ABNFComponent(false, 0, -1, 284), new ABNFComponent(false, 0, -1, 282), new ABNFComponent(false, 0, -1, -1) };
        abnf_sta = new ABNFComponent[] { new ABNFComponent(true, 1, 1, 263), new ABNFComponent(false, 1, 1, 268), new ABNFComponent(false, 1, 1, 264), new ABNFComponent(false, 1, 1, 296), new ABNFComponent(false, 0, 1, 1), new ABNFComponent(false, 0, -1, 25), new ABNFComponent(false, 0, 1, 281), new ABNFComponent(false, 0, 1, 294), new ABNFComponent(false, 0, -1, 279), new ABNFComponent(false, 0, 1, 278), new ABNFComponent(false, 0, -1, 292), new ABNFComponent(false, 0, 1, 261), new ABNFComponent(false, 0, 1, 262), new ABNFComponent(false, 0, -1, 284), new ABNFComponent(false, 0, -1, -1) };
        abnf_asr = new ABNFComponent[] { new ABNFComponent(true, 1, 1, 263), new ABNFComponent(false, 1, 1, 264), new ABNFComponent(false, 1, 1, 296), new ABNFComponent(false, 1, 1, 283), new ABNFComponent(false, 1, 1, 293), new ABNFComponent(false, 1, 1, 258), new ABNFComponent(false, 0, 1, 1), new ABNFComponent(false, 0, 1, 278), new ABNFComponent(false, 0, -1, 284), new ABNFComponent(false, 0, -1, 282), new ABNFComponent(false, 0, -1, -1) };
        abnf_asa = new ABNFComponent[] { new ABNFComponent(true, 1, 1, 263), new ABNFComponent(false, 1, 1, 268), new ABNFComponent(false, 1, 1, 264), new ABNFComponent(false, 1, 1, 296), new ABNFComponent(false, 0, 1, 1), new ABNFComponent(false, 0, 1, 278), new ABNFComponent(false, 0, 1, 281), new ABNFComponent(false, 0, 1, 294), new ABNFComponent(false, 0, -1, 279), new ABNFComponent(false, 0, -1, 292), new ABNFComponent(false, 0, 1, 261), new ABNFComponent(false, 0, 1, 262), new ABNFComponent(false, 0, -1, 284), new ABNFComponent(false, 0, -1, -1) };
    }
    
    public static class ABNFComponent
    {
        public boolean fixed_position;
        public int min_count;
        public int max_count;
        public int code;
        
        public ABNFComponent(final boolean fixed_position, final int min_count, final int max_count, final int code) {
            super();
            this.fixed_position = fixed_position;
            this.min_count = min_count;
            this.max_count = max_count;
            this.code = code;
        }
    }
    
    public static class CheckABNFFailure
    {
        public AVP failed_avp;
        public int result_code;
        public String error_message;
        
        public CheckABNFFailure(final AVP failed_avp, final int result_code, final String error_message) {
            super();
            this.failed_avp = failed_avp;
            this.result_code = result_code;
            this.error_message = error_message;
        }
    }
}
