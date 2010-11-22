package nexi.sengoku.easy;

import java.io.File;
import java.io.FileReader;
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
		Logger.getRootLogger().setLevel(Level.DEBUG);
		Logger.getLogger("com.gargoylesoftware.htmlunit").setLevel(Level.ERROR);
		Logger.getLogger("org.apache.http").setLevel(Level.ERROR);
		Logger.getLogger(World.class).setLevel(Level.DEBUG);
		Logger.getLogger(DoMissionTask.class).setLevel(Level.DEBUG);

		Properties properties = new Properties();
		properties.load(new FileReader(new File("sengoku.properties")));
		
		String baseUrl = null;
		if (properties.containsKey("baseUrl")) {
			logger.info("using baseUrl from properties " + properties.getProperty("baseUrl").trim());
			baseUrl = properties.getProperty("baseUrl").trim();
		}
		if (baseUrl == null) {
			logger.info("logging in to yahoo");
			HtmlPage page = new Auth(properties).loginToYahooWithRetry();
			logger.info("logged in to yahoo");

			logger.info("logging in to world server");
			HtmlPage worldsPage = page.getAnchorByText("ゲームスタート").click();
			baseUrl = worldsPage.getUrl().toString();
			logger.info("logged in to world server. Base Url " + baseUrl);
		}

		World loginWorld = new World(
				Integer.parseInt(properties.getProperty("loginWorld")),
				baseUrl
		);		
		loginWorld.load();
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
