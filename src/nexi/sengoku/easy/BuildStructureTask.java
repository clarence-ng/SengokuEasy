package nexi.sengoku.easy;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Logger;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.html.HtmlArea;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSpan;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multiset;
import com.google.common.collect.TreeMultimap;
import com.google.common.collect.TreeMultiset;

public class BuildStructureTask extends AbstractTask<Boolean> {

	private static final Logger logger = Logger.getLogger(BuildStructureTask.class);
	private final Pattern pattern = Pattern.compile("(.*?)LV.(1??[0-9])");

	private final List<String> villageIds = new ArrayList<String>();
	private final Set<Structure> woodS = new TreeSet<Structure>();
	private final Set<Structure> cottonS = new TreeSet<Structure>();
	private final Set<Structure> ironS = new TreeSet<Structure>();
	private final Set<Structure> wheatS = new TreeSet<Structure>();

	private final TreeSet<Structure> storageS = new TreeSet<Structure>();

	private Resources resource = null;
	private Production production = null;

	public BuildStructureTask(Context context) {
		super(context);
		this.villageIds.addAll(PropertyHelper.getPropertyAsList("villageIds", context.properties));
	}

	public BuildStructureTask(Context context, List<String> villageIds) {
		super(context);
		this.villageIds.addAll(villageIds);
	}

	private static class Structure implements Comparable<Structure> {
		public final long typeId;
		public final String type;
		public final String x;
		public final String y;
		public final String villageId;
		public final long lvl;

		public Structure(long typeId, String type, String x, String y, String villageId, long lvl) {
			this.typeId = typeId;
			this.type = type;
			this.x = x;
			this.y = y;
			this.villageId = villageId;
			this.lvl = lvl;
		}

