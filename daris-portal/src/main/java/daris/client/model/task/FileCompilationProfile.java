package daris.client.model.task;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FileCompilationProfile {

	public static final FileCompilationProfile PSSD_IMPORT = new FileCompilationProfile("pssd.import.fcp",
			"Profile to import local files or directories.", new ArrayList<String>(Arrays.asList(new String[] {
					"service", "args" })));

	public static final FileCompilationProfile DICOM_INGEST = new FileCompilationProfile("pssd.dicom.ingest.fcp",
			"Profile to import local dicom files.", new ArrayList<String>(Arrays.asList(new String[] { "args" })));

	private static final String NAMESPACE = "/pssd/fcp";

	private String _namespace;
	private String _name;
	private String _description;
	private List<String> _variables;

	protected FileCompilationProfile(String name, String description, List<String> variables) {
		_name = name;
		_description = description;
		_namespace = NAMESPACE;
		_variables = variables;
	}

	public String name() {
		return _name;
	}

	public String namespace() {
		return _namespace;
	}

	public String path() {
		return namespace() + "/" + name();
	}

	public String description() {
		return _description;
	}

	public List<String> variables() {
		return _variables;
	}

	public boolean hasVariable(String name) {
		if (_variables != null && !_variables.isEmpty()) {
			return _variables.contains(name);
		}
		return false;
	}

}
