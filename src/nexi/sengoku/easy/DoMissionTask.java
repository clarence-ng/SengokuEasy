package nexi.sengoku.easy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

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

public class DoMissionTask extends AbstractTask<Boolean> {

	private static final Logger logger = Logger.getLogger(DoMissionTask.class);

	private final String villageId;
	private final Mission mission;

	public DoMissionTask(Context context, String villageId, Mission mission) {
		super(context);
		this.villageId = villageId;
		this.mission = mission;
	}

	public static final class Args {
		@Option(name="-w")
		public Long worldId;
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
	public Boolean call() throws Exception {
		try {				
//			HtmlPage groupPage = (HtmlPage) context.getPage(context.getBaseUrl()
//					+ String.format("/card/deck.php?ano=0&dmo=nomal&p=1&myselect="));
//			
//			Object leader = groupPage.getFirstByXPath("//div[@id='ig_bg_decksection1right']//div[@class='ig_deck_unitdata_leader']");
//			if (leader != null) {
//				
//				Object groupLoc = groupPage.getFirstByXPath("//div[@id='ig_bg_decksection1right']//div[@class='ig_deck_unitdata_assign deck_wide_select']");
//				//新本拠地
//				
//				List<?> members = groupPage.getByXPath("//div[@id='ig_bg_decksection1right']//div[@class='g_deck_unitdata_subleader']");
//				if (members.size() < 3) {
//					
//				}
//				
//
//			}


			HtmlPage p = (HtmlPage) context.getPage(context.getBaseUrl()
					+ String.format("/village_change.php?village_id=%s&from=menu&page=/facility/dungeon.php", villageId));

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
						if (logger.isDebugEnabled()) {
							logger.debug(e2.getTextContent());
						}
					}
				}
			}

			final Map<String,List<General>> teams = new HashMap<String, List<General>>();
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
							if (logger.isDebugEnabled()) {
								logger.debug(e2.getTextContent().trim());
							}
							generals.add(new General(e2.getTextContent().trim()));
						} 
					}
					//Parse HP
					int h = 0;
					for (HtmlElement e2 : itr.next().getAllHtmlChildElements()){
						if (e2 instanceof HtmlSpan && !e2.getTextContent().contains("-")) {
							if (logger.isDebugEnabled()) {
								logger.debug(e2.getTextContent().trim());
							}
							int hp = Integer.parseInt(e2.getTextContent().trim());
							generals.get(h).setHp(hp);
							h++;
						}
					}
					teams.put(teamName, generals);
				} 
			}

			HtmlDivision buttonDiv = form.getFirstByXPath("//div[@class='btnarea']");
			if (logger.isDebugEnabled()) {
				logger.debug(teams);
			}

			long minHp = Long.parseLong(context.properties.getProperty("minMissionHp").trim());

			final AtomicBoolean hasSent = new AtomicBoolean();

			for (final Map.Entry<String, HtmlRadioButtonInput> goableTeam : teamGoButtons.entrySet()) {
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
							hasSent.set(true);
							StringBuilder sb = new StringBuilder();
							for (String k : teams.keySet()) {
								sb.append(teams.get(k)).append("\n");
							}
							return true;
						}
					});

					((HtmlAnchor) buttonDiv.getChildElements().iterator().next()).click();

					context.webClient.waitForBackgroundJavaScript(2500L);
				}
			}

			if (!hasSent.get()) {
				logger.info("Did not send anyone. Threshold:" + minHp + " Teams:" + teams);
			}
		} catch(Exception e) {
			logger.info("throw", e);
		}
		return true;
	}

}
