package nexi.sengoku.easy;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.gargoylesoftware.htmlunit.ElementNotFoundException;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlMap;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

public class World {
	private static final Logger logger = Logger.getLogger(World.class);

	private final String baseUrl;
	private final int id;
	private final WebClient webClient;

	private volatile long wood;
	private volatile long woodMax;
	private volatile long cloth;
	private volatile long clothMax;
	private volatile long iron;
	private volatile long ironMax;
	private volatile long wheat;
	private volatile long wheatMax;

	public static World loadNewWorld(Auth auth, WebClient webClient, Properties properties) throws ElementNotFoundException, IOException, FailingHttpStatusCodeException, WeAreBrokenException {
		World world = null;
		
		String baseUrl = auth.login();
		
		boolean worldSuccess = false;
		while (!worldSuccess) {
			world = new World(
					Integer.parseInt(properties.getProperty("loginWorld")),
					baseUrl,
					webClient
			);		
			worldSuccess = world.load();
			if (!worldSuccess) {
				logger.info("Couldn't log in to world. Trying from fresh session");
				baseUrl = auth.loginAndSaveSession();
			}
		}
		
		return world;
	}
	
	public World(int world, String worldsPageString, WebClient webClient) {
		this.id = world;
		this.baseUrl = worldsPageString;
		this.webClient = webClient;
	}

	public String getBaseUrl() {
		return String.format("http://w%03d.sengokuixa.jp", id);
	}
	
	public WebClient getWebClient() {
		return webClient;
	}
	
	public boolean load() throws FailingHttpStatusCodeException, MalformedURLException, IOException, WeAreBrokenException {
		
		logger.debug(String.format("%s&wd=w%03d", baseUrl, id));
		HtmlPage page = (HtmlPage) webClient.getPage(String.format("%s&wd=w%03d", baseUrl, id));
		logger.debug(webClient.getCookieManager().getCookies());

		//check if page load succeed in a hacky way.
		if (page.getElementById("wood") == null) {
			return false;
		}
		
		wood = Long.parseLong(page.getElementById("wood").getTextContent());
		woodMax = Long.parseLong(page.getElementById("wood_max").getTextContent());
		cloth = Long.parseLong(page.getElementById("stone").getTextContent());
		clothMax = Long.parseLong(page.getElementById("stone_max").getTextContent());
		iron = Long.parseLong(page.getElementById("iron").getTextContent());
		ironMax = Long.parseLong(page.getElementById("iron_max").getTextContent());
		wheat = Long.parseLong(page.getElementById("rice").getTextContent());
		wheatMax = Long.parseLong(page.getElementById("rice_max").getTextContent());		

		HtmlMap mapOverlayMap = (HtmlMap)page.getElementById("mapOverlayMap");		
		VillageMap map = VillageMap.createVillageMapFromHtmlMapElement(mapOverlayMap);
		map.displayVillageMap();	
		
		return true;
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
	
	public VillageMap getVillageMap() {
		return null;
	}
}
