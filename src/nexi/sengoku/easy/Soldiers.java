package nexi.sengoku.easy;

public enum Soldiers {
	LIGHT_INFANTRY(11,11,15,2),
	POLEARM_INFANTRY(16,16,16,2),
	LIGHT_ARCHER(10,12,16,1),
	LONG_ARCHER(15,17,18,1),
	LIGHT_CALVARY(10,10,22,1),
	ELITE_CALVARY(17,15,23,1),
	HAMMER(3,8,8,10),
	TOWER(14,5,10,7),
	RIFLEMAN(18,26,15,1);
	
	private final int atk;
	private final int def;
	private final int ms;
	private final int bDmg;
	
	// TODO: Add resource cost
	// TODO: Base build duration
	
	Soldiers (int atk, int def, int ms, int bDmg){
		this.atk = atk;
		this.def = def;
		this.ms = ms;
		this.bDmg = bDmg;
	}

	public int getAttackPoints(){
		return this.atk;
	}

	public int getDefencePoints(){
		return this.def;
	}

	public int getMovespeed(){
		return this.ms;
	}

	public int getBuildingDamage(){
		return this.bDmg;
	}
}
