package mdnet.base.settings;

import com.google.gson.annotations.SerializedName;

public final class WebSettings {
	@SerializedName("ui_websocket_port")
	private final int uiWebsocketPort;
	@SerializedName("ui_port")
	private final int uiPort;

	public WebSettings() {
		this.uiWebsocketPort = 33333;
		this.uiPort = 8080;
	}

	public WebSettings(int uiWebsocketPort, int uiPort) {
		this.uiWebsocketPort = uiWebsocketPort;
		this.uiPort = uiPort;
	}

	public int getUiWebsocketPort() {
		return uiWebsocketPort;
	}

	public int getUiPort() {
		return uiPort;
	}

	@Override
	public String toString() {
		return "WebSettings{" + "uiWebsocketPort=" + uiWebsocketPort + ", uiPort=" + uiPort + '}';
	}
}
