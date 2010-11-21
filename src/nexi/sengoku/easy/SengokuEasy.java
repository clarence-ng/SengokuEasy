package nexi.sengoku.easy;

import java.io.File;
import java.io.FileReader;
import java.util.Properties;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.w3c.dom.NodeList;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlArea;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlMap;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

public class SengokuEasy {

	private static final Logger logger = Logger.getLogger(SengokuEasy.class);

	public static void main (String... args) throws Exception {
		
		BasicConfigurator.configure();
		Logger.getRootLogger().setLevel(Level.DEBUG);
		Logger.getLogger("com.gargoylesoftware.htmlunit").setLevel(Level.ERROR);
		Logger.getLogger("org.apache.http").setLevel(Level.ERROR);
		Logger.getLogger("nexi.sengoku.easy.Auth").setLevel(Level.WARN);

		Properties properties = new Properties();
		properties.load(new FileReader(new File("sengoku.properties")));
		
		logger.info("logging in to yahoo");
		HtmlPage page = new Auth(properties).loginToYahooWithRetry();
		logger.info("logged in to yahoo");

		logger.info("logging in to world server");
		HtmlPage worldsPage = page.getAnchorByText("ゲームスタート").click();
		logger.info("logged in to world server");
		logger.info(worldsPage.getUrl().toString());
		
		World loginWorld = new World(
				Integer.parseInt((String)properties.get("loginWorld")),
				worldsPage.getUrl().toString()
			);		
		loginWorld.load();
		
	}
}
