package mdnet.webui;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class WebConsole extends WebSocketServer {

	private final static Logger LOGGER = LoggerFactory.getLogger(WebConsole.class);

	public WebConsole(int port) {
		super(new InetSocketAddress(port));
	}

	@Override
	public void onOpen(WebSocket conn, ClientHandshake handshake) {
		LOGGER.info("Webclient {} connected", conn);
	}

	@Override
	public void onClose(WebSocket conn, int code, String reason, boolean remote) {
		LOGGER.info("Webclient {} disconnected: {} ", conn, reason);
	}

	@Override
	public void onMessage(WebSocket conn, String message) {
		parseMessage(message);
	}

	@Override
	public void onMessage(WebSocket conn, ByteBuffer message) {
		// parseMessage(message.array().toString());
	}

	@Override
	public void onError(WebSocket conn, Exception ex) {
		ex.printStackTrace();
		if (conn != null) {
			// some errors like port binding failed may not be assignable to a specific
			// websocket
		}
	}

	@Override
	public void onStart() {
		LOGGER.info("Listening for connections on port: {}", this.getPort());
		setConnectionLostTimeout(0);
		setConnectionLostTimeout(100);
	}

	protected abstract void parseMessage(String message);

	// void parseCommand(String x) {
	// switch (x) {
	// case "help":
	// this.broadcast(formatMessage("command", "Available commands:"));
	// this.broadcast(formatMessage("command", "you"));
	// this.broadcast(formatMessage("command", "are"));
	// this.broadcast(formatMessage("command", "big"));
	// this.broadcast(formatMessage("command", "gay"));
	// break;
	// case "stop":
	// this.broadcast(formatMessage("command", "Mangadex Client has shut down,
	// shutting down web client now"));
	// return;
	// default:
	// this.broadcast(formatMessage("command", "That command was not recognized"));
	// this.broadcast(formatMessage("command", "Try help for a list of available
	// commands"));
	// break;
	// }
	// }

	public void sendMessage(String type, Object message) {
		// JSONObject out = new JSONObject();
		// switch (type) {
		// case "command" :
		// out.put("type", "command");
		// out.put("data", message.toString());
		// break;
		// case "stats" :
		// out.put("type", "stats");
		// AtomicReference<Statistics> temp = (AtomicReference<Statistics>) message;
		// out.put("hits", temp.get().getCacheHits());
		// out.put("misses", temp.get().getCacheMisses());
		// out.put("bytes_sent", temp.get().getBytesSent());
		// out.put("req_served", temp.get().getRequestsServed());
		// out.put("dataval", "empty");
		// out.put("dataval", "empty");
		// out.put("dataval", "empty");
		// break;
		// case "auth" :
		// break;
		// default :
		// out.put("type", "command");
		// out.put("data", message.toString());
		// break;
		// }
		// broadcast(out.toString());
	}
}
