package daris.client;

import java.io.File;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.codec.binary.Base64;

import arc.mf.client.RemoteServer;
import arc.mf.client.ServerClient;
import arc.mf.client.ServerClient.ExSessionInvalid;
import arc.mf.client.ServerClient.Input;
import arc.mf.client.ServerClient.Output;
import arc.streams.LongInputStream;
import arc.streams.SizedInputStream;
import arc.streams.UnsizedInputStream;
import arc.utils.URLUtil;
import arc.xml.XmlDoc;
import arc.xml.XmlDoc.Element;

public class Execute {

	public static class ArgumentParseException extends Exception {

		private static final long serialVersionUID = -2791932124828517791L;

		public ArgumentParseException(String msg) {
			super(msg);
		}

	}

	public static enum Command {
		EXECUTE, LOGOFF, LOGON;

		public static Command fromString(String s) {
			if (s != null) {
				if (LOGON.name().equalsIgnoreCase(s)) {
					return LOGON;
				}
				if (EXECUTE.name().equalsIgnoreCase(s)) {
					return EXECUTE;
				}
				if (LOGOFF.name().equalsIgnoreCase(s)) {
					return LOGOFF;
				}
			}
			return null;
		}

		@Override
		public String toString() {
			return super.toString().toLowerCase();
		}

	}

	private static class Options {
		private String _domain;
		private Boolean _encrypt = null;
		private String _host;
		private OutputFormat _outputFormat = OutputFormat.SHELL;
		private String _password;
		private Integer _port = null;
		private String _sid;
		private String _token;
		private Boolean _useHttp = null;
		private String _user;

		public Options() {
			try {
				// try read from system environment variables first.
				Map<String, String> env = System.getenv();
				setHost(env.get("MFLUX_HOST"));
				setPort(env.get("MFLUX_PORT"));
				setTransport(env.get("MFLUX_TRANSPORT"));
				setSid(env.get("MFLUX_SID"));
				setToken(env.get("MFLUX_TOKEN"));
				setAuth(env.get("MFLUX_AUTH"));
				setOutputFormat(env.get("MFLUX_OUTPUT"));
				// read from jvm properties (it overrides system environment
				// variables
				setHost(System.getProperty("mf.host"));
				setPort(System.getProperty("mf.port"));
				setTransport(System.getProperty("mf.transport"));
				setSid(System.getProperty("mf.sid"));
				setToken(System.getProperty("mf.token"));
				setAuth(System.getProperty("mf.auth"));
				setOutputFormat(System.getProperty("mf.output"));
			} catch (Throwable e) {
				e.printStackTrace(System.err);
			}
		}

		public String domain() {
			return _domain;
		}

		public boolean encrypt() {
			if (_encrypt == null) {
				return false;
			} else {
				return _encrypt;
			}
		}

		public boolean hasAuth() {
			return _domain != null && _user != null && _password != null;
		}

		public String host() {
			return _host;
		}

		public void loadSidFromDefaultLocation() throws Throwable {
			if (_host == null) {
				throw new ArgumentParseException("Missing option mf.host.");
			}
			_sid = readFromSidFile(new File(getDefaultSidFilePath(_host)));
		}

		public OutputFormat outputFormat() {
			return _outputFormat;
		}

		public String password() {
			return _password;
		}

		public Integer port() {
			return _port;
		}

		public void setAuth(String auth) throws Throwable {
			if (auth == null) {
				_domain = null;
				_user = null;
				_password = null;
				return;
			}
			String[] parts = auth.split(",");
			if (parts.length == 3) {
				_domain = parts[0];
				_user = parts[1];
				if (parts[2]
						.matches("^([A-Za-z0-9+/]{4})*([A-Za-z0-9+/]{4}|[A-Za-z0-9+/]{3}=|[A-Za-z0-9+/]{2}==)$")
						&& Base64.isBase64(parts[2])) {
					// password is base64 encoded.
					_password = new String(Base64.decodeBase64(parts[2]))
							.trim();
				} else {
					_password = parts[2];
				}
				return;
			}
			throw new ArgumentParseException(
					"Invalid option mf.auth: "
							+ auth
							+ ". Expects a comma-separated string in the form of domain,user,password.");
		}

		public void setHost(String host) {
			_host = host;
		}

		public void setOutputFormat(String outputFormat) {
			_outputFormat = OutputFormat.fromString(outputFormat,
					OutputFormat.SHELL);
		}

