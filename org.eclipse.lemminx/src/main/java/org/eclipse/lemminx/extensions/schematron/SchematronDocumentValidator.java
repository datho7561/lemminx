package org.eclipse.lemminx.extensions.schematron;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.xml.transform.stream.StreamSource;

import org.eclipse.lemminx.dom.DOMDocument;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

import name.dmaus.schxslt.Result;
import name.dmaus.schxslt.Schematron;
import name.dmaus.schxslt.SchematronException;

public class SchematronDocumentValidator {

	private static final Range ZERO_RANGE = new Range(new Position(0, 0), new Position(0, 1));

	Logger LOGGER = Logger.getLogger(SchematronDocumentValidator.class.getName());

	public List<Diagnostic> validate(DOMDocument xmlDocument, List<File> schemaFiles, CancelChecker cancelChecker) {

		List<Diagnostic> diagnostics = new ArrayList<>();
		String xmlDocumentURI = xmlDocument.getDocumentURI();
		File xmlDocumentFile = new File(xmlDocumentURI);

		for (File schema : schemaFiles) {
			Schematron schematron = new Schematron(new StreamSource(schema));
			try {
				Result validationResult = schematron.validate(new StreamSource(xmlDocumentFile));
				if (!validationResult.isValid()) {
					diagnostics.addAll(getDiagnosticFromErrorMessage(validationResult.getValidationMessages()));
				}
				LOGGER.info(xmlDocumentFile.getAbsolutePath() + " was valid according to " + schema.getAbsolutePath());
			} catch (SchematronException e) {
				LOGGER.warning("Unable to validate against schema: " + schema.getAbsolutePath());
				e.printStackTrace();
			}
			cancelChecker.checkCanceled();
		}

		return diagnostics;

	}

	private List<Diagnostic> getDiagnosticFromErrorMessage(List<String> errorMessages) {
		return errorMessages.stream() //
				.map(msg -> new Diagnostic(ZERO_RANGE, msg))
				.collect(Collectors.toList());
	}

}
