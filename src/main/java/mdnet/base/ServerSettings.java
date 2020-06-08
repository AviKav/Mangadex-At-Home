package mdnet.base;

import com.google.gson.annotations.SerializedName;

import java.util.Objects;

public final class ServerSettings {
	@SerializedName("image_server")
	private final String imageServer;
	private final TlsCert tls;
	@SerializedName("latest_build")
	private final int latestBuild;

	public ServerSettings(String imageServer, TlsCert tls, int latestBuild) {
		this.imageServer = Objects.requireNonNull(imageServer);
		this.tls = tls;
		this.latestBuild = latestBuild;
	}

	public String getImageServer() {
		return imageServer;
	}

	public TlsCert getTls() {
		return tls;
	}

	public int getLatestBuild() {
		return latestBuild;
	}

	@Override
	public String toString() {
		return "ServerSettings{" + "imageServer='" + imageServer + '\'' + ", tls=" + tls + ", latestBuild="
				+ latestBuild + '}';
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		ServerSettings that = (ServerSettings) o;

		if (!imageServer.equals(that.imageServer))
			return false;
		return Objects.equals(tls, that.tls);
	}

	@Override
	public int hashCode() {
		int result = imageServer.hashCode();
		result = 31 * result + (tls != null ? tls.hashCode() : 0);
		return result;
	}

	public static final class TlsCert {
		@SerializedName("created_at")
		private final String createdAt;
		@SerializedName("private_key")
		private final String privateKey;
		private final String certificate;

		public TlsCert(String createdAt, String privateKey, String certificate) {
			this.createdAt = Objects.requireNonNull(createdAt);
			this.privateKey = Objects.requireNonNull(privateKey);
			this.certificate = Objects.requireNonNull(certificate);
		}

		public String getCreatedAt() {
			return createdAt;
		}

		public String getPrivateKey() {
			return privateKey;
		}

		public String getCertificate() {
			return certificate;
		}

		@Override
		public String toString() {
			return "TlsCert{" + "createdAt='" + createdAt + '\'' + ", privateKey='" + "<hidden>" + '\''
					+ ", certificate='" + "<hidden>" + '\'' + '}';
		}

		@Override
		public boolean equals(Object o) {
			if (this == o)
				return true;
			if (o == null || getClass() != o.getClass())
				return false;

			TlsCert tlsCert = (TlsCert) o;

			if (!createdAt.equals(tlsCert.createdAt))
				return false;
			if (!privateKey.equals(tlsCert.privateKey))
				return false;
			return certificate.equals(tlsCert.certificate);
		}

		@Override
		public int hashCode() {
			int result = createdAt.hashCode();
			result = 31 * result + privateKey.hashCode();
			result = 31 * result + certificate.hashCode();
			return result;
		}
	}
}
