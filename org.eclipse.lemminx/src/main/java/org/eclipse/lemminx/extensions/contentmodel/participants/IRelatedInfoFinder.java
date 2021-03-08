package org.eclipse.lemminx.extensions.contentmodel.participants;

import java.util.List;

import org.apache.xerces.xni.XMLLocator;
import org.eclipse.lemminx.dom.DOMDocument;
import org.eclipse.lsp4j.DiagnosticRelatedInformation;

public interface IRelatedInfoFinder {

	List<DiagnosticRelatedInformation> findRelatedInformation(
			XMLLocator location,
			String errorKey,
			DOMDocument document);

}