		public void setPort(int port) throws Throwable {
			if (port < 0 || port > 65535) {
				throw new ArgumentParseException("Invalid mf.port: " + port
						+ ". Expects a integer between 0 and 65535.");
			}
			_port = port;
		}

		public void setPort(String port) throws Throwable {
			if (port == null) {
				_port = null;
				return;
			}
			int p = 0;
			try {
				p = Integer.parseInt(port);
			} catch (Throwable e) {
				throw new ArgumentParseException("Failed to parse mf.port: "
						+ port + ".");
			}
			setPort(p);
		}

		public void setSid(String sid) throws Throwable {
			if (sid == null) {
				_sid = null;
				return;
			}
			boolean isFile = new File(sid).exists() && new File(sid).isFile();
			if (isFile) {
				_sid = readFromSidFile(new File(sid));
				if (_sid == null) {
					throw new ArgumentParseException("Invalid sid file: " + sid);
				}
			} else {
				_sid = sid;
			}
		}

		public void setToken(String token) throws Throwable {
			_token = token;
		}

		public void setTransport(String transport) throws Throwable {
			if (transport == null) {
				_useHttp = null;
				_encrypt = null;
				return;
			}
			if (!("HTTP".equalsIgnoreCase(transport)
					|| "HTTPS".equalsIgnoreCase(transport) || "TCP/IP"
						.equalsIgnoreCase(transport))) {
				throw new ArgumentParseException("Invalid mf.transport: "
						+ transport);
			}
			if (transport.equalsIgnoreCase("HTTP")) {
				_useHttp = true;
				_encrypt = false;
			} else if (transport.equalsIgnoreCase("HTTPS")) {
				_useHttp = true;
				_encrypt = true;
			} else if (transport.equalsIgnoreCase("TCP/IP")) {
				_useHttp = false;
				_encrypt = false;
			}
		}

		public String sid() {
			return _sid;
		}

		public String token() {
			return _token;
		}

		public String transport() {
			if (_useHttp == null || _encrypt == null) {
				return null;
			}
			if (_useHttp == true) {
				return _encrypt == true ? "HTTPS" : "HTTP";
			} else {
				return "TCP/IP";
			}
		}

		public boolean useHttp() {
			if (_useHttp == null) {
				return true;
			} else {
				return _useHttp;
			}
		}

		public String user() {
			return _user;
		}
	}

	public static enum OutputFormat {
		SHELL, XML;
		public static OutputFormat fromString(String s,
				OutputFormat defaultValue) {
			if (s != null) {
				if (XML.name().equalsIgnoreCase(s)) {
					return XML;
				} else if (SHELL.name().equalsIgnoreCase(s)) {
					return SHELL;
				}
			}
			return defaultValue;
		}
	}

	private static class ServiceCommand {

		private static ServerClient.Input createInput(String s)
				throws Throwable {
			if (s.startsWith("file:") || s.startsWith("FILE:")) {
				return new ServerClient.FileInput(new File(s.substring(5)));
			} else if (s.startsWith("http://") || s.startsWith("HTTP://")
					|| s.startsWith("https://") || s.startsWith("HTTPS://")
					|| s.startsWith("ftp://") || s.startsWith("FTP://")) {
				return createUrlInput(s);
			} else {
				return new ServerClient.FileInput(new File(s));
			}
		}

		private static ServerClient.Input createUrlInput(String url)
				throws Throwable {
			URL urlObject = new URL(url);
			final URLConnection conn = urlObject.openConnection();
			String type = conn.getContentType();
			if (type != null) {
				int idx = type.indexOf(';');
				if (idx != -1) {
					type = type.substring(0, idx);
				}
			}
			LongInputStream is = URLUtil.openStream(urlObject.toExternalForm());
			long length = is.remaining();
			String source = url.replace('\\', '/');
			if (length < 0L) {
				return new ServerClient.Input(type, new UnsizedInputStream(is),
						source);
			} else {
				return new ServerClient.Input(type, new SizedInputStream(is,
						length), source);
			}
		}

		private static ServerClient.Output createOutput(String s)
				throws Throwable {
			if (s.startsWith("file:") || s.startsWith("FILE:")) {
				return new ServerClient.FileOutput(new File(s.substring(5)));
			} else {
				return new ServerClient.FileOutput(new File(s));
			}
		}

