package hk.hku.cs.data.preprocessor;

import static org.junit.Assert.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import java.io.*;

public class SimpleXMLWriterTest {
	@Before
	public void setUp() throws Exception {
		try {
			OutputStream outputStream = new FileOutputStream(new File("unit_test.xml"));
			SimpleXMLWriter xmlWriter = new SimpleXMLWriter(outputStream);
			xmlWriter.writeStartTag("a");
			xmlWriter.writeStartTag("b");
			xmlWriter.writeStartTag("c");
			xmlWriter.writeText("123");
			xmlWriter.writeEndTag("c");
			xmlWriter.writeStartTag("d");
			xmlWriter.writeEndTag();
			xmlWriter.writeTag("e");
			xmlWriter.writeStartTag("d");
			xmlWriter.writeText("456");
			xmlWriter.writeEndTag("d");
			xmlWriter.writeEndTag();
			xmlWriter.writeTag("f");
			xmlWriter.writeEndTag();
			xmlWriter.close();
			outputStream.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testWriteText() {
		try {
			File xmlFile = new File("unit_test.xml");
			InputStream inputStream = new FileInputStream(xmlFile);
			InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
			BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
			
			String xmlLine;
			
			if ((xmlLine = bufferedReader.readLine()) != null) {
				assertEquals("<a>", xmlLine);
			}
			
			if ((xmlLine = bufferedReader.readLine()) != null) {
				assertEquals("  <b>", xmlLine);
			}
			
			if ((xmlLine = bufferedReader.readLine()) != null) {
				assertEquals("    <c>123</c>", xmlLine);
			}
			
			if ((xmlLine = bufferedReader.readLine()) != null) {
				assertEquals("    <d></d>", xmlLine);
			}
			
			if ((xmlLine = bufferedReader.readLine()) != null) {
				assertEquals("    <e/>", xmlLine);
			}
			
			if ((xmlLine = bufferedReader.readLine()) != null) {
				assertEquals("    <d>456</d>", xmlLine);
			}
			
			if ((xmlLine = bufferedReader.readLine()) != null) {
				assertEquals("  </b>", xmlLine);
			}
			
			if ((xmlLine = bufferedReader.readLine()) != null) {
				assertEquals("  <f/>", xmlLine);
			}
			
			if ((xmlLine = bufferedReader.readLine()) != null) {
				assertEquals("</a>", xmlLine);
			}
			
			bufferedReader.close();
			inputStreamReader.close();
			inputStream.close();
			xmlFile.delete();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
