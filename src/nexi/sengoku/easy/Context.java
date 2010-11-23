package nexi.sengoku.easy;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

public class Context {

	private static final Logger logger = Logger.getLogger(Context.class);

	public final Properties properties;
	public final WebClient webClient;
	public final Auth auth;
	public final long worldId;

	public Context(long worldId, Auth auth, WebClient webClient, Properties properties) {
		this.worldId = worldId;
		this.auth = auth;
		this.webClient = webClient;
		this.properties = properties;
	}

	public HtmlPage getPage(String url) throws FailingHttpStatusCodeException, MalformedURLException, IOException {
		if (properties.containsKey("debugFile")) {
			logger.debug("found debug file " + properties.getProperty("debugFile"));
			return webClient.getPage(properties.getProperty("debugFile"));
		} else {
			logger.debug("getting page " + properties.getProperty("debugFile"));
			return webClient.getPage(url);
		}
	}

	public String getBaseUrl() {
		return String.format("http://w%03d.sengokuixa.jp", worldId);
	}

	public String getLoginUrl() throws IOException {
		return auth.login();
	}

	public String newLoginUrl() {
		for (;;) {
			try {
				return auth.loginAndSaveSession();
			} catch (Exception e) {
				logger.error("Failed to start new session", e);
			}
		}
	}

}
