package daris.client.ui.query.filter.item;

import daris.client.model.query.filter.CompositeFilter;
import daris.client.model.query.filter.Filter;
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
import daris.client.model.query.filter.mf.CTypeFilter;
import daris.client.model.query.filter.mf.ClassFilter;
import daris.client.model.query.filter.mf.ContentStatusFilter;
import daris.client.model.query.filter.mf.ContentStoreFilter;
import daris.client.model.query.filter.mf.CreatedByFilter;
import daris.client.model.query.filter.mf.MetadataFilter;
import daris.client.model.query.filter.mf.ModifiedByFilter;
import daris.client.model.query.filter.mf.NamespaceFilter;
import daris.client.model.query.filter.mf.TextFilter;
import daris.client.model.query.filter.mf.TimeFilter;
import daris.client.model.query.filter.mf.TimeRangeFilter;
import daris.client.model.query.filter.mf.TypeFilter;
import daris.client.model.query.filter.pssd.ObjectMetadataFilter;
import daris.client.model.query.filter.pssd.ObjectTagFilter;
import daris.client.ui.query.filter.form.CompositeFilterForm;
import daris.client.ui.query.filter.item.dicom.PatientBirthDateFilterItem;
import daris.client.ui.query.filter.item.dicom.PatientIdFilterItem;
import daris.client.ui.query.filter.item.dicom.PatientNameFilterItem;
import daris.client.ui.query.filter.item.dicom.PatientSexFilterItem;
import daris.client.ui.query.filter.item.dicom.SeriesModalityFilterItem;
import daris.client.ui.query.filter.item.dicom.SeriesProtocolFilterItem;
import daris.client.ui.query.filter.item.dicom.SeriesScanDateFilterItem;
import daris.client.ui.query.filter.item.dicom.SeriesSizeFilterItem;
import daris.client.ui.query.filter.item.dicom.StudyIngestDateFilterItem;
import daris.client.ui.query.filter.item.dicom.StudyScanDateFilterItem;
import daris.client.ui.query.filter.item.mf.ACLFilterItem;
import daris.client.ui.query.filter.item.mf.AssetFilterItem;
import daris.client.ui.query.filter.item.mf.AssetModifiedFilterItem;
import daris.client.ui.query.filter.item.mf.CIDFilterItem;
import daris.client.ui.query.filter.item.mf.CTypeFilterItem;
import daris.client.ui.query.filter.item.mf.ClassFilterItem;
import daris.client.ui.query.filter.item.mf.ContentStatusFilterItem;
import daris.client.ui.query.filter.item.mf.ContentStoreFilterItem;
import daris.client.ui.query.filter.item.mf.CreatedByFilterItem;
import daris.client.ui.query.filter.item.mf.MetadataFilterItem;
import daris.client.ui.query.filter.item.mf.ModifiedByFilterItem;
import daris.client.ui.query.filter.item.mf.NamespaceFilterItem;
import daris.client.ui.query.filter.item.mf.TextFilterItem;
import daris.client.ui.query.filter.item.mf.TimeFilterItem;
import daris.client.ui.query.filter.item.mf.TimeRangeFilterItem;
import daris.client.ui.query.filter.item.mf.TypeFilterItem;
import daris.client.ui.query.filter.item.pssd.ObjectMetadataFilterItem;
import daris.client.ui.query.filter.item.pssd.ObjectTagFilterItem;

public class FilterItemFactory {

