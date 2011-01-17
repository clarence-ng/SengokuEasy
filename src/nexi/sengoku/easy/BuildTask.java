package nexi.sengoku.easy;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.log4j.Logger;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.HttpMethod;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

public class BuildTask extends AbstractTask<Boolean> {

	private static final Logger logger = Logger.getLogger(BuildTask.class);

	public BuildTask(Context context) {
		super(context);
	}

	@Override
	public Boolean call() throws Exception {
		HtmlPage page;
		try {
			
			page = (HtmlPage) context.webClient.getPage("http://w015.sengokuixa.jp/village_change.php?village_id=161615&from=menu&page=village.php");
			logger.info(page.asText());
			
			WebRequest wr2 = new WebRequest(new URL("http://w015.sengokuixa.jp/facility/build.php?x=1&y=4&vid=161615"));
			wr2.setHttpMethod(HttpMethod.POST);
			HtmlPage p = (HtmlPage) context.webClient.getPage(wr2);
			logger.info(p.asText());
			
		} catch (FailingHttpStatusCodeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return true;
	}

}
