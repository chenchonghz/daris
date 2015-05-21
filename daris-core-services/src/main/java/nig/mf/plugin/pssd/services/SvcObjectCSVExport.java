package nig.mf.plugin.pssd.services;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import arc.mf.plugin.PluginService;
import arc.mf.plugin.ServiceExecutor;
import arc.mf.plugin.dtype.EnumType;
import arc.mf.plugin.dtype.StringType;
import arc.mf.plugin.dtype.XmlDocType;
import arc.xml.XmlDoc;
import arc.xml.XmlDoc.Element;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;

import com.opencsv.CSVWriter;

public class SvcObjectCSVExport extends PluginService {
	private Interface _defn;

	public SvcObjectCSVExport() {
		_defn = new Interface();
		Interface.Element pwhere = new Interface.Element("where",
				StringType.DEFAULT, "Selection query", 1, 1);
		Interface.Element ptype = new Interface.Element("type", new EnumType(
				new String[] { "project", "subject", "ex-method", "method",
						"study", "dataset" }), "Selection of object type", 0, 1);
		Interface.Element pQuery = new Interface.Element("pquery",
				XmlDocType.DEFAULT, "Define query for parent object", 0, 1);
		pQuery.add(pwhere);
		pQuery.add(ptype);
		_defn.add(pQuery);
		Interface.Element where = new Interface.Element("where",
				StringType.DEFAULT, "Selection query", 1, 1);
		Interface.Element type = new Interface.Element("type", new EnumType(
				new String[] { "project", "subject", "ex-method", "method",
						"study", "dataset" }), "Selection of object type", 0, 1);
		Interface.Element query = new Interface.Element("query",
				XmlDocType.DEFAULT, "Define query for object", 1, 1);
		query.add(where);
		query.add(type);
		_defn.add(query);
		Interface.Element xpath = new Interface.Element("xpath",
				StringType.DEFAULT, "Xpath for output value", 1,
				Integer.MAX_VALUE);
		Interface.Element delimiter = new Interface.Element("delimiter",
				new EnumType(new String[] { "tab", "comma", "semicolon","space", "other" }),
				"Input the delimiter for csv file: tab, semicolon, comma, space, other. Defaults to comma. "
						+ "If sets to other, the value attribute must be specified.",0, 1);
		delimiter.add(new Interface.Attribute("value", StringType.DEFAULT,
				"Input the delimiter string value if it is set to other.", 0));
		Interface.Element xpathout = new Interface.Element("outputxpath",
				new EnumType(new String[] { "false", "true" }),
				"Select if output xpath as the header of csv table", 0, 1);
		Interface.Element output = new Interface.Element("output",
				XmlDocType.DEFAULT, "Define output characters", 1, 1);
		output.add(delimiter);
		output.add(xpathout);
		output.add(xpath);
		_defn.add(output);
	}

	@Override
	public Access access() {
		return ACCESS_ACCESS;
	}

	@Override
	public Interface definition() {
		return _defn;
	}

	@Override
	public String description() {
		return "This service execute a query to the asset and output xpath value as csv file. " +
				"The asset can be queried in two ways: i): if only define 'query' element, the service" +
				"will look for every asset match with input query; ii): if user define both 'pquery' " +
				"and 'query', the service will first look for assets match with 'pquery', which will then" +
				"be queried using 'query'";
	}

	@Override
	public void execute(Element args, Inputs in, Outputs out, XmlWriter w)
			throws Throwable {
		XmlDocMaker results = new XmlDocMaker("result");
		ServiceExecutor executor = executor();
		String pwhere = args.value("pquery/where");
		String ptype = args.value("pquery/type");
		String where = args.value("query/where");
		String type = args.value("query/type");
		boolean xpathoutput = getXpathOption(args);
		char delimiter = getDelimiter(args);
		Collection<String> xpaths = args.values("output/xpath");
		if (pwhere == null && ptype == null) {
			Collection<Element> elements = getXmlElements(where, type,
					xpathoutput, xpaths, executor);
			results = writeCSVFile(elements, delimiter, xpathoutput, xpaths, out);
		} else {
			Collection<Element> pcids = getXmlElements(pwhere, ptype,
					xpathoutput, executor);
			if (pcids == null) {
				results = writeCSVFile(null, xpaths, out);
			} else {
				ArrayList<String> cids = new ArrayList<String>();
				Iterator<Element> cidIt = pcids.iterator();
				while (cidIt.hasNext()) {
					cids.add(cidIt.next().value("cid").toString());
				}
				StringBuilder sb = new StringBuilder();
				sb.append(where + " and (");
				for (String cid : cids) {
					sb.append("cid='" + cid + "' or cid starts with '" + cid
							+ "' or ");
				}
				sb.delete(sb.length() - 4, sb.length());
				sb.append(" )");
				where = sb.toString();
				Collection<Element> elements = getXmlElements(where, type,
						xpathoutput, xpaths, executor);
				results = writeCSVFile(elements, delimiter, xpathoutput,xpaths, out);
			}
		}
		w.add(results.root());
	}

