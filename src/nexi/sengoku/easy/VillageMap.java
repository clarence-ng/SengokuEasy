package nexi.sengoku.easy;
import java.util.Formatter;
import java.util.Locale;

import org.apache.log4j.Logger;

import com.gargoylesoftware.htmlunit.html.HtmlArea;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlMap;

public class VillageMap {
	private static final Logger logger = Logger.getLogger(VillageMap.class);
	
	// All maps are 8 by 8, unusable blocks padded with barren land
	private static final int X_SIZE = 8;
	private static final int Y_SIZE = 8;
	private static VillageMapTile [][] tiles = new VillageMapTile [X_SIZE][Y_SIZE];
	
	private VillageMap (){};
	
	public static VillageMap createVillageMapFromHtmlMapElement (HtmlMap mapElement) throws WeAreBrokenException{
		VillageMap instance = new VillageMap();
		Iterable <HtmlElement> childElements = mapElement.getChildElements();

		int curr_x_index = X_SIZE - 1;
		int curr_y_index = Y_SIZE - 1;
		
		for (HtmlElement element : childElements){
			HtmlArea areaElement = (HtmlArea) element;
			String onClickLink = areaElement.getHrefAttribute();
			String structureName = areaElement.getAltAttribute();			
			tiles[curr_x_index][curr_y_index] = new VillageMapTile (structureName,onClickLink);

			logger.debug ("X,Y = " + curr_x_index + "," + curr_y_index + " " + structureName);
			curr_y_index --;
			if (curr_y_index < 0){
				curr_x_index --;
				if (curr_x_index != -1 ) {
					curr_y_index = Y_SIZE - 1;
				}
			}
		}
		
		if (!(curr_x_index == -1 && curr_y_index == -1)){
			// Either the UI changed or we are broken
			logger.error("Size of village map  doesn't match our " +
					"expected dimensions of [" + X_SIZE + "X" + Y_SIZE + "]");
			throw new WeAreBrokenException();
		}
		
		return instance;
	}
	
	public void displayVillageMap (){       
		
		StringBuilder sb = new StringBuilder();
	    Formatter formatter = new Formatter(sb, Locale.JAPANESE);

		formatter.format("\n" +
				"%2s%20s%20s%20s%20s%20s%20s%20s%20s",
				" ","0","1","2","3","4","5","6","7"
			);
		for (int x = 0; x < X_SIZE; x ++){
			formatter.format("\n" +
					"%2s%20s%20s%20s%20s%20s%20s%20s",
					x,
					tiles[0][x].toString(),
     				tiles[1][x].toString(),
					tiles[2][x].toString(),
					tiles[3][x].toString(),
					tiles[4][x].toString(),
					tiles[5][x].toString(),
					tiles[6][x].toString(),
					tiles[7][x].toString()
			);
		}
		logger.info(formatter.toString());
	}
	
	public VillageMapTile getTile (int x, int y){
		return tiles [x][y];
	}
}
