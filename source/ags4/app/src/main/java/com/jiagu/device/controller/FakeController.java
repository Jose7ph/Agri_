package com.jiagu.device.controller;

import androidx.annotation.NonNull;

import com.jiagu.device.channel.IChannel;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FakeController implements IController, IChannel.IDataFilter {

    private IChannel.IWriter writer;
    private Listener listener;
    public FakeController(IChannel.IWriter w, Listener l) {
        writer = w;
        listener = l;
    }

    @Nullable
    @Override
    public byte[] onData(@NotNull byte[] data) {
        listener.onRadioData(0, data);
        return null;
    }

    @Override
    public void sendRadio(int index, byte[] data) {
        writer.write(data);
    }

    @Override
    public void readParameters() {
        listener.onControllerState("key", Helper.KEY_MAPPING);
    }

    @Override
    public void setParameters(@NotNull String cmd, @NotNull String value) {}

    @Override
    public void sendRadioRtcm(@NotNull byte[] rtcm) {}

    @Override
    public void readId() {}

    @Override
    public void pushButtonHandler(@NonNull ButtonHandler handler) {}

    @Override
    public void popButtonHandler() {}
}
