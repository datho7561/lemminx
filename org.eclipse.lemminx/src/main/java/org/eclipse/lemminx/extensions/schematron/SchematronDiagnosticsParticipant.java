package org.eclipse.lemminx.extensions.schematron;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.eclipse.lemminx.dom.DOMDocument;
import org.eclipse.lemminx.dom.XMLModel;
import org.eclipse.lemminx.extensions.contentmodel.settings.XMLValidationSettings;
import org.eclipse.lemminx.services.extensions.diagnostics.IDiagnosticsParticipant;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

public class SchematronDiagnosticsParticipant implements IDiagnosticsParticipant {

	private final Logger LOGGER = Logger.getLogger(SchematronDiagnosticsParticipant.class.getName());

	private SchematronSchemaCache cache = new SchematronSchemaCache();
	private SchematronDocumentValidator validator = new SchematronDocumentValidator();

	@Override
	public void doDiagnostics(DOMDocument xmlDocument, List<Diagnostic> diagnostics,
			XMLValidationSettings validationSettings, CancelChecker cancelChecker) {
		List<File> files = getSchemaFiles(xmlDocument);
		if (files == null) {
			LOGGER.warning("No Schematrons found");
			return;
		}
		cancelChecker.checkCanceled();
		for (File file : files) {
			LOGGER.info("Schematron Schema " + file.getAbsolutePath() + " found");
		}
		diagnostics.addAll(validator.validate(xmlDocument, files, cancelChecker));
	}

	/**
	 * Returns a list of the resolved location of all the schemas as URIs, or null if there are no schemas
	 *
	 * @return a list of the resolved location of all the schemas as URIs, or null if there are no schemas
	 */
	private List<File> getSchemaFiles(DOMDocument xmlDocument) {
		if (!xmlDocument.hasXMLModel()) {
			return null;
		}
		List<File> files = new ArrayList<>();

		for (XMLModel xmlModel : xmlDocument.getXMLModels()) {
			String href = xmlModel.getHref();
			File file = new File(href);
			if (file.exists()) {
				files.add(file);
			}
		}
		return files.size() > 0 ? files : null;
	}

}
