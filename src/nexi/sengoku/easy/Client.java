package nexi.sengoku.easy;

import java.io.IOException;
import java.net.URL;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.CookieManager;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.RefreshHandler;
import com.gargoylesoftware.htmlunit.WebClient;

public class Client {

	public static final WebClient newWebClient() {
		// Create and initialize WebClient object
		WebClient webClient = new WebClient(BrowserVersion.FIREFOX_3_6);
		webClient.setThrowExceptionOnScriptError(false);
		webClient.setRefreshHandler(new RefreshHandler() {
			public void handleRefresh(Page page, URL url, int arg) throws IOException {
				System.out.println("handleRefresh");
			}
		});

		return webClient;
	}
	
	public static final WebClient newWebClient(CookieManager cookieManager) {
		// Create and initialize WebClient object
		WebClient webClient = newWebClient();
		webClient.setCookieManager(cookieManager);

		return webClient;
	}
}
