package nexi.sengoku.easy;

import java.io.IOException;
import java.net.MalformedURLException;

import org.apache.log4j.Logger;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

public class Village {

	private static final Logger logger = Logger.getLogger(Village.class);

	private final Context context;
	
	public Village(Context context) {
		this.context = context;
	}

	public void load() throws FailingHttpStatusCodeException, MalformedURLException, IOException {	

	}

}
