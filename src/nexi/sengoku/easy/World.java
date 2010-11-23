package nexi.sengoku.easy;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.kohsuke.args4j.Option;

import com.gargoylesoftware.htmlunit.ElementNotFoundException;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlDivision;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlMap;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

public class World {
	private static final Logger logger = Logger.getLogger(World.class);

	private volatile long wood;
	private volatile long woodMax;
	private volatile long cloth;
	private volatile long clothMax;
	private volatile long iron;
	private volatile long ironMax;
	private volatile long wheat;
	private volatile long wheatMax;

	private final List<Village> villages = new ArrayList<Village>();
	private final List<General> generals = new ArrayList<General>();

	@Option(name="-w", required=true)
	private int id;
	public void setId(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}
	
	public void load(Context context) throws ElementNotFoundException, IOException, FailingHttpStatusCodeException, WeAreBrokenException {
		String worldSelectUrl = context.getLoginUrl();
		boolean worldSuccess = false;
		while (!worldSuccess) {
			worldSuccess = internalLoad(context, worldSelectUrl);
			if (!worldSuccess) {
				logger.info("Couldn't log in to world. Trying from fresh session");
				worldSelectUrl = context.newLoginUrl();
			}
		}
	}

	private boolean internalLoad(Context context, String worldSelectUrl) throws FailingHttpStatusCodeException, MalformedURLException, IOException, WeAreBrokenException {

		logger.debug(String.format("%s&wd=w%03d", context.getBaseUrl(), context.worldId));
		HtmlPage page = (HtmlPage) context.getPage(String.format("%s&wd=w%03d", worldSelectUrl, context.worldId));
		logger.debug(context.webClient.getCookieManager().getCookies());

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

		updateVillageIds(page);

		HtmlMap mapOverlayMap = (HtmlMap)page.getElementById("mapOverlayMap");		
		VillageMap map = VillageMap.createVillageMapFromHtmlMapElement(mapOverlayMap);
		map.displayVillageMap();	

		return true;
	}

	public void updateVillageIds(HtmlPage page) throws IOException {
		HtmlAnchor otherVillage = parseVillageIds(page);
		if (otherVillage != null) {
			HtmlPage otherPage = otherVillage.click();
			parseVillageIds(otherPage);
		}
	}

	private HtmlAnchor parseVillageIds(HtmlPage page) {
		HtmlAnchor someVillage = null;

		HtmlDivision villagesDiv = (HtmlDivision)page.getFirstByXPath("//div[@class='sideBoxInner basename']");
		for (HtmlElement anchor : villagesDiv.getElementsByTagName("a")) {
			String[] query = ((HtmlAnchor) anchor).getHrefAttribute().split("\\?");
			String[] params = query[1].split("&");
			for (int i = 0; i < params.length; i++) {
				String[] arg = params[i].split("=");
				if (arg.length >= 2 && arg[0].equals("village_id")) {
					logger.info(arg[1]);
					someVillage = (HtmlAnchor) anchor;
				}
			}
		}

		return someVillage;
	}

	public List<General> getGenerals() {
		return generals;
	}

	public List<Village> getVillages() {
		return villages;
	}

	public GeneralMarket getGeneralMarket() {
		return null;
	}

	public VillageMap getVillageMap() {
		return null;
	}
}
