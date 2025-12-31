package com.jiagu.device.controller;

import android.util.Log;

import androidx.annotation.NonNull;

import com.jiagu.api.helper.MemoryHelper;
import com.jiagu.device.channel.IChannel;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class SiYiProtocol extends SiYiBaseProtocol implements IController {

    private final IChannel.IWriter writer;
    private final Listener listener;

    public SiYiProtocol(IChannel.IWriter w, Listener l) {
        super();
        writer = w;
        listener = l;
    }

    private String product = "";
    public void setProduct(String prod) {
        product = prod;
    }

    @Override
    public byte[] onData(byte[] data) {
        byte[] rest = super.onData(data);
        if (rest != null) {
            listener.onRadioData(0, rest);
        }
        return null;
    }

    private final MemoryHelper.MemoryReader reader = new MemoryHelper.MemoryReader();
    private byte joytype = 0;
    @Override
    protected void parseSiYi() {
        if (ownedLen <= 0) return;
        switch (owned[7]) {
            case 0x16:
                switch (owned[8]) {
                    case 0:
                    case 1:
                    case 2:
                        listener.onControllerState("pairing", "pairing");
                        break;
                    case 3:
                        listener.onControllerState("pairing", "ok");
                        break;
                }
                joytype = owned[10];
                switch (joytype) {
                    case 0:
                        listener.onControllerState("type", "jp");
                        break;
                    case 1:
                        listener.onControllerState("type", "us");
                        break;
                    case 2:
                        listener.onControllerState("type", "cn");
                        break;
                    default:
                        listener.onControllerState("type", "unknown");
                }
                break;
            case 0x17:
                listener.onControllerState("state", (owned[8] == 1)? "ok" : "error");
                break;
            case 0x40:
                String id = new String(owned, 8, 10);
                listener.onControllerState("id", id);
                break;
//            case 0x1F:
//                parser.receiveData(owned, 8, ownedLen - 10);
//                if (parser.accuracy <= 50) {
//                    int type = (parser.accuracy <= 1.5) ? 3 : ((parser.accuracy <= 8) ? 2 : 1);
//                    LocationInfo info = new LocationInfo(null, null, parser.accuracy, null);
//                    listener.onControllerState("location", new RtkLatLng(parser.lat, parser.lng, parser.alt, type, info));
//                } else {
//                    listener.onControllerState("location", new RtkLatLng(0, 0, 0, 0, EMPTY_LOCINFO));
//                }
//                break;
            case 0x50:
//                LogFileHelper.log("GPS:" + MemoryHelper.dumpData(owned, 0, ownedLen));
                byte[] data = new byte[ownedLen - 10];
                System.arraycopy(owned, 8, data, 0, data.length);
//                Log.v("shero", "" + new String(data, 0, data.length));
                listener.onControllerState("jm_gps", data);
                break;
            case 0x51:
//                LogFileHelper.log("STA: " + MemoryHelper.dumpData(owned, 0, ownedLen));
                byte[] dataBase = new byte[ownedLen - 10];
                System.arraycopy(owned, 8, dataBase, 0, dataBase.length);
                listener.onControllerState("jm_station", dataBase);
                break;
            case 0x44: // 链路
                reader.init(owned, 8, ownedLen - 10);
                if (product.equals("UNIRC7")) {
                    reader.skip(5);
                    int s1 = reader.readLEUShort();
                    listener.onControllerState("rssi", s1);
                } else {
                    int signal = MemoryHelper.LittleEndian.toInt(owned, 8);
                    listener.onControllerState("rssi", signal);
                }
                break;
            case 0x43:
                reader.init(owned, 8, ownedLen - 10);
                reader.skip(11);
//                String res2 = String.format(Locale.US, "MK15数传 down:%6d", reader.readLEInt());
//                Log.v("shero", res2);
//                LogFileHelper.log(res2);
//                LogFileHelper.log("MK15 数传(" + ownedLen + ")");
//                Log.v("shero", "MK15 数传(" + ownedLen + ")");
                break;
            case 0x48:
                if (product.equals("UNIRC7")) parseMapping(mk15_2);
                else parseMapping(mk15);
                break;
            case 0x47: // version
                parseVersion();
                break;
            case 0x42:
                parseChannels();
                break;
        }
    }

    private void parseChannels() {
        if (channelMapping == null) return;
        MemoryHelper.MemoryReader r = new MemoryHelper.MemoryReader(owned, 8, ownedLen - 10);
        int count = Math.min(16, channelMapping.length + 4);
        int[] chs = new int[count];
        for (int i = 0; i < count; i++) {
            chs[i] = r.readLEShort();
        }
        button.processButton(channelMapping, chs);
    }

    private String makeVersion(int v) {
        return String.format(Locale.US, "%d.%d.%d", (v >> 16) & 0xFF, (v >> 8) & 0xFF, v & 0xFF);
    }

    private void parseVersion() {
        int rc = MemoryHelper.LittleEndian.toInt(owned, 8);
        int rf = MemoryHelper.LittleEndian.toInt(owned, 12);
        int gi = MemoryHelper.LittleEndian.toInt(owned, 16);
        int si = MemoryHelper.LittleEndian.toInt(owned, 20);
        listener.onControllerState("version", new RcVersion(
                makeVersion(rc),
                makeVersion(rf),
                makeVersion(gi),
                makeVersion(si))
        );
    }

    private static final Map<Integer, String> mk15 = new HashMap<>();
    private static final Map<Integer, String> mk15_2 = new HashMap<>();
    static {
        mk15.put(4, TYPE_ROLLER_PLUS + "LD");
        mk15.put(5, TYPE_ROLLER_PLUS + "RD");
        mk15.put(0x500, TYPE_LEVERS + "SA");
        mk15.put(0x501, TYPE_LEVERS + "SB");
        mk15.put(0x502, TYPE_LEVERS + "SC");
        mk15.put(0x100, TYPE_KEYSTR + "A");
        mk15.put(0x101, TYPE_KEYSTR + "B");
        mk15.put(0x102, TYPE_KEYSTR + "C");
        mk15.put(0x103, TYPE_KEYSTR + "D");

        //J1 J2 J3 J4 J5 J6 LD RD SA SB S1 S2 S3 S4 L1 L2 R1 R2 R3 RSSI M1 M2 M3 M4 M5 M6
        mk15_2.put(0x0008, TYPE_ROCKER + "J5");//小摇杆
        mk15_2.put(0x0009, TYPE_ROCKER + "J6");//小摇杆
//        mk15_2.put(0x0100, TYPE_KEYSTR + "M");//飞行通道 M1到M6
        mk15_2.put(0x0109, TYPE_KEYSTR + "M1");//下方6个按键
        mk15_2.put(0x010A, TYPE_KEYSTR + "M2");
        mk15_2.put(0x010B, TYPE_KEYSTR + "M3");
        mk15_2.put(0x010C, TYPE_KEYSTR + "M4");
        mk15_2.put(0x010D, TYPE_KEYSTR + "M5");
        mk15_2.put(0x010E, TYPE_KEYSTR + "M6");
        mk15_2.put(0x0004, TYPE_ROLLER_SIZE + "LD");//左侧滑轮
        mk15_2.put(0x0005, TYPE_ROLLER_SIZE + "RD");//右侧滑轮
        mk15_2.put(0x0100, TYPE_KEYSTR + "S1");//上方左侧按键
        mk15_2.put(0x0101, TYPE_KEYSTR + "S2");//上方右侧按键
        mk15_2.put(0x0102, TYPE_KEYSTR + "S3");//下方左侧按键
        mk15_2.put(0x0103, TYPE_KEYSTR + "S4");//下方右侧按键
        mk15_2.put(0x0500, TYPE_KEYSTR + "SA");//左侧三通道按键 GPS模式
        mk15_2.put(0x0501, TYPE_KEYSTR + "SB");//右侧三通道按键 AB点
        mk15_2.put(0x0201, TYPE_KEYSTR + "RSSI");
        mk15_2.put(0x0104, TYPE_KEYSTR + "L1");//下方左侧按键
        mk15_2.put(0x0105, TYPE_KEYSTR + "L2");//下方左侧按键
        mk15_2.put(0x0106, TYPE_KEYSTR + "R1");//下方右侧按键
        mk15_2.put(0x0107, TYPE_KEYSTR + "R2");//下方右侧按键
        mk15_2.put(0x0108, TYPE_KEYSTR + "R3");//下方右侧按键

    }
    private String markToString(int key, Map<Integer, String> model) {
        return model.containsKey(key)? model.get(key) : TYPE_NA + "----";
    }

    private String[] channelMapping = null;
    private void parseMapping(Map<Integer, String> model) {
        ArrayList<String> chs = new ArrayList<>();
        int idx = 16;
        while (idx <= ownedLen - 4) {
            int key = MemoryHelper.BigEndian.toShort(owned, idx);
            String mark = markToString(key, model);
//            if (mark.equals("")) {
//                break;
//            }
            chs.add(mark);
            idx += 2;
        }
        String[] out = new String[chs.size()];
        chs.toArray(out);
        channelMapping = out;
        Log.d("yuhang", "parseMapping: " + channelMapping);
        listener.onControllerState("key", channelMapping);
    }

    @Override
    public void readParameters() {
        byte[] out = wrapCommand(0x16, null);
        writer.write(out);
    }

    @Override
    public void setParameters(String cmd, @NotNull String value) {
        switch (cmd) {
            case "type":
                setType(value);
                break;
            case "receiver":
                setReceiverParam();
                break;
            case "rssi":
                requireRssi();
                requireLink();
                break;
            case "mapping":
                requireMapping();
                break;
            case "version":
                requireVersion();
                break;
            case "pair":
                pairing();
                break;
            case "channel":
                requireChannel();
                break;
        }
    }

    public byte[] jmStationSetPosition(byte[] data) {
        return wrapCommand(0x51, (byte) 0, data);
    }

    private void setType(String type) {
        byte t = 0;
        switch (type) {
            case "us": t = 1; break;
            case "cn": t = 2; break;
        }
        joytype = t;
        byte[] cmd = product.equals("UNIRC7")? new byte[] {0, 5, t, 0, 5} : new byte[] {0, 5, t, 0};
        byte[] out = wrapCommand(0x17, cmd);
        writer.write(out);
    }

    private void pairing() {
        byte[] cmd = product.equals("UNIRC7")? new byte[] {1, 5, joytype, 0, 5} : new byte[] {1, 5, joytype, 0};
        byte[] out = wrapCommand(0x17, cmd);
        writer.write(out);
    }

    private void setReceiverParam() {
        byte[] cmd = product.equals("UNIRC7")? new byte[] {0, 5, joytype, 0, 5} :new byte[] {0, 5, joytype, 0};
        byte[] out = wrapCommand(0x17, cmd);
        writer.write(out);
    }

    private void requireRssi() {
        byte[] out = wrapCommand(0x44, null);
        writer.write(out);
    }

    private void requireLink() {
        byte[] out = wrapCommand(0x43, null);
        writer.write(out);
    }

    private void requireMapping() {
        byte[] out = wrapCommand(0x48, null);
        writer.write(out);
    }

    private void requireVersion() {
        byte[] out = wrapCommand(0x47, null);
        writer.write(out);
    }

    private void requireChannel() {
        byte[] data = new byte[] {2};
        for (int i = 0; i < 3; i++) {
            byte[] out = wrapCommand(0x42, data);
            writer.write(out);
        }
    }

    @Override
    public void sendRadio(int index, @NotNull byte[] data) {
        writer.write(data);
    }

    @Override
    public void sendRadioRtcm(@NotNull byte[] rtcm) {
        byte[] out = wrapCommand(0x3A, rtcm);
        writer.write(out);
    }

    @Override
    public void readId() {
        byte[] out = wrapCommand(0x40, null);
        writer.write(out);
    }

    private ButtonHelper button = new ButtonHelper();
    @Override
    public void pushButtonHandler(@NonNull ButtonHandler handler) {
        button.pushHandler(handler);
    }

    @Override
    public void popButtonHandler() {
        button.popHandler();
    }
}
