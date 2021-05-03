package org.eclipse.lemminx.extensions.schematron;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class SchematronSchemaCache {

	private List<URI> cached;

	public SchematronSchemaCache() {
		cached = new ArrayList<>();
	}

	public File getSchema(URI schema) {
		String path = schema.getPath();
		if (path == null) {
			return null;
		}
		File file = new File(path);
		if (!file.exists()) {
			return null;
		}
		return file;
	}

}