    @SuppressWarnings("unchecked")
    public static <T extends Filter> FilterItem<T> createItem(CompositeFilterForm form, T filter,
            boolean editable) {
        if (filter instanceof CompositeFilter) {
            return (FilterItem<T>) new CompositeFilterItem(form, (CompositeFilter) filter, editable);
        } else if (filter instanceof ACLFilter) {
            return (FilterItem<T>) new ACLFilterItem(form, (ACLFilter) filter, editable);
        } else if (filter instanceof AssetFilter) {
            return (FilterItem<T>) new AssetFilterItem(form, (AssetFilter) filter, editable);
        } else if (filter instanceof AssetModifiedFilter) {
            return (FilterItem<T>) new AssetModifiedFilterItem(form, (AssetModifiedFilter) filter,
                    editable);
        } else if (filter instanceof CIDFilter) {
            return (FilterItem<T>) new CIDFilterItem(form, (CIDFilter) filter, editable);
        } else if (filter instanceof ClassFilter) {
            return (FilterItem<T>) new ClassFilterItem(form, (ClassFilter) filter, editable);
        } else if (filter instanceof ContentStatusFilter) {
            return (FilterItem<T>) new ContentStatusFilterItem(form, (ContentStatusFilter) filter,
                    editable);
        } else if (filter instanceof ContentStoreFilter) {
            return (FilterItem<T>) new ContentStoreFilterItem(form, (ContentStoreFilter) filter,
                    editable);
        } else if (filter instanceof CreatedByFilter) {
            return (FilterItem<T>) new CreatedByFilterItem(form, (CreatedByFilter) filter, editable);
        } else if (filter instanceof TimeFilter) {
            return (FilterItem<T>) new TimeFilterItem(form, (TimeFilter) filter, editable);
        } else if (filter instanceof TimeRangeFilter) {
            return (FilterItem<T>) new TimeRangeFilterItem(form, (TimeRangeFilter) filter, editable);
        } else if (filter instanceof ObjectMetadataFilter) {
            return (FilterItem<T>) new ObjectMetadataFilterItem(form,
                    (ObjectMetadataFilter) filter, editable);
        } else if (filter instanceof ObjectTagFilter) {
            return (FilterItem<T>) new ObjectTagFilterItem(form, (ObjectTagFilter) filter, editable);
        } else if (filter instanceof MetadataFilter) {
            return (FilterItem<T>) new MetadataFilterItem(form, (MetadataFilter) filter, editable);
        } else if (filter instanceof ModifiedByFilter) {
            return (FilterItem<T>) new ModifiedByFilterItem(form, (ModifiedByFilter) filter,
                    editable);
        } else if (filter instanceof NamespaceFilter) {
            return (FilterItem<T>) new NamespaceFilterItem(form, (NamespaceFilter) filter, editable);
        } else if (filter instanceof TextFilter) {
            return (FilterItem<T>) new TextFilterItem(form, (TextFilter) filter, editable);
        } else if (filter instanceof TypeFilter) {
            return (FilterItem<T>) new TypeFilterItem(form, (TypeFilter) filter, editable);
        } else if (filter instanceof CTypeFilter) {
            return (FilterItem<T>) new CTypeFilterItem(form, (CTypeFilter) filter, editable);
        } else if (filter instanceof PatientBirthDateFilter) {
            return (FilterItem<T>) new PatientBirthDateFilterItem(form,
                    (PatientBirthDateFilter) filter, editable);
        } else if (filter instanceof PatientIdFilter) {
            return (FilterItem<T>) new PatientIdFilterItem(form, (PatientIdFilter) filter, editable);
        } else if (filter instanceof PatientNameFilter) {
            return (FilterItem<T>) new PatientNameFilterItem(form, (PatientNameFilter) filter,
                    editable);
        } else if (filter instanceof PatientSexFilter) {
            return (FilterItem<T>) new PatientSexFilterItem(form, (PatientSexFilter) filter,
                    editable);
        } else if (filter instanceof SeriesScanDateFilter) {
            return (FilterItem<T>) new SeriesScanDateFilterItem(form,
                    (SeriesScanDateFilter) filter, editable);
        } else if (filter instanceof SeriesProtocolFilter) {
            return (FilterItem<T>) new SeriesProtocolFilterItem(form,
                    (SeriesProtocolFilter) filter, editable);
        } else if (filter instanceof SeriesModalityFilter) {
            return (FilterItem<T>) new SeriesModalityFilterItem(form,
                    (SeriesModalityFilter) filter, editable);
        } else if (filter instanceof SeriesSizeFilter) {
            return (FilterItem<T>) new SeriesSizeFilterItem(form, (SeriesSizeFilter) filter,
                    editable);
        } else if (filter instanceof StudyScanDateFilter) {
            return (FilterItem<T>) new StudyScanDateFilterItem(form, (StudyScanDateFilter) filter,
                    editable);
        } else if (filter instanceof StudyIngestDateFilter) {
            return (FilterItem<T>) new StudyIngestDateFilterItem(form,
                    (StudyIngestDateFilter) filter, editable);
        }
        throw new AssertionError("Filter item for filter class: " + filter.getClass().getName()
                + " is not available yet.");
    }

}
