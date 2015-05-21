package daris.client.model.dicom.messages;

import java.util.List;
import java.util.Vector;

import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlWriter;
import arc.mf.object.ObjectMessage;
import daris.client.model.dicom.DicomAE;
import daris.client.model.dicom.DicomAE.Access;

public class DicomAEAccess extends ObjectMessage<List<DicomAE.Access>>{

	@Override
	protected void messageServiceArgs(XmlWriter w) {
		// no args
	}

	@Override
	protected String messageServiceName() {
		return "om.pssd.dicom.ae.access";
	}

	@Override
	protected List<Access> instantiate(XmlElement xe) throws Throwable {
		if(xe!=null){
			List<String> as = xe.values("access");
			if(as!=null){
				List<Access>  accesses = new Vector<Access>(as.size());
				for (String a : as){
					accesses.add(Access.fromString(a));
				}
				if(!accesses.isEmpty()){
					return accesses;
				}
			}
		}
		return null;
	}

	@Override
	protected String objectTypeName() {
		return null;
	}

	@Override
	protected String idToString() {
		// TODO Auto-generated method stub
		return null;
	}



}
