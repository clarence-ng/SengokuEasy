package nexi.sengoku.easy;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.kohsuke.args4j.Option;

import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

public class DoUpgradeTask extends AbstractTask {
	private static final Logger logger = Logger.getLogger(DoUpgradeTask.class);
	private VillageMapTile tile;
	private int villageId;
	
	public DoUpgradeTask(Context context, VillageMapTile tile, int villageId) {
		super(context);
		this.tile = tile;
		this.villageId = villageId;
	}
	

	public static final class Args {
		@Option(name="-index", required=true)
		public int villageIndex;
		
		@Option(name="-x")
		public int x;

		@Option(name="-y")
		public int y;
	}

	@Override
	public void run() {		
		
		try {
			String villageLink = context.getBaseUrl() + String.format("/village_change.php?village_id=%d&from=menu&page=village.php", villageId);
			logger.debug("Switching to village page: " + villageLink);
			Thread.sleep(1000);
			
			// TODO: Handle Invalid input, throw exception
			if (!isValidUpgrade(tile)){
				logger.error("Error executing doBuildingTask");
			}
			
			String fullClickLink = context.getBaseUrl() + "/" + tile.getLink();
			logger.debug("Upgrade link: " + fullClickLink);
			HtmlPage confirmationPage = (HtmlPage) context.getPage(fullClickLink);
			logger.debug("Confirmation page: " + confirmationPage.asXml());
			
			// Required Resources
			String woodString = ((HtmlElement)confirmationPage.getFirstByXPath
					("//div[@class='icon_wood']")).getTextContent();
			int woodCost = extractResourceAmount(woodString);			

			String cottonString = ((HtmlElement)confirmationPage.getFirstByXPath
					("//div[@class='icon_cotton']")).getTextContent();
			int cottonCost = extractResourceAmount(cottonString);

			String ironString = ((HtmlElement)confirmationPage.getFirstByXPath
					("//div[@class='icon_iron']")).getTextContent();
			int ironCost = extractResourceAmount(ironString);
			
			String foodString = ((HtmlElement)confirmationPage.getFirstByXPath
					("//div[@class='icon_food']")).getTextContent();
			int foodCost = extractResourceAmount(foodString);
			
			logger.info("Upgrade building " + tile.getStructure().toString());
			// TODO: logger.info("Current resources:");
			logger.info("Cost: W:" + woodCost + " C:" + cottonCost + " I:" + ironCost + " F:" + foodCost);
			// TODO: Parse population cost.
						
			HtmlElement div = confirmationPage.getFirstByXPath
				("//div[@class='ig_tilesection_btnarea_left']");
			
			for (HtmlElement element : div.getChildElements()){
				if(element instanceof HtmlAnchor){
					String fullConfirmationLink = context.getBaseUrl() + "/facility/" + ((HtmlAnchor) element).getHrefAttribute();
					logger.debug("Build confirmation link: " + fullConfirmationLink);
					
					context.getPage(fullConfirmationLink);
					tile.getStructure().upgrade(); // Increase lv by 1
				}
			}
		}
		catch (Exception e){
			logger.error("Upgrade task failed.");
			 e.printStackTrace();
		}
	}
	
	private boolean isValidUpgrade(VillageMapTile tile) {
		switch (tile.getStructure().getStructureType()){
			case EMPTY_FIELD:
				logger.error("Fail: Attempting to upgrade empty field.");
				return false;
			case IRON:
				logger.error("Fail: Attempting to upgrade on IRON field.");
				return false;
			case COTTON:
				logger.error("Fail: Attempting to upgrade on COTTON.");
				return false;
			case WOOD:
				logger.error("Fail: Attempting to upgrade on WOOD.");
				return false;
			case BARREN_LAND:
				logger.error("Fail: Attempting to upgrade on BARREN_LAND.");
				return false;
			case DRY_FIELD:
				logger.error("Fail: Attempting to upgrade on DRY_FIELD.");
				return false;
				
			default: // OK
				logger.debug("Doing upgrade on valid tile.");
				return true;
		}
	}
	
	private int extractResourceAmount (String resourceString){
		// Takes resource string in this format 木 334 and return the int value of resources
		Pattern p = Pattern.compile(".*?([0-9]+)");
		Matcher m = p.matcher(resourceString);
		if(m.matches()){
			return Integer.parseInt(m.group(1));
		}
		else {
			logger.error("Parsing resource string failed. String = " + resourceString);
			return -1;
		}
	}
}
