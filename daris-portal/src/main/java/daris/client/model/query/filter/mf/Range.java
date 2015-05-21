package daris.client.model.query.filter.mf;

import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlWriter;
import daris.client.util.ClassUtil;

public abstract class Range<T> {

    public static final String RANGE_FROM_SPEC_INCLUSIVE = "[";
    public static final String RANGE_FROM_SPEC_EXCLUSIVE = "(";
    public static final String RANGE_TO_SPEC_INCLUSIVE = "]";
    public static final String RANGE_TO_SPEC_EXCLUSIVE = ")";

    private T _from;
    private T _to;
    private boolean _fromInclusive;
    private boolean _toInclusive;

    public Range(T from, boolean fromInclusive, T to, boolean toInclusive) {
        _from = from;
        _to = to;
        _fromInclusive = fromInclusive;
        _toInclusive = toInclusive;
    }

    public Range(XmlElement xe) throws Throwable {
        _from = parse(xe.value("from"));
        _fromInclusive = xe.booleanValue("from/@inclusive", true);
        _to = parse(xe.value("to"));
        _fromInclusive = xe.booleanValue("to/@inclusive", true);

    }

    public T from() {
        return _from;
    }

    public void setFrom(T from) {
        _from = from;
    }

    public boolean fromInclusive() {
        return _fromInclusive;
    }

    public void setFromInclusive(boolean fromInclusive) {
        _fromInclusive = fromInclusive;
    }

    public T to() {
        return _to;
    }

    public void setTo(T to) {
        _to = to;
    }

    public boolean toInclusive() {
        return _toInclusive;
    }

    public void setToInclusive(boolean toInclusive) {
        _toInclusive = toInclusive;
    }

    protected abstract String format(T o);

    protected abstract T parse(String s);

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        save(sb);
        return sb.toString();
    }

    public void save(StringBuilder sb) {
        sb.append(_fromInclusive ? RANGE_FROM_SPEC_INCLUSIVE : RANGE_FROM_SPEC_EXCLUSIVE);
        sb.append(format(_from));
        sb.append(",");
        sb.append(format(_to));
        sb.append(_toInclusive ? RANGE_TO_SPEC_INCLUSIVE : RANGE_TO_SPEC_EXCLUSIVE);
    }

    public void save(XmlWriter w) {
        w.push("range", new String[] { "class", ClassUtil.simpleClassNameOf(_from) });
        w.add("from", new String[] { "inclusive", String.valueOf(_fromInclusive) }, format(_from));
        w.add("to", new String[] { "inclusive", String.valueOf(_toInclusive) }, format(_to));
        w.pop();
    }

}
