package nexi.sengoku.easy;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.management.RuntimeErrorException;

import nexi.sengoku.easy.Context.MasterContext;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Duration;
import org.joda.time.Hours;
import org.joda.time.Interval;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlParagraph;
import com.gargoylesoftware.htmlunit.html.HtmlSpan;
import com.gargoylesoftware.htmlunit.html.HtmlTable;
import com.gargoylesoftware.htmlunit.html.HtmlTableRow;
import com.google.common.collect.Multimap;
import com.google.common.collect.Ordering;
import com.google.common.collect.TreeMultimap;

public class WarReportTask implements Callable<Void> {

	private static final Logger logger = Logger.getLogger(WarReportTask.class);

	public final DateTimeFormatter reportDateTimeFormatter = DateTimeFormat.forPattern("MM/dd HH:mm:ss").withZone(DateTimeZone.forID("Asia/Tokyo"));

	public static enum ReportType {
		Attack,
		Defense
	}

	private final long worldId;
	private final MasterContext masterContext = new MasterContext();
	private final ThreadLocal<Context> context = new ThreadLocal<Context>() {
		@Override 
		protected Context initialValue() {
			return masterContext.newContext(worldId);
		}
	};
	private final ExecutorService executor = Executors.newFixedThreadPool(10);

	private final Set<Lord> lordNoActivities = new TreeSet<Lord>(
			new Comparator<Lord>() {
				@Override
				public int compare(Lord o1, Lord o2) {
					if (o1.getTotalPopulation() < o2.getTotalPopulation()) {
						return -1;
					} else if (o1.getTotalPopulation() < o2.getTotalPopulation()) {
						return 1;
					} else {
						return 0;
					}
				}
			});

	private final Multimap<Integer, Lord> lordInactivities = TreeMultimap.create(
			Ordering.natural(), new Comparator<Lord>() {
				@Override
				public int compare(Lord o1, Lord o2) {
					if (o1.getTotalPopulation() < o2.getTotalPopulation()) {
						return 1;
					} else if (o1.getTotalPopulation() > o2.getTotalPopulation()) {
						return -1;
					} else {
						return o1.getName().compareTo(o2.getName());
					}
				}
			});

	public WarReportTask(Long worldId) {
		this.worldId = worldId;
	}

	public static final class ConsoleArgs {
		@Option(name="-do", required=true)
		String action;
		@Option(name="-c", required=true)
		String countryId;
	}

	@Override
	public Void call() throws Exception {
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		while (true) {
			logger.info("Accepting command");
			String[] input = reader.readLine().split(" ");
			ConsoleArgs args = new ConsoleArgs();
			CmdLineParser parser = new CmdLineParser(args);
			parser.parseArgument(input);
			if (args.action.contains("init")) {
				generateInitialReport(args.countryId);
			}

			for (Integer elapse : lordInactivities.keySet()) {
				logger.info(elapse + lordInactivities.get(elapse).size());
			}

			logger.info(lordNoActivities.size());
		}
	}

	private void generateInitialReport(String countryId) {
		logger.info("generating initial report");

		DateTime now = new DateTime(DateTimeZone.forID("Asia/Tokyo"));
		logger.info("Report time: " + reportDateTimeFormatter.print(now));

		HtmlPage rankingPage = (HtmlPage) context.get().getPage(context.get().getBaseUrl()
				+ String.format("/war/war_ranking.php?m=&c=%s", countryId));

		HtmlAnchor lastPageAnchor = (HtmlAnchor) rankingPage.getFirstByXPath("//a[@title='last page']");
		int pageCount = Integer.parseInt(lastPageAnchor.getHrefAttribute().split("p=")[1]);

		for (int p = 1; p <= pageCount; p++) {
			rankingPage = (HtmlPage) context.get().getPage(context.get().getBaseUrl() + 
					String.format("/war/war_ranking.php?m=&c=%s&p=%d", countryId, p));

			List<?> anchors = rankingPage.getByXPath("//table[@class='ig_battle_table']//a[contains(@href,'user')]");
			for (Object o: anchors) {
				String lordName = ((HtmlAnchor) o).asText();
				String lordUrl = ((HtmlAnchor) o).getHrefAttribute();

				HtmlPage lordPage = (HtmlPage) context.get().getPage(context.get().getBaseUrl()
						+ lordUrl);

				HtmlParagraph pop = (HtmlParagraph) lordPage.getFirstByXPath("//div[@class='pro3']//p[@class='para']");
				if (pop == null) {
					continue;
				} 

				HtmlSpan popRank = (HtmlSpan) lordPage.getFirstByXPath("//div[@class='pro3']//p[@class='para']//span");
				String popString = pop.asText().replace(popRank.asText(), "");
				logger.info("pop" + pop.asText().replace(popRank.asText(), ""));

				HtmlPage lordReportsPage;
				lordReportsPage = (HtmlPage) context.get().getPage(context.get().getBaseUrl()
						+ String.format("/war/list.php?name=lord&word=%s", lordName));


				HtmlTable table = (HtmlTable) lordReportsPage.getFirstByXPath("//table[@class='ig_battle_table']");
				for (int i = 1; i < table.getRowCount(); i++) {
					HtmlTableRow row = table.getRow(i);
					if (row.getCells().size() <3) {
						logger.debug("no activity:" + lordName);

						Lord lord = new Lord(lordName, "", Long.parseLong(popString), new ArrayList<Village>(0));
						lordNoActivities.add(lord);
						break;
					} else {
						String imgSrc = row.getCell(0).getElementsByTagName("img").iterator().next().getAttribute("src");
						ReportType type = imgSrc.contains("icon_battle") ? ReportType.Attack : ReportType.Defense;

						logger.info(lordName + "" + type);
						if (type == ReportType.Defense) {
							boolean fallen = row.getCell(1).getElementsByTagName("div").iterator().next().getElementsByTagName("img").size() > 0 ? true : false;
							String description = row.getCell(2).asText();
							DateTime eventTime = reportDateTimeFormatter.parseDateTime(row.getCell(2).asText()).withYear(now.getYear());
							Interval period = new Interval(eventTime, now);

							Lord lord = new Lord(lordName, "", Long.parseLong(popString), new ArrayList<Village>(0));

							int elapseHours = Hours.hoursIn(period).getHours();

							logger.info("put" + elapseHours + " " + lord);
							lordInactivities.put(elapseHours, lord);
							break;
						}
					}
				}	
			}
		} 
	}
	
	private final class ReportLordTask implements Callable<Boolean> {

		@Override
		public Boolean call() throws Exception {
			// TODO Auto-generated method stub
			return null;
		}
		
	}

}
