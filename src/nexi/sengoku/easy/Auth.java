package nexi.sengoku.easy;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.gargoylesoftware.htmlunit.ElementNotFoundException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

public class Auth {

	private static final Logger logger = Logger.getLogger(Auth.class);

	private final Properties properties;

	public Auth(Properties properties) {
		this.properties = properties;
	}

	public final HtmlPage loginToYahoo() throws IOException {
		WebClient webClient = Client.newWebClient();
		HtmlPage page = (HtmlPage) webClient.getPage("http://sengokuixa.jp/index.php?event=OAuth");

		logger.debug(page.asText());

		HtmlForm form = page.getFormByName("login_form");

		// Enter login and passwd
		form.getInputByName("login").setValueAttribute(properties.getProperty("yahooLogin").trim());
		form.getInputByName("passwd").setValueAttribute(properties.getProperty("yahooPassword").trim());

		// Click "Sign In" button/link
		HtmlInput b = (HtmlInput) page.getElementById(".save");

		page = (HtmlPage) b.click();

		return page;	
	}

	public final HtmlPage loginToYahooWithRetry() {
		HtmlPage page = null;
		boolean success = false;

		while (!success) {
			try {
				page = loginToYahoo();
				page.getAnchorByText("ゲームスタート");
				success = true;
			} catch(Exception e) {
				logger.warn("Failed to login to yahoo. Retrying", e);
			}
		}

		return page;
	}

	private static HtmlPage loginToYahoo(Properties properties) {
		logger.info("logging in to yahoo");
		HtmlPage sengokuPage = new Auth(properties).loginToYahooWithRetry();
		logger.info("logged in to yahoo");
		return sengokuPage;
	}

	public HtmlPage loginToSengoku(HtmlPage sengokuPage) throws ElementNotFoundException, IOException {
		logger.info("logging in to world server");
		HtmlPage worldsPage = sengokuPage.getAnchorByText("ゲームスタート").click();
		logger.info("logged in to world server");
		return worldsPage;
	}
	
	public String login() throws ElementNotFoundException, IOException {
		String baseUrl = null;
		if (properties.containsKey("baseUrl")) {
			logger.info("using baseUrl from properties " + properties.getProperty("baseUrl").trim());
			baseUrl = properties.getProperty("baseUrl").trim();
		} else {
			baseUrl = loginAndSaveSession();
		}
		return baseUrl;
	} 
	
	public String loginAndSaveSession() throws ElementNotFoundException, IOException {
		HtmlPage sengokuPage = loginToYahoo(properties);
		HtmlPage worldsPage = loginToSengoku(sengokuPage);
		
		String baseUrl = worldsPage.getUrl().toString();
		properties.setProperty("baseUrl", baseUrl);
		FileWriter writer = new FileWriter(SengokuEasy.propertiesFilePath);
		properties.store(writer, "SengokuEasy");
		
		return baseUrl;
	}

}
