package nexi.sengoku.easy;

/**
 * Object comparison only by name and ignores hp.
 * @author Nexi
 *
 */
public class General {
	public final String name;
	private long hp;
	
	public General(String name) {
		this.name = name;
	}

	public void setHp(long hp) {
		this.hp = hp;
	}

	public long getHp() {
		return hp;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
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
		General other = (General) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("General ");
		builder.append("[name=");
		builder.append(name);
		builder.append(", hp=");
		builder.append(hp);
		builder.append("]");
		return builder.toString();
	}
	
}
