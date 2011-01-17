package nexi.sengoku.easy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Logger;

import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlImage;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

public class StatusTask extends AbstractTask<Boolean> {

	private static final Logger logger = Logger.getLogger(StatusTask.class);

	public StatusTask(Context context) {
		super(context);
	}

	@Override
	public Boolean call() throws Exception{
		Map<String, Status> previousStatuses = new HashMap<String, Status>();
		Map<String, Status> previousEnemyStatuses = new HashMap<String, Status>();

		for (;;) {
			try {
				previousStatuses = updateStatus(previousStatuses, "all", false);
				previousEnemyStatuses = updateStatus(previousEnemyStatuses, "enemy", false);

				for (;;) {
					previousStatuses = updateStatus(previousStatuses, "all", true);
					previousEnemyStatuses = updateStatus(previousEnemyStatuses, "enemy", true);
					Thread.sleep(10000L);
				}
			} catch(Exception e) {

			}
		}

	}

	private Map<String, Status> updateStatus(Map<String, Status> previousStatuses, String modeString, boolean mail) {
		List<Status> currentStatuses = new ArrayList<Status>();
		try {
			HtmlPage page = (HtmlPage) context.getPage(context.getBaseUrl()
					+ String.format("/facility/unit_status.php?dmo=%s", modeString));

			List<Status> newStatuses = new ArrayList<Status>();
			List<Status> changedStatuses = new ArrayList<Status>();
			List<Status> expiredStatuses = new ArrayList<Status>();

			List<?> units = (List<?>) page.getByXPath("//div[@class='ig_fight_statusarea']//div[@class='ig_fightunit_title']" + 
			"| //div[@class='ig_fight_statusarea']//div[@class='ig_dungeonunit_title']");
			List<?> enemyUnits = (List<?>) page.getByXPath("//div[@class='ig_fight_statusarea']//div[@class='ig_fightunit_title2']");
			List<?> statusImgs = (List<?>) page.getByXPath("//div[@class='ig_fight_statusarea']//table[@class='paneltable table_fightlist']//img[contains(@src,'mode')]" +
			"| //div[@class='ig_fight_statusarea']//table[@class='paneltable table_fightlist']//img[contains(@src,'icon_search.png')]"	);
			List<?> countDowns = (List<?>) page.getByXPath("//div[@class='ig_fight_statusarea']//table[@class='paneltable table_fightlist']//span[@class='count_down']");

			for (Object o : units) {
				String team = ((HtmlElement)o).getTextContent().trim();
				Status status = new Status();
				status.team = team.split("\\]")[0].split("\\[")[1];
				currentStatuses.add(status);
			}
			for (Object o : enemyUnits) {
				String team = ((HtmlElement)o).getTextContent().trim();
				Status status = new Status();
				status.team = team.trim();
				currentStatuses.add(status);
			}

			int i = 0;
			for (Object o : statusImgs) {
				String[] parts = ((HtmlImage)o).getSrcAttribute().split("/");
				String[] fileParts = parts[parts.length-1].split("\\.");
				String status = fileParts[0];
				currentStatuses.get(i++).status = status;
			}
			Iterator<?> cdIter = countDowns.iterator();
			for (Status s: currentStatuses) {
				if (!s.status.contains("wait")) {
					String countdown = ((HtmlElement) cdIter.next() ).getTextContent();
					s.countdown = countdown;
				}
			}

			for (Status status: currentStatuses) {
				if (!previousStatuses.containsKey(status.team)) {
					newStatuses.add(status);
				} else {
					if (!previousStatuses.get(status.team).status.equals(status.status)) {
						changedStatuses.add(status);
					}
				}
			}
			for (Status status: previousStatuses.values()) {
				if (!currentStatuses.contains(status)) {
					expiredStatuses.add(status);
				}
			}

			//			logger.debug("Statuses: " + currentStatuses);

			if (!(expiredStatuses.isEmpty() && newStatuses.isEmpty() && changedStatuses.isEmpty())) {
				StringBuilder content = new StringBuilder();
				if (!newStatuses.isEmpty()) {
					logger.info("New statuses:\n" + newStatuses);
					content.append("New:\n");
					for (Status status: newStatuses) {
						content.append(status + "\n");
					}
				}
				if (!changedStatuses.isEmpty()) {
					logger.info("Changed statuses:\n" + changedStatuses);
					content.append("Changed:\n");
					for (Status status: changedStatuses) {
						content.append(status + "\n");
					}
				}
				if (!expiredStatuses.isEmpty()) {
					logger.info("Expired statuses:\n" + expiredStatuses);
					content.append("Expired:\n");
					for (Status status: expiredStatuses) {
						content.append(status + "\n");
					}
				}
				if (content.length() > 0 && mail) {
					new Mailman(context.properties).tryDeliver("SengokuEasy: status change", content.toString());
				}
			}
		} catch (Exception e) {
			logger.error("failed to get status", e);
			new Mailman(context.properties).tryDeliver("Failed to get status.\n" + ExceptionUtils.getFullStackTrace(e));
		}

		Map<String,Status> newStatuses = new HashMap<String, Status>();
		for (Status s: currentStatuses) {
			newStatuses.put(s.team, s);
		}
		return newStatuses;
	}

}
