package nexi.sengoku.easy;

import com.gargoylesoftware.htmlunit.WebClient;

public class Context {

	public final int world;
	public final WebClient webClient;
	
	public Context(int world, WebClient webClient) {
		this.world = world;
		this.webClient = webClient;
	}
}