	@Override
	public String name() {
		return "om.pssd.object.csv.export";
	}

	public List<Element> getXmlElements(String where, String type,
			boolean xpathoutput, ServiceExecutor executor) throws Throwable {
		return getXmlElements(where, type, xpathoutput, null, executor);
	}

	public List<Element> getXmlElements(String where, String type,
			boolean xpathoutput, Collection<String> xpaths,
			ServiceExecutor executor) throws Throwable {
		// format the query string
		XmlDocMaker assetquery = new XmlDocMaker("args");
		StringBuilder wherequery = new StringBuilder();
		wherequery.append(where);
		if (type != null) {
			wherequery.append(" and model='om.pssd." + type + "'");
		}
		String[] attribute = new String[] { "ename", "cid" };
		assetquery.add("where", wherequery.toString());
		assetquery.add("xpath", attribute, "cid");
		assetquery.add("action", "get-values");
		if (xpaths != null) {
			for (String xpath : xpaths) {
				attribute = new String[] {"ename", xpath.replace("/", "___")};
				assetquery.add("xpath", attribute, xpath);
			}
		}
		// get the queried element
		XmlDoc.Element element = executor.execute("asset.query",
				assetquery.root());
		return element.elements();
	}

	public XmlDocMaker writeCSVFile(Collection<Element> elements,
			Collection<String> xpaths, Outputs out) throws Throwable {
		return writeCSVFile(null, ',', false, xpaths, out);
	}

	public XmlDocMaker writeCSVFile(Collection<Element> elements,
			char delimiter, boolean xpathOutput, Collection<String> xpaths,
			Outputs out) throws Throwable {
		XmlDocMaker doc = new XmlDocMaker("nbResults");
		File of = PluginService.createTemporaryFile();
		CSVWriter csvWriter = new CSVWriter(new OutputStreamWriter(
				new BufferedOutputStream(new FileOutputStream(of, true))),
				delimiter, '"');
		int count = 0;
		Collection<String[]> table = createContentTable(elements, delimiter, xpathOutput, xpaths, out);
		if (table != null){
			count = table.size() -1;
			Iterator<String[]> tableInt = table.iterator();
			while (tableInt.hasNext()){
				csvWriter.writeNext(tableInt.next());
			}
		}else{
			String [] fail = new String [] {"No Matched results for queried elements!"};
			csvWriter.writeNext(fail);
		}
		doc.add("count", Integer.toString(count));
		csvWriter.close();
		PluginService.Output output = out.output(0);
		output.setData(new TempFileInputStream(of), of.length(), "plain/text");
		return doc;
	}
	
