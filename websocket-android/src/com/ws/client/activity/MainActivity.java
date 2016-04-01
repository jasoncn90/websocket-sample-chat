package com.ws.client.activity;

import java.net.URI;
import java.nio.ByteBuffer;

import org.java_websocket.drafts.Draft_17;
import org.java_websocket.handshake.ServerHandshake;

import com.ws.client.R;
import com.ws.client.socket.OnSocketStatusChangeListener;
import com.ws.client.socket.SocketClient;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	static final String SERVER_URL = "108.207.6.4";
	static final String SERVER_PORT = "9003";

	Button send;
	EditText content;
	SocketClient client;
	ScrollView scroll;
	LinearLayout chatLayout;
	boolean connected = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		initUI();
		// create a socket client and connect
		client = new SocketClient(URI.create("ws://" + SERVER_URL + ":" + SERVER_PORT), new Draft_17());
		client.setListener(listener);
		client.connect();
	}

	private void initUI() {
		setTitle("WebSocket Test");
		send = (Button) findViewById(R.id.btn);
		content = (EditText) findViewById(R.id.content);
		scroll = (ScrollView) findViewById(R.id.scroll);
		chatLayout = (LinearLayout) findViewById(R.id.chat_layout);
		send.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (connected) {
					String message = "android : " + content.getText().toString();
					client.send(message);
					addToChat(message);
					content.setText("");
				} else {
					toast("socket not connected!");
				}
			}
		});
	}

	// init the listener
	private OnSocketStatusChangeListener listener = new OnSocketStatusChangeListener() {

		@Override
		public void onMessage(final String message) {
			runOnUiThread(new Runnable() {

				@Override
				public void run() {
					addToChat(message);
				}
			});
		}

		@Override
		public void onMessage(ByteBuffer blob) {

		}

		@Override
		public void onOpen(ServerHandshake handshake) {
			connected = true;
			toast("socket connected.");
		}

		@Override
		public void onClose(final int code, final String reason, boolean remote) {
			connected = false;
			toast("socket closed, code -> " + code + " reason -> " + reason);
		}

		@Override
		public void onError(final Exception ex) {
			connected = false;
			toast("socket error -> " + ex.getMessage());
		}
	};

	// add message to chat layout
	private void addToChat(String message) {
		TextView textView = new TextView(MainActivity.this);
		textView.setText(message);
		textView.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		textView.setTextColor(Color.WHITE);
		textView.setTextSize(20f);
		chatLayout.addView(textView);
		// scroll to bottom
		scroll.post(new Runnable() {
			@Override
			public void run() {
				scroll.fullScroll(View.FOCUS_DOWN);
			}
		});
	}

	@Override
	protected void onDestroy() {
		// close the client when exit
		client.close();
		super.onDestroy();
	}

	// show toast
	private void toast(final String message) {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
			}
		});
	}
}
