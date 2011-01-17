package nexi.sengoku.easy;

import java.util.Iterator;
import java.util.List;

import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlUnorderedList;

public class Production {
	public final long wood;
	public final long cotton;
	public final long iron;
	public final long wheat;

	public Production (HtmlPage page) {
		List<?> ul = page.getByXPath("//ul[@class='side_make']");
		Iterator<HtmlElement> itr = ((HtmlUnorderedList)ul.get(0)).getChildElements().iterator();

		wood = parseListElement(itr);
		cotton = parseListElement(itr);
		iron = parseListElement(itr);
		wheat = parseListElement(itr);
	}

	private long parseListElement(Iterator<HtmlElement> itr) {
		String[] split = itr.next().getTextContent().split("\\+");
		long p = Long.parseLong(split[0].trim());
		if (split.length > 1) {
			p +=  Long.parseLong(split[1].trim());
		}
		return p;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Production ");
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (cotton ^ (cotton >>> 32));
		result = prime * result + (int) (iron ^ (iron >>> 32));
		result = prime * result + (int) (wheat ^ (wheat >>> 32));
		result = prime * result + (int) (wood ^ (wood >>> 32));
		return result;
	}
}
