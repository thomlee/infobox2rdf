package hk.hku.cs.data.preprocessor;

import org.xml.sax.*;
import org.xml.sax.helpers.*;
import java.io.*;
import java.util.*;

public class InfoboxDataCleanser {
	private static class InfoboxDataHandler extends SAXHandler {
		static final String INFOBOX_NAME_ELEMENT_NAME = "name";
		static final String PROPERTY_ELEMENT_NAME = "property";
		static final String VALUE_ELEMENT_NAME = "value";
		SimpleXMLWriter xmlWriter;
		String elementContent;
		int cleansedInfoboxCount;
		
		InfoboxDataHandler(String outputXmlPathname) throws FileNotFoundException {
			super();
			PrintStream outputStream = new PrintStream(new File(outputXmlPathname));
			this.xmlWriter = new SimpleXMLWriter(outputStream);
			this.elementContent = "";
			this.cleansedInfoboxCount = 0;
		}
		
		public void startElement(String uri, String localName, String qName, Attributes attributes) {
			this.xmlWriter.writeStartTag(localName);
			this.elementContent = "";
		}
		
		public void characters(char ch[], int start, int length) {
			this.elementContent += new String(ch, start, length);
		}
		
		public void endElement(String uri, String localName, String qName) {
			this.elementContent = this.convertHtmlSpaceCode(this.elementContent);
			
			if (localName.equals(INFOBOX_NAME_ELEMENT_NAME)) {
				this.xmlWriter.writeText(cleanseAndResolveInfoboxName(this.elementContent));
				System.out.print(++this.cleansedInfoboxCount + " infoboxes cleansed\r");
			}
			else if (localName.equals(PROPERTY_ELEMENT_NAME)) {
				this.xmlWriter.writeText(cleanseInfoboxPredicateName(this.elementContent));
			}
			else if (localName.equals(VALUE_ELEMENT_NAME)) {
				this.xmlWriter.writeText(cleanseData(this.elementContent));
			}
			else {
				if (!this.elementContent.startsWith("\n")) {
					this.xmlWriter.writeText(this.elementContent);
				}
			}
			
			this.xmlWriter.writeEndTag(localName);
			this.elementContent = "";
		}
		
		private String convertHtmlSpaceCode(String inputStr) {
			return inputStr.replaceAll("&nbsp;", " ");
		}
		
		protected void finalize() {
			this.xmlWriter.close();
		}
	}
	
	private List<String> parseErrors;
	private List<String> parseWarnings;
	
	InfoboxDataCleanser() {
		this.parseErrors = new ArrayList<String>();
		this.parseWarnings = new ArrayList<String>();
	}
	
	void initRedirect(String templateXmlPathname) throws Exception {
		try {
			InfoboxRedirectResolver.initRedirect(templateXmlPathname);
		}
		catch (SAXParseException e) {
			this.parseErrors = InfoboxRedirectResolver.getErrors();
			this.parseWarnings = InfoboxRedirectResolver.getWarnings();
		}
		catch (Exception e) {
			throw e;
		}
	}
	
	void cleanse(String dataXmlPathname, String outputXmlPathname) throws Exception {
		System.out.println("Cleansing infobox data...");
		
		try {
			InfoboxDataHandler dataHandler = new InfoboxDataHandler(outputXmlPathname);
			this.parseErrors = dataHandler.parseErrors;
			this.parseWarnings = dataHandler.parseWarnings;

			XMLReader xmlReader = XMLReaderFactory.createXMLReader();
			xmlReader.setContentHandler(dataHandler);
			xmlReader.setErrorHandler(dataHandler);

			FileReader fileReader = new FileReader(dataXmlPathname);
			xmlReader.parse(new InputSource(fileReader));
			System.out.println("\nCleansing completed");
		}
		catch (SAXParseException e) {
			System.out.println("\nCleansing aborted");
		}
		catch (Exception e) {
			System.out.println(e.getMessage());
			System.out.println("\nCleansing aborted");
			throw e;
		}
	}
	
	private static String cleanseAndResolveInfoboxName(String infoboxName) {
		String cleansedInfoboxName = cleanseData(infoboxName);

		return InfoboxRedirectResolver.resolveRedirect(cleansedInfoboxName);
	}

	private static String cleanseInfoboxPredicateName(String predicateName) {
		String cleansedPredicateName = cleanseData(predicateName);
		
		return cleansedPredicateName.toLowerCase();
	}

	private static String cleanseData(String inputStr) {
		String cleansedStr = HtmlTagCleanser.cleanse(inputStr);
		cleansedStr = WikiMarkupCleanser.cleanse(cleansedStr);
		
		return cleansedStr.replaceAll("\\s{2,}", " ").trim();
	}
	
	boolean isErrorFound() {
		return (!this.parseErrors.isEmpty());
	}

	boolean isWarningFound() {
		return (!this.parseWarnings.isEmpty());
	}

	List<String> getErrors() {
		return Collections.unmodifiableList(this.parseErrors);
	}

	List<String> getWarnings() {
		return Collections.unmodifiableList(this.parseWarnings);
	}
}