package mdnet.base.settings;

import com.google.gson.annotations.SerializedName;

public final class WebSettings {
	@SerializedName("client_websocket_port")
	private final int clientWebsocketPort;

	public WebSettings() {
		this.clientWebsocketPort = 33333;
	}

	public WebSettings(int clientWebsocketPort) {
		this.clientWebsocketPort = clientWebsocketPort;
	}

	public int getClientWebsocketPort() {
		return clientWebsocketPort;
	}

	@Override
	public String toString() {
		return "WebSettings{" + "clientWebsocketPort=" + clientWebsocketPort + '}';
	}
}
