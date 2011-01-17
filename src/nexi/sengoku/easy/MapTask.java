package nexi.sengoku.easy;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlArea;
import com.gargoylesoftware.htmlunit.html.HtmlImage;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlParagraph;
import com.gargoylesoftware.htmlunit.html.HtmlSpan;
import com.google.common.collect.Ordering;
import com.google.common.collect.TreeMultimap;

public class MapTask extends AbstractTask<Boolean> {
	private static final Logger logger = Logger.getLogger(StatusTask.class);

	private static  final Pattern onMouseoverPattern = Pattern.compile(".*(\\('(.*)',\\s*'(.*)',\\s*'(.*)',\\s*'(.*)',\\s*'(.*)',\\s*'(.*)',\\s*'(.*)',\\s*'(.*)',\\s*'(.*)',\\s*'(.*)',\\s*'(.*)',\\s*'(.*)',\\s*'(.*)',\\s*'(.*)'\\)).*");
	private static  final Pattern coordinatePattern = Pattern.compile("\\((.*),(.*)\\)");

	private final List<Coordinates> coordinates = new ArrayList<Coordinates>();
	private final Coordinates mainCoordinates;
	private final long world;

	public MapTask(Context context, long world, List<Coordinates> coordinates, Coordinates mainCoordinates) {
		super(context);
		this.coordinates.addAll(coordinates);
		this.world = world;
		this.mainCoordinates = mainCoordinates;
	}

	@Override
	public Boolean call() throws Exception {
		Map<LandType, Map<Coordinates, UserLand>> enemies = new TreeMap<LandType, Map<Coordinates, UserLand>>();

		Map<Coordinates, UserLand> enemyCapitals = new TreeMap<Coordinates, UserLand>(new DistanceComparator());
		Map<Coordinates, UserLand> enemyCamps = new TreeMap<Coordinates, UserLand>(new DistanceComparator());
		Map<Coordinates, UserLand> enemyVillages = new TreeMap<Coordinates, UserLand>(new DistanceComparator());
		Map<Coordinates, UserLand> enemyForts = new TreeMap<Coordinates, UserLand>(new DistanceComparator());
		Map<Coordinates, UserLand> enemyStrongholds = new TreeMap<Coordinates, UserLand>(new DistanceComparator());
		Map<Coordinates, UserLand> unknowns = new TreeMap<Coordinates, UserLand>(new DistanceComparator());
		
		enemies.put(LandType.capital, enemyCapitals);
		enemies.put(LandType.camp, enemyCamps);
		enemies.put(LandType.village, enemyVillages);
		enemies.put(LandType.fort, enemyForts);
		enemies.put(LandType.stronghold, enemyStrongholds);
		enemies.put(LandType.unknown, unknowns);

		TreeMultimap<Long, Land> openLands = TreeMultimap.create(Ordering.natural(), new Comparator<Land>() {
			@Override
			public int compare(Land o1, Land o2) {
				double dO1 = Math.pow((o1.coordinates.x - mainCoordinates.x), 2) + Math.pow((o1.coordinates.y - mainCoordinates.y), 2);
				double dO2 = Math.pow((o2.coordinates.x - mainCoordinates.x), 2) + Math.pow((o2.coordinates.y - mainCoordinates.y), 2);
				return (int) (dO2 - dO1);
			}
		});

		for (Coordinates coord: coordinates) {
			try {
				HtmlPage page = (HtmlPage) context.getPage(context.getBaseUrl()
						+ String.format("/map.php?x=%d&y=%d&c=%d&type=3", coord.x, coord.y, world));

				//				logger.info(page.asXml());

				List<?> tileClasses = page.getByXPath("//div[@id='ig_mapsAll']//img");
				List<?> tileInfos = page.getByXPath("//map[@id='mapOverlayMap']//area");

				int i = 0;
				for (Object tileClass: tileClasses) {
					String[] split = ((HtmlImage) tileClass).getSrcAttribute().split("/");
					String[] split2 = split[split.length-1].split("_");
					String type = split2[0];
					if (type.trim().equals("field")) {
						Matcher m = onMouseoverPattern.matcher(((HtmlArea) tileInfos.get(i)).getAttribute("onmouseover"));
						m.matches();

						Matcher coordM = coordinatePattern.matcher(m.group(5));
						coordM.matches();
						Coordinates foundCoord = new Coordinates(Long.parseLong(coordM.group(1)), Long.parseLong(coordM.group(2)));

						int level = m.group(7).length();
						Land l = new Land(level, foundCoord);

						openLands.put((long) level, l);
					} else if (split2.length > 1 && split2[1].equals("r")){
						LandType landType = null;
						if (type.trim().equals("capital")) { 
							landType = LandType.capital;
						} else if (type.trim().equals("camp")) {
							landType = LandType.camp;
						} else if (type.trim().equals("fort")) {
							landType = LandType.fort;
						} else if (type.trim().equals("village")) {
							landType = LandType.village;
						} else if (type.trim().equals("stronghold")) {
							landType = LandType.stronghold;
						} else {
							landType = LandType.unknown;
						}

						Matcher m = onMouseoverPattern.matcher(((HtmlArea) tileInfos.get(i)).getAttribute("onmouseover"));
						m.matches();

						String owner = m.group(3);
						String alliance = m.group(6);
						Matcher coordM = coordinatePattern.matcher(m.group(5));
						coordM.matches();
						Coordinates foundCoord = new Coordinates(Long.parseLong(coordM.group(1)), Long.parseLong(coordM.group(2)));

						Long population = 0L;
						if (LandType.stronghold.equals(landType)) {
							HtmlPage landPage = (HtmlPage) context.getPage(context.getBaseUrl()
									+ String.format("/land.php?x=%d&y=%d&c=%d", foundCoord.x, foundCoord.y, world));
							HtmlAnchor userLink = (HtmlAnchor) landPage.getFirstByXPath("//div[@class='ig_mappanel_dataarea']//a");
							if (userLink != null) {
								HtmlPage userPage = userLink.click();
								HtmlParagraph p = (HtmlParagraph) userPage.getFirstByXPath("//div[@class='pro3']//p[@class='para']");
								HtmlSpan s = (HtmlSpan) userPage.getFirstByXPath("//div[@class='pro3']//p[@class='para']//span");
								population = Long.parseLong(p.getTextContent().split(s.getTextContent())[0]);
							}
						} else {
							population = m.group(4).contains("-")? 0 : Long.parseLong(m.group(4));
						}
						
						
						UserLand ul = new UserLand(owner, alliance, population, foundCoord, landType);
						enemies.get(landType).put(foundCoord, ul);
					} else {
					}
					i++;
				}
			} catch (Exception e) {
				logger.error("Error loading page for map task", e);
			}
		}

		logger.info(prettyPrint(enemies));
//		logger.info(prettyPrint(openLands));
		return true;
	}


