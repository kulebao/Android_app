package com.qiniu.android.dns.local;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Random;

import com.qiniu.android.dns.DnsException;
import com.qiniu.android.dns.Domain;
import com.qiniu.android.dns.IResolver;
import com.qiniu.android.dns.NetworkInfo;
import com.qiniu.android.dns.Record;

/**
 * Created by bailong on 15/6/16.
 */
public final class Resolver implements IResolver {
    private static final Random random = new Random();

    final InetAddress address;

    public Resolver(InetAddress address) {
        this.address = address;
    }

    @Override
    public Record[] resolve(Domain domain, NetworkInfo info) throws IOException {
        int id;
        synchronized (random) {
            id = random.nextInt() & 0XFF;
        }
        byte[] query = DnsMessage.buildQuery(domain.domain, id);
        byte[] answer = udpCommunicate(query);
        if (answer == null) {
            throw new DnsException(domain.domain, "cant get answer");
        }
        Record[] records = DnsMessage.parseResponse(answer, id, domain.domain);

        return records;
    }

    private byte[] udpCommunicate(byte[] question) throws IOException {
        DatagramSocket socket = null;
        try {
            socket = new DatagramSocket();
            DatagramPacket packet = new DatagramPacket(question, question.length,
                    address, 53);
            socket.setSoTimeout(5000);
            socket.send(packet);
            packet = new DatagramPacket(new byte[1500], 1500);
            socket.receive(packet);

            return packet.getData();
        } finally {
            if (socket != null) {
                socket.close();
            }
        }
    }
}
