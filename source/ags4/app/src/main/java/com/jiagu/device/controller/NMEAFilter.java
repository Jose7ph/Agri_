package com.jiagu.device.controller;

import android.util.Log;

import com.jiagu.device.model.LocationInfo;
import com.jiagu.device.model.RtkLatLng;

public class NMEAFilter extends SimpleFilter {

    private static final int MYBUF_LEN = 512;
    private static final LocationInfo EMPTY_LOCINFO = new LocationInfo(0, null, null, null);

    public interface NMEAListener {
        void onGGA(RtkLatLng location);
        void onPdtInfo(String id);
    }

    private NMEAListener listener;
    public NMEAFilter(NMEAListener l) {
        super(MYBUF_LEN);
        listener = l;
    }

    @Override
    public void feedData(byte[] bytes) {
        for (byte b : bytes) {
            switch (state) {
                case 0: if (b == '$') feedByte(b); else clearMyData(b); break;
                case 1:
                    if (b == 'G' || b == 'P') feedByte(b);
                    else if (b == '$') resetMyData(b, 1);
                    else clearMyData(b);
                    break;
                case 2:
                    if (b < 10) clearMyData(b);
                    else if (b == '\r') {
                        if (!checkBcc(owned, ownedLen)) clearMyData(b);
                        else {
                            parseNMEA(owned, ownedLen);
                            state = 8;
                            ownedLen = 0;
                        }
                    } else if (ownedLen < MYBUF_LEN) {
                        owned[ownedLen++] = b;
                    } else {
                        clearMyData(b);
                        state = 0;
                        ownedLen = 0;
                    }
                    break;
                case 8: if (b == '\n') state = 0; else clearMyData(b); break;
            }
        }
    }

    private void parseNMEA(byte[] data, int len) {
        String nmea = new String(data, 0, len);
//        Log.d("yuhang", nmea);
        String[] strs = nmea.split(",");
        switch (strs[0]) {
            case "$GPGGA":
            case "$GNGGA":
                processGGA(strs);
                break;
            case "$PDTINFO":
                processPdtInfo(strs);
        }
    }

    private double transformLatLng(double val) {
        int a = (int) (val / 100);
        val -= a * 100;
        return a + val / 60;
    }

    private void processGGA(String[] strs) {
        try {
            double lat = transformLatLng(Double.parseDouble(strs[2]));
            double lng = transformLatLng(Double.parseDouble(strs[4]));
            double alt = Double.parseDouble(strs[9]);
            if (strs[3].equals("S")) lat = -lat;
            if (strs[5].equals("W")) lng = -lng;
            int gps_num = Integer.parseInt(strs[7]);
            float hdop = Float.parseFloat(strs[8]);
            int type = Integer.parseInt(strs[6]);
            int locType = 0;
            switch (type) {
                case 4:
                case 5: locType = 3; break;
                default:
                    if (gps_num > 10 || hdop <= 1.8) locType = 2;
                    else if (gps_num >= 4) locType = 1;
                    break;
            }
            listener.onGGA(new RtkLatLng(lat, lng, alt, locType, new LocationInfo(type, gps_num, null, hdop)));
        } catch (NumberFormatException e) {
            Log.e("yuhang", "parse GGA error: " + e.getMessage());
            listener.onGGA(new RtkLatLng(0, 0, 0, 0, EMPTY_LOCINFO));
        }
    }

    private void processPdtInfo(String[] strs) {
        if (strs.length >= 4) {
            listener.onPdtInfo(strs[3].substring(0, strs[3].indexOf('*')));
        }
    }

    private boolean checkBcc(byte[] data, int len) {
        int i = 1;
        for (; i < len; i++) {
            if (data[i] == '*') break;
        }
        int bcc = Checksum.calcBcc(data, 1, i - 1);
        if (i < len - 1) {
            try {
                String str = new String(data, i + 1, len - i - 1);
                int sum = Integer.valueOf(str, 16);
                return bcc == sum;
            } catch (NumberFormatException e) {
                Log.e("yuhang", "parse GGA error: " + e.getMessage());
                return false;
            }
        }
        return false;
    }
}
