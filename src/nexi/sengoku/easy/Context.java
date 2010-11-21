package nexi.sengoku.easy;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Properties;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

public class Context {

	public final World world;
	private final WebClient webClient;
	private final Properties properties;

	public Context(World world, WebClient webClient, Properties properties) {
		this.world = world;
		this.webClient = webClient;
		this.properties = properties;
	}

	public HtmlPage getPage(String url) throws FailingHttpStatusCodeException, MalformedURLException, IOException {
		if (properties.containsKey("debugFile")) {
			System.out.println("found debug" + properties.getProperty("debugFile"));
			return webClient.getPage(properties.getProperty("debugFile"));
		} else {
			System.out.println("getting page " + url);
			return webClient.getPage(url);
		}
	}
}
