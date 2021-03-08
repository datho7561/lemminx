package org.eclipse.lemminx.extensions.contentmodel.participants;

import java.util.Collections;
import java.util.List;

import org.apache.xerces.xni.XMLLocator;
import org.eclipse.lemminx.dom.DOMDocument;
import org.eclipse.lemminx.utils.XMLPositionUtility;
import org.eclipse.lsp4j.DiagnosticRelatedInformation;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.Range;

public class XMLRelatedInfoFinder implements IRelatedInfoFinder {

	private static String CLOSING_TAG_EXPECTED_HERE = "Closing tag expected here";

	@Override
	public List<DiagnosticRelatedInformation> findRelatedInformation(XMLLocator location, String errorKey,
			DOMDocument document) {

		XMLSyntaxErrorCode syntaxCode = XMLSyntaxErrorCode.get(errorKey);

		if (syntaxCode == null) {
			return Collections.emptyList();
		}

		int offset = location.getCharacterOffset() - 1;
		int documentSize = document.getEnd() + 1;

		switch (syntaxCode) {
		case ElementUnterminated:
		case ETagRequired: {
			Range range = XMLPositionUtility.createRange(offset - 1, offset - 1, document);
			return Collections.singletonList(new DiagnosticRelatedInformation(
					new Location(document.getDocumentURI(), range), CLOSING_TAG_EXPECTED_HERE));
		}
		case MarkupEntityMismatch: {
			Range range = XMLPositionUtility.createRange(documentSize - 1, documentSize - 1, document);
			return Collections.singletonList(new DiagnosticRelatedInformation(
					new Location(document.getDocumentURI(), range), CLOSING_TAG_EXPECTED_HERE));
		}
		default:
		}
		return Collections.emptyList();
	}

}