		private static XmlDoc.Element parseArgsFromXmlString(String args)
				throws Throwable {
			StringBuilder sb = new StringBuilder(args);
			if (!args.startsWith("<args>")) {
				sb.insert(0, "<args>");
			}
			if (!args.endsWith("</args>")) {
				sb.append("</args>");
			}
			return new XmlDoc().parse(sb.toString());
		}

		private XmlDoc.Element _args;

		private List<ServerClient.Input> _inputs;

		private ServerClient.Output _output;

		private String _service;

		private void addInput(String input) throws Throwable {
			if (_inputs == null) {
				_inputs = new Vector<ServerClient.Input>();
			}
			_inputs.add(createInput(input));
		}

		public String args() {
			if (_args == null) {
				return null;
			}
			String s = _args.toString();
			if (s.startsWith("<args>")) {
				s = s.substring(6);
			}
			if (s.endsWith("</args>")) {
				s = s.substring(0, s.length() - 7);
			}
			return s;
		}

		public List<ServerClient.Input> inputs() {
			return _inputs;
		}

		public ServerClient.Output output() {
			return _output;
		}

		public String service() {
			return _service;
		}

		public void setArgs(String[] argsArray) throws Throwable {
			StringBuilder sb = new StringBuilder();
			for (String arg : argsArray) {
				sb.append(arg);
				sb.append(" ");
			}
			XmlDoc.Element args = null;
			String argsStr = sb.toString().trim();
			if (argsStr.startsWith("<") && argsStr.endsWith(">")) {
				// xml string
				args = parseArgsFromXmlString(argsStr);
			} else if (new File(argsStr).exists() && new File(argsStr).isFile()) {
				// xml file
				args = parseArgsFromXmlString(new String(
						Files.readAllBytes(Paths.get(argsStr))).replace("\r\n",
						"").trim());
			} else {
				throw new ArgumentParseException(
						"Failed parse service arguments: " + argsStr
								+ ". Expects a xml string or a xml file path.");
			}
			if (args != null) {
				List<XmlDoc.Element> ies = args.elements("in");
				if (ies != null) {
					for (XmlDoc.Element ie : ies) {
						addInput(ie.value());
						args.remove(ie);
					}
				}
				List<XmlDoc.Element> oes = args.elements("out");
				if (oes != null) {
					if (oes.size() > 1) {
						throw new ArgumentParseException(
								"Expects at most 1 service output. Found "
										+ oes.size());
					}
					XmlDoc.Element oe = oes.get(0);
					_output = createOutput(oe.value());
					args.remove(oe);
				}
			}
			_args = args;
		}

		public void setService(String service) throws Throwable {
			_service = service;
		}

	}

	public static class ShellString {

		public static String format(Element re, boolean includeTopLevelElement) {
			if (re == null) {
				return null;
			}
			StringBuilder sb = new StringBuilder();
			if (includeTopLevelElement) {
				format(sb, re, 0, 4);
			} else {
				if (re.hasSubElements()) {
					List<XmlDoc.Element> ses = re.elements();
					for (XmlDoc.Element se : ses) {
						format(sb, se, 0, 4);
					}
				}
			}
			return sb.toString();
		}

		private static void format(StringBuilder sb, Element re, int indent,
				int step) {
			sb.append(new String(new char[indent]).replace('\0', ' '));
			sb.append(":" + re.name());
			if (re.hasAttributes()) {
				List<XmlDoc.Attribute> attrs = re.attributes();
				for (XmlDoc.Attribute attr : attrs) {
					sb.append(" -" + attr.name());
					sb.append(" " + attr.value());
				}
			}
			if (re.hasValue()) {
				sb.append(" " + re.value());
			}
			sb.append("\n");
			if (re.hasSubElements()) {
				List<XmlDoc.Element> ses = re.elements();
				for (XmlDoc.Element se : ses) {
					format(sb, se, indent + step, step);
				}
			}
		}
	}

	public static final String DEFAULT_PREFIX = "daris-client";

	public static String prefix() {
		String prefix = System.getProperty("dc.prefix");
		if (prefix != null) {
			return prefix;
		} else {
			return DEFAULT_PREFIX;
		}
	}

