package daris.client.model.query.filter.mf;

import arc.mf.client.util.IsNotValid;
import arc.mf.client.util.IsValid;
import arc.mf.client.util.Validity;
import arc.mf.client.xml.XmlWriter;
import daris.client.model.query.filter.Filter;
import daris.client.model.query.filter.operators.CompareOperator;

public class CSizeFilter extends Filter {

	public static enum Unit {
		B, KB, MB, GB;
		public long numberOfBytes() {
			switch (this) {
			case KB:
				return 1000;
			case MB:
				return 1000000;
			case GB:
				return 1000000000;
			default:
				return 1;
			}
		}
	}

	private CompareOperator _op;
	private Long _csize;

	public CSizeFilter(CompareOperator op, Long csize) {
		_op = op;
		_csize = csize;
	}

	public CSizeFilter() {
		this(null, null);
	}

	public CompareOperator operator() {
		return _op;
	}

	public Long csize() {
		return _csize;
	}

	@Override
	public void save(StringBuilder sb) {
		sb.append("csize" + _op.value() + _csize);
	}

	@Override
	protected void saveXml(XmlWriter w) {
		w.add("csize", new String[] { "operator", _op.value() }, _csize);
	}

	@Override
	public Validity valid() {
		if (_op == null) {
			return new IsNotValid("csize operator is not set.");
		}
		if (_csize == null) {
			return new IsNotValid("csize value is not set.");
		}
		return IsValid.INSTANCE;
	}

	@Override
	public Filter copy() {
		return new CSizeFilter(_op, _csize);
	}

	public void setOperator(CompareOperator op) {
		_op = op;

	}

	public void setCSize(Long csize) {
		if (csize == null || csize < 0) {
			_csize = null;
		} else {
			_csize = csize;
		}
	}

	public void setCSize(Long n, Unit unit) {
		if (n == null || n < 0) {
			_csize = null;
			return;
		}
		_csize = n * unit.numberOfBytes();
	}
}
