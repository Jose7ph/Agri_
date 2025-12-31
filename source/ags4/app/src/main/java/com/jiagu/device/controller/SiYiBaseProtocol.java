package com.jiagu.device.controller;

import com.jiagu.api.helper.MemoryHelper;

public abstract class SiYiBaseProtocol extends SimpleFilter {

    private static final int MYBUF_LEN = 2048;

    private static final int ST_ZERO = 0;
    private static final int ST_C_HEAD1 = 1;
    private static final int ST_C_HEAD2 = 2;
    private static final int ST_C_LEN = 3;

    public SiYiBaseProtocol() {
        super(MYBUF_LEN);
    }

    private final byte[] HEADER = {0x55, 0x66};
    public SiYiBaseProtocol(byte[] header) {
        super(MYBUF_LEN);
        if (header.length != 2) {
            throw new IllegalArgumentException("header length must be 2");
        }
        HEADER[0] = header[0];
        HEADER[1] = header[1];
    }

    private void addToMyData(byte b, int s) {
        owned[ownedLen++] = b;
        state = s;
    }

    private int expected = 0;
    @Override
    public void feedData(byte[] bs) {
        for (byte b : bs) {
            switch (state) {
                case ST_ZERO:
                    if (b == HEADER[0]) {
                        addToMyData(b, ST_C_HEAD1);
                    } else {
                        rest[restLen++] = b;
                    }
                    break;
                case ST_C_HEAD1:
                    if (b == HEADER[1]) {
                        addToMyData(b, ST_C_HEAD2);
                        expected = 5;
                    } else if (b == HEADER[0]) {
                        throwMyData();
                        addToMyData(b, ST_C_HEAD1);
                    } else {
                        clearMyData(b);
                        state = ST_ZERO;
                    }
                    break;
                case ST_C_HEAD2:
                    owned[ownedLen++] = b;
                    if (ownedLen >= expected) {
                        state = ST_C_LEN;
                        expected = MemoryHelper.LittleEndian.toShort(owned, 3) & 0xFFFF;
                        expected += 10;
                        if (expected > MYBUF_LEN) {
                            throwMyData();
                        }
                    }
                    break;
                case ST_C_LEN:
                    owned[ownedLen++] = b;
                    if (ownedLen >= expected) {
                        if (!checkData()) {
                            throwMyData();
                        } else {
                            parseSiYi();
                            state = ST_ZERO;
                            ownedLen = 0;
                        }
                    }
                    break;
            }
        }
    }

    private boolean checkData() {
        int crc = Checksum.crc16_ccitt(owned, 0, expected - 2);
        int chksum = MemoryHelper.LittleEndian.toShort(owned, expected - 2) & 0xFFFF;
        return crc == chksum;
    }

    protected abstract void parseSiYi();

    private short seq = 0;
    protected byte[] wrapCommand(int cmdId, byte ackFlag, byte[] cmd) {
        int len = (cmd != null) ? cmd.length : 0;
        byte[] out = new byte[len + 10];
        out[0] = HEADER[0];
        out[1] = HEADER[1];
        out[2] = ackFlag;
        MemoryHelper.LittleEndian.putShort(out, 3, (short) len);
        MemoryHelper.LittleEndian.putShort(out, 5, seq++);
        out[7] = (byte) cmdId;
        if (len > 0) {
            System.arraycopy(cmd, 0, out, 8, len);
        }
        int crc = Checksum.crc16_ccitt(out, 0, len + 8);
        MemoryHelper.LittleEndian.putShort(out, 8 + len, (short) crc);
        return out;
    }

    protected byte[] wrapCommand(int cmdId, byte[] cmd) {
        return wrapCommand(cmdId, (byte) 1, cmd);
    }
}
