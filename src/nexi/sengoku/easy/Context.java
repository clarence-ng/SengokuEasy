package nexi.sengoku.easy;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebRequestSettings;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

public class Context {

	private static final Logger logger = Logger.getLogger(Context.class);

	public final World world;
	public final Properties properties;
	public final WebClient webClient;

	public Context(World world, WebClient webClient, Properties properties) {
		this.world = world;
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

}
