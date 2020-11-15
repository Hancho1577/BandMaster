package bandMaster;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Base64;

import javax.net.ssl.HttpsURLConnection;

import cn.nukkit.plugin.PluginBase;
import cn.nukkit.scheduler.AsyncTask;
import cn.nukkit.utils.Config;

public class BandMaster extends PluginBase {
	private final static String USER_AGENT = "Mozilla/5.0";
	private final static String POST_URL = "https://openapi.band.us/v2.2/band/post/create";
	private String token;
	private String band_key;

	@Override
	public void onEnable() {
		saveDefaultConfig();
		Config config = this.getConfig();

		this.token = config.get("token", "");
		this.band_key = config.get("band_key", "");
		if (this.token.isEmpty() || this.band_key.isEmpty()) {
			this.getServer().getLogger().warning("밴드 token 또는 band key가 비어있습니다.");
			this.getServer().getPluginManager().disablePlugin(this);
		}
	}

	public void postBand(String content, boolean alarm, boolean async) {
		if (async) {
			this.getServer().getScheduler().scheduleAsyncTask(this, new AsyncTask() {
				@Override
				public void onRun() {
					postBand(content, alarm, false);
				}
			});
			return;
		}

		String encodedContent;
		try {
			encodedContent = URLEncoder.encode(content, "UTF-8");
			this.sendPost(POST_URL, "access_token=" + token + "&band_key=" + band_key + "&content=" + encodedContent
					+ "&do_push=" + alarm);
		} catch (UnsupportedEncodingException e) {
			this.getLogger().info("", e);
		}
	}

	private void sendPost(String targetUrl, String parameters) {
		// https://gist.github.com/developer-sdk/336eddb1a9935b6184eb5822b1bda154 에서 가져옴
		// https://118k.tistory.com/225
		URL url;

		try {
			url = new URL(targetUrl);
			HttpsURLConnection con = (HttpsURLConnection) url.openConnection();

			con.setRequestProperty("Authorization",
					"Basic " + new String(Base64.getEncoder().encode(this.token.getBytes())));
			con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			con.setRequestProperty("User-Agent", USER_AGENT);
			con.setRequestMethod("POST"); // HTTP POST 메소드 설정
			con.setDoOutput(true); // POST 파라미터 전달을 위한 설정
			con.setConnectTimeout(10000);

			DataOutputStream wr = new DataOutputStream(con.getOutputStream());
			wr.writeBytes(parameters);
			wr.flush();
			wr.close();

			this.getLogger().warning("response CODE : " + con.getResponseCode());
		} catch (IOException e) {
			this.getLogger().error("", e);
		}
	}
}
