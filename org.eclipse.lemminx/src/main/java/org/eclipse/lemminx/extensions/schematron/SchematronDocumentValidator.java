package org.eclipse.lemminx.extensions.schematron;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.eclipse.lemminx.commons.BadLocationException;
import org.eclipse.lemminx.dom.DOMDocument;
import org.eclipse.lemminx.dom.DOMElement;
import org.eclipse.lemminx.dom.DOMNode;
import org.eclipse.lemminx.utils.XMLPositionUtility;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import name.dmaus.schxslt.Result;
import name.dmaus.schxslt.Schematron;
import name.dmaus.schxslt.SchematronException;

public class SchematronDocumentValidator {

	private static final Range ZERO_RANGE = new Range(new Position(0, 0), new Position(0, 1));

	// Example:
	// failed-assert /Q{}Person[1] If the Title is "Mr" then the gender of the person must be "Male".
	private static final Pattern SCHEMATRON_MESSAGE_DECODER = Pattern.compile("failed-assert ([^ ]+) (.*)");

	private final XPathFactory xpathFactory = XPathFactory.newInstance();

	private final DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();

	Logger LOGGER = Logger.getLogger(SchematronDocumentValidator.class.getName());

	public List<Diagnostic> validate(DOMDocument xmlDocument, List<File> schemaFiles, CancelChecker cancelChecker) {
		List<Diagnostic> diagnostics = new ArrayList<>();
		try {
			URI xmlDocumentURI = new URI(xmlDocument.getDocumentURI());
			File xmlDocumentFile = new File(xmlDocumentURI);
			for (File schema : schemaFiles) {
				Schematron schematron = new Schematron(new StreamSource(schema));
				try {
					Result validationResult = schematron.validate(new StreamSource(xmlDocumentFile));
					if (!validationResult.isValid()) {
						for (String message : validationResult.getValidationMessages()) {
							diagnostics.add(getSchematronMessageAsDiagnostic(message, xmlDocument, xmlDocumentFile));
						}
					}
					LOGGER.info(xmlDocumentFile.getAbsolutePath() + " was valid according to " + schema.getAbsolutePath());
				} catch (SchematronException e) {
					LOGGER.log(Level.SEVERE, "Unable to validate against schema: " + schema.getAbsolutePath(), e);
				}
				cancelChecker.checkCanceled();
			}
		} catch (URISyntaxException e1) {
			LOGGER.log(Level.SEVERE, "Unable to turn document URI into a URI", e1);
		}
		return diagnostics;
	}

	private Diagnostic getSchematronMessageAsDiagnostic(String message, DOMDocument xmlDocument, File xmlDocumentFile) {

		Diagnostic d = new Diagnostic(ZERO_RANGE, message);

		Matcher m = SCHEMATRON_MESSAGE_DECODER.matcher(message);

		if (m.find()) {
			d.setMessage(m.group(2));
			try {
				String expression = m.group(1);
				expression = expression.replace("Q{}", "");
				expression = expression.replaceFirst("\\[1\\]", "");
				XPath xpath = xpathFactory.newXPath();
				XPathExpression compiledExpression = xpath.compile(expression);
				NodeList nodeList = (NodeList) compiledExpression.evaluate(xmlDocument, XPathConstants.NODESET);
				if (nodeList.getLength() > 0) {
					Node node = nodeList.item(0);
					DOMNode domNode = (DOMNode) node;
					d.setRange(getRangeFromDOMNode(domNode, xmlDocument));
				}
			} catch (Exception e) {
			}
		}

		return d;

	}

	private Range getRangeFromDOMNode(DOMNode node, DOMDocument xmlDocument) throws BadLocationException {
		switch (node.getNodeType()) {
		case Node.ELEMENT_NODE:
			DOMElement element = (DOMElement) node;
			return XMLPositionUtility.selectStartTagName(element);
		default:
			return new Range(xmlDocument.positionAt(node.getStart()), xmlDocument.positionAt(node.getEnd()));
		}
	}

}
