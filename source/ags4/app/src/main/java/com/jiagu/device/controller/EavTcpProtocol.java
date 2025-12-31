
package com.jiagu.device.controller;

import com.jiagu.api.helper.MemoryHelper;
import com.jiagu.device.channel.IChannel;

public class EavTcpProtocol extends SimpleFilter {

    private static final int MYBUF_LEN = 2048;

    private static final int ST_ZERO = 0;
    private static final int ST_C_HEAD1 = 1;
    private static final int ST_C_HEAD2 = 2;
    private static final int ST_C_LEN = 3;

    private final IController.Listener listener;
    private final IChannel.IWriter writer;
    public EavTcpProtocol(IChannel.IWriter w, IController.Listener l) {
        super(MYBUF_LEN);
        listener = l;
        writer = w;
    }

    private void addToMyData(byte b, int s) {
        owned[ownedLen++] = b;
        state = s;
    }

    private int expected = 0;
    @Override
    public void feedData(byte[] bs) {
//        Log.v("yuhang", "G: " + MemoryHelper.dumpData(bs, 0, bs.length));
        for (byte b : bs) {
            switch (state) {
                case ST_ZERO:
                    if (b == 0x66) {
                        addToMyData(b, ST_C_HEAD1);
                    } else {
                        rest[restLen++] = b;
                    }
                    break;
                case ST_C_HEAD1:
                    if (b == 0x55) {
                        addToMyData(b, ST_C_HEAD2);
                        expected = 10;
                    } else if (b == 0x66) {
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
                        expected = MemoryHelper.BigEndian.toShort(owned, 8) & 0xFFFF;
                        expected += 12;
                        if (expected > MYBUF_LEN) {
                            throwMyData();
                        }
                    }
                    break;
                case ST_C_LEN:
                    owned[ownedLen++] = b;
                    if (ownedLen >= expected) {
                        if (!checkData()) {
//                            Log.v("yuhang", "crc error");
                            throwMyData();
                        } else {
                            parseInfo();
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
        int chksum = MemoryHelper.BigEndian.toShort(owned, expected - 2) & 0xFFFF;
        return crc == chksum;
    }

    private short seq = 0;
    protected byte[] wrapCommand(int cmdId, byte[] cmd, int dst) {
        int len = (cmd != null) ? cmd.length : 0;
        byte[] out = new byte[len + 12];
        out[0] = 0x66;
        out[1] = 0x55;
        // Dst：消息的目的对象， 0表示遥控端的图传，2表示飞机端的图传
        out[2] = (byte) dst;
        out[3] = 3;
        MemoryHelper.BigEndian.putShort(out, 4, seq++);
        MemoryHelper.BigEndian.putShort(out, 6, (short) cmdId);
        MemoryHelper.BigEndian.putShort(out, 8, (short) len);
        if (len > 0) {
            System.arraycopy(cmd, 0, out, 10, len);
        }
        int crc = Checksum.crc16_ccitt(out, 0, len + 10);
        MemoryHelper.BigEndian.putShort(out, 10 + len, (short) crc);
//        Log.v("yuhang", "S: " + MemoryHelper.dumpData(out, 0, out.length));
        return out;
    }

    public void requireRssi() {
        byte[] out = wrapCommand(10, null, 0);
        writer.write(out);

        byte[] out2 = wrapCommand(10, null, 2);
        writer.write(out2);
    }

    // rt_info_t  rtinfo;
    // rtinfo. bb_status.usr[0].rssi[0]   // 天线1
    // rtinfo. bb_status.usr[0].rssi[1]   // 天线2
    //
    // #define MAX_GAIN_NUM              4
    // #define MAX_FREQ_CHAN_NUM         32
    //
    // typedef struct {
    //     ar_bb_status_t bb_status; // 300+字节
    //     rb_rate_t rb_rate;  // other members
    // } rt_info_t;
    //
    // typedef struct {
    //     ar_bb_fs_status_t  fs_info;
    //     ar_bb_fs_status_t  fs_info2;
    //     ar_bb_user_status_t user[4];
    // } ar_bb_status_t;
    //
    // typedef struct {
    //     uint8_t   band;
    //     uint8_t   chan_num;
    //     uint8_t   main_chan;
    //     uint8_t   opt_chan;
    //     int16_t   chan_power[MAX_FREQ_CHAN_NUM]; // 32
    // } ar_bb_fs_status_t;
    //
    // typedef struct {
    //     uint8_t   status;         // 0 disconnect; 1 connect;
    //     uint8_t   mcs;            // 0 ~ 5
    //     uint8_t   snr;            //signal noise ratio
    //     uint8_t   slot_power;     // slot rf tx power
    //     uint16_t  ldpc_err;       //ldpc err count
    //     uint16_t  ldpc_cnt;       //ldpc number
    //     uint32_t  target_bitrate; // unit: kbps
    //     uint32_t  bb_bitrate;     // tx bitrate in theory decided by current MCS
    //     int16_t   rssi[MAX_GAIN_NUM]; // local rssi
    //     int16_t   peer_br_rssi[MAX_GAIN_NUM];   // peer BR rssi only for AP side
    //     int16_t   peer_slot_rssi[MAX_GAIN_NUM]; // peer slot rssi
    //     uint8_t   peer_br_snr;    // peer BR snr only for AP side
    //     uint8_t   peer_slot_snr;  // peer slot snr
    //     uint8_t   peer_mcs;       // peer mcs
    //     uint8_t   data_rx_block;  // data blocking
    //     uint32_t  peer_br_ldpc_err;   // peer br ldpc err
    //     uint32_t  peer_slot_ldpc_err; // peer slot ldpc err
    //     uint32_t  br_freq;        // BR frequency
    //     uint32_t  slot_tx_freq;   // slot TX frequency
    //     uint32_t  slot_rx_freq;   // slot RX frequency
    // } ar_bb_user_status_t;
    protected void parseInfo() {
        MemoryHelper.MemoryReader r = new MemoryHelper.MemoryReader(owned, 2, ownedLen - 4);
        r.skip(1); // skip version
        int dst = r.readByte();
        r.skip(2);
        int cmdId = r.readBEShort();
        r.skip(2);
//        Log.v("yuhang", "parseInfo cmdId: " + cmdId);
        // TODO confirm cmd id
        if (cmdId != 10) return;
//        Log.v("yuhang", "parseInfo cmdId: " + cmdId);
//        Log.v("lee", "parseInfo: " + MemoryHelper.dumpData(owned, 0, ownedLen));

        r.skip(4); // skip result code
        r.skip((64 + 4) * 2); // skip fs_info & fs_info2
        r.skip(4 + 4 + 8); // skip others
        short rssi1 = r.readBEShort();
        short rssi2 = r.readBEShort();
        short rssi3 = r.readBEShort();
        short rssi4 = r.readBEShort();
        short peer_br_rssi1 = r.readBEShort();
        short peer_br_rssi2 = r.readBEShort();
        short peer_br_rssi3 = r.readBEShort();
        short peer_br_rssi4 = r.readBEShort();
        short peer_slot_rssi1 = r.readBEShort();
        short peer_slot_rssi2 = r.readBEShort();
        short peer_slot_rssi3 = r.readBEShort();
        short peer_slot_rssi4 = r.readBEShort();
        short peer_br_snr = r.readByte();
        short peer_slot_snr = r.readByte();
        short peer_mcs = r.readByte();
        short data_rx_block = r.readByte();
        long peer_br_ldpc_err = r.readBELong();
        long peer_slot_ldpc_err = r.readBELong();
        long br_freq = r.readBELong();
        long slot_tx_freq = r.readBELong();
        long slot_rx_freq = r.readBELong();
        String detail = "dst: " + dst + " rssi1: " + rssi1 + " rssi2: " + rssi2 + " rssi3: " + rssi3 + " rssi4: " + rssi4 +
                " peer_br_rssi1: " + peer_br_rssi1 + " peer_br_rssi2: " + peer_br_rssi2 + " peer_br_rssi3: " + peer_br_rssi3 + " peer_br_rssi4: " + peer_br_rssi4 +
                " peer_slot_rssi1: " + peer_slot_rssi1 + " peer_slot_rssi2: " + peer_slot_rssi2 + " peer_slot_rssi3: " + peer_slot_rssi3 + " peer_slot_rssi4: " + peer_slot_rssi4 +
                " peer_br_snr: " + peer_br_snr + " peer_slot_snr: " + peer_slot_snr + " peer_mcs: " + peer_mcs + " data_rx_block: " + data_rx_block +
                " peer_br_ldpc_err: " + peer_br_ldpc_err + " peer_slot_ldpc_err: " + peer_slot_ldpc_err + " br_freq: " + br_freq + " slot_tx_freq: " + slot_tx_freq + " slot_rx_freq: " + slot_rx_freq;
//        Log.v("EAV Controller: ", detail);
        int minRssi = Math.min(rssi1, rssi2);
        if (dst == 0) {
            listener.onControllerState("s1_local", detail);
            listener.onControllerState("rssi", convertRssiToPercentage(-minRssi));
        }else {
            listener.onControllerState("s1_peer", detail);
        }
    }
    private static final int RSSI_MIN = -110; // Minimum RSSI value in dBm (weakest signal)
    private static final int RSSI_MAX = -40;  // Maximum RSSI value in dBm (strongest signal)

    public static int convertRssiToPercentage(int rssiDbm) {

        // Calculate percentage using linear mapping
        int percentage = (rssiDbm - RSSI_MIN) * 100 / (RSSI_MAX - RSSI_MIN);

        return percentage;
    }
}
