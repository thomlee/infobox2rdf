package hk.hku.cs.data.preprocessor;

import org.xml.sax.*;
import org.xml.sax.helpers.*;
import java.io.*;
import java.util.*;
import java.util.regex.*;

class InfoboxRedirectResolver {
	private static class TemplateHandler extends SAXHandler {
		static final String NAME_ELEMENT_NAME = "name";
		static final String CONTENT_ELEMENT_NAME = "content";
		Pattern infoboxTemplatePattern;
		Pattern infoboxRedirectPattern;
		String infoboxName;
		int infoboxTemplateCount;
		boolean isInName;
		boolean isInContent;
		boolean isInfoboxTemplate;

		TemplateHandler() {
			super();
			this.infoboxTemplatePattern = Pattern.compile("^Infobox (.*)", Pattern.CASE_INSENSITIVE);
			String infoboxRedirect = "^#REDIRECT\\s*\\[\\[Template:Infobox (.*)\\]\\]";
			this.infoboxRedirectPattern = Pattern.compile(infoboxRedirect, Pattern.CASE_INSENSITIVE);
			this.infoboxTemplateCount = 0;
			this.isInName = false;
			this.isInContent = false;
			this.isInfoboxTemplate = false;
		}

		public void startElement(String uri, String localName, String qName, Attributes attributes) {
			if (localName.equals(NAME_ELEMENT_NAME)) {
				this.isInName = true;
			}
			else if (localName.equals(CONTENT_ELEMENT_NAME)) {
				this.isInContent = true;
			}
		}

		public void characters(char ch[], int start, int length) {
			if (this.isInName) {
				String templateName = new String(ch, start, length);
				Matcher matcher = this.infoboxTemplatePattern.matcher(templateName);
				if (matcher.find()) {
					this.infoboxName = templateName.substring(matcher.start(1), matcher.end(1)).trim();
					this.isInfoboxTemplate = true;
				}
				else {
					this.isInfoboxTemplate = false;
				}
			}
			else if (this.isInContent) {
				if (this.isInfoboxTemplate) {
					String templateContent = new String(ch, start, length);
					Matcher matcher = this.infoboxRedirectPattern.matcher(templateContent);
					if (matcher.find()) {
						String redirectInfoboxName = templateContent.substring(matcher.start(1),
								matcher.end(1)).trim();
						addRedirect(this.infoboxName, redirectInfoboxName);
					}

					System.out.print(++this.infoboxTemplateCount + " infobox templates read\r");
				}
			}
		}

		public void endElement(String uri, String localName, String qName) {
			if (localName.equals(NAME_ELEMENT_NAME)) {
				this.isInName = false;
			}
			else if (localName.equals(CONTENT_ELEMENT_NAME)) {
				this.isInContent = false;
			}
		}
	}

	private static Map<String, String> infoboxRedirects = new HashMap<String, String>();
	private static List<String> parseErrors = new ArrayList<String>();
	private static List<String> parseWarnings = new ArrayList<String>();

	private static void addRedirect(String infoboxName, String redirectName) {
		infoboxRedirects.put(infoboxName, redirectName);
	}

	static void initRedirect(String templateXmlPathname) throws Exception {
		System.out.println("Reading infobox templates...");

		try {
			TemplateHandler templateHandler = new TemplateHandler();
			parseErrors = templateHandler.parseErrors;
			parseWarnings = templateHandler.parseWarnings;

			XMLReader xmlReader = XMLReaderFactory.createXMLReader();
			xmlReader.setContentHandler(templateHandler);
			xmlReader.setErrorHandler(templateHandler);

			FileReader fileReader = new FileReader(templateXmlPathname);
			xmlReader.parse(new InputSource(fileReader));
			System.out.println("\nReading finished");
		}
		catch (SAXParseException e) {
			System.out.println("\nReading aborted");
			throw e;
		}
		catch (Exception e) {
			System.out.println(e.getMessage());
			System.out.println("\nReading aborted");
			throw e;
		}
	}

	static String resolveRedirect(String infoboxName) {
		String redirectedName  = infoboxRedirects.get(infoboxName);

		if (redirectedName != null) {
			return resolveRedirect(redirectedName);
		}
		else {
			return infoboxName;
		}
	}

	static int getRedirectedInfoboxCount() {
		return infoboxRedirects.size();
	}

	static boolean isErrorFound() {
		return (!parseErrors.isEmpty());
	}

	static boolean isWarningFound() {
		return (!parseWarnings.isEmpty());
	}

	static List<String> getErrors() {
		return Collections.unmodifiableList(parseErrors);
	}

	static List<String> getWarnings() {
		return Collections.unmodifiableList(parseWarnings);
	}
}
