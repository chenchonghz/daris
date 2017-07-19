package daris.client.cli;


import java.io.PrintStream;
import java.nio.file.Paths;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.concurrent.TimeUnit;

import daris.client.pssd.ArchiveType;
import daris.client.pssd.DatasetUtils;
import arc.mf.client.ServerClient;
import arc.mf.client.ServerClient.Connection;
import arc.xml.XmlDoc;
import arc.xml.XmlStringWriter;


public class ServerPerformanceCLI  extends ServerProcedureCLI  {

	private Integer _nit = null;

	protected ServerPerformanceCLI(String[] args) throws Throwable {
		super(args);
	}

	@Override
	protected void parseCommandOptions(List<String> args) throws Throwable {
		int n = args.size();
		for (int i = 0; i < n;) {
			if (args.get(i).equals("--nit")) {
				if (_nit != null) {
					throw new IllegalArgumentException(
							"More than one --nit specified. Expects only one.");
				}
				_nit = Integer.parseInt(args.get(i + 1));
				i += 2;
			} else {
				throw new IllegalArgumentException(
						"Unexpected argument: " + args.get(i));
			}
		}
		if (_nit == null) {
			throw new IllegalArgumentException("Missing --nit argument.");
		}
	}

	@Override
	protected void execute(Connection cxn) throws Throwable {
		testPerformance(cxn, _nit);
	}

	@Override
	protected String name() {
		return "daris-server-performance";
	}

	@Override
	protected void printCommandOptionsUsage(PrintStream s) {
		s.println(
				"  --nit <pid>  The number of iterations.");
	}



	public static void main(String[] args) {
		ServerPerformanceCLI cmd = null;
		try {
			cmd = new ServerPerformanceCLI(args);
			cmd.execute();
		} catch (Throwable t) {
			t.printStackTrace(System.err);
			if (cmd != null && (t instanceof IllegalArgumentException)) {
				cmd.printUsage(System.err);
			}
		}
	}
	public static void testPerformance(Connection cxn, Integer nIt) throws Throwable {


		//
		for (int i=0; i<nIt; i++) {
			System.out.println("Iteration " + i);
			long t1NS = System.nanoTime();
			long t1MS = TimeUnit.MILLISECONDS.convert(t1NS, TimeUnit.NANOSECONDS);
			Double timeSMS = execute("asset.namespace.list", cxn);
			long t2NS = System.nanoTime();
			long t2MS = TimeUnit.MILLISECONDS.convert(t2NS, TimeUnit.NANOSECONDS);
			Long t = t2MS - t1MS;
			Double tEMS = t.doubleValue();

			System.out.println("   Server  " + timeSMS);
			System.out.println("   Elapsed " + tEMS);
			System.out.println("   Diff    " + (tEMS - timeSMS));
		}
	}


	private static Double execute (String service, ServerClient.Connection cxn) throws Throwable  {
		XmlStringWriter w = new XmlStringWriter("args");
		w.add("service", new String[]{"name", service});
		w.add("time", "true");
		XmlDoc.Element r = cxn.execute("service.execute", w.document());
		XmlDoc.Element t = r.element("time");
		return time(t);
	}


	private static Double time (XmlDoc.Element t) throws Throwable {
		Double time = Double.parseDouble(t.value());
		String units = t.value("@units");
		if (units.equals("sec")) {
			return time * (Double)1000.0;
		} else if (units.equals("min")) {
			return time * (Double)1000.0 * (Double)60.0;
		} else {
			return time;
		}
	}

}
