package com.jiagu.device.controller;

import com.jiagu.api.helper.MemoryHelper;

public class UBXParser {

    private byte[] mRecvBuf = new byte[4096];
    private int mRecvLen = 0;
    private int mRecvState = 0;
    private int mPackLen = 0;

    private static final int ST_PROTO = 1;
    private static final int ST_HEAD = 2;
    private static final int ST_PAYLOAD = 3;

    public void receiveData(byte[] data, int offset, int len) {
        for (int i = 0; i < len; i++, offset++) {
            int in = data[offset] & 0xFF;
            switch (mRecvState) {
                case 0:
                    mRecvState = (in == 0xB5) ? ST_PROTO : 0;
                    break;
                case ST_PROTO:
                    if (in == 0x62) {
                        mRecvState = ST_HEAD;
                    } else if (in == 0xB5) {
                        mRecvState = ST_PROTO;
                    } else {
                        mRecvState = 0;
                    }
                    break;

                case ST_HEAD:
                    mRecvBuf[mRecvLen++] = (byte) in;
                    if (mRecvLen == 4) {
                        mPackLen = MemoryHelper.LittleEndian.toShort(mRecvBuf, 2) & 0xFFFF;
                        mRecvState = ST_PAYLOAD;
                    }
                    break;

                default:
                    mRecvBuf[mRecvLen++] = (byte) in;
                    if (mRecvLen == mPackLen + 6) { // CLASS ID LENGTH ... CK_A CK_B
                        if (checkData()) {
                            processData();
                        }
                        mRecvLen = mRecvState = 0;
                    } else if (mRecvLen >= 4096) {
                        mRecvLen = mRecvState = 0;
                    }
            }
        }
    }

    private boolean checkData() {
        byte CK_A = 0, CK_B = 0;
        int i;
        for(i = 0; i < mRecvLen - 2; i++) {
            CK_A += mRecvBuf[i];
            CK_B += CK_A;
        }
        return (mRecvBuf[i] == CK_A && mRecvBuf[i+1] == CK_B);
    }

    private void processData() {
        if (mRecvBuf[0] == 1) {
            if (mRecvBuf[1] == 2) { // NAV-POSLLH
                parsePosition();
            } else if (mRecvBuf[1] == 4) { // NAV-DOP
                parseDop();
            }
        }
    }

    public double lng, lat;
    public double alt;
    public float hDOP;
    public float accuracy;
    private void parsePosition() {
        MemoryHelper.MemoryReader reader = new MemoryHelper.MemoryReader(mRecvBuf, 4, mRecvLen - 2);
        reader.skip(4);
        lng = reader.readLEInt() / 1E7;
        lat = reader.readLEInt() / 1E7;
        alt = reader.readLEInt() / 1000f;
        reader.skip(4);
        accuracy = reader.readLEUInt() / 1000f;
    }

    private void parseDop() {
        MemoryHelper.MemoryReader reader = new MemoryHelper.MemoryReader(mRecvBuf, 4, mRecvLen - 2);
        reader.skip(12);
        hDOP = reader.readLEUShort() / 100f;
    }
}
