package daris.client.model.query.messages;

import java.util.List;

import arc.mf.client.Output;
import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlWriter;
import arc.mf.object.Null;
import arc.mf.object.ObjectMessage;
import daris.client.model.query.ResultCollectionRef;
import daris.client.model.query.options.QueryOptions;
import daris.client.model.query.options.QueryOptions.Purpose;

public class QueryResultExport extends ObjectMessage<Null> {

    public static enum Format {
        xml, csv
    }

    private String _where;
    private QueryOptions _options;
    private long _offset = -1;
    private int _size = -1;
    private Format _format;
    private String _ofn;

    protected QueryResultExport(String where, QueryOptions options, long offset, int size, Format format,
            String outputFileName) {
        _where = where;
        _options = options;
        _size = size;
        _offset = offset;
        _format = format;
        _ofn = outputFileName;
    }

    public QueryResultExport(ResultCollectionRef<?> rc, long offset, int size, Format format, String outputFileName) {
        this(rc.query().filter().toString(), rc.query().options(), offset, size, format, outputFileName);
    }

    public QueryResultExport(ResultCollectionRef<?> rc, Format format, String outputFileName) {
        this(rc.query().filter().toString(), rc.query().options(), -1, -1, format, outputFileName);
    }

    public int size() {
        return _size;
    }

    public void setSize(int size) {
        _size = size;
    }

    public long offset() {
        return _offset;
    }

    public void setOffset(long offset) {
        _offset = offset;
    }

    public void setOutputFormat(Format format) {
        if (format != _format) {
            _format = format;
        }
    }

    public void setOutputFileName(String fileName) {
        _ofn = fileName;
        String ext = "." + _format;
        if (!(fileName.endsWith(ext) || fileName.endsWith(ext.toUpperCase()))) {
            _ofn += ext;
        }
    }

    @Override
    protected void messageServiceArgs(XmlWriter w) {
        w.add("where", _where);
        _options.save(w, Purpose.EXPORT);
        if (_offset >= 0) {
            w.add("idx", _offset + 1);
        }
        if (_size > 0) {
            w.add("size", _size);
        } else {
            w.add("size", "infinity");
        }
        w.add("output-format", _format);
    }

    @Override
    protected String messageServiceName() {
        return "asset.query";
    }

    @Override
    protected Null instantiate(XmlElement xe) throws Throwable {
        return new Null();
    }

    @Override
    protected String objectTypeName() {
        return Null.class.getName();
    }

    @Override
    protected String idToString() {
        return null;
    }

    @Override
    protected int numberOfOutputs() {
        return 1;
    }

    @Override
    protected void process(Null o, List<Output> outputs) throws Throwable {
        if (outputs != null && !outputs.isEmpty()) {
            for (Output output : outputs) {
                output.download(_ofn);
            }
        }
    }

    public Format outputFormat() {
        return _format;
    }

    public String outputFileName() {
        return _ofn;
    }

}
