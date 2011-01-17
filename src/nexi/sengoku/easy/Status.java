package nexi.sengoku.easy;

public class Status {
	public String team;
	public String status;
	public String countdown;
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((team == null) ? 0 : team.hashCode());
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
		Status other = (Status) obj;
		if (team == null) {
			if (other.team != null)
				return false;
		} else if (!team.equals(other.team))
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Status ");
		builder.append("[team=");
		builder.append(team);
		builder.append(", status=");
		builder.append(status);
		builder.append(", countdown=");
		builder.append(countdown);
		builder.append("]");
		return builder.toString();
	}

}
