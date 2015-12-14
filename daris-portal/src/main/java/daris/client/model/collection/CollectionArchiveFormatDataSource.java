package daris.client.model.collection;

import java.util.ArrayList;
import java.util.List;

import arc.mf.dtype.DynamicEnumerationDataHandler;
import arc.mf.dtype.DynamicEnumerationDataSource;
import arc.mf.dtype.DynamicEnumerationExistsHandler;
import arc.mf.dtype.EnumerationType.Value;
import arc.mf.object.ObjectResolveHandler;
import daris.client.model.collection.archive.ArchiveFormat;
import daris.client.model.object.DObjectRef;

public class CollectionArchiveFormatDataSource
        implements DynamicEnumerationDataSource<ArchiveFormat> {

    private String _cid;
    private boolean _includeAttachments;

    public CollectionArchiveFormatDataSource(String cid,
            boolean includeAttachments) {
        _cid = cid;
        _includeAttachments = includeAttachments;
    }

    public CollectionArchiveFormatDataSource(DObjectRef obj,
            boolean includeAttachments) {
        this(obj.id(), includeAttachments);
    }

    @Override
    public boolean supportPrefix() {
        return false;
    }

    @Override
    public void exists(final String value,
            final DynamicEnumerationExistsHandler handler) {
        if (value == null) {
            handler.exists(value, false);
            return;
        }
        ArchiveFormat.availableFormatsForCollection(_cid, _includeAttachments,
                new ObjectResolveHandler<List<ArchiveFormat>>() {

                    @Override
                    public void resolved(List<ArchiveFormat> formats) {
                        handler.exists(value, formats != null && formats
                                .contains(ArchiveFormat.fromString(value)));
                    }
                });
    }

    @Override
    public void retrieve(String prefix, long start, long end,
            final DynamicEnumerationDataHandler<ArchiveFormat> handler) {
        ArchiveFormat.availableFormatsForCollection(_cid, _includeAttachments,
                new ObjectResolveHandler<List<ArchiveFormat>>() {

                    @Override
                    public void resolved(List<ArchiveFormat> formats) {
                        if (formats != null && !formats.isEmpty()) {
                            List<Value<ArchiveFormat>> values = new ArrayList<Value<ArchiveFormat>>(
                                    formats.size());
                            for (ArchiveFormat format : formats) {
                                values.add(new Value<ArchiveFormat>(format));
                            }
                            handler.process(0, formats.size(), formats.size(),
                                    values);
                            return;
                        }
                        handler.process(0, 0, 0, null);
                    }
                });
    }

}
