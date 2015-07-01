package daris.installer;

import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import arc.mf.client.RemoteServer;
import arc.mf.client.ServerClient;
import arc.utils.ProgressMonitor;
import daris.installer.DarisInstaller.PackageEntry;

public class DarisInstallerCLI {

	public static void main(String[] args) {

		CommandLineParser parser = new DefaultParser();
		Options options = null;
		try {
			options = createOptions();
			CommandLine line = parser.parse(options, args);
			validate(line);
			execute(line);
		} catch (ParseException pe) {
			System.err.println(pe.getMessage());
			if (options != null) {
				HelpFormatter formatter = new HelpFormatter();
				formatter.setWidth(120);
				formatter.printHelp("daris-installer <options>", options);
			}
		} catch (Throwable e) {
			e.printStackTrace(System.err);
		}
	}

	private static Options createOptions() {
		Options options = new Options();
		Option nogui = new Option(
				"nogui",
				"No GUI. Command line interface. If it is set, host, port, transport, domain, user and password options will be required.");
		options.addOption(nogui);
		Option pkgs = new Option(
				"pkgs",
				"The packages to install. Can include essentials, core-services, portal, sinks, analyzers and transcoders. If not given, essentials, core-services and portal packages will be installed.");
		pkgs.setArgName("pkg1,pkg2,pkg3");
		pkgs.setArgs(5);
		pkgs.setValueSeparator(',');
		pkgs.setRequired(false);
		options.addOption(pkgs);

		Option host = new Option("host", "The Mediaflux server host.");
		host.setArgName("host");
		host.setArgs(1);
		host.setRequired(false);
		options.addOption(host);

		Option port = new Option("port", "The Mediaflux server port.");
		port.setArgName("port");
		port.setArgs(1);
		port.setRequired(false);
		options.addOption(port);

		Option transport = new Option("transport",
				"The Mediaflux server transport protocol. Can be http, https or tcp.");
		transport.setArgName("transport");
		transport.setArgs(1);
		transport.setRequired(false);
		options.addOption(transport);

		Option domain = new Option("domain",
				"The Mediaflux authentication domain.");
		domain.setArgName("domain");
		domain.setArgs(1);
		domain.setRequired(false);
		options.addOption(domain);

		Option user = new Option("user", "The Mediaflux user name.");
		user.setArgName("user");
		user.setArgs(1);
		user.setRequired(false);
		options.addOption(user);

		Option password = new Option("password",
				"The Mediaflux user's password.");
		password.setArgName("password");
		password.setArgs(1);
		password.setRequired(false);
		options.addOption(password);
		return options;
	}

	private static void validate(CommandLine line) throws ParseException {
		if (line.hasOption("nogui")) {
			if (!line.hasOption("host")) {
				throw new ParseException("Missing required option: host");
			}
			if (!line.hasOption("port")) {
				throw new ParseException("Missing required option: port");
			}
			if (!line.hasOption("transport")) {
				throw new ParseException("Missing required option: transport");
			}
			if (!line.hasOption("domain")) {
				throw new ParseException("Missing required option: domain");
			}
			if (!line.hasOption("user")) {
				throw new ParseException("Missing required option: user");
			}
			if (!line.hasOption("password")) {
				throw new ParseException("Missing required option: password");
			}
		}
	}

	private static Set<PackageEntry.Type> parsePackageTypes(CommandLine line)
			throws Throwable {
		String[] pkgNames = line.hasOption("pkgs") ? line
				.getOptionValues("pkgs") : null;
		Set<PackageEntry.Type> selections = new TreeSet<PackageEntry.Type>();
		if (pkgNames != null) {
			for (String pkgName : pkgNames) {
				PackageEntry.Type pkgType = PackageEntry.Type
						.fromString(pkgName);
				if (pkgType == null) {
					throw new IllegalArgumentException("Invalid package name: "
							+ pkgName);
				}
				selections.add(pkgType);
			}
		} else {
			selections.add(PackageEntry.Type.ESSENTIALS);
			selections.add(PackageEntry.Type.CORE_SERVICES);
			selections.add(PackageEntry.Type.PORTAL);
		}
		return selections;
	}

	private static ConnectionSettings parseConnectionSettings(CommandLine line)
			throws Throwable {
		ConnectionSettings cs = new ConnectionSettings();
		cs.setHost(line.getOptionValue("host"));
		cs.setPort(Integer.parseInt(line.getOptionValue("port")));
		String transport = line.getOptionValue("transport");
		if ("HTTP".equalsIgnoreCase(transport)) {
			cs.setUseHttp(true);
			cs.setEncrypt(false);
		} else if ("HTTPS".equalsIgnoreCase(transport)) {
			cs.setUseHttp(true);
			cs.setEncrypt(true);
		} else if ("TCP".equalsIgnoreCase(transport)) {
			cs.setUseHttp(false);
			cs.setEncrypt(false);
		}
		cs.setDomain(line.getOptionValue("domain"));
		cs.setUser(line.getOptionValue("user"));
		cs.setPassword(line.getOptionValue("password"));
		return cs;
	}

	private static void execute(CommandLine line) throws Throwable {
		if (line.hasOption("nogui")) {
			executeCLI(line);
		} else {
			executeGUI(line);
		}
	}

	private static void executeGUI(final CommandLine line) throws Throwable {

		DarisInstallerGUI.start(parsePackageTypes(line));
	}

	private static void executeCLI(CommandLine line) throws Throwable {
		ConnectionSettings cs = parseConnectionSettings(line);
		Set<PackageEntry.Type> pkgTypes = parsePackageTypes(line);
		Map<PackageEntry.Type, PackageEntry> pkgEntries = DarisInstaller
				.getPackageEntries(pkgTypes);
		RemoteServer server = new RemoteServer(cs.host(), cs.port(),
				cs.useHttp(), cs.encrypt());
		server.setConnectionPooling(true);
		ServerClient.Connection cxn = null;
		try {
			cxn = server.open();
			cxn.connect(cs.domain(), cs.user(), cs.password());
			for (PackageEntry.Type pkgType : pkgTypes) {
				final PackageEntry pe = pkgEntries.get(pkgType);
				ProgressMonitor pm = new ProgressMonitor() {

					@Override
					public boolean abort() {
						return false;
					}

					@Override
					public void begin(int i, long l) {
						System.out.print("Installing " + pe.name() + "... ");
					}

					@Override
					public void beginMultiPart(int i, long l) {
					}

					@Override
					public void end(int i) {
						System.out.println("done.");
					}

					@Override
					public void endMultiPart(int i) {
					}

					@Override
					public void update(long l) throws Throwable {
					}
				};
				cxn.setProgressMonitor(pm);
				DarisInstaller.installPackage(cxn, pe);
			}
			System.out.println("Completed.");
		} finally {
			if (cxn != null) {
				cxn.close();
			}
			server.discard();
		}
	}
}
