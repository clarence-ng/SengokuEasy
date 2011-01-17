package nexi.sengoku.easy;

import java.util.List;

public class Lord {

	private final String name;
	private final String alliance;
	private final long totalPopulation;
	private final List<Village> villages;

	public Lord(String name, String alliance, long totalPopulation,
			List<Village> villages) {
		this.name = name;
		this.alliance = alliance;
		this.totalPopulation = totalPopulation;
		this.villages = villages;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Lord [totalPopulation=");
		builder.append(totalPopulation);
		builder.append(", villages=");
		builder.append(villages);
		builder.append(", name=");
		builder.append(name);
		builder.append(", alliance=");
		builder.append(alliance);
		builder.append("]");
		return builder.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((getAlliance() == null) ? 0 : getAlliance().hashCode());
		result = prime * result + ((getName() == null) ? 0 : getName().hashCode());
		result = prime * result
				+ (int) (getTotalPopulation() ^ (getTotalPopulation() >>> 32));
		result = prime * result
				+ ((getVillages() == null) ? 0 : getVillages().hashCode());
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
		Lord other = (Lord) obj;
		if (getAlliance() == null) {
			if (other.getAlliance() != null)
				return false;
		} else if (!getAlliance().equals(other.getAlliance()))
			return false;
		if (getName() == null) {
			if (other.getName() != null)
				return false;
		} else if (!getName().equals(other.getName()))
			return false;
		if (getTotalPopulation() != other.getTotalPopulation())
			return false;
		if (getVillages() == null) {
			if (other.getVillages() != null)
				return false;
		} else if (!getVillages().equals(other.getVillages()))
			return false;
		return true;
	}

	public String getName() {
		return name;
	}

	public String getAlliance() {
		return alliance;
	}

	public long getTotalPopulation() {
		return totalPopulation;
	}

	public List<Village> getVillages() {
		return villages;
	}

}
