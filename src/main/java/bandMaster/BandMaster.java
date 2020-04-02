package bandMaster;

import java.io.DataOutputStream;
import java.io.UnsupportedEncodingException;
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
		Config config = getConfig();
		this.token = config.get("token", "");
		this.band_key = config.get("band_key", "");
		if (this.token.equals("") || this.band_key.equals(""))
			getServer().getPluginManager().disablePlugin(this);
	}

	public void postBand(String content, boolean alarm, boolean async) {
		try {
			String encodedContent = URLEncoder.encode(content, "UTF-8");
			if (async) {
				this.getServer().getScheduler().scheduleAsyncTask(this, new AsyncTask() {
					@Override
					public void onRun() {
						try {
							sendPost(POST_URL, "access_token=" + token + "&band_key=" + band_key + "&content="
									+ encodedContent + "&do_push=" + alarm);
						} catch (Exception e) {
							getLogger().error(e.getMessage());
						}
					}
				});
			} else {
				try {
					sendPost(POST_URL, "access_token=" + token + "&band_key=" + band_key + "&content=" + encodedContent
							+ "&do_push=" + alarm);
				} catch (Exception e) {
					getLogger().error(e.getMessage());
				}
			}
		} catch (UnsupportedEncodingException e1) {
			getLogger().error(e1.getMessage());
		}
	}

	private void sendPost(String targetUrl, String parameters) throws Exception {
		// https://gist.github.com/developer-sdk/336eddb1a9935b6184eb5822b1bda154 에서 가져옴
		// https://118k.tistory.com/225

		URL url = new URL(targetUrl);
		HttpsURLConnection con = (HttpsURLConnection) url.openConnection();

		con.setRequestProperty("Authorization",
				"Basic " + new String(Base64.getEncoder().encode(this.token.getBytes())));
		con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
		con.setRequestProperty("User-Agent", USER_AGENT);
		con.setRequestMethod("POST"); // HTTP POST 메소드 설정
		con.setDoOutput(false); // POST 파라미터 전달을 위한 설정

		DataOutputStream wr = new DataOutputStream(con.getOutputStream());
		wr.writeBytes(parameters);
		wr.flush();
		wr.close();

		int responseCode = con.getResponseCode();
		getLogger().warning("response CODE : " + responseCode);

	}

}
