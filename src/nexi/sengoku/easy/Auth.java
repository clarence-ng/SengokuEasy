package nexi.sengoku.easy;

import java.io.IOException;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

public class Auth {

	private static final Logger logger = Logger.getLogger(Auth.class);

	private final Properties properties;
	private final WebClient webClient;

	public Auth(Properties properties, WebClient webClient) {
		this.properties = properties;
		this.webClient = webClient;
	}

	/**
	 * Return the page after logging in from Yahoo.
	 * @return
	 * @throws IOException
	 */
	private final HtmlPage loginToYahoo() throws IOException {
		logger.debug("loginToYahoo");
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

	private final HtmlPage loginToYahooWithRetry() {
		logger.debug("loginToYahooWithRetry");
		for (;;) {
			try {
				HtmlPage page = loginToYahoo();
				if (loggedInToYahooAndAtSengokuPage(page)) {
					return page;
				} 
			} catch(Exception e) {
				logger.info("Got excpetion when logging in from yahoo. Sleeping 5 minutes.", e);
				try {
					Thread.sleep(60000 * 5);
				} catch (InterruptedException e1) {
				}
			}
		}
	}

	private HtmlPage loginToWorldSelect(HtmlPage sourcePage) throws Exception {
		logger.debug("loginToWorldSelect");
		HtmlAnchor loginButton = sourcePage.getAnchorByText("ゲームスタート");
		return loginButton.click();
	}

	private final HtmlPage getSengokuMainPage() {
		HtmlPage sourcePage;
		for (;;) {
			try {
				sourcePage = webClient.getPage("http://sengokuixa.jp");
				return sourcePage;
			} catch (Exception e) {
				logger.info("error loading main sengoku page. Sleeping 1 minute.", e);
				try {
					Thread.sleep(1000L);
				} catch (InterruptedException ie) {
				}
			}
		}
	}
	/**
	 * Returns page after logging in from Sengoku main.
	 * @return
	 */
	public final HtmlPage loginToWorldSelectWithRetry()  {
		logger.debug("loginToWorldSelectWithRetry");

		for (;;) {
			try {
				HtmlPage sourcePage = getSengokuMainPage();

				List<?> elements = sourcePage.getByXPath("//a[contains(@href,'OAuth')]");
				if (elements.size() > 0) {
					logger.info("logging in to yahoo");
					sourcePage = loginToYahooWithRetry();
				} 

				elements = sourcePage.getByXPath("//a[contains(@title,'ゲームスタート')]");
				if (elements.size() > 0) {
					logger.info("logged in to yahoo. Logging in to world select.");
					HtmlPage page = loginToWorldSelect(sourcePage);
					if (atWorldSelectPage(page)) {
						return page;
					} else {
						logger.info("Not at world select page. Retrying after 30 seconds.");
						Thread.sleep(30000L);
					}
				} else {
					logger.info("Cannot find login button. Retrying after 30 seconds.");
					Thread.sleep(30000L);
				}
			} catch (Exception e) {
				logger.info("Got error. Retrying after 30 seconds..", e);
				try {
					Thread.sleep(30000L);
				} catch (InterruptedException e1) {
				}
			}
		}
	}

	private HtmlPage loginToWorld(HtmlPage sourcePage, long worldId) throws Exception {
		String pageUrl = String.format("%s&wd=w%03d", sourcePage.getUrl().toString(), worldId);
		logger.debug("loginToWorld " + pageUrl);
		return webClient.getPage(pageUrl);
	}

	public HtmlPage loginToWorldWithRetry(long worldId) {
		logger.debug("loginToWorldWithRetry");
		HtmlPage page = null;
		HtmlPage sourcePage = loginToWorldSelectWithRetry();

		for (;;) {
			try {
				page = loginToWorld(sourcePage, worldId);
				if (atWorldPage(page)) {
					logger.info("logged in to world " + worldId);
					return page;
				} else {
					sourcePage = loginToWorldSelectWithRetry();
				}
			} catch (Exception e) {
				sourcePage = loginToWorldSelectWithRetry();
				logger.info("Got excpetion when logging in from world selection. Sleeping 30 seconds.", e);
				try {
					Thread.sleep(30000L);
				} catch (InterruptedException e1) {
				}
			}	
		}
	}

	private boolean atWorldPage(HtmlPage page) {
		logger.debug("atWorldPage");
		boolean atWorld = ( page.getElementById("wood") != null);
		if (!atWorld && logger.isDebugEnabled()) {
			logger.debug("Not at world. got page\n" + page.asText());
		}
		return atWorld;
	}

	private boolean atWorldSelectPage(HtmlPage page) {
		logger.debug("atWorldSelectPage");
		try {
			if (!page.getTitleText().contains("ワールド選択")) {
				logger.info("Couldn't find ワールド選択  from page.");
				if (logger.isDebugEnabled()) {
					logger.debug("Got page\n" + page.asText());
				}
				return false;
			} else {
				return true;
			}
		} catch(Exception e) {
			logger.error("Unexpected error", e);
			return false;
		}
	}

	private boolean loggedInToYahooAndAtSengokuPage(HtmlPage page) {
		logger.debug("atSengokuPage");
		try {
			page.getAnchorByText("ゲームスタート");
			return true;
		} catch(Exception e) {
			logger.info("Couldn't find ゲームスタート from page.");
			if (logger.isDebugEnabled()) {
				logger.debug("Got page\n" + page.asText());
			}
			return false;
		}
	}

}
