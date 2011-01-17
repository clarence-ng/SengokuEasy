package nexi.sengoku.easy;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlDivision;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

public class PageHelper {

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
	
}
