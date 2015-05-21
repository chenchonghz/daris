package nig.mf.plugin.pssd.servlets;

import java.util.ArrayList;
import java.util.List;

public class HtmlBuilder {

    public static final int NAV_HEIGHT = 30;

    public static final int TAB_HEIGHT = 30;

    public static final int TAB_WIDTH = 150;

    public static class Item {
        public final String label;
        public final String href;

        public Item(String label, String href) {
            this.label = label;
            this.href = href;
        }
    }

    private String _location;

    private StringBuilder _head;

    private String _title;

    private List<String> _styles;

    private StringBuilder _content;

    private List<Item> _navItems;

    private List<Item> _tabItems;

    public HtmlBuilder(String location) {

        _location = location;

        _head = new StringBuilder();

        _title = null;

        _styles = new ArrayList<String>();
        _styles.add("body {font-family:\"Myriad Set Pro\",\"Lucida Grande\",Helvetica,Arial,Verdana,sans-serif; line-height: 1.5em; margin-top:"
                + NAV_HEIGHT + "px;}");
        _styles.add("div.nav {height:"
                + NAV_HEIGHT
                + "px; width:100%; line-height:"
                + NAV_HEIGHT
                + "px; display:block; position:absolute; top:0; left:0; color:#eee; background: rgba(0,0,0,0.6); font-size:9pt;}");
        _styles.add("div.logout {width:60px; height:100%; display:inline-block; position:absolute; right:0px;}");
        _styles.add("a.nav:link {text-decoration:none; color:#eee;}");
        _styles.add("a.nav:visited {text-decoration:none; color:#eee;}");
        _styles.add("a.nav:hover {text-decoration:underline; color:#eee;}");
        _styles.add("a.nav:active {text-decoration:none; color:#eee;}");
        _styles.add("div.tab {height:" + TAB_HEIGHT + "px; width:100%; margin-top:5px;}");
        _styles.add("ul.tab {list-style-type: none; margin: 0; padding: 0; overflow: hidden;}");
        _styles.add("li.tab {float: left;}");
        _styles.add("a.tab:link {text-decoration:none; color:#666; display:block; border-top:3px solid #fff; border-right: 1px solid #fff; text-align:center; font-size:9pt; font-weight:normal; background-color:#ddd; width:"
                + TAB_WIDTH + "px; height:100%;}");
        _styles.add("a.tab:visited {text-decoration:none; color:#666; display:block; border-top:3px solid #fff; border-right: 1px solid #fff; text-align:center; font-size:9pt; font-weight:normal; background-color:#ddd; width:"
                + TAB_WIDTH + "px; height:100%;}");
        _styles.add("a.tab:hover {text-decoration:underline; color:#666; display:block; border-top:3px solid #fff; border-right: 1px solid #fff; text-align:center; font-size:9pt; font-weight:normal; background-color:#ddd; width:"
                + TAB_WIDTH + "px; height:100%;}");
        _styles.add("a.tab:active {text-decoration:none; color:#666; display:block; border-top:3px solid #fff; border-right: 1px solid #fff; text-align:center; font-size:9pt; font-weight:normal; background-color:#ddd; width:"
                + TAB_WIDTH + "px; height:100%;}");
        _styles.add("span.tab {display:block; border-top:3px solid #fff; border-right: 1px solid #fff; text-align:center; font-size:9pt; font-weight:bold; background-color:#eee; color:#666; width:"
                + TAB_WIDTH + "px; height:100%;}");
        _styles.add("tr:nth-child(even) {background:#eee;}");
        _styles.add("tr:nth-child(odd) {background:#fff;}");
        _styles.add("tr.head {background:#eee;}");
        _styles.add("td {font-size: 1em; line-height: 1.5em;}");
        _styles.add("th {font-size: 1em; line-height: 1.5em;}");
        _styles.add("pre {margin:0px; padding-top:10px; padding-bottom:10px; font-family:Menlo,monospace; font-size:1em; background-color:#eee; color:#111; }");

        _content = new StringBuilder();
        _navItems = new ArrayList<Item>();
        _tabItems = new ArrayList<Item>();
    }

