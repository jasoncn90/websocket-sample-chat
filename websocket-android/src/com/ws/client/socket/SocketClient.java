package com.ws.client.socket;

import java.net.URI;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.logging.Logger;

import org.java_websocket.WebSocket;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.framing.FrameBuilder;
import org.java_websocket.framing.Framedata;
import org.java_websocket.handshake.ServerHandshake;

public class SocketClient extends WebSocketClient {

	OnSocketStatusChangeListener listener;

	public void setListener(OnSocketStatusChangeListener listener) {
		this.listener = listener;
	}

	public SocketClient(URI serverURI) {
		super(serverURI);
	}

	public SocketClient(URI serverUri, Draft draft) {
		super(serverUri, draft);
	}

	public SocketClient(URI arg0, Draft arg1, Map<String, String> arg2, int arg3) {
		super(arg0, arg1, arg2, arg3);
	}

	Logger logger = Logger.getLogger(SocketClient.class.getSimpleName());

	@Override
	public void onMessage(String message) {
		logger.info("on message -> " + message);
		if (listener != null) {
			listener.onMessage(message);
		}
	}

	@Override
	public void onMessage(ByteBuffer blob) {
		logger.info("on message -> " + blob);
		if (listener != null) {
			listener.onMessage(blob);
		}
	}

	@Override
	public void onError(Exception ex) {
		logger.info("on error -> " + ex);
		ex.printStackTrace();
		if (listener != null) {
			listener.onError(ex);
		}
	}

	@Override
	public void onOpen(ServerHandshake handshake) {
		logger.info("on open -> " + handshake);
		if (listener != null) {
			listener.onOpen(handshake);
		}
	}

	@Override
	public void onClose(int code, String reason, boolean remote) {
		logger.info("on close -> " + code + " " + reason);
		if (listener != null) {
			listener.onClose(code, reason, remote);
		}
	}

	@Override
	public void onWebsocketMessageFragment(WebSocket conn, Framedata frame) {
		logger.info("on websocket message fragment -> " + frame.toString());
		FrameBuilder builder = (FrameBuilder) frame;
		builder.setTransferemasked(true);
		getConnection().sendFrame(frame);
	}

}