	private static void execute(Options options, String[] cmdArgs)
			throws Throwable {

		ServiceCommand svc = new ServiceCommand();
		svc.setService(cmdArgs[0]);
		if (cmdArgs.length > 1) {
			svc.setArgs(Arrays.copyOfRange(cmdArgs, 1, cmdArgs.length));
		}
		XmlDoc.Element re = null;
		if (options.hasAuth()) {
			re = execute(options.host(), options.port(), options.useHttp(),
					options.encrypt(), options.domain(), options.user(),
					options.password(), svc.service(), svc.args(),
					svc.inputs(), svc.output());
		} else {
			re = execute(options.host(), options.port(), options.useHttp(),
					options.encrypt(), options.sid(), options.token(),
					svc.service(), svc.args(), svc.inputs(), svc.output());
		}
		if (options.outputFormat() == OutputFormat.XML) {
			System.out.println(re.toString());
		} else {
			System.out.println(ShellString.format(re, false));
		}

	}

	private static XmlDoc.Element execute(String host, int port,
			boolean useHttp, boolean encrypt, String sid, String token,
			String service, String args, List<Input> inputs, Output output)
			throws Throwable {
		RemoteServer server = new RemoteServer(host, port, useHttp, encrypt);
		ServerClient.Connection cxn = null;
		XmlDoc.Element re = null;
		try {
			cxn = server.open();
			if (token != null) {
				cxn.connectWithToken(token);
			} else if (sid != null) {
				cxn.reconnect(sid);
			} else {
				throw new Exception("No sid or token is specified.");
			}
			re = cxn.executeMultiInput(service, args, inputs, output);
		} finally {
			if (cxn != null) {
				cxn.close();
			}
		}
		return re;
	}

	private static XmlDoc.Element execute(String host, int port,
			boolean useHttp, boolean encrypt, String domain, String user,
			String password, String service, String args, List<Input> inputs,
			Output output) throws Throwable {
		RemoteServer server = new RemoteServer(host, port, useHttp, encrypt);
		ServerClient.Connection cxn = null;
		XmlDoc.Element re = null;
		try {
			cxn = server.open();
			cxn.connect(domain, user, password);
			re = cxn.executeMultiInput(service, args, inputs, output);
		} finally {
			if (cxn != null) {
				cxn.close();
			}
		}
		return re;
	}

	public static String getDefaultSidFilePath(String host) {
		return System.getProperty("user.home") + File.separatorChar
				+ ".MFLUX_SID_" + host;
	}

	private static void logoff(Options options) throws Throwable {

		File sidFile = new File(getDefaultSidFilePath(options.host()));
		if (sidFile.exists()) {
			sidFile.delete();
		}
		RemoteServer server = new RemoteServer(options.host(), options.port(),
				options.useHttp(), options.encrypt());
		ServerClient.Connection cxn = null;
		try {
			cxn = server.open();
			cxn.logoff();
		} finally {
			if (cxn != null) {
				cxn.close();
			}
		}
	}

	private static void logon(Options options, String[] cmdArgs)
			throws Throwable {
		if (cmdArgs == null || cmdArgs.length != 3) {
			throw new ArgumentParseException(
					"Invalid arguments for logon command.");
		}
		String domain = cmdArgs[0];
		String user = cmdArgs[1];
		String password = cmdArgs[2];
		if (options.sid() != null) {
			logoff(options);
		}
		RemoteServer server = new RemoteServer(options.host(), options.port(),
				options.useHttp(), options.encrypt());
		ServerClient.Connection cxn = null;
		String sid = null;
		try {
			cxn = server.open();
			sid = cxn.connect(domain, user, password);
		} finally {
			if (cxn != null) {
				cxn.close();
			}
		}
		if (sid == null) {
			throw new Exception("Failed to authenticate.");
		}
		writeToDefaultSidFile(options.host(), sid);
		System.out.println(sid);
	}

