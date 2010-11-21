package nexi.sengoku.easy;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

public class SengokuEasy {

	private static final Logger logger = Logger.getLogger(SengokuEasy.class);

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main (String... args) throws Exception {

		BasicConfigurator.configure();
		Logger.getRootLogger().setLevel(Level.INFO);
		Logger.getLogger("com.gargoylesoftware.htmlunit").setLevel(Level.ERROR);
		Logger.getLogger("org.apache.http").setLevel(Level.ERROR);
		Logger.getLogger(World.class).setLevel(Level.DEBUG);
		Logger.getLogger(DoMissionTask.class).setLevel(Level.DEBUG);

		Properties properties = new Properties();
		properties.load(new FileReader(new File("sengoku.properties")));

		String baseUrl = "http://world.sengokuixa.jp/world/select_world.php?p=6%2B2HYllM1poiwqM76me%2BLrnOvbWE%2B3eiecNWXtwLh5ly0mIYxpKn92YeTWybhr7ewuB5dzkvi0Z5ikUIqtf0u3l%2BSeze35Hf6hLbYzo47qEUq9EQcWjnwRmy4ZXzYeHcMk4NlGIwCLg%3D&cd=e524583492838ed12c5c4b176b2b8c61&ts=1290383469&ch=YGID_";

		if (baseUrl == null) {
			logger.info("logging in to yahoo");
			HtmlPage page = new Auth(properties).loginToYahooWithRetry();
			logger.info("logged in to yahoo" + page.asText());

			logger.info("logging in to world server");
			HtmlPage worldsPage = page.getAnchorByText("ゲームスタート").click();
			baseUrl = worldsPage.getUrl().toString();
			logger.info("logged in to world server. Base Url:" + baseUrl);
		}

		World world9 = new World(9, baseUrl);
		world9.load();
	}

	public static void debug(HtmlElement element, int tabs, Logger logger) {
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < tabs; i++) {
			builder.append("\t"); 
		}
		logger.debug(builder.toString() + "e:" + element.getId() 
				+ " type " + element.getClass().getSimpleName() 
				+ " class " + element.getAttribute("class"));
		for (HtmlElement child : element.getChildElements()) {	
			debug(child, tabs+1, logger);
		}
	}
}