	private final String prettyPrint(Map<LandType, Map<Coordinates, UserLand>> map) {
		StringBuilder sb = new StringBuilder();
		for (LandType type: map.keySet()) {
			sb.append(type +"\n");
			for (UserLand land : map.get(type).values()) {
				sb.append("\t" + land + "\n");
			}
		}
		return sb.toString();
	}

	private final String prettyPrint(TreeMultimap<Long, Land> map) {
		StringBuilder sb = new StringBuilder();
		for (Long lvl: map.asMap().keySet()) {
			sb.append(lvl +"\n");
			for (Land land : map.asMap().get(lvl)) {
				sb.append("\t" + land + "\n");
			}
		}
		return sb.toString();
	}

	private final class DistanceComparator implements Comparator<Coordinates> {
		@Override
		public int compare(Coordinates o1, Coordinates o2) {
			double dO1 = Math.pow((o1.x - mainCoordinates.x), 2) + Math.pow((o1.y - mainCoordinates.y), 2);
			double dO2 = Math.pow((o2.x - mainCoordinates.x), 2) + Math.pow((o2.y - mainCoordinates.y), 2);
			return (int) (dO2 - dO1);
		}
	}

	public final class Land {
		public final int level;
		public final Coordinates coordinates;

		public Land(int level, Coordinates coordinates) {
			this.level = level;
			this.coordinates = coordinates;
		}	

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("Land ");
			builder.append("[level=");
			builder.append(level);
			builder.append(", coordinates=");
			builder.append(coordinates);
			builder.append("]");
			return builder.toString();
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result
			+ ((coordinates == null) ? 0 : coordinates.hashCode());
			result = prime * result + level;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Land other = (Land) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (coordinates == null) {
				if (other.coordinates != null)
					return false;
			} else if (!coordinates.equals(other.coordinates))
				return false;
			if (level != other.level)
				return false;
			return true;
		}

		private MapTask getOuterType() {
			return MapTask.this;
		}

	}

	public final class UserLand {
		public final String owner;
		public final String alliance;
		public final long population;
		public final Coordinates coordinates;
		public final LandType type;

		public UserLand(String owner, String alliance, long population,
				Coordinates coordinates, LandType type) {
			super();
			this.owner = owner;
			this.alliance = alliance;
			this.population = population;
			this.coordinates = coordinates;
			this.type = type;
		}

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("UserLand [population=");
			builder.append(population);
			builder.append(", type=");
			builder.append(type);
			builder.append(", owner=");
			builder.append(owner);
			builder.append(", alliance=");
			builder.append(alliance);
			builder.append(", coordinates=");
			builder.append(coordinates);
			builder.append("]");
			return builder.toString();
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result
			+ ((alliance == null) ? 0 : alliance.hashCode());
			result = prime * result
			+ ((coordinates == null) ? 0 : coordinates.hashCode());
			result = prime * result + ((owner == null) ? 0 : owner.hashCode());
			result = prime * result + (int) (population ^ (population >>> 32));
			result = prime * result + ((type == null) ? 0 : type.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			UserLand other = (UserLand) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (alliance == null) {
				if (other.alliance != null)
					return false;
			} else if (!alliance.equals(other.alliance))
				return false;
			if (coordinates == null) {
				if (other.coordinates != null)
					return false;
			} else if (!coordinates.equals(other.coordinates))
				return false;
			if (owner == null) {
				if (other.owner != null)
					return false;
			} else if (!owner.equals(other.owner))
				return false;
			if (population != other.population)
				return false;
			if (type == null) {
				if (other.type != null)
					return false;
			} else if (!type.equals(other.type))
				return false;
			return true;
		}

		private MapTask getOuterType() {
			return MapTask.this;
		}

	}

	public enum LandType {
		field,
		fort,
		capital,
		village,
		camp,
		stronghold, 
		unknown
	}

}
