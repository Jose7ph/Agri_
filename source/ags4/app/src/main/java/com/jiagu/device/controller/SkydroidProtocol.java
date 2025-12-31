package com.jiagu.device.controller;

import androidx.annotation.NonNull;

import com.jiagu.api.helper.MemoryHelper;
import com.jiagu.device.channel.IChannel;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class SkydroidProtocol extends SimpleFilter implements IController {

    private static final int MYBUF_LEN = 256 + 32;
    private static final byte[][] HEADER = {
            "fengyingdianzi:".getBytes(),
            "SKYDROID:".getBytes()
    };
    private static final int[] STATE_IDX = {0, 100};

    public SkydroidProtocol(IChannel.IWriter w, Listener l) {
        super(MYBUF_LEN);
        writer = w;
        listener = l;
    }

    private String product = "";
    public void setProduct(String prod) {
        product = prod;
    }

    private final IChannel.IWriter writer;
    private final Listener listener;
    private final boolean hasRtk = false;

    private int expected = 0;
    private int header = 0;
    @Override
    public void feedData(byte[] bytes) {
//        Log.v("shero", "feedData:" + MemoryHelper.dumpData(bytes));
        for (byte b : bytes) {
            switch (state) {
                case 0:
                    if (b == HEADER[0][0]) {
                        feedByte(b);
                        state = 1;
                        header = 0;
                    } else if (b == HEADER[1][0]) {
                        feedByte(b);
                        state = STATE_IDX[1] + 1;
                        header = 1;
                    } else clearMyData(b);
                    break;
                case 15: feedByte(b); break; // FUN
                case 16: // LEN
                    feedByte(b);
                    expected = (b & 0xFF) + HEADER[header].length + 3;
                    break;
                case 17:
                    owned[ownedLen++] = b;
//                    Log.v("shero", "ownedLen:" + ownedLen + " expected:" + expected);
                    if (ownedLen == expected) {
                        if (!checkData()) throwMyData();
                        else {
                            int cmdIdx = HEADER[header].length;
//                            Log.v("shero", "cmd:" + (owned[cmdIdx] & 0xFF) + "(" + expected + ") " + " " + ":" + MemoryHelper.dumpData(owned, 0, ownedLen));
                            processCommand(owned[cmdIdx] & 0xFF);
                            state = 0;
                            ownedLen = 0;
                        }
                    }
                    break;
                default:
                    int idx = state - STATE_IDX[header];
                    if (b == HEADER[header][idx]) {
                        feedByte(b);
                        if (idx == HEADER[header].length - 1) state = 15;
                    } else if (b == HEADER[0][0]) {
                        resetMyData(b, 1);
                        header = 0;
                    } else if (b == HEADER[1][0]) {
                        resetMyData(b, 101);
                        header = 1;
                    } else {
                        clearMyData(b);
                    }
                    break;
            }
        }
    }

    @Override
    public byte[] onData(byte[] data) {
        byte[] rest = super.onData(data);
        if (rest != null) {
            listener.onRadioData(currentIndex, rest);
        }
        return null;
    }

    private boolean checkData() {
        byte bcc = Checksum.calcBcc(owned, ownedLen - 1);
//        Log.v("shero", "[遥控器校验]:" + owned[ownedLen - 1] + " calc:" + bcc);
        return bcc == owned[ownedLen - 1];
    }

    private void processCommand(int cmd) {
        int idx = HEADER[header].length + 1;
        int len = owned[idx] & 0xFF;
        if (len == 2 && owned[17] == 'N' && owned[18] == 'O') {
            listener.onControllerState("state", "error");
            return;
        }

//        Log.v("shero", "cmd:" + cmd + " len:" + len);
        switch (cmd) {
            case 0xA2:
                if (product.equals("H12")) {
                    parseParam(owned, len, H12, 6);
                } else {
                    parseParam(owned, len, T12, 6);
                }
                break;
            case 0xC2:
                parseParam(owned, len, T10, 6);
                break;
            case 0xE2:
                if (product.equals("H12Pro")) {
                    parseParam(owned, len, H12PRO, 5);
                } else if (product.equals("H20")) {
                    parseParam(owned, len, H20, 5);
                } else {
                    parseParam(owned, len, H16, 5);
                }
                break;
            case 0xAC:
                parseDroneId(owned, len);
                break;
            case 0xB4:
                parseId(owned, len);
                break;
            case 0xEE:
                parsePairing(owned, len);
                break;
            case 0xB1:
                parseChannel(owned, len);
                break;
        }
    }

    private long t0 = 0;
    private void parseChannel(byte[] owned, int len) {
        int off = HEADER[header].length + 2;
        int size = len / 2;
        int[] values = new int[size];
        for (int i = 0; i < size; i++, off += 2) {
            values[i] = MemoryHelper.BigEndian.toShort(owned, off);
            if (product.equals("H16")) {
                values[i] = values[i] * 5 /8 + 874;
            }
        }
        long t = System.currentTimeMillis();
        if (t > t0 + 100) { // there are too many H16 channel data
            t0 = t;
            listener.onControllerState("channel", values);
//            Log.v("shero", "map:" + Arrays.toString(channelMapping) + " value:" + Arrays.toString(values));
            if (channelMapping != null) {
                button.processButton(channelMapping, values);
            }
        }
    }

    private void parsePairing(byte[] data, int len) {
        byte b = data[HEADER[header].length + 1];
        if (b == 8) {
            listener.onControllerState("pairing", "pairing");
            pairing2();
        } else if (b == 2) {
            String rsp = new String(data, HEADER[header].length + 2, 2);
            if (rsp.equals("OK")) {
                listener.onControllerState("pairing", "ok");
            }
        }
    }

    private void parseId(byte[] data, int len) {
        String id = MemoryHelper.byteToString(data, HEADER[header].length + 2, len);
        listener.onControllerState("id", id);
    }

    private void parseParam(byte[] data, int len, String[] model, int packLen) {
//        LogFileHelper.log("parse param len = " + len);
        if (len < 48) {
            return;
        }
        int off = HEADER[header].length + 2;
        int idx = 0;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            int key = data[off + idx] & 0xFF;
            String mark = markToString(key, model);
            if (!mark.isEmpty()){
                sb.append(mark.substring(1));
            }
            idx += packLen;
        }
        String type = sb.toString();
        switch (type) {
            case "X2Y2Y1X1":
                listener.onControllerState("type", "us");
                break;
            case "X2Y1Y2X1":
                listener.onControllerState("type", "jp");
                break;
            case "X1Y1Y2X2":
                listener.onControllerState("type", "cn");
                break;
            default:
                listener.onControllerState("type", "unknown");
        }

        ArrayList<String> chs = new ArrayList<>();
        while (idx <= len - packLen) {
            int key = data[off + idx] & 0xFF;
            String mark = markToString(key, model);
            chs.add(mark);
            idx += packLen;
        }
        String[] out = new String[chs.size()];
        chs.toArray(out);
        channelMapping = out;
//        LogFileHelper.log("H20:" + Arrays.toString(out));
        listener.onControllerState("key", out);
    }

    private String[] channelMapping = null;

    private String markToString(int mark, String[] model) {
        if (mark > model.length || mark <= 0) return "";
        return model[mark - 1];
    }

    private final static String[] T10 = {
            TYPE_NONE + "A",
            TYPE_NONE + "B",
            TYPE_NONE + "C",
            TYPE_NONE + "D",
            TYPE_NONE + "E",
            TYPE_NONE + "F",
            TYPE_ROCKER + "X1",
            TYPE_ROCKER + "Y1",
            TYPE_ROCKER + "X2",
            TYPE_ROCKER + "Y2",
    };
    private final static String[] T12 = {
            TYPE_KEYSTR + "A",//1
            TYPE_KEYSTR + "B",//2
            TYPE_KEYSTR + "C",//3
            TYPE_KEYSTR + "D",//4
            TYPE_LEVERS + "E",//5
            TYPE_LEVERS + "F",//6
            TYPE_LEVERS + "G",//7
            TYPE_LEVERS + "H",//8
            TYPE_ROCKER + "X1",//9
            TYPE_ROCKER + "Y1",//A
            TYPE_ROCKER + "X2",//B
            TYPE_ROCKER + "Y2",//C
            TYPE_ROCKER + "X3",//D
            TYPE_ROCKER + "Y3",//E
    };
    private final static String[] H12 = {//X2 Y2 Y1 X1 E F A G C D B H
            TYPE_KEYSTR + "A",//1
            TYPE_KEYSTR + "B",//2
            TYPE_KEYSTR + "C",//3
            TYPE_KEYSTR + "D",//4
            TYPE_LEVERS + "E",//5
            TYPE_LEVERS + "F",//6
            TYPE_ROLLER_SIZE + "G",//7
            TYPE_ROLLER_SIZE + "H",//8
            TYPE_ROCKER + "X1",//9
            TYPE_ROCKER + "Y1",//A
            TYPE_ROCKER + "X2",//B
            TYPE_ROCKER + "Y2",//C
    };
    private final static String[] H16 = {
            TYPE_ROCKER + "X1",
            TYPE_ROCKER + "Y1",
            TYPE_ROCKER + "X2",
            TYPE_ROCKER + "Y2",
            TYPE_ROCKER + "X3",
            TYPE_ROCKER + "Y3",
            TYPE_NONE + "Mode",
            TYPE_NONE + "H",
            TYPE_LEVERS + "SW1",
            TYPE_LEVERS + "SW2",
            TYPE_LEVERS + "SW3",
            TYPE_LEVERS + "SW4",
            TYPE_ROLLER_SIZE + "AUX1",
            TYPE_ROLLER_SIZE + "AUX2",
            TYPE_NONE + "PPM1",
            TYPE_NONE + "PPM2",
    };
    private final static String[] H12PRO = {
            TYPE_KEYSTR + "A",
            TYPE_KEYSTR + "B",
            TYPE_KEYSTR + "C",
            TYPE_KEYSTR + "D",
            TYPE_LEVERS + "E",
            TYPE_LEVERS + "F",
            TYPE_ROLLER_SIZE + "G",
            TYPE_ROLLER_SIZE + "H",
            TYPE_ROCKER + "X1",
            TYPE_ROCKER + "Y1",
            TYPE_ROCKER + "X2",
            TYPE_ROCKER + "Y2",
            TYPE_NONE + "AUX1",
            TYPE_NONE + "AUX2",
            TYPE_NONE + "PPM1",
            TYPE_NONE + "PPM2",
    };
