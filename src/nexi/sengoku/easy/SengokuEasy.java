package nexi.sengoku.easy;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.gargoylesoftware.htmlunit.ElementNotFoundException;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.HttpMethod;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebRequestSettings;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.util.NameValuePair;

public class SengokuEasy {

	private static final Logger logger = Logger.getLogger(SengokuEasy.class);
	
	public static final String propertiesFilePath = "sengoku.properties";
	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main (String... args) throws Exception {

		BasicConfigurator.configure();
		Logger.getRootLogger().setLevel(Level.INFO);
		Logger.getLogger("com.gargoylesoftware.htmlunit").setLevel(Level.ERROR);
		Logger.getLogger("org.apache.http").setLevel(Level.ERROR);
		
		new SengokuEasy().run(args);
	}

	public void run(String... args) throws FileNotFoundException, IOException, ElementNotFoundException, FailingHttpStatusCodeException, WeAreBrokenException {
		File propertiesFile = new File(propertiesFilePath);
		Properties properties = new Properties();
		properties.load(new FileReader(propertiesFile));

		Auth auth = new Auth(properties);
		WebClient webClient = Client.newWebClient();

		World loginWorld = World.loadNewWorld(auth, webClient, properties);

		ExecutorService taskExecutor = Executors.newCachedThreadPool();

		Context context = new Context(loginWorld, webClient, properties);

		DoMissionTask doMission = new DoMissionTask(context, 18190, Mission.SeaOfForest);
		taskExecutor.submit(doMission);
	}
	
	public static void debug(HtmlElement element, int tabs, Logger logger) {
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < tabs; i++) {
			builder.append("\t"); 
		}
		logger.debug(builder.toString() + "id:" + element.getId() 
				+ " type " + element.getClass().getSimpleName() 
				+ " class " + element.getAttribute("class"));
		for (HtmlElement child : element.getChildElements()) {	
			debug(child, tabs+1, logger);
		}
	}
}
