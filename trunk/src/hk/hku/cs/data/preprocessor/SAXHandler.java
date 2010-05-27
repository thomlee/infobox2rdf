package hk.hku.cs.data.preprocessor;

import org.xml.sax.*;
import org.xml.sax.helpers.*;
import java.util.*;

abstract class SAXHandler extends DefaultHandler {
	List<String> parseErrors;
	List<String> parseWarnings;
	
	SAXHandler() {
		super();
		this.resetErrorsAndWarnings();
	}
	
	void resetErrorsAndWarnings() {
		this.parseErrors = new ArrayList<String>();
		this.parseWarnings = new ArrayList<String>();
	}

	public void error(SAXParseException e) {
		this.parseErrors.add("(line " + e.getLineNumber() + ", column " + e.getColumnNumber() +
				"): "+ e.getMessage());
	}

	public void fatalError(SAXParseException e) {
		this.parseErrors.add("(line " + e.getLineNumber() + ", column " + e.getColumnNumber() +
				"): "+ e.getMessage());
	}

	public void warning(SAXParseException e) {
		this.parseWarnings.add("(line " + e.getLineNumber() + ", column " + e.getColumnNumber() +
				"): "+ e.getMessage());
	}
}
