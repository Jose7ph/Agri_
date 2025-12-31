package com.jiagu.device.controller;

import com.jiagu.device.channel.IChannel;

import org.jetbrains.annotations.NotNull;

abstract class SimpleFilter implements IChannel.IDataFilter {

	byte[] owned;
	int ownedLen = 0;
	int state = 0;

	protected byte[] rest = new byte[4096];
	protected int restLen = 0;

	SimpleFilter(int myLen) {
		owned = new byte[myLen];
	}

	@Override
	public byte[] onData(@NotNull byte[] data) {
		feedData(data);
		if (restLen > 0) {
			byte[] bytes = new byte[restLen];
			System.arraycopy(rest, 0, bytes, 0, restLen);
			restLen = 0;
			return bytes;
		}
		return null;
	}

	abstract void feedData(byte[] bytes);

	void feedByte(byte b) {
		state++;
		owned[ownedLen++] = b;
	}

	private byte[] enlargeBuffer(byte[] buf, int len) {
		byte[] tmp = new byte[buf.length * 2];
		System.arraycopy(buf, 0, tmp, 0, len);
		return tmp;
	}

	private void clearMyData() {
		if (restLen + ownedLen + 1 > rest.length) {
			rest = enlargeBuffer(rest, restLen);
		}
		if (ownedLen > 0) {
			System.arraycopy(owned, 0, rest, restLen, ownedLen);
			restLen += ownedLen;
		}
	}

	void clearMyData(byte b) {
		clearMyData();
		ownedLen = 0;
		rest[restLen++] = b;
		state = 0;
	}

	void resetMyData(byte b, int s) {
		clearMyData();
		ownedLen = 1;
		owned[0] = b;
		state = s;
	}

	void throwMyData() {
		clearMyData();
		ownedLen = 0;
		state = 0;
	}
}