	public Collection<String[]> createContentTable(
			Collection<Element> elements, char delimiter, boolean xpathOutput,
			Collection<String> xpaths, Outputs out) throws Throwable {
		Collection<String[]> tableContent = new Vector<String[]>();
		//create header dynamically
		Map<String, Integer> header = new LinkedHashMap<String, Integer>();
		if (elements != null) {
			Iterator<Element> Headers = elements.iterator();
			Vector<String> tmp = new Vector<String>();
			//add default 'cid' xpath into xpaths
			tmp.add("cid");
			for (String xpath : xpaths){
				tmp.add(xpath);
			}
			xpaths = tmp;
			while (Headers.hasNext()) {
				List<Element> csvHeaders = Headers.next().elements();
				for (Element csvheader : csvHeaders) {
					String hname = csvheader.name().replace("___","/");
					if (header.containsKey(hname)) {
						int c = countXpathName(csvHeaders, xpaths, csvheader.name());
						XmlDoc.Attribute a = new XmlDoc.Attribute("count",c);
						csvheader.add(a);
						if (header.get(hname) < c) {
							header.put(hname, c);
						}
					} else {
						int c = countXpathName(csvHeaders, xpaths, csvheader.name());
						XmlDoc.Attribute a = new XmlDoc.Attribute("count",c);
						csvheader.add(a);
						header.put(hname, c);
					}
				}
			}
			Vector<String> hst = new Vector<String>();
			Iterator<String> xpathsInt = xpaths.iterator();
			while (xpathsInt.hasNext()) {
				String name = xpathsInt.next();
				int nbEle = header.get(name);
				if (! xpathOutput){
					name = name.split("/")[name.split("/").length - 1];
				}
				if (nbEle == 0 || nbEle == 1){
					hst.add(name);
				}else{
					for (int i = 0; i != nbEle; i++){
						hst.add(name + "[" + i + "]");
					}
				}
			}
			String[] headerString = new String[hst.size()];
			for (int i = 0; i != hst.size(); i++) {
				headerString[i] = hst.elementAt(i);
			}
			tableContent.add(headerString);
			
			
			// save content
			Headers = elements.iterator();
			while (Headers.hasNext()) {
				Iterator<Element> csvContents = Headers.next().elements()
						.iterator();
				Vector<String> content = new Vector<String>();
				while (csvContents.hasNext()){
					Element e = csvContents.next();
					String hname = e.name();
					hname = e.name().replace("___", "/");
					int nbout = Integer.parseInt(e.value("@count"));
					int nb = header.get(hname);
					if (nb == 0) {
						content.add("");
					} else {
						for (int i = 0; i != nbout; i++) {
							content.add(e.value());
							if (csvContents.hasNext() && i != nbout - 1) {
								e = csvContents.next();
							}
						}
						for (int i = 0; i != nb - nbout; i++) {
							content.add("");
						}
					}
				}
				String[] contentString = new String[content.size()];
				for (int i = 0; i != content.size(); i++) {
					contentString[i] = content.elementAt(i);
				}
				tableContent.add(contentString);
			}
		}else{
			tableContent.add(null);
		}
		return tableContent;
	}
	
	public int countXpathName(List<Element> elements,
			Collection<String> xpaths, String name) throws Throwable {
		int count=0;
		int nbquery=0;
		for (Element element : elements ){
			if (element.name().equals(name) && element.value() != null){
				count++;
			}
		}
		for(String xpath : xpaths){
			if (xpath.equals(name.replace("___", "/"))){
				nbquery++;
			}
		}
		if (nbquery == 0){
			return 0;
		} else {
			return count/nbquery;
		}
	}

	public char getDelimiter(Element args) throws Throwable {
		char delimiter = ',';
		String delimiterType = args.value("output/delimiter");
		if (delimiterType != null) {
			if (delimiterType.equals("tab")) {
				delimiter = '	';
			} else if (delimiterType.equals("semicolon")) {
				delimiter = ';';
			} else if (delimiterType.equals("comma")) {
				delimiter = ',';
			} else if (delimiterType.equals("space")) {
				delimiter = ' ';
			} else if (delimiterType.equals("other")) {
				String dv = args.value("output/delimiter/@value");
				if (dv == null) {
					throw new Exception("delimiter value attribute is not set.");
				}
				if (dv.length() != 1) {
					throw new Exception("invalid delimiter: " + dv
							+ " Expected a single character.");
				}
				delimiter = dv.charAt(0);
			}
		}
		return delimiter;
	}

	public boolean getXpathOption(Element args) throws Throwable {
		boolean option = false;
		String xpathOption = args.value("output/outputxpath");
		if (xpathOption != null) {
			if (xpathOption.equals("false"))
				option = false;
			else if (xpathOption.equals("true"))
				option = true;
			else
				throw new Exception("Expected input \"true\" or \"false\"");
		}
		return option;
	}

	@Override
	public int maxNumberOfOutputs() {
		return 1;
	}

	@Override
	public int minNumberOfOutputs() {
		return 1;
	}
	
	public class TempFileInputStream extends FileInputStream {
		private File _file;

		public TempFileInputStream(File file) throws FileNotFoundException {
			super(file);
			_file = file;
		}

		public void close() throws IOException {
			super.close();
			_file.delete();
		}
	}
}