//    66 65 6E 67 79 69 6E 67 64 69 61 6E 7A 69 3A
//    E2 50 E2=cmdId 0x50=80 Len
//    03 69 96 C3 01 通道1
//    04 69 96 C3 01 通道2
//    02 69 96 C3 01 通道3
//    01 69 96 C3 01 通道4
//    05 69 96 C3 01 通道5
//    0C 69 96 C3 01 通道6
//    0D 69 96 C3 01 通道7
//    0E 69 96 C3 01 通道8
//    07 69 96 C3 01 通道9
//    08 69 96 C3 01 通道10
//    09 69 96 C3 01 通道11
//    0A 69 96 C3 01 通道12
//    0B 69 96 C3 01 通道13
//    0F 69 96 C3 01 通道14
//    10 69 96 C3 01 通道15
//    06 69 96 C3 01 通道16
//    9A 校验
    private final static String[] H20 = {
            TYPE_ROCKER + "X1",     //1
            TYPE_ROCKER + "Y1",     //2
            TYPE_ROCKER + "X2",     //3
            TYPE_ROCKER + "Y2",     //4
            TYPE_LEVERS + "SW1",    //5
            TYPE_ROLLER_SIZE + "AUX1",//6
            TYPE_KEYSTR + "L1",     //7
            TYPE_KEYSTR + "L2",     //8
            TYPE_KEYSTR + "R1",     //9
            TYPE_KEYSTR + "R2",     //A
            TYPE_KEYSTR + "STOP",   //B
            TYPE_KEYSTR + "H",      //C
            TYPE_KEYSTR + "LIGHT",  //D
            TYPE_KEYSTR + "PUMP",   //E
            TYPE_KEYSTR + "B1",     //F
            TYPE_KEYSTR + "B2",     //10
    };

    private int currentIndex = 0;
    private void parseDroneId(byte[] data, int len) {
        if (len != 4) return;
        int off = 17;
        for (int i = 0; i < 4; i++, off++) {
            if (data[off] == 3 || data[off] == 1) {
                listener.onControllerState("drone", i);
                currentIndex = i;
                return;
            }
        }
    }

    private byte[] makeCommand(byte[] prefix, byte[] pack) {
        int size = pack.length + prefix.length + 1;
        byte[] out = new byte[size];
        System.arraycopy(prefix, 0, out, 0, prefix.length);
        System.arraycopy(pack, 0, out, prefix.length, pack.length);
        out[size - 1] = Checksum.calcBcc(out, size - 1);
        return out;
    }

    @Override
    public void readId() {
        byte[] cmd = {(byte) 0xB4, 2, 'R', 1};
        byte[] out = makeCommand(HEADER[0], cmd);
        writer.write(out);
    }

    @Override
    public void readParameters() {
        byte[] out = makeCommand(HEADER[0], new byte[]{(byte) 0xA2, 0x02, 'R', 0x01});
        writer.write(out);
    }

    @Override
    public void setParameters(String type, @NotNull String value) {
        switch (type) {
            case "type":
                setUSJapan(value);
                break;
            case "receiver":
                setReceiverParam();
                break;
            case "pair":
                pairing();
                break;
            case "channel":
                readChannels();
                break;
        }
    }

    private void readChannels() {
        byte[] pack = {(byte) 0xB1, 0x02, 'R', 1};
        byte[] out = makeCommand(HEADER[0], pack);
        writer.write(out);
    }

    private void pairing() {
        byte[] pack = {(byte) 0xEE, 0x05, 0x52, 0x45, 0x53, 0x45, 0x54};
        byte[] out = makeCommand(HEADER[0], pack);
        writer.write(out);
    }

    private void pairing2() {
        byte[] pack = {(byte) 0xEE, 0x05, 0x41, 0x42, 0x43, 0x44, 0x45};
        byte[] out = makeCommand(HEADER[0], pack);
        writer.write(out);
    }

    private void setUSJapan(String type) {
        byte[] pack = {(byte) 0xA9, 0x02, 0, 0};
        switch (type) {
            case "us":
                pack[2] = 1;
                break;
            case "jp":
                pack[3] = 1;
                break;
            default:
                return;
        }
        byte[] out = makeCommand(HEADER[0], pack);
        writer.write(out);
    }

    private void setReceiverParam() {
        byte[] pack = {(byte) 0xA6, 0x02, 1, 0};
        byte[] out = makeCommand(HEADER[0], pack);
        writer.write(out);
    }

    private final byte[] rtkCmd = {(byte) 0xAB, 0x06, 'G','P','S','R','T','K'};
    public void startLocating() {
        if (hasRtk) {
            if (writer != null) {
                writer.write(makeCommand(HEADER[0], rtkCmd));
            }
        }
    }

    public void stopLocating() {
        if (writer != null) {
            byte[] cmd = {(byte) 0xAB, 0x06, 'G','P','S','O','F','F'};
            writer.write(makeCommand(HEADER[0], cmd));
        }
    }

    public void changeDrone(int id) {
        byte[] exchgCmd = {(byte) 0xAC, 0x05, 'W', 0, 0, 0, 0};
        for (int i = 0; i < 4; i++) {
            exchgCmd[3 + i] = (byte) ((i == id)? 3 : 0);
        }
        if (writer != null) {
            writer.write(makeCommand(HEADER[0], exchgCmd));
        }
    }

    @Override
    public void sendRadio(int index, @NotNull byte[] data) {
        writer.write(data);
    }

    @Override
    public void sendRadioRtcm(@NotNull byte[] rtcm) {
//        writer.write(rtcm);
        byte[] out = wrapCommand(0x3A, rtcm);
        writer.write(out);
    }

    public void getRssi() {
        byte[] buf = new byte[8 + 9];
        buf[0] = (byte) 0xFE;
        buf[1] = 9;
        buf[2] = 0x10;
        buf[3] = (byte) 0xFF;
        buf[4] = (byte) 0xBE;
        buf[5] = 0;

        buf[6 + 4] = 6;
        buf[6 + 5] = 8;
        buf[6 + 8] = 3;

        buf[9 + 8 - 2] = (byte) 0xFF;
        buf[9 + 8 - 1] = (byte) 0x94;
        writer.write(buf);
    }

    private short seq = 0;
    private byte[] wrapCommand(int cmdId, byte[] cmd) {
        int len = (cmd != null) ? cmd.length : 0;
        byte[] out = new byte[len + 10];
        out[0] = 0x55;
        out[1] = 0x66;
        out[2] = 1;
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

    private ButtonHelper button = new ButtonHelper();
    @Override
    public void pushButtonHandler(@NonNull ButtonHandler handler) { button.pushHandler(handler); }

    @Override
    public void popButtonHandler() { button.popHandler(); }
}
