package nexi.sengoku.easy;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class UrlHelper {

	public static Map<String,String> getQueryParameters (URL url) {
		Map<String,String> queryParams = new HashMap<String, String>();
		
		String query =url.getQuery();
		String[] queryElements = query.split("&");
		for (int i = 0; i < queryElements.length; i++) {
			String[] queryElement = queryElements[i].split("=");
			queryParams.put(queryElement[0], queryElement[1]);
		}
		
		return queryParams;
	}
	
}
