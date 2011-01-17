package nexi.sengoku.easy;

import com.gargoylesoftware.htmlunit.html.HtmlPage;

public class Resources {

	public final long wood;
	public final long cotton;
	public final long iron;
	public final long wheat;

	public Resources (HtmlPage page) {
		wood = Long.parseLong(page.getElementById("wood").getTextContent());
		cotton = Long.parseLong(page.getElementById("stone").getTextContent());
		iron = Long.parseLong(page.getElementById("iron").getTextContent());
		wheat = Long.parseLong(page.getElementById("rice").getTextContent());
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Resources ");
		builder.append("[wood=");
		builder.append(wood);
		builder.append(", cotton=");
		builder.append(cotton);
		builder.append(", iron=");
		builder.append(iron);
		builder.append(", wheat=");
		builder.append(wheat);
		builder.append("]");
		return builder.toString();
	}

}
