package daris.client.model.doc;

import java.util.Comparator;

import arc.mf.model.asset.document.MetadataDocument;

public class MetadataDocumentComparator implements Comparator<MetadataDocument> {

	@Override
	public int compare(MetadataDocument doc1, MetadataDocument doc2) {

		String path1 = doc1.path();
		String ns1 = MetadataDocument.namespacePart(path1);
		String name1 = MetadataDocument.namePart(path1);
		String path2 = doc2.path();
		String ns2 = MetadataDocument.namespacePart(path2);
		String name2 = MetadataDocument.namePart(path2);
		if (ns1 == null && ns2 != null) {
			return -1;
		}
		if (ns1 != null && ns2 == null) {
			return 1;
		}
		if (ns1 != null && ns2 != null) {
			int r = ns1.compareTo(ns2);
			if (r == 0) {
				return name1.compareTo(name2);
			} else {
				return r;
			}
		}
		return name1.compareTo(name2);
	}

}
