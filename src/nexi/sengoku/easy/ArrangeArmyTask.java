package nexi.sengoku.easy;

import java.util.List;

import org.apache.log4j.Logger;

import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlButton;
import com.gargoylesoftware.htmlunit.html.HtmlButtonInput;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSelect;
import com.gargoylesoftware.htmlunit.html.HtmlSpan;
import com.gargoylesoftware.htmlunit.html.HtmlTable;
import com.gargoylesoftware.htmlunit.html.HtmlTableRow;

public class ArrangeArmyTask extends AbstractTask<Boolean> {

	private static final Logger logger = Logger.getLogger(ArrangeArmyTask.class);

	public ArrangeArmyTask(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	@Override
	public Boolean call() throws Exception {
		logger.info("arranging army");
		
		HtmlPage page = (HtmlPage) context.getPage(context.getBaseUrl()
				+ "/facility/set_unit_list.php?show_num=100&sort_order_1=6&sort_order_2=2&sort_order_type_2=1");


		List<?> attacks = page.getByXPath("//span[@class='ig_card_status_att']");
		List<?> soldierTypesSelect = page.getByXPath("//select[contains(@id,'unit_id_select')]");
		List<?> soldierCounts = page.getByXPath("//input[contains(@id,'unit_cnt_text')]");
		List<?> changeCountButton = page.getByXPath("//input[contains(@id,'btn_change')]");
		List<?> cardIds = page.getByXPath("//input[contains(@id,'card_id_arr')]");

		for (int i = 0; i < cardIds.size(); i++) {
			String cardId = ((HtmlInput) cardIds.get(i)).getValueAttribute();

			HtmlInput solderCountInput = (HtmlInput) soldierCounts.get(i);
			Long soldierCount = Long.parseLong(solderCountInput.getValueAttribute());

			Object costE = page.getFirstByXPath(String.format("//div[contains(@id,'%s')]//span[@class='ig_card_cost']", cardId));
			Double cost = Double.parseDouble(((HtmlSpan) costE).getTextContent().trim());
			Object rareE = page.getFirstByXPath(String.format("//div[contains(@id,'%s')]//span[contains(@class,'rarerity')]", cardId));
			Long rareity = Long.parseLong(((HtmlSpan) rareE).getAttribute("class").split("_")[1]);
			Long attack = Long.parseLong( ((HtmlSpan) attacks.get(i)).getTextContent().trim());

			if ( cost > 2) {
				break;
			} else if (cost < 2 || (cost == 2 && rareity == 2)) {
				if (soldierCount < 1) {
					((HtmlSelect) soldierTypesSelect.get(i)).setSelectedAttribute("330", true);
					solderCountInput.setValueAttribute("1");
					HtmlButtonInput button = ((HtmlButtonInput) changeCountButton.get(i));
					button.click();
					logger.info("set " + cost + " " + rareity + " " + attack + " " + soldierCount);
					Thread.sleep(1000L);
				}
			} else if (attack < 800 && (cost < 2 || (cost == 2 && rareity == 1))) {
//				if (soldierCount < 10) {
//					((HtmlSelect) soldierTypesSelect.get(i)).setSelectedAttribute("330", true);
//					solderCountInput.setValueAttribute("10");
//					HtmlButtonInput button = ((HtmlButtonInput) changeCountButton.get(i));
//					button.click();
//					logger.info("set " + cost + " " + rareity + " " + attack + " " + soldierCount);
//					Thread.sleep(1000L);
//				}
			} 
		}

		return true;
	}

}
