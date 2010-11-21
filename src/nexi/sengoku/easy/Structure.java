package nexi.sengoku.easy;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

public class Structure {
	private static final Logger logger = Logger.getLogger(World.class);
	private final StructureType structureType;
	private int level = -1;
	private static Map <String, StructureType> structureNameToTypeMap 
		= new HashMap <String, StructureType> ();
	
	static {
		// Home bases
		structureNameToTypeMap.put("本丸", StructureType.HOME_BASE);
		structureNameToTypeMap.put("砦", StructureType.SUB_BASE);
		// TODO: VILLAGE
		
		// Utility/Research structures
		structureNameToTypeMap.put("学舎", StructureType.UNIVERSITY);
		structureNameToTypeMap.put("市", StructureType.MARKET);
		// TODO: TEMPLE, CHURCH
		
		// Production structures
		structureNameToTypeMap.put("木工所", StructureType.WOOD_FACTORY);
		structureNameToTypeMap.put("たたら場", StructureType.IRON_FACTORY);
		structureNameToTypeMap.put("機織り場", StructureType.COTTON_FACTORY);
		structureNameToTypeMap.put("水田", StructureType.WATER_FIELD);
		
		// Storage structures
		structureNameToTypeMap.put("蔵", StructureType.STORAGE);
		structureNameToTypeMap.put("長屋", StructureType.SOLDER_STORAGE);
		
		// Unit training structures
		structureNameToTypeMap.put("足軽兵舎", StructureType.INFANTRY_RAX);
		structureNameToTypeMap.put("弓兵舎", StructureType.ARCHER_RAX);
		structureNameToTypeMap.put("厩舎", StructureType.CALVARY_RAX);
		structureNameToTypeMap.put("兵器鍛冶", StructureType.WEAPON_FACTORY);
		
		// Un-buildable structures
		structureNameToTypeMap.put("畑", StructureType.DRY_FIELD);
		structureNameToTypeMap.put("森林", StructureType.WOOD);
		structureNameToTypeMap.put("綿花", StructureType.COTTON);
		structureNameToTypeMap.put("鉄鉱山", StructureType.IRON);
		structureNameToTypeMap.put("荒地", StructureType.BARREN_LAND);
		structureNameToTypeMap.put("平地", StructureType.EMPTY_FIELD);
	}
	
	public Structure (String structureText) throws WeAreBrokenException {
		
		// Structure text should be in this format - 木工所 LV.5
		// First split the structure name and the level
		
		Pattern p = Pattern.compile("(.*?)LV.([0-9])");  // Is there lv 10?
		Matcher m = p.matcher(structureText);
		
		if(m.matches()){
			String structureName = "";
			String levelText = "";
			
			structureName = m.group(1).trim();
			levelText = m.group(2).trim();
			this.level = Integer.parseInt(levelText);
			
			if (structureNameToTypeMap.containsKey(structureName)){
				this.structureType = structureNameToTypeMap.get(structureName);
			}
			else {
				logger.error("Failed parsing structure text. " +
				"StructureName = " + structureName + " and Level = " + levelText);
				throw new WeAreBrokenException();
			}
		}
		else {
			// May belong to structures that doesn't have a level, say 荒地
			if (structureNameToTypeMap.containsKey(structureText)){
				this.structureType = structureNameToTypeMap.get(structureText);
			}
			else {
				logger.error("Unknown structure name " + structureText);
				throw new WeAreBrokenException();
			}
		}
	}
	
	public String toString(){
		if (level != -1){
			return structureType.toString() + "_LV." + level;
		}
		else {
			return structureType.toString();
		}
	}

	public StructureType getStructureType(){
		return this.structureType;
	}
	
	public int getLevel(){
		return this.level;
	}
	
	public void setLevel(int level){
		this.level = level;
	}
	
}
