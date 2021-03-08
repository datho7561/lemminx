package org.eclipse.lemminx.extensions.contentmodel.participants;

import java.util.ArrayList;
import java.util.List;

import org.apache.xerces.xni.XMLLocator;
import org.eclipse.lemminx.dom.DOMDocument;
import org.eclipse.lsp4j.DiagnosticRelatedInformation;

public class AggregateRelatedInfoFinder implements IRelatedInfoFinder {

	private static IRelatedInfoFinder[] RELATED_INFO_FINDERS = {
		new XMLRelatedInfoFinder()
	};

	private static AggregateRelatedInfoFinder INSTANCE = null;

	private AggregateRelatedInfoFinder() {}

	public static AggregateRelatedInfoFinder getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new AggregateRelatedInfoFinder();
		}
		return INSTANCE;
	}

	@Override
	public List<DiagnosticRelatedInformation> findRelatedInformation(
			XMLLocator location,
			String errorKey,
			DOMDocument document) {
		List<DiagnosticRelatedInformation> relatedInfo = new ArrayList<>();
		for (IRelatedInfoFinder relatedInfoFinder : RELATED_INFO_FINDERS) {
			relatedInfo.addAll(relatedInfoFinder.findRelatedInformation(
				location,
				errorKey,
				document
			));
		}
		return relatedInfo;
	}

}
