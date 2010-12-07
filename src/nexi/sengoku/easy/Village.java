package nexi.sengoku.easy;

import java.io.IOException;
import java.net.MalformedURLException;

import org.apache.log4j.Logger;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

public class Village {

	private static final Logger logger = Logger.getLogger(Village.class);

	private final Context context;
	private String villageName;
	private int villageId;
	private VillageMap villageMap;
	
	public Village(Context context, int villageId,  String villageName) {
		this.context = context;
		this.villageName = villageName;
		this.villageId = villageId;
	}

	public void setVillageMap(VillageMap villageMap){
		this.villageMap = villageMap;
	}
	
	public int getVillageId(){
		return villageId;
	}
	
	public String getVillageName(){
		return villageName;
	}
	
	public VillageMap getVillageMap(){
		return villageMap;
	}
	
	public void displayVillageMap(){
		villageMap.displayVillageMap();
	}
	
	public void load() throws FailingHttpStatusCodeException, MalformedURLException, IOException {	

	}

}
