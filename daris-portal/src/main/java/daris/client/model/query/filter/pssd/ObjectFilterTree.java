package daris.client.model.query.filter.pssd;

import daris.client.model.object.DObjectRef;
import daris.client.model.query.filter.FilterTree;
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
import daris.client.model.query.filter.mf.ACLFilter.ACLOperator;
import daris.client.model.query.filter.mf.AssetFilter;
import daris.client.model.query.filter.mf.CIDFilter;
import daris.client.model.query.filter.mf.CSizeFilter;
import daris.client.model.query.filter.mf.CTimeFilter;
import daris.client.model.query.filter.mf.CTimeRangeFilter;
import daris.client.model.query.filter.mf.CTypeFilter;
import daris.client.model.query.filter.mf.ContentStatusFilter;
import daris.client.model.query.filter.mf.ContentStoreFilter;
import daris.client.model.query.filter.mf.CreatedByFilter;
import daris.client.model.query.filter.mf.MTimeFilter;
import daris.client.model.query.filter.mf.MTimeRangeFilter;
import daris.client.model.query.filter.mf.ModifiedByFilter;
import daris.client.model.query.filter.mf.NamespaceFilter;
import daris.client.model.query.filter.mf.TextFilter;
import daris.client.model.query.filter.mf.TypeFilter;

public class ObjectFilterTree extends FilterTree {

	public ObjectFilterTree(DObjectRef project) {

		super(
				new FilterEntry[] {
						new FilterEntry("composite", new ObjectCompositeFilter(
								project, null),
								"Composite filter (drag to filter pane on left)"),

						new FilterEntry("metadata", new ObjectMetadataFilter(
								project, null, null, null, null, false),
								"Object metadata filter (drag to filter pane on left)"),

						new FilterEntry("tag", new ObjectTagFilter(project,
								null, null),
								"Object tag filter (drag to filter pane on left)"),

						new FilterEntry("text",
								new TextFilter(null, null, null),
								"Text filter (drag to filter pane on left)"),

						new FilterEntry("asset/acl", new ACLFilter(
								ACLOperator.FOR_ROLE, null),
								"Asset ACL filter (drag to filter pane on left)"),

						new FilterEntry("asset/cid", new CIDFilter(),
								"Asset CID filter (drag to filter pane on left)"),

						new FilterEntry("asset/type", new TypeFilter(),
								"Asset MIME type filter (drag to filter pane on left) "),

						new FilterEntry("asset/ctype", new CTypeFilter(),
								"Asset content MIME type filter (drag to filter pane on left)"),

						new FilterEntry("asset/csize", new CSizeFilter(),
								"Asset content size filter (drag to filter pane on left)"),

						new FilterEntry("asset/content-status",
								new ContentStatusFilter(),
								"Asset content status filter (drag to filter pane on left)"),

						new FilterEntry("asset/content-store",
								new ContentStoreFilter(),
								"Asset content store filter (drag to filter pane on left)"),

						new FilterEntry("asset/created-by",
								new CreatedByFilter(),
								"Asset created-by filter (drag to filter pane on left)"),

						new FilterEntry("asset/ctime", new CTimeFilter(),
								"Asset ctime filter"),

						new FilterEntry("asset/ctime.range",
								new CTimeRangeFilter(),
								"Asset ctime range filter (drag to filter pane on left)"),

						new FilterEntry("asset/mtime", new MTimeFilter(),
								"Asset mtime filter"),

						new FilterEntry("asset/mtime.range",
								new MTimeRangeFilter(),
								"Asset mtime range filter (drag to filter pane on left)"),

						new FilterEntry("asset/modified-by",
								new ModifiedByFilter(),
								"Asset modified-by filter (drag to filter pane on left)"),

						new FilterEntry("asset/namespace",
								new NamespaceFilter(),
								"Asset namespace filter (drag to filter pane on left)"),

						new FilterEntry("asset/property", new AssetFilter(null,
								null),
								"Asset filter (drag to filter pane on left)"),

						new FilterEntry("dicom/patient/id",
								new PatientIdFilter(),
								"Dicom patient id filter (drag to filter pane on left)"),

						new FilterEntry("dicom/patient/name",
								new PatientNameFilter(),
								"Dicom patient name filter (drag to filter pane on left)"),

						new FilterEntry("dicom/patient/sex",
								new PatientSexFilter(),
								"Dicom patient sex filter (drag to filter pane on left)"),

						new FilterEntry("dicom/patient/birth date",
								new PatientBirthDateFilter(),
								"Dicom patient birth date filter (drag to filter pane on left)"),

						new FilterEntry("dicom/study/scan date",
								new StudyScanDateFilter(),
								"Dicom study scan date filter (drag to filter pane on left)"),

						new FilterEntry("dicom/study/ingest date",
								new StudyIngestDateFilter(),
								"Dicom study ingest date filter (drag to filter pane on left)"),

						new FilterEntry("dicom/series/protocol",
								new SeriesProtocolFilter(),
								"Dicom series protocol filter (drag to filter pane on left)"),

						new FilterEntry("dicom/series/modality",
								new SeriesModalityFilter(),
								"Dicom series modality filter (drag to filter pane on left)"),

						new FilterEntry("dicom/series/size",
								new SeriesSizeFilter(),
								"Dicom series size filter (drag to filter pane on left)"),

						new FilterEntry("dicom/series/scan date",
								new SeriesScanDateFilter(),
								"Dicom series scan date filter (drag to filter pane on left)")

				});

	}
}
