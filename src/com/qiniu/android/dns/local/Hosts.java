package com.qiniu.android.dns.local;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Random;

import com.qiniu.android.dns.Domain;
import com.qiniu.android.dns.NetworkInfo;

/**
 * Created by bailong on 15/6/18.
 */
public final class Hosts {

    private final Hashtable<String, ArrayList<Value>> hosts = new Hashtable<String, ArrayList<Value>>();
    private final Random random = new Random();

    public String[] query(Domain domain, NetworkInfo info) {
        ArrayList<Value> vals = hosts.get(domain.domain);
        if (vals == null || vals.isEmpty()) {
            return null;
        }
        vals = filte(vals, info);
        return toIps(vals);
    }

    private ArrayList<Value> filte(ArrayList<Value> origin, NetworkInfo info) {
        ArrayList<Value> normal = new ArrayList<Value>();
        ArrayList<Value> specical = new ArrayList<Value>();
        for (Value v : origin) {
            if (v.provider == NetworkInfo.ISP_GENERAL) {
                normal.add(v);
            }
            if (info.provider != NetworkInfo.ISP_GENERAL
                    && v.provider == info.provider) {
                specical.add(v);
            }
        }
        if (specical.size() != 0) {
            return specical;
        }
        return normal;
    }

    public String[] toIps(ArrayList<Value> vals) {
        int size = vals.size();

        String[] r = new String[size];
        for (int i = 0; i < size; i++) {
            Value v = vals.get(i);
            r[i] = v.ip;
        }
        return r;
    }

    public Hosts put(String domain, Value val) {
        ArrayList<Value> vals = hosts.get(domain);
        if (vals == null) {
            vals = new ArrayList<Value>();
        }
        vals.add(val);
        hosts.put(domain, vals);
        return this;
    }

    public Hosts put(String domain, String val) {
        put(domain, new Value(val));
        return this;
    }

    public static class Value {
        public final String ip;
        public final int provider;

        public Value(String ip, int provider) {
            this.ip = ip;
            this.provider = provider;
        }

        public Value(String ip) {
            this(ip, NetworkInfo.ISP_GENERAL);
        }

        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || !(o instanceof Value)) {
                return false;
            }
            Value another = (Value) o;
            return this.ip.equals(another.ip)
                    && this.provider == another.provider;
        }
    }
}
