package nexi.sengoku.easy;

import java.io.IOException;
import java.net.MalformedURLException;

import org.apache.log4j.Logger;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

public class World {

	private static final Logger logger = Logger.getLogger(World.class);
			
	private final String worldsPageString;
	private final int world;
	
	private volatile Context context;
	
	private volatile long wood;
	private volatile long woodMax;
	private volatile long cloth;
	private volatile long clothMax;
	private volatile long iron;
	private volatile long ironMax;
	private volatile long wheat;
	private volatile long wheatMax;
	
	public World(int world, String worldsPageString) {
		this.world = world;
		this.worldsPageString = worldsPageString;
	}
	
	public void load() throws FailingHttpStatusCodeException, MalformedURLException, IOException {
		WebClient webClient = Client.newWebClient();
		
		logger.debug(String.format("%s&wd=w%03d", worldsPageString, world));
		HtmlPage page = (HtmlPage) webClient.getPage(String.format("%s&wd=w%03d", worldsPageString, world));
		logger.debug(webClient.getCookieManager().getCookies());
		
		WebClient villageClient = Client.newWebClient(webClient.getCookieManager());
		
		page = (HtmlPage) villageClient.getPage(String.format("http://w%03d.sengokuixa.jp/village.php", world));
		logger.debug(page.asText());
	}
}
