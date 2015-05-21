package daris.client.model.type;

import java.util.ArrayList;
import java.util.List;

import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlStringWriter;
import arc.mf.dtype.DynamicEnumerationDataHandler;
import arc.mf.dtype.DynamicEnumerationDataSource;
import arc.mf.dtype.DynamicEnumerationExistsHandler;
import arc.mf.dtype.EnumerationType.Value;
import arc.mf.model.type.MimeTypeRef;
import arc.mf.object.ObjectRef;
import arc.mf.object.ObjectResolveHandler;

public class TypeEnum extends ObjectRef<List<MimeTypeRef>> implements DynamicEnumerationDataSource<MimeTypeRef> {

	private String _stype;

	public TypeEnum() {
		this(null);
	}

	public TypeEnum(String stype) {
		_stype = stype;
	}

	@Override
	public boolean supportPrefix() {
		return false;
	}

	@Override
	public void exists(final String value, final DynamicEnumerationExistsHandler handler) {
		resolve(new ObjectResolveHandler<List<MimeTypeRef>>() {

			@Override
			public void resolved(List<MimeTypeRef> types) {
				if (types != null && !types.isEmpty()) {
					for (MimeTypeRef type : types) {
						if (type.type().equals(value)) {
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
			final DynamicEnumerationDataHandler<MimeTypeRef> handler) {
		resolve(new ObjectResolveHandler<List<MimeTypeRef>>() {

			@Override
			public void resolved(List<MimeTypeRef> types) {
				if (types == null || types.isEmpty()) {
					handler.process(0, 0, 0, null);
				}
				List<Value<MimeTypeRef>> vs = new ArrayList<Value<MimeTypeRef>>(types.size());
				for (MimeTypeRef t : types) {
					Value<MimeTypeRef> v = new Value<MimeTypeRef>(t.type(), null, t);
					vs.add(v);
				}
				List<Value<MimeTypeRef>> rvs = vs;
				int start1 = (int) start;
				int end1 = (int) end;
				long total = vs.size();
				if (start1 > 0 || end1 < vs.size()) {
					if (start1 >= vs.size()) {
						rvs = null;
					} else {
						if (end1 > vs.size()) {
							end1 = vs.size();
						}
						rvs = vs.subList(start1, end1);
					}
				}
				handler.process(start1, end1, total, rvs);
				return;
			}
		});
	}

	@Override
	protected void resolveServiceArgs(XmlStringWriter w) {
		if (_stype != null) {
			w.add("stype", _stype);
		}
	}

	@Override
	protected String resolveServiceName() {
		return "type.list";
	}

	@Override
	protected List<MimeTypeRef> instantiate(XmlElement xe) throws Throwable {
		if (xe != null) {
			List<XmlElement> tes = xe.elements("type");
			if (tes != null && !tes.isEmpty()) {
				List<MimeTypeRef> types = new ArrayList<MimeTypeRef>(tes.size());
				for (XmlElement te : tes) {
					types.add(new MimeTypeRef(te.value()));
				}
				return types;
			}
		}
		return null;
	}

	@Override
	public String referentTypeName() {
		return "List of MIME type";
	}

	@Override
	public String idToString() {
		return null;
	}

}
