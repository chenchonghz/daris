package daris.client.model.query.filter;

import arc.mf.client.util.Validity;
import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlWriter;
import arc.mf.model.asset.AssetQueryWhereClause;
import daris.client.model.query.filter.dicom.PatientBirthDateFilter;
import daris.client.model.query.filter.dicom.PatientIdFilter;
import daris.client.model.query.filter.dicom.PatientNameFilter;
import daris.client.model.query.filter.dicom.PatientSexFilter;
import daris.client.model.query.filter.dicom.SeriesModalityFilter;
import daris.client.model.query.filter.dicom.SeriesProtocolFilter;
import daris.client.model.query.filter.dicom.SeriesScanDateFilter;
import daris.client.model.query.filter.dicom.SeriesSizeFilter;
import daris.client.model.query.filter.dicom.StudyIngestDateFilter;
import daris.client.model.query.filter.dicom.StudyScanDateFilter;
import daris.client.model.query.filter.mf.ACLFilter;
import daris.client.model.query.filter.mf.AssetFilter;
import daris.client.model.query.filter.mf.AssetModifiedFilter;
import daris.client.model.query.filter.mf.CIDFilter;
import daris.client.model.query.filter.mf.CTimeFilter;
import daris.client.model.query.filter.mf.CTimeRangeFilter;
import daris.client.model.query.filter.mf.ClassFilter;
import daris.client.model.query.filter.mf.ContentStatusFilter;
import daris.client.model.query.filter.mf.ContentStoreFilter;
import daris.client.model.query.filter.mf.CreatedByFilter;
import daris.client.model.query.filter.mf.MTimeFilter;
import daris.client.model.query.filter.mf.MTimeRangeFilter;
import daris.client.model.query.filter.mf.MetadataFilter;
import daris.client.model.query.filter.mf.ModifiedByFilter;
import daris.client.model.query.filter.mf.NamespaceFilter;
import daris.client.model.query.filter.mf.TextFilter;
import daris.client.model.query.filter.pssd.ObjectCompositeFilter;
import daris.client.model.query.filter.pssd.ObjectMetadataFilter;
import daris.client.model.query.filter.pssd.ObjectTagFilter;
import daris.client.model.query.filter.pssd.ObjectTypeFilter;

public abstract class Filter {
    @Override
    public final String toString() {
        if (!valid().valid()) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        save(sb);
        return sb.toString();
    }

    public abstract void save(StringBuilder sb);

    public final void save(XmlWriter w, String[] attrs) {
        String[] attributes = attrs == null ? new String[2]
                : new String[attrs.length + 2];
        attributes[0] = "class";
        attributes[1] = getClass().getName();
        if (attrs != null) {
            for (int i = 0; i < attrs.length; i++) {
                attributes[i + 2] = attrs[i];
            }
        }
        w.push("filter", attributes);
        saveXml(w);
        w.pop();
    }

    public final void save(XmlWriter w) {
        save(w, null);
    }

    protected abstract void saveXml(XmlWriter w);

    public AssetQueryWhereClause asAssetQueryWhereClause(long total) {
        return new AssetQueryWhereClause(toString(), total);
    }

    public AssetQueryWhereClause asAssetQueryWhereClause() {
        return new AssetQueryWhereClause(toString(), -1);
    }

    public String simpleClassName() {
        String className = getClass().getName();
        return className.substring(className.lastIndexOf('.') + 1);
    }

    public abstract Validity valid();

    public abstract Filter copy();

    public static Filter instantiate(XmlElement xe) throws Throwable {
        String className = xe.value("@class");
        if (CompositeFilter.class.getName().equals(className)) {
            return new CompositeFilter(xe);
        }
        if (ACLFilter.class.getName().equals(className)) {
            return new ACLFilter(xe);
        }
        if (AssetFilter.class.getName().equals(className)) {
            return new AssetFilter(xe);
        }
        if (AssetModifiedFilter.class.getName().equals(className)) {
            return new AssetModifiedFilter(xe);
        }
        if (CIDFilter.class.getName().equals(className)) {
            return new CIDFilter(xe);
        }
        if (ClassFilter.class.getName().equals(className)) {
            return new ClassFilter(xe);
        }
        if (ContentStatusFilter.class.getName().equals(className)) {
            return new ContentStatusFilter(xe);
        }
        if (ContentStoreFilter.class.getName().equals(className)) {
            return new ContentStoreFilter(xe);
        }
        if (CreatedByFilter.class.getName().equals(className)) {
            return new CreatedByFilter(xe);
        }
        if (CTimeFilter.class.getName().equals(className)) {
            return new CTimeFilter(xe);
        }
        if (CTimeRangeFilter.class.getName().equals(className)) {
            return new CTimeRangeFilter(xe);
        }
        if (MetadataFilter.class.getName().equals(className)) {
            return new MetadataFilter(xe);
        }
        if (ModifiedByFilter.class.getName().equals(className)) {
            return new ModifiedByFilter(xe);
        }
        if (MTimeFilter.class.getName().equals(className)) {
            return new MTimeFilter(xe);
        }
        if (MTimeRangeFilter.class.getName().equals(className)) {
            return new MTimeRangeFilter(xe);
        }
        if (NamespaceFilter.class.getName().equals(className)) {
            return new NamespaceFilter(xe);
        }
        if (TextFilter.class.getName().equals(className)) {
            return new TextFilter(xe);
        }
        /*
         * Dicom filters
         */
        if (PatientBirthDateFilter.class.getName().equals(className)) {
            return new PatientBirthDateFilter(xe);
        }
        if (PatientIdFilter.class.getName().equals(className)) {
            return new PatientIdFilter(xe);
        }
        if (PatientNameFilter.class.getName().equals(className)) {
            return new PatientNameFilter(xe);
        }
        if (PatientSexFilter.class.getName().equals(className)) {
            return new PatientSexFilter(xe);
        }
        if (StudyIngestDateFilter.class.getName().equals(className)) {
            return new StudyIngestDateFilter(xe);
        }
        if (StudyScanDateFilter.class.getName().equals(className)) {
            return new StudyScanDateFilter(xe);
        }
        if (SeriesModalityFilter.class.getName().equals(className)) {
            return new SeriesModalityFilter(xe);
        }
        if (SeriesProtocolFilter.class.getName().equals(className)) {
            return new SeriesProtocolFilter(xe);
        }
        if (SeriesScanDateFilter.class.getName().equals(className)) {
            return new SeriesScanDateFilter(xe);
        }
        if (SeriesSizeFilter.class.getName().equals(className)) {
            return new SeriesSizeFilter(xe);
        }
        /*
         * Pssd object filters
         */
        if (ObjectCompositeFilter.class.getName().equals(className)) {
            return new ObjectCompositeFilter(xe);
        }
        if (ObjectMetadataFilter.class.getName().equals(className)) {
            return new ObjectMetadataFilter(xe);
        }
        if (ObjectTagFilter.class.getName().equals(className)) {
            return new ObjectTagFilter(xe);
        }
        if (ObjectTypeFilter.class.getName().equals(className)) {
            return new ObjectTypeFilter(xe);
        }
        throw new IllegalArgumentException(
                "Failed to instantiate filter from XML: " + xe.toString());
    }

    @SuppressWarnings("unchecked")
    public static <T extends Filter> T copy(T filter) {
        return (T) filter.copy();
    }
}
