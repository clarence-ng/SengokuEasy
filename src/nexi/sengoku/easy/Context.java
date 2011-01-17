package nexi.sengoku.easy;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

public class Context {

	private static final Logger logger = Logger.getLogger(Context.class);

	public final Properties properties;
	public WebClient webClient; //Guarded by this
	public final Auth auth;
	public final long worldId;

	public Context(long worldId, Auth auth, WebClient webClient, Properties properties) {
		this.worldId = worldId;
		this.auth = auth;
		this.webClient = webClient;
		this.properties = properties;
	}

	public synchronized HtmlPage getPage(String url) {
		HtmlPage page = null;

		try {
			url = URLEncoder.encode(url, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}

		for (;;) {
			try {
				if (properties.containsKey("debugFile")) {
					logger.debug("found debug file " + properties.getProperty("debugFile"));
					return webClient.getPage(properties.getProperty("debugFile"));
				} else {
					logger.debug("getting page " + url);
					page = webClient.getPage(url);

					if (page.getElementById("wood") != null) {
						return page;
					} else {
						logger.info("Couldn't get page. Trying logging in first.");
						if (logger.isDebugEnabled()) {
							logger.debug(page.asText());
						}
						webClient = auth.loginToWorldWithRetry(worldId).getEnclosingWindow().getWebClient();
					}
				}	
			} catch(Exception e) {
				logger.info(e);
				try {
					Thread.sleep(2000L);
				} catch (InterruptedException e1) {
					logger.warn("Ignoring interrupt");
				}
			}
		}
	}

	public synchronized String getBaseUrl() {
		return String.format("http://w%03d.sengokuixa.jp", worldId);
	}

	public static final class MasterContext {
		Map<Long, Context> contexts = new HashMap<Long, Context> ();
		private final Auth auth;
		private final Properties properties;

		public MasterContext() {
			File propertiesFile = new File(SengokuEasy.propertiesFilePath);
			properties = new Properties();
			try {
				properties.load(new FileReader(propertiesFile));
			} catch (Exception e) {
				logger.error("failed to initialize properties", e);
			} 
			auth = new Auth(properties, Client.newWebClient());
		}

		public MasterContext(Auth auth, Properties properties) {
			this.auth = auth;
			this.properties = properties;
		}

		public synchronized Context getContext(Long worldId){
			if (!contexts.containsKey(worldId)) {
				newContext(worldId);
			} 
			return contexts.get(worldId);
		}

		public synchronized Context newContext(Long worldId) {
			WebClient newClient = auth.loginToWorldWithRetry(worldId).getWebClient();
			Context context = new Context(worldId, auth, newClient, properties);
			contexts.put(worldId, context);
			return context;
		}

	}
}
