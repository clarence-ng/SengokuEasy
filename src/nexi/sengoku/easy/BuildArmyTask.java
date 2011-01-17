package nexi.sengoku.easy;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Logger;

import com.gargoylesoftware.htmlunit.ConfirmHandler;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlArea;
import com.gargoylesoftware.htmlunit.html.HtmlDivision;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlImage;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlTable;
import com.gargoylesoftware.htmlunit.html.HtmlTableCell;
import com.gargoylesoftware.htmlunit.html.HtmlTableRow;

public class BuildArmyTask extends AbstractTask<Boolean> {

	public enum UnitType {
		cavalry("c2", "厩舎", "horse", "330"),
		archer("a2", "弓兵舎", "bow", "326"),
		infantry("s2", "足軽兵舎", "ashigaru", "322"),
		castlehummer("ch", "兵器鍛冶", "icon_castlehummer", "334"),
		hummer("h", "兵器鍛冶", "icon_hummer", "333");
		
		public final String alias;
		public final String buildingName;
		public final String id;
		public final String name;

		private UnitType(String alias, String id, String name, String buildingName) {
			this.alias = alias;
			this.id = id;
			this.name = name;
			this.buildingName = buildingName;
		}
	}

	public static final Map<String, UnitType> unitTypes = new HashMap<String, UnitType>();
	static {
		for (UnitType t: UnitType.values()) {
			unitTypes.put(t.alias, t);
		}
	}

	public enum UnitCost {
		cavalry(8,9,15,22),
		archer(15,10,6,5),
		infantry(10,15,5,6);

		public final long wood;
		public final long cotton;
		public final long iron;
		public final long wheat;

		private UnitCost(long wood, long cotton, long iron, long wheat) {
			this.wood = wood;
			this.cotton = cotton;
			this.iron = iron;
			this.wheat = wheat;
		}
	}

	private static final Logger logger = Logger.getLogger(BuildArmyTask.class);
	private final List<String> villageIds = new ArrayList<String>();
	private final List<UnitType> typesToTrain = new ArrayList<UnitType>();

	public BuildArmyTask(Context context, List<UnitType> typesToTrain, List<String> villageIds) {
		super(context);
		this.typesToTrain.addAll(typesToTrain);
		this.villageIds.addAll(villageIds);
	}

	@Override
	public Boolean call() throws Exception {
		boolean success = true;

		HtmlPage currentTrainedPage = (HtmlPage) context.getPage(context.getBaseUrl()
				+ "/facility/unit_list.php");
		List<?> tables = currentTrainedPage.getByXPath("//table[@class='paneltable table_fightlist2']");
		
		
		Map<UnitType, Boolean> currentTrainingMap = new HashMap<UnitType, Boolean>();
		
		if (tables.size() > 1) {
			HtmlTable table = (HtmlTable) tables.get(1);
			for (int i = 1; i < table.getRowCount(); i++) {
				HtmlTableRow row = table.getRow(i);
				HtmlTableCell cell = row.getCell(0);
				HtmlImage img = (HtmlImage) cell.getElementsByTagName("img").get(0);
				
				for (UnitType t: UnitType.values()) {
					if (img.getSrcAttribute().contains(t.name)) {
						currentTrainingMap.put(t, true);
					}
				}
			}
		}
		
		logger.info("training " + currentTrainingMap.keySet()
				+ " villages " + villageIds
				+ " types to train " + typesToTrain);
		
		for (String villageId: villageIds) {
			for (UnitType type: typesToTrain) {
				try {	
					List<?> waitingUnitText = currentTrainedPage.getByXPath("//div[@class='ig_fightunit_title']");
					String totalWaiting = ((HtmlDivision) waitingUnitText.get(0)).asText();

					StringBuilder strB = new StringBuilder();
					
					if (!currentTrainingMap.containsKey(type)) {
						strB.append(train(context, villageId, type, "100"));
					}
				
					if (strB.length() > 0) {
						String msg = strB.toString();
						if (msg.contains("Failed to train")) {
							success = false;
							break;
						} else {
//							new Mailman(context.properties).tryDeliver("Building army at:" + villageId + "\n" + strB.toString() + "\nWaiting total:" + totalWaiting);
						}
					}
				} catch(Exception e) {
					new Mailman(context.properties).tryDeliver("Failed to build army at:" + villageId + "\n" + ExceptionUtils.getFullStackTrace(e));
				}
			}
		}		
		return success;
	}

	public String train(Context context, String villageId, UnitType type, String value) throws FailingHttpStatusCodeException, MalformedURLException, IOException {
		final AtomicBoolean trained = new AtomicBoolean();

		HtmlPage page = (HtmlPage) context.getPage(context.getBaseUrl()
				+ String.format("/village_change.php?village_id=%s", villageId));
		HtmlPage trainingPage = null;
		HtmlPage trainingConfirmPage = null;
		List<?> elements = page.getByXPath(String.format("//map[@name='mapOverlayMap']//area[contains(@title,'%s')]", type.id));
		if (elements.size() <= 0) {
			logger.info("no building for type " + type.id);
			return "";
		}
		HtmlArea area = (HtmlArea) elements.get(0);
		trainingPage = area.click();	
		
		List<HtmlForm> forms = trainingPage.getForms();
		
		for (HtmlForm createUnitForm: forms) {
			try {
				createUnitForm.getInputByName(String.format("unit_value[%s]", type.buildingName)).setValueAttribute(value);	
			} catch(Exception e) {
				logger.debug("looping through and finding the form for the unit. Can't find input " + String.format("unit_value[%s]", type.buildingName) + " for form. Trying next.");
				continue;
			}
			
			trainingConfirmPage = (HtmlPage)createUnitForm.getInputByName("send").click();

			context.webClient.setConfirmHandler(new ConfirmHandler() {
				@Override
				public boolean handleConfirm(Page arg0, String arg1) {
					logger.info("Confirmed training");
					trained.set(true);
					return true;
				}
			});

			List<?> submitButton = trainingConfirmPage.getByXPath("//a[contains(@onclick,'dataForm')]");

			if (submitButton.size() > 0) {
				logger.info("training " + value + " " + type.name());
				((HtmlAnchor) submitButton.get(0)).click();

				context.webClient.waitForBackgroundJavaScript(5000L);

				String msg = "";
				if (!trained.get()) {
					Resources res = new Resources(trainingConfirmPage);
					msg = "Failed to train " + value + " " + type.name() + "\nCurrent resources " + res + " ";
				} else {
					msg = "Trained " + value + " " + type.name() + " ";
				}
				return msg;
			} else {
				return "Failed to train: insufficient resource";
			}	
						
		}
		
		return "Failed to train: can't find form";
	}

}
