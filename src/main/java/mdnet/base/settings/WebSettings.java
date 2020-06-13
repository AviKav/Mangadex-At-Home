package mdnet.base.settings;

import com.google.gson.annotations.SerializedName;

public final class WebSettings {
	@SerializedName("ui_port")
	private final int uiPort;

	public WebSettings() {
		this.uiPort = 8080;
	}

	public WebSettings(int uiPort) {
		this.uiPort = uiPort;
	}

	public int getUiPort() {
		return uiPort;
	}

	@Override
	public String toString() {
		return "WebSettings{" +
				"uiPort=" + uiPort +
				'}';
	}
}
