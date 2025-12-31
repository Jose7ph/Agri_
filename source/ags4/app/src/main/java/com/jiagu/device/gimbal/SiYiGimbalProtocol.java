package com.jiagu.device.gimbal;

import com.jiagu.device.channel.IChannel;
import com.jiagu.device.controller.SiYiBaseProtocol;

public class SiYiGimbalProtocol extends SiYiBaseProtocol {

    public SiYiGimbalProtocol(IChannel.IWriter w) {
        super();
    }

    @Override
    protected void parseSiYi() {}

    public byte[] setYawPitchRoll(float yaw, float pitch, float roll) {
        byte[] cmd = new byte[] {(byte) ((int)yaw & 0xFF), (byte)((int)pitch & 0xFF)};
        return wrapCommand(0x07, cmd);
    }

    public byte[] setGimbalCmd(int cmd, byte[] data) {
        return wrapCommand(cmd, data);
    }
}