	public static void main(String[] args) {
		if (args == null || args.length == 0) {
			showHelp(null);
			System.exit(1);
			return;
		}
		String[] optionsArray = null;
		String[] cmdArgsArray = null;
		Command cmd = null;
		boolean showHelp = false;
		for (int i = 0; i < args.length; i++) {
			if ("-help".equals(args[i])) {
				showHelp = true;
			} else {
				cmd = Command.fromString(args[i]);
				if (cmd != null) {
					optionsArray = Arrays.copyOfRange(args, 0, i);
					if (i + 1 < args.length) {
						cmdArgsArray = Arrays.copyOfRange(args, i + 1,
								args.length);
					}
					break;
				}
			}
		}
		if (showHelp) {
			showHelp(cmd);
			System.exit(0);
			return;
		}
		if (cmd == null || optionsArray == null || optionsArray.length == 0) {
			System.err.println("Invalid arguments.");
			showHelp(null);
			System.exit(1);
			return;
		}
		try {
			Options options = parseOptions(optionsArray, cmd);
			switch (cmd) {
			case EXECUTE:
				if (cmdArgsArray == null || cmdArgsArray.length == 0) {
					throw new ArgumentParseException(
							"Missing arguments for execute command.");
				}
				execute(options, cmdArgsArray);
				break;
			case LOGON:
				if (cmdArgsArray == null || cmdArgsArray.length == 0) {
					throw new ArgumentParseException(
							"Missing arguments for logon command.");
				}
				logon(options, cmdArgsArray);
				break;
			case LOGOFF:
				if (cmdArgsArray != null) {
					throw new ArgumentParseException(
							"Unexpected arguments for logoff command.");
				}
				logoff(options);
			}
		} catch (ExSessionInvalid e) {
			System.err.println("Error: " + e.getMessage()
					+ " Please logon first or provide a token.");
			showHelp(null);
		} catch (ArgumentParseException e) {
			System.err.println("Error: " + e.getMessage());
			showHelp(cmd);
		} catch (Throwable t) {
			t.printStackTrace(System.err);
		}
	}

	private static Options parseOptions(String[] options, Command cmd)
			throws Throwable {
		Options o = new Options();

		for (int i = 0; i < options.length;) {
			String opt = options[i];
			if ("-help".equals(opt)) {
				i++;
			} else if ("-mf.host".equals(opt)) {
				if (i + 1 < options.length) {
					o.setHost(options[i + 1]);
					i += 2;
				} else {
					throw new ArgumentParseException(
							"Missing value for option mf.host.");
				}
			} else if ("-mf.port".equals(opt)) {
				if (i + 1 < options.length) {
					o.setPort(options[i + 1]);
					i += 2;
				} else {
					throw new ArgumentParseException(
							"Missing value for option mf.port.");
				}
			} else if ("-mf.transport".equals(opt)) {
				if (i + 1 < options.length) {
					o.setTransport(options[i + 1]);
					i += 2;
				} else {
					throw new ArgumentParseException(
							"Missing value for option mf.transport.");
				}
			} else if ("-mf.sid".equals(opt)) {
				if (i + 1 < options.length) {
					o.setSid(options[i + 1]);
					i += 2;
				} else {
					throw new ArgumentParseException(
							"Missing value for option mf.sid.");
				}
			} else if ("-mf.token".equals(opt)) {
				if (i + 1 < options.length) {
					o.setToken(options[i + 1]);
					i += 2;
				} else {
					throw new ArgumentParseException(
							"Missing value for option mf.token.");
				}
			} else if ("-mf.auth".equals(opt)) {
				if (i + 1 < options.length) {
					o.setAuth(options[i + 1]);
					i += 2;
				} else {
					throw new ArgumentParseException(
							"Missing value for option mf.auth.");
				}
			} else if ("-mf.output".equals(opt)) {
				if (i + 1 < options.length) {
					o.setOutputFormat(options[i + 1]);
					i += 2;
				} else {
					throw new ArgumentParseException(
							"Missing value for option mf.output.");
				}
			} else {
				throw new ArgumentParseException("Unexpected option: " + opt);
			}
		}
		if (o.host() == null) {
			throw new ArgumentParseException("Missing option mf.host.");
		}
		if (o.port() <= 0) {
			throw new ArgumentParseException("Missing option mf.port.");
		}
		if (o.transport() == null) {
			throw new ArgumentParseException("Missing option mf.transport.");
		}
		if (Command.EXECUTE == cmd) {
			if (!o.hasAuth() && o.sid() == null && o.token() == null) {
				o.loadSidFromDefaultLocation();
				if (o.sid() == null) {
					throw new ArgumentParseException(
							"Not logged on and missing token. Option -mf.sid or -mf.token is required for command: execute.");
				}
			}
		}
		return o;
	}

	public static String readFromDefaultSidFile(String host) throws Throwable {
		return readFromSidFile(new File(getDefaultSidFilePath(host)));
	}

	public static String readFromSidFile(File sidFile) throws Throwable {
		if (!sidFile.exists()) {
			return null;
		}
		String sid = new String(Files.readAllBytes(Paths.get(sidFile
				.getAbsolutePath()))).replace("\r\n", "").trim();
		if (sid == null || sid.length() == 0) {
			return null;
		} else {
			return sid;
		}
	}

