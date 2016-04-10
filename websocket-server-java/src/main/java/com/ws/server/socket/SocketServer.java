package com.ws.server.socket;

import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.java_websocket.WebSocket;
import org.java_websocket.drafts.Draft;
import org.java_websocket.drafts.Draft_17;
import org.java_websocket.framing.FrameBuilder;
import org.java_websocket.framing.Framedata;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.springframework.beans.factory.InitializingBean;

public class SocketServer extends WebSocketServer implements InitializingBean {

	public static final int PORT = 9003;
	public static final String TICKET = "test";
	private volatile static SocketServer instance;

	// remember client names
	private Map<WebSocket, String> names = new HashMap<WebSocket, String>();

	private SocketServer() throws UnknownHostException {
	}

	private SocketServer(int port, Draft d) throws UnknownHostException {
		super(new InetSocketAddress(port), Collections.singletonList(d));
	}

	private SocketServer(InetSocketAddress address, Draft d) {
		super(address, Collections.singletonList(d));
	}

	@Override
	public void onOpen(WebSocket conn, ClientHandshake handshake) {
		// check the ticket and name
		// cannot get url params through another way using this library...
		String[] params = handshake.getResourceDescriptor()
				.substring(handshake.getResourceDescriptor().indexOf('?') + 1).split("&");
		String ticket = null;
		String name = null;
		for (String param : params) {
			if (param.startsWith("ticket")) {
				ticket = param.substring(param.indexOf('=') + 1);
			}
			if (param.startsWith("name")) {
				name = param.substring(param.indexOf('=') + 1);
			}
		}
		if (ticket == null || !ticket.equals(TICKET)) {
			conn.close(3000, "auth failed");
			return;
		}
		if (name == null || name.equals("")) {
			conn.close(3002, "please input your name!");
			return;
		}
		if (names.values().contains(name)) {
			conn.close(3001, "name " + name + " already exist!");
			return;
		}
		// tell clients who join the chat
		for (WebSocket socket : connections()) {
			socket.send(name + " joined the chat.");
		}
		names.put(conn, name);
	}

	@Override
	public void onClose(WebSocket conn, int code, String reason, boolean remote) {
		// tell clients who left the chat
		for (WebSocket socket : connections()) {
			socket.send(names.get(conn) + " left the chat.");
		}
		names.remove(conn);
	}

	@Override
	public void onError(WebSocket conn, Exception ex) {
		System.out.println("error -> " + ex);
		ex.printStackTrace();
	}

	@Override
	public void onMessage(WebSocket conn, String message) {
		System.out.println("message from " + names.get(conn) + " -> " + message);
		for (WebSocket socket : connections()) {
			if (socket == conn) {
				continue;
			}
			socket.send(names.get(conn) + ": " + message);
		}
	}

	@Override
	public void onMessage(WebSocket conn, ByteBuffer blob) {
		// System.out.println("byteBuffer -> " + blob);
		// conn.send(blob);
	}

	@Override
	public void onWebsocketMessageFragment(WebSocket conn, Framedata frame) {
		FrameBuilder builder = (FrameBuilder) frame;
		builder.setTransferemasked(false);
		conn.sendFrame(frame);
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (instance == null) {
			instance = new SocketServer(PORT, new Draft_17());
			instance.start();
			System.out.println("started websocket at -> " + PORT);
		}
	}

}
