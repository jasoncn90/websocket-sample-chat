package com.ws.server.socket;

import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
	private volatile static SocketServer instance;
	private List<WebSocket> conns = new ArrayList<WebSocket>();

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
		conns.add(conn);
		System.out.println("connection -> " + conn.hashCode() + " connected");
		System.out.println("opened connection number -> " + conns.size());
	}

	@Override
	public void onClose(WebSocket conn, int code, String reason, boolean remote) {
		conns.remove(conn);
		System.out.println("connection -> " + conn.hashCode() + " closed");
	}

	@Override
	public void onError(WebSocket conn, Exception ex) {
		System.out.println("error -> " + ex);
		ex.printStackTrace();
	}

	@Override
	public void onMessage(WebSocket conn, String message) {
		System.out.println("message from " + conn.hashCode() + " -> " + message);
		for (WebSocket socket : conns) {
			if (socket == conn) {
				continue;
			}
			socket.send(message);
		}
	}

	@Override
	public void onMessage(WebSocket conn, ByteBuffer blob) {
		System.out.println("byteBuffer -> " + blob);
		conn.send(blob);
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