		@Override
		public int compareTo(Structure o) {
			if (this.lvl > o.lvl) {
				return 1;
			} else if (this.lvl < o.lvl) {
				return -1;
			} else {
				if (this.typeId > o.typeId) {
					return 1;
				} else if (this.typeId < o.typeId) {
					return -1;
				} else {
					return 0;
				}
			}
		}

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("Structure [villageId=");
			builder.append(villageId);
			builder.append(", type=");
			builder.append(type);
			builder.append(", lvl=");
			builder.append(lvl);
			builder.append(", x=");
			builder.append(x);
			builder.append(", y=");
			builder.append(y);
			builder.append("]");
			return builder.toString();
		}	
	}

	@Override
	public Boolean call() throws Exception {
		try {
			for (String villageId: villageIds) {
				woodS.clear();
				cottonS.clear();
				ironS.clear();
				wheatS.clear();

				if (villageId.equals("791953")) {
					processStructures(villageId);
					Structure chosen = getStructureWithLowestLevel();
					if (chosen.lvl < 9) {
						buildChosen(chosen);
					}
				} else if (villageId.equals("657120") 
						|| villageId.equals("657898") 
						|| villageId.equals("161615") 
						|| villageId.equals("490195") 
						|| villageId.equals("317933")) {
					processStructures(villageId);
					Structure chosen = getStructureWithLowestLevel();
					if (chosen.lvl < 10) {
						buildChosen(chosen);
					}
				} else if (villageId.equals("18190")) {
					processStructures(villageId);

					Structure storage = storageS.first();
					if (storage.lvl < 10) {
						buildChosen(storage);
					} 
				}
			}

			//			for (String villageId: villageIds) {
			//				if (!villageId.equals("27522") && !villageId.equals("18190")) {
			//					processStructures(villageId);
			//				}
			//			}
			//
			//			Structure chosen = getStructureWithLowestLevel();
			//
			//			if (chosen.lvl > 7  ) {
			//				return true;
			//			} else {
			//				buildChosen(chosen);
			//			}

		} catch (Exception e) {
			logger.error(e);
			new Mailman(context.properties).tryDeliver("Failed to upgrade building.\n" + ExceptionUtils.getFullStackTrace(e));
		} 
		return true;
	}

	private void buildChosen(Structure chosen) throws MalformedURLException,
	IOException {
		HtmlPage page = context.getPage(context.getBaseUrl()
				+ String.format("/village_change.php?village_id=%s", chosen.villageId));

		List<?> inProg = page.getByXPath("//span[@class='buildStatus']");
		if (inProg.size() > 0) {
			if (inProg.size() == 1 && ((HtmlSpan) inProg.get(0)).getTextContent().contains("研究")) {
				//just research. keep going
			} else {
				StringBuilder sb = new StringBuilder();
				for (Object o: inProg) {
					sb.append(((HtmlSpan) o).getTextContent());
				}
				logger.info("Already building something at village " + chosen.villageId + ", no-op. Building:" + sb.toString());
				return;
			}
		} 

		context.getPage(context.getBaseUrl()
				+ String.format("/facility/build.php?x=%s&y=%s&vid=%s", chosen.x, chosen.y, chosen.villageId));

		page = context.getPage(context.getBaseUrl()
				+ String.format("/village_change.php?village_id=%s", chosen.villageId));
		inProg = page.getByXPath("//span[@class='buildStatus']");
		if (inProg.size() <= 0) {
			logger.info("Did not build chosen: " + chosen);
		} 

		StringBuilder sb = new StringBuilder();
		sb.append("Upgrading " + chosen.type + " " + chosen.lvl +"\n");
		sb.append("Resources " + resource +"\n");
		sb.append("Production " + production +"\n");

		//		new Mailman(context.properties).tryDeliver("Building structure.\n" + sb.toString());
	}

	private void processStructures(String villageId) throws FailingHttpStatusCodeException, MalformedURLException, IOException {
		HtmlPage page = (HtmlPage) context.getPage(context.getBaseUrl()
				+ String.format("/village_change.php?village_id=%s", villageId));

		if (resource == null) {
			resource = new Resources(page);
		}
		if (production == null) {
			production = new Production(page);
		}

		List<?> areas = page.getByXPath("//map[@name='mapOverlayMap']//area[contains(@href,'facility')]");
		for (Object o : areas) {
			HtmlArea area = (HtmlArea) o;

			String href = area.getHrefAttribute();

			Matcher m = pattern.matcher(area.getAltAttribute());

			if (m.matches()) {
				String[] params = URLDecoder.decode(href).split("\\?")[1].split("&");
				String x = params[0].split("=")[1].trim();
				String y = params[1].split("=")[1].trim();


				String levelText = m.group(2).trim();
				long lvl = Long.parseLong(levelText);

				String structureName = m.group(1).trim();
				
				if (structureName.equals("木工所")) {
					Structure struct = new Structure(0L, structureName, x, y, villageId, lvl);
					woodS.add(struct);
				} else if (structureName.equals("機織り場")) {
					Structure struct = new Structure(2L, structureName, x, y, villageId, lvl);
					cottonS.add(struct);
				} else if (structureName.equals("たたら場")) {
					Structure struct = new Structure(1L, structureName, x, y, villageId, lvl);
					ironS.add(struct);
				} else if (structureName.equals("水田")) {
					Structure struct = new Structure(3L, structureName, x, y, villageId, lvl);
					wheatS.add(struct);
				} else if (structureName.equals("蔵")) {
					Structure struct = new Structure(4L, structureName, x, y, villageId, lvl);
					storageS.add(struct);
				}

			}
		}
	}

	//Choose lowest building level
	private Structure getStructureWithLowestLevel() {
		logger.info("choosing structure with lowerst lvl");
		TreeSet<Structure> allS = new TreeSet<Structure>();

		allS.addAll(woodS);
		allS.addAll(cottonS);
		allS.addAll(ironS);
		allS.addAll(wheatS);

		return allS.first();
	}

	//choose lowerst resource
	private Structure getStructureWithLowestResource(Resources res) {
		logger.info("choosing structure with lowerst resources");
		if (res.wood <= res.cotton 
				&& res.wood <= res.iron
				&& res.wood <= res.wheat) {
			return woodS.iterator().next();
		} else if (res.cotton <= res.wood 
				&& res.cotton <= res.iron
				&& res.cotton <= res.wheat) {
			return cottonS.iterator().next();
		} else if (res.iron <= res.cotton 
				&& res.iron <= res.wood
				&& res.iron <= res.wheat) {
			return ironS.iterator().next();
		} else if (res.wheat <= res.cotton 
				&& res.wheat <= res.iron
				&& res.wheat <= res.wood) {
			return wheatS.iterator().next();
		} else {
			return null;
		}
	}

	private Structure getWheatWithLowestLevel() {
		return wheatS.iterator().next();
	}

	private Structure getStructureWithLowestProduction(Production prod) {
		logger.info("choosing structure with lowerst production");
		//choose lowest production
		if (prod.wood <= prod.cotton 
				&& prod.wood <= prod.iron
				&& prod.wood <= prod.wheat) {
			return woodS.iterator().next();
		} else if (prod.cotton <= prod.wood 
				&& prod.cotton <= prod.iron
				&& prod.cotton <= prod.wheat) {
			return cottonS.iterator().next();
		} else if (prod.iron <= prod.cotton 
				&& prod.iron <= prod.wood
				&& prod.iron <= prod.wheat) {
			return ironS.iterator().next();
		} else if (prod.wheat <= prod.cotton 
				&& prod.wheat <= prod.iron
				&& prod.wheat <= prod.wood) {
			return wheatS.iterator().next();
		} else {
			return null;
		}
	}

}
