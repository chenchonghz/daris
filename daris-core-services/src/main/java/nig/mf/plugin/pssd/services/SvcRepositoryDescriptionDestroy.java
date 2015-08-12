package nig.mf.plugin.pssd.services;

import nig.mf.plugin.pssd.RepositoryDescription;
import arc.mf.plugin.PluginService;
import arc.xml.XmlDoc;
import arc.xml.XmlWriter;

public class SvcRepositoryDescriptionDestroy extends PluginService {
	
	private Interface _defn;

	public SvcRepositoryDescriptionDestroy() {
		
		// matches DocType daris:pssd-repository-description
		
		_defn = new Interface();

		
	}

	public String name() {
		return "om.pssd.repository.description.destroy";
	}

	public String description() {
		return "Destroy the repository description record.";
	}

	public Interface definition() {
		return _defn;
	}

	public Access access() {
		return ACCESS_MODIFY;
	}

	public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w)
			throws Throwable {

		RepositoryDescription.destroyRepositoryDescription(executor());
	}
}
