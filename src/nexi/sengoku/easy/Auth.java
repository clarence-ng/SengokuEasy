package nexi.sengoku.easy;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.DefaultCssErrorHandler;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.RefreshHandler;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlButton;
import com.gargoylesoftware.htmlunit.html.HtmlButtonInput;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlTable;
import com.gargoylesoftware.htmlunit.html.HtmlTableRow;

public class Auth {

	private static final Logger logger = Logger.getLogger(Auth.class);

	public final HtmlPage loginToYahoo() throws IOException {
		WebClient webClient = Client.newWebClient();
		// visit Yahoo Mail login page and get the Form object
		HtmlPage page = (HtmlPage) webClient.getPage("http://sengokuixa.jp/index.php?event=OAuth");

		logger.debug(page.asText());

		HtmlForm form = page.getFormByName("login_form");

		// Enter login and passwd
		form.getInputByName("login").setValueAttribute("vazjrsengoku");
		form.getInputByName("passwd").setValueAttribute("psistorm");

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
				success = true;
			} catch(IOException e) {
				logger.info("Failed to login to yahoo. Retrying", e);
			}
		}

		return page;
	}




}