    /**
     * Sets the current (authenticated) location/url.
     * 
     * @param url
     */
    public void setLocation(String url) {
        _location = url;
    }

    /**
     * Sets the title of the html page.
     * 
     * @param title
     */
    public void setTitle(String title) {
        _title = title;
    }

    /**
     * Adds a css style.
     * 
     * @param style
     */
    public void addStyle(String style) {
        _styles.add(style);
    }

    /**
     * Adds a nav item.
     * 
     * @param label
     * @param href
     */
    public void addNavItem(String label, String href) {
        _navItems.add(new Item(label, href));
    }

    /**
     * Adds a tab item.
     * 
     * @param label
     * @param href
     */
    public void addTabItem(String label, String href) {
        _tabItems.add(new Item(label, href));
    }

    protected boolean hasNavItems() {
        return _navItems != null && !_navItems.isEmpty();
    }

    protected boolean hasTabItems() {
        return _tabItems != null && !_tabItems.isEmpty();
    }

    public void appendToHead(String element) {
        _head.append(element);
    }

    public void prependToHead(String element) {
        _head.insert(0, element);
    }

    public void appendContent(String element) {
        _content.append(element);
    }

    public void prependToBody(String element) {
        _content.insert(0, element);
    }

    public String buildHtml() {
        StringBuilder sb = new StringBuilder();
        sb.append("<html>\n");
        /*
         * head
         */
        sb.append("<head>\n");
        if (_title != null) {
            sb.append("<title>");
            sb.append(_title);
            sb.append("</title>\n");
        }
        if (_styles != null && !_styles.isEmpty()) {
            sb.append("<style type=\"text/css\">\n");
            for (String style : _styles) {
                sb.append(style);
                sb.append("\n");
            }
            sb.append("</style>\n");
        }
        sb.append(_head.toString());
        sb.append("</head>\n");
        /*
         * body
         */
        sb.append("<body>\n");

        if (_location != null) {
            // nav bar
            //
            // show nav bar only if the location is set (i.e. authenticated).
            sb.append("<div class=\"nav\">\n");
            if (hasNavItems()) {
                sb.append(" &nbsp; ");
                Item item1 = _navItems.get(0);
                if (item1.href == null) {
                    sb.append(item1.label);
                } else {
                    sb.append("<a class=\"nav\" href=\"" + item1.href + "\">" + item1.label
                            + "</a>");
                }
                for (int i = 1; i < _navItems.size(); i++) {
                    sb.append(" &gt; ");
                    Item item = _navItems.get(i);
                    if (item.href == null) {
                        sb.append(item.label);
                    } else {
                        sb.append("<a class=\"nav\" href=\"" + item.href + "\">" + item.label
                                + "</a>");
                    }
                }
            }
            sb.append("<div class=\"logout\"><a class=\"nav\" href=\""
                    + MainServlet.logoffUrlFor(_location) + "\">Logout</a></div>\n");
            sb.append("</div>\n");

            // tab bar
            //
            // show tab bar only if the location is set (i.e. authenticated).
            if (_tabItems != null && !_tabItems.isEmpty()) {
                sb.append("<div class=\"tab\">");
                sb.append("<ul class=\"tab\">\n");
                for (Item item : _tabItems) {
                    sb.append("<li class=\"tab\">\n");
                    if (item.href == null) {
                        sb.append("<span class=\"tab\">" + item.label + "</span>\n");
                    } else {
                        sb.append("<a class=\"tab\" href=\"" + item.href + "\">" + item.label
                                + "</a>\n");
                    }
                    sb.append("</li>\n");
                }
                sb.append("</ul>\n");
                sb.append("</div>\n");
            }
        }

        sb.append(_content.toString());
        sb.append("</body>\n");
        sb.append("</html>\n");
        return sb.toString();
    }
}
