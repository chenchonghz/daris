package daris.client.util;

public class HtmlUtil {

	public static native String safeHtml(String s)/*-{
		s = s.replace(/[\&]/g, "&amp;");
		s = s.replace(/[\"]/g, "&quot;");
		s = s.replace(/[\<]/g, "&lt;");
		s = s.replace(/[\>]/g, "&gt;");
		return s;
	}-*/;
}
