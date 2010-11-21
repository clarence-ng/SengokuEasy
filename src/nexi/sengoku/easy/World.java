package nexi.sengoku.easy;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;

import org.apache.log4j.Logger;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

public class World {
	private static final Logger logger = Logger.getLogger(World.class);

	private final String baseUrl;
	private final int id;

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
		this.id = world;
		this.baseUrl = worldsPageString;
	}

	public String getBaseUrl() {
		return String.format("http://w%03d.sengokuixa.jp", id);
	}
	public void load() throws FailingHttpStatusCodeException, MalformedURLException, IOException {
		WebClient webClient = Client.newWebClient();

		logger.debug(String.format("%s&wd=w%03d", baseUrl, id));
		HtmlPage page = (HtmlPage) webClient.getPage(String.format("%s&wd=w%03d", baseUrl, id));
		logger.debug(webClient.getCookieManager().getCookies());

//		WebClient villageClient = Client.newWebClient(webClient.getCookieManager());
//		page = (HtmlPage) villageClient.getPage(getBaseUrl() + "/village.php");
		
		wood = Long.parseLong(page.getElementById("wood").getTextContent());
		woodMax = Long.parseLong(page.getElementById("wood_max").getTextContent());
		cloth = Long.parseLong(page.getElementById("stone").getTextContent());
		clothMax = Long.parseLong(page.getElementById("stone_max").getTextContent());
		iron = Long.parseLong(page.getElementById("iron").getTextContent());
		ironMax = Long.parseLong(page.getElementById("iron_max").getTextContent());
		wheat = Long.parseLong(page.getElementById("rice").getTextContent());
		wheatMax = Long.parseLong(page.getElementById("rice_max").getTextContent());
		
	}
	
	public List<General> getGenerals() {
		return null;
	}

	public List<Village> getVillages() {
		return null;
	}
	
	public GeneralMarket getGeneralMarket() {
		return null;
	}
	
	public Map getMap() {
		return null;
	}
}
