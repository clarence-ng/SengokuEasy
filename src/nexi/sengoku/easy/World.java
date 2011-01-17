package nexi.sengoku.easy;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

	private final List<General> generals = new ArrayList<General>();

	@Option(name="-w", required=true)
	private long id;
	public void setId(long id) {
		this.id = id;
	}

	public long getId() {
		return id;
	}
	
	public void load(Context context) throws ElementNotFoundException, IOException, FailingHttpStatusCodeException, WeAreBrokenException {
		HtmlPage page = context.auth.loginToWorldWithRetry(context.worldId);
		
		wood = Long.parseLong(page.getElementById("wood").getTextContent());
		woodMax = Long.parseLong(page.getElementById("wood_max").getTextContent());
		cloth = Long.parseLong(page.getElementById("stone").getTextContent());
		clothMax = Long.parseLong(page.getElementById("stone_max").getTextContent());
		iron = Long.parseLong(page.getElementById("iron").getTextContent());
		ironMax = Long.parseLong(page.getElementById("iron_max").getTextContent());
		wheat = Long.parseLong(page.getElementById("rice").getTextContent());
		wheatMax = Long.parseLong(page.getElementById("rice_max").getTextContent());		

		getVillageIds(page);

		HtmlMap mapOverlayMap = (HtmlMap)page.getElementById("mapOverlayMap");		
		VillageMap map = VillageMap.createVillageMapFromHtmlMapElement(mapOverlayMap);
		map.displayVillageMap();	
		
	}

	public static List<Long> getVillageIds(HtmlPage page) throws IOException {
		Set<Long> villageIds = new HashSet<Long>();
		HtmlAnchor otherVillage = parseVillageIds(page, villageIds);
		if (otherVillage != null) {
			HtmlPage otherPage = otherVillage.click();
			parseVillageIds(otherPage, villageIds);
		}
		return new ArrayList<Long> (villageIds);
	}

	private static HtmlAnchor parseVillageIds(HtmlPage page, Set<Long> villageIds) {
		HtmlAnchor someVillage = null;

		HtmlDivision villagesDiv = (HtmlDivision)page.getFirstByXPath("//div[@class='sideBoxInner basename']");
		for (HtmlElement anchor : villagesDiv.getElementsByTagName("a")) {
			String[] query = ((HtmlAnchor) anchor).getHrefAttribute().split("\\?");
			String[] params = query[1].split("&");
			for (int i = 0; i < params.length; i++) {
				String[] arg = params[i].split("=");
				if (arg.length >= 2 && arg[0].equals("village_id")) {
					villageIds.add(Long.parseLong(arg[1]));
					someVillage = (HtmlAnchor) anchor;
				}
			}
		}

		return someVillage;
	}

	public List<General> getGenerals() {
		return generals;
	}

	

	public VillageMap getVillageMap() {
		return null;
	}
}
