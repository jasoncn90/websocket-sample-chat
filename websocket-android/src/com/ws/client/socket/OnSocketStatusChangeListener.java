package com.ws.client.socket;

import java.nio.ByteBuffer;

import org.java_websocket.handshake.ServerHandshake;

public interface OnSocketStatusChangeListener {

	public void onMessage(String message);

	public void onMessage(ByteBuffer blob);

	public void onOpen(ServerHandshake handshake);

	public void onClose(int code, String reason, boolean remote);

	public void onError(Exception ex);
}
