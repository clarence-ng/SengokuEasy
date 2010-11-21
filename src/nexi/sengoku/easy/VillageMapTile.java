package nexi.sengoku.easy;

public class VillageMapTile {
	
	private Structure structure;
	private String onClickLink;
	
	public VillageMapTile (String structureText, String onClickLink) throws WeAreBrokenException{
		this.structure = new Structure (structureText);
		this.onClickLink = onClickLink;
	}

	public VillageMapTile (Structure structure, String onClickLink){
		this.structure = structure;
		this.onClickLink = onClickLink;
	}
	
	public String toString(){
		return structure.toString();
	}
	
	public Structure getStructure (){
		return this.structure;
	}
	
	public void setStructure (Structure structure){
		this.structure = structure;
	}
	
	public String getLink (){
		return this.onClickLink;
	}
	
	public void setLink (String link){
		this.onClickLink = link;
	}
	
}
