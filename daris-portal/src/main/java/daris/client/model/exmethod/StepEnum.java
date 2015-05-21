package daris.client.model.exmethod;

import java.util.List;
import java.util.Vector;

import arc.mf.dtype.DynamicEnumerationDataHandler;
import arc.mf.dtype.DynamicEnumerationDataSource;
import arc.mf.dtype.DynamicEnumerationExistsHandler;
import arc.mf.dtype.EnumerationType.Value;
import arc.mf.object.ObjectMessageResponse;
import daris.client.model.exmethod.messages.ExMethodStudyStepFind;
import daris.client.model.study.Study;

public class StepEnum implements DynamicEnumerationDataSource<StepItem> {

	private String _exMethodId;
	private String _studyType;

	public StepEnum(String exMethodId, String studyType) {

		_exMethodId = exMethodId;
		_studyType = studyType;
	}
	
	public StepEnum(Study study) {
		this(study.exMethodId(), study.studyType());
	}

	@Override
	public boolean supportPrefix() {

		return false;
	}

	@Override
	public void exists(final String value, final DynamicEnumerationExistsHandler handler) {

		new ExMethodStudyStepFind(_exMethodId, null, _studyType).send(new ObjectMessageResponse<List<StepItem>>() {

			@Override
			public void responded(List<StepItem> ss) {
				if (ss != null) {
					for (StepItem s : ss) {
						if (s.toString().equals(value)) {
							handler.exists(value, true);
							return;
						}
					}
				}
				handler.exists(value, false);
			}
		});

	}

	@Override
	public void retrieve(String prefix, final long start, final long end,
			final DynamicEnumerationDataHandler<StepItem> handler) {

		new ExMethodStudyStepFind(_exMethodId,null, _studyType).send(new ObjectMessageResponse<List<StepItem>>() {

			@Override
			public void responded(List<StepItem> ss) {
				if (ss == null) {
					handler.process(0, 0, 0, null);
					return;
				}
				List<Value<StepItem>> values = new Vector<Value<StepItem>>(ss.size());
				for (StepItem s : ss) {
					values.add(new Value<StepItem>(s));
				}
				List<Value<StepItem>> rvs = values;
				int start1 = (int) start;
				int end1 = (int) end;
				long total = values.size();
				if (start1 > 0 || end1 < values.size()) {
					if (start1 >= values.size()) {
						rvs = null;
					} else {
						if (end1 > values.size()) {
							end1 = values.size();
						}
						rvs = values.subList(start1, end1);
					}
				}
				handler.process(start1, end1, total, rvs);
			}
		});
	}

}