	private static void showHelp(Command cmd) {
		if (cmd == null) {
			System.out.println("Usage: " + prefix()
					+ " [<options>] <command> [<args>]");
			System.out.println("");
			System.out.println("Description:");
			System.out
					.println("    Execute a service on the remote Mediaflux server or logon/logoff Mediaflux.");
			System.out.println("");
			System.out.println("Options:");
			System.out
					.println("    -help                           Prints usage.");
			System.out
					.println("    -mf.host <host>                 The Mediaflux server host.");
			System.out
					.println("    -mf.port <port>                 The Mediaflux server port.");
			System.out
					.println("    -mf.transport <transport>       The Mediaflux server transport. Can be http, https or tcp/ip.");
			System.out
					.println("    -mf.sid <sid>                   The Mediaflux session code (or the file contains the session code). For execute command only.");
			System.out
					.println("    -mf.token <token>               The Mediaflux secure identity token. For execute command only.");
			System.out
					.println("    -mf.auth <domain,user,password> The Mediaflux user authentication details, it is in the form of domain,user,password. For execute command only.");
			System.out
					.println("    -mf.output <xml|shell>          The output format. Can be xml or shell. For execute command only.");
			System.out.println("");
			System.out.println("The available commands are:");
			System.out.println("    logon      Log on Mediaflux.");
			System.out.println("    execute    Execute a service.");
			System.out.println("    logoff     Log off Mediaflux.");
			System.out.println("");
			System.out.println("See '" + prefix()
					+ " -help <command>' to read about a specific command.");
		} else {
			switch (cmd) {
			case LOGON:
				System.out.println("Usage: " + prefix()
						+ " [<options>] logon <domain> <user> <password>");
				System.out.println("");
				System.out.println("Description:");
				System.out
						.println("    Log on Mediaflux with specified arguments.");
				System.out.println("");
				System.out.println("Options:");
				System.out
						.println("    -help                           Prints usage for logon command.");
				System.out
						.println("    -mf.host <host>                 The Mediaflux server host.");
				System.out
						.println("    -mf.port <port>                 The Mediaflux server port.");
				System.out
						.println("    -mf.transport <transport>       The Mediaflux server transport. Can be http, https or tcp/ip.");
				break;
			case EXECUTE:
				System.out.println("Usage: " + prefix()
						+ " [<options>] execute <service> [<args|args-file>]");
				System.out.println("");
				System.out.println("Description:");
				System.out.println("    Execute the specfied service.");
				System.out.println("");
				System.out.println("Options:");
				System.out
						.println("    -help                           Prints usage for execute command.");
				System.out
						.println("    -mf.host <host>                 The Mediaflux server host.");
				System.out
						.println("    -mf.port <port>                 The Mediaflux server port.");
				System.out
						.println("    -mf.transport <transport>       The Mediaflux server transport. Can be http, https or tcp/ip.");
				System.out
						.println("    -mf.sid <sid>                   The Mediaflux session code (or the file contains the session code).");
				System.out
						.println("    -mf.token <token>               The Mediaflux secure identity token.");
				System.out
						.println("    -mf.auth <domain,user,password> The Mediaflux user authentication details, it is in the form of domain,user,password.");
				System.out
						.println("    -mf.output <xml|shell>          The output format. Can be xml or shell.");
				break;
			case LOGOFF:
				System.out
						.println("Usage: " + prefix() + " [<options>] logoff");
				System.out.println("");
				System.out.println("Description:");
				System.out.println("    Log off Mediaflux.");
				System.out.println("");
				System.out.println("Options:");
				System.out
						.println("    -help                           Prints usage for execute command.");
				System.out
						.println("    -mf.host <host>                 The Mediaflux server host.");
				System.out
						.println("    -mf.port <port>                 The Mediaflux server port.");
				System.out
						.println("    -mf.transport <transport>       The Mediaflux server transport. Can be http, https or tcp/ip.");
				break;
			default:
				break;
			}
		}
	}

	public static void writeToDefaultSidFile(String host, String sid)
			throws Throwable {
		String path = getDefaultSidFilePath(host);
		Files.write(Paths.get(path), sid.getBytes(), StandardOpenOption.CREATE,
				StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
	}

}
