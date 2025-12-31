package com.jiagu.device.controller;

import android.util.Log;
import android.util.SparseIntArray;

import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class Mavlink2 {
    public interface PacketListener {
        void onPacket(@NotNull byte[] pack, int len);
    }

    public static int msgId(@NotNull byte[] pack) {
        return (pack[7] & 0xFF) + ((pack[8] & 0xFF) << 8) + ((pack[9] & 0xFF) << 16);
    }

    private final byte[] buf = new byte[320];
    private final SparseIntArray crcExtra = new SparseIntArray();
    private int tail = 0;
    private final PacketListener listener;
    public Mavlink2(PacketListener l, Map<Integer, Integer> extra) {
        listener = l;
        for (int key : extra.keySet()) {
            crcExtra.append(key, extra.get(key));
        }
    }

    private static final int ST_STX = 1;
    private static final int ST_LEN = 2;
    private static final int ST_FLAG = 3;
    private int state = 0;
    private int expected = 0;
    public void feedData(byte[] data, int len) {
        for (int i = 0; len > 0; i++, len--) {
            byte b = data[i];
            switch (state) {
                case 0:
                    if (b == (byte) 0xFD) {
                        buf[tail++] = b;
                        state = ST_STX;
                    }
                    break;
                case ST_STX:
                    expected = (b & 0xFF) + 10;
                    buf[tail++] = b;
                    state = ST_LEN;
                    break;
                case ST_LEN:
                    buf[tail++] = b;
                    if (b == 1) expected += 12; // + 13 signature -1 current
                    else expected--;
                    state = ST_FLAG;
                    break;
                case ST_FLAG:
                    int actual = Math.min(expected, len);
                    System.arraycopy(data, i, buf, tail, actual);
                    len -= actual - 1;
                    i += actual - 1;
                    tail += actual;
                    expected -= actual;
                    if (expected == 0) {
                        if (checkCRC()) {
                            if (listener != null) {
                                listener.onPacket(buf, tail);
                            }
                        } else {
                            Log.e("yuhang", "mavlink: crc error");
                        }
                        tail = 0;
                        state = 0;
                    }
                    break;
            }
        }
    }

    private byte seq = 0;
    public byte[] wrapPacket(int msgId, byte[] data) {
        byte[] out = new byte[data.length + 12];
        int len = (data != null)? data.length : 0;
        out[0] = (byte) 0xFD;
        out[1] = (byte) len;
        out[2] = 0;
        out[3] = 0;
        out[4] = seq++;
        out[5] = 0;
        out[6] = 0;
        out[7] = (byte) (msgId & 0xFF);
        out[8] = (byte) ((msgId >> 8) & 0xFF);
        out[9] = (byte) ((msgId >> 16) & 0xFF);
        if (len > 0) {
            System.arraycopy(data, 0, out, 10, len);
        }
        Integer extra = crcExtra.get(msgId);
        out[len + 10] = (byte) ((extra == null)? 0 : extra);
        int crc = Checksum.crc_mcrf4xx(out, 1, len + 10);
        out[len + 10] = (byte) (crc & 0xFF);
        out[len + 11] = (byte) (crc >> 8);
        return out;
    }

    private final byte[] e = new byte[1];
    private boolean checkCRC() {
        int crc1 = ((buf[tail - 1] & 0xFF) << 8) + (buf[tail - 2] & 0xFF);
        int crc2 = Checksum.crc_mcrf4xx(buf, 1, tail - 3);
        int msgId = msgId(buf);
        Integer extra = crcExtra.get(msgId);
        if (extra != null) {
            e[0] = (byte) (int) extra;
            crc2 = Checksum.crc_mcrf4xx(crc2, e, 0, 1);
        }
        return crc1 == crc2;
    }
}
