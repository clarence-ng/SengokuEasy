package nexi.sengoku.easy;

import java.io.IOException;
import java.net.MalformedURLException;
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
	private VillageMap villageMap;

	private final List<Village> villages = new ArrayList<Village>();
	private final List<General> generals = new ArrayList<General>();
	
	private Set<Integer> villageIdSet = new HashSet<Integer>();
	
	private static World instance = null;

	@Option(name="-w", required=true)
	private int id;
	
	// World is singleton
	private World (){}
	
	public static final class Args {
		
		@Option(name="-index", required=true)
		public int villageIndex;
	}

	
	public static World getInstance(){
		if (null == instance){
			instance = new World ();
		}
		return instance;
	}
	
	public void setId(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}
	
	private boolean isLoadSuccess(HtmlPage page){
		if (page.getElementById("wood") == null) {
			logger.error("Update resources failed.");
			return false;
		}
		return true;
	}
	
	// Should be called in every page load
	public void updateResources(HtmlPage page){
		//check if page load succeed in a hacky way.

		wood = Long.parseLong(page.getElementById("wood").getTextContent());
		woodMax = Long.parseLong(page.getElementById("wood_max").getTextContent());
		cloth = Long.parseLong(page.getElementById("stone").getTextContent());
		clothMax = Long.parseLong(page.getElementById("stone_max").getTextContent());
		iron = Long.parseLong(page.getElementById("iron").getTextContent());
		ironMax = Long.parseLong(page.getElementById("iron_max").getTextContent());
		wheat = Long.parseLong(page.getElementById("rice").getTextContent());
		wheatMax = Long.parseLong(page.getElementById("rice_max").getTextContent());
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
	    
	    if(isLoadSuccess(page)){
			updateResources(page);
			updateVillageIds(context, page);
			createVillageMapForAllVillages(context);
			return true;
	    }
	    
	    return false;
	}

	public void updateVillageIds(Context context, HtmlPage page) throws IOException {
		HtmlAnchor otherVillage = parseVillageIds(context, page);
		if (otherVillage != null) {
			HtmlPage otherPage = otherVillage.click();
			parseVillageIds(context, otherPage);
		}
	}
	
	private void createVillageMapForAllVillages(Context context) throws IOException, WeAreBrokenException {
		logger.info("Creating village map for all villages.");
		
		// Go to the page of each village and get it's map
		for (Village village : villages){
			String villageLink = context.getBaseUrl() + String.format("/village_change.php?village_id=%d&from=menu&page=village.php", village.getVillageId());
			logger.info("Visiting village link: " + villageLink);
			HtmlPage villagePage = (HtmlPage) context.getPage(villageLink);
			HtmlMap mapOverlayMap = (HtmlMap)villagePage.getElementById("mapOverlayMap");		
			village.setVillageMap(VillageMap.createVillageMapFromHtmlMapElement(mapOverlayMap));
		}
		logger.info("Done creating village maps.");
	}

	private HtmlAnchor parseVillageIds(Context context, HtmlPage page) {
		HtmlAnchor someVillage = null;

		HtmlDivision villagesDiv = (HtmlDivision)page.getFirstByXPath("//div[@class='sideBoxInner basename']");
		
		// Only works for multiple villages.
		for (HtmlElement anchor : villagesDiv.getElementsByTagName("a")) {
			String[] query = ((HtmlAnchor) anchor).getHrefAttribute().split("\\?");
			String[] params = query[1].split("&");
			for (int i = 0; i < params.length; i++) {
				String[] arg = params[i].split("=");
				if (arg.length >= 2 && arg[0].equals("village_id")) {
					int villageId = Integer.parseInt(arg[1]);
					String villageTitle = anchor.getAttribute("Title");
					logger.debug("Village title:" + villageTitle + " id:" + villageId);						
					if(!villageIdSet.contains(Integer.valueOf(villageId))){
						villageIdSet.add(Integer.valueOf(villageId));
						villages.add(new Village(context, villageId, villageTitle));
					}
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
		return villageMap;
	}
	
	public long getWoodMax(){
		return woodMax;
	}
	
	public long getIronMax(){
		return ironMax;
	}
	
	public long getClothMax(){
		return clothMax;
	}
	
	public long getWheatMax(){
		return wheatMax;
	}
	
	public long getWood(){
		return wood;
	}
	
	public long getIron(){
		return iron;
	}
	
	public long getCloth(){
		return cloth;
	}
	
	public long getWheat(){
		return wheat;
	}
	
	public int getVillageIdFromIndex(int index){
		return villages.get(index).getVillageId();
	}
	
	public Village getVillageFromIndex(int index){
		return villages.get(index);
	}
	
	public void listVillages(){
		for(int i = 0; i < villages.size(); i++){
			logger.debug(i + ": VillageId = " + villages.get(i).getVillageId());
			logger.info(i + ": " + villages.get(i).getVillageName());
		}
	}
	
	public void printResource(){
		logger.info("Wood: " + wood + "/" + woodMax);
		logger.info("Cloth: " + cloth + "/" + clothMax);
		logger.info("Iron: " + iron + "/" + ironMax);
		logger.info("Wheat: " + wheat + "/" + wheatMax);
	}
}