package daris.client.model.dicom;

import java.util.List;
import java.util.Vector;

import arc.mf.dtype.DynamicEnumerationDataHandler;
import arc.mf.dtype.DynamicEnumerationDataSource;
import arc.mf.dtype.DynamicEnumerationExistsHandler;
import arc.mf.dtype.EnumerationType.Value;
import arc.mf.object.ObjectMessageResponse;
import daris.client.model.dicom.DicomAE.Access;
import daris.client.model.dicom.messages.DicomAEAccess;

public class DicomAEAccessEnum implements DynamicEnumerationDataSource<DicomAE.Access> {

	@Override
	public boolean supportPrefix() {
		return false;
	}

	@Override
	public void exists(String value, DynamicEnumerationExistsHandler handler) {
		handler.exists(value, DicomAE.Access.fromString(value) != null);
	}

	@Override
	public void retrieve(String prefix, final long start, final long end, final DynamicEnumerationDataHandler<Access> handler) {
		new DicomAEAccess().send(new ObjectMessageResponse<List<Access>>(){

			@Override
			public void responded(List<Access> as) {
				List<Value<Access>> values = new Vector<Value<Access>>();
				if(as!=null){
					for(Access a :as){
						values.add(new Value<Access>(a));
					}
				}
				if(values.isEmpty()){
					// at least you can add your private entry
					values.add(new Value<Access>(Access.PRIVATE));
				}
				handler.process(start, end, values.size(), values);
			}});
	}

}
