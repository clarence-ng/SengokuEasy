package nexi.sengoku.easy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.Option;

import com.gargoylesoftware.htmlunit.ConfirmHandler;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlDivision;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlRadioButtonInput;
import com.gargoylesoftware.htmlunit.html.HtmlSpan;
import com.gargoylesoftware.htmlunit.html.HtmlTable;

public class DoMissionTask extends AbstractTask {

	private static final Logger logger = Logger.getLogger(DoMissionTask.class);

	private final long villageId;
	private final Mission mission;

	public DoMissionTask(Context context, long villageId, Mission mission) {
		super(context);
		this.villageId = villageId;
		this.mission = mission;
	}

	public static final class Args {
		@Option(name="-v", required=true)
		public long villageId;
		@Option(name="-r")
		public boolean repeat;
		@Option(name="-m", required=true)
		public void setMission(long missionId) throws CmdLineException {
			if (missionId == 1) {
				mission = Mission.Valley;
			} else if (missionId == 2) {
				mission = Mission.SeaOfForest;
			} else if (missionId == 3) {
				mission = Mission.CliffTemple;
			} else if (missionId == 4) {
				mission = Mission.SpringOfLongevity;
			} else {
				throw new CmdLineException("Invalid missionId. Must be 1-4");
			}
		}
		public Mission mission;
	}
	
	@Override
	public void run() {
		try {
			HtmlPage p = (HtmlPage) context.getPage(context.getBaseUrl()
					+ String.format("/village_change.php?village_id=%d&from=menu&page=/facility/dungeon.php", villageId));

			HtmlForm form = (HtmlForm) p.getElementById("dungeon_input_form");
			SengokuEasy.debug(form, 0, logger);

			Map<Integer, HtmlRadioButtonInput> missionbuttons = new HashMap<Integer, HtmlRadioButtonInput>();
			int m = 0;
			for (HtmlElement e : form.getElementById("dungeon_list_body").getChildElements()) {
				for (HtmlElement e2: e.getAllHtmlChildElements()) {
					if (e2 instanceof HtmlRadioButtonInput) {
						missionbuttons.put(m, (HtmlRadioButtonInput) e2); 
						m++;
					} else if (e2 instanceof HtmlSpan) {
						logger.debug(e2.getTextContent());
					}
				}
			}

			Map<String,List<General>> teams = new HashMap<String, List<General>>();
			Map<String,HtmlRadioButtonInput> teamGoButtons = new HashMap<String, HtmlRadioButtonInput>();

			for (HtmlElement e : form.getElementsByTagName("table")) {
				if (e instanceof HtmlTable) {
					List<General> generals = new ArrayList<General>(); 
					Iterator<HtmlElement> itr = e.getChildElements().iterator().next().getChildElements().iterator();
					//Parse team name
					String teamName = itr.next().getChildElements().iterator().next().getTextContent().trim(); 
					for (HtmlElement e2 : itr.next().getAllHtmlChildElements()){
						if (e2 instanceof HtmlRadioButtonInput) {
							//Parse radio button to do quest
							teamGoButtons.put(teamName, (HtmlRadioButtonInput) e2);
						} else if (e2 instanceof HtmlAnchor && !e2.getTextContent().contains("-")) {
							//Parse generals within the team
							logger.debug(e2.getTextContent().trim());
							generals.add(new General(e2.getTextContent().trim()));
						} 
					}
					//Parse HP
					int h = 0;
					for (HtmlElement e2 : itr.next().getAllHtmlChildElements()){
						if (e2 instanceof HtmlSpan && !e2.getTextContent().contains("-")) {
							logger.debug(e2.getTextContent().trim());
							int hp = Integer.parseInt(e2.getTextContent().trim());
							generals.get(h).setHp(hp);
							h++;
						}
					}
					teams.put(teamName, generals);
				} 
			}
			
			HtmlDivision buttonDiv = form.getFirstByXPath("//div[@class='btnarea']");
			logger.debug(teams);

			long minHp = Long.parseLong(context.properties.getProperty("minMissionHp").trim());

			for (Map.Entry<String, HtmlRadioButtonInput> goableTeam : teamGoButtons.entrySet()) {
				boolean allHealthy = true;
				for (General general: teams.get(goableTeam.getKey())) {
					if (general.getHp() < minHp) {
						allHealthy = false;
						break;
					}
				}
				if (allHealthy) {
					//All generals are healthy, send them for mission!
					logger.info("Sending team " + goableTeam.getKey() 
							+ " to mission " + mission
							+ " team " + teams.get(goableTeam.getKey()));
					missionbuttons.get(mission.missionId).click();
					goableTeam.getValue().click();

					context.webClient.setConfirmHandler(new ConfirmHandler() {
						@Override
						public boolean handleConfirm(Page arg0, String arg1) {
							logger.info("Confirmed sending.");
							return true;
						}
					});
					
					((HtmlAnchor) buttonDiv.getChildElements().iterator().next()).click();
					
					context.webClient.waitForBackgroundJavaScript(2500L);
				}
			}
			
			logger.info("Did not send anyone. Threshold:" + minHp + " Teams:" + teams);
		} catch(Exception e) {
			logger.error("Error executing doMission", e);
		}
	}

}
