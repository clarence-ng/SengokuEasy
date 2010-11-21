package nexi.sengoku.easy;

public enum Mission {
	Valley(0),
	SeaOfForest(1),
	CliffTemple(2),
	SpringOfLongevity(3);
	
	public final int missionId;
	
	private Mission(int missionId) {
		this.missionId = missionId;
	}
}
