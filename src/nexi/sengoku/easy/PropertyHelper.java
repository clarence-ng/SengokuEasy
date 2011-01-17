package nexi.sengoku.easy;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class PropertyHelper {

	public static final List<String> getPropertyAsList(String key, Properties properties) {
		List<String> values = new ArrayList<String>();
		
		String val = properties.get(key).toString();
		String[] vals = val.split(",");
		for (int i =0; i < vals.length; i++) {
			values.add(vals[i].trim());
		}
		
		return values;
	}
}
