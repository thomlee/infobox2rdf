package hk.hku.cs.data.preprocessor;

import static org.junit.Assert.*;
import org.junit.Test;

public class InfoboxRedirectResolverTest {
	@Test
	public void testResolveRedirect() {
		try {
			InfoboxRedirectResolver.initRedirect("./trunk/test/test_templates.xml");
			assertFalse(InfoboxRedirectResolver.isErrorFound());
			assertFalse(InfoboxRedirectResolver.isWarningFound());
			assertEquals("infobox3", InfoboxRedirectResolver.resolveRedirect("infobox1"));
			assertEquals("infobox3", InfoboxRedirectResolver.resolveRedirect("infobox2"));
			assertEquals("infobox3", InfoboxRedirectResolver.resolveRedirect("infobox1/doc"));
			assertEquals(3, InfoboxRedirectResolver.getRedirectedInfoboxCount());
		}
		catch (Exception e) {
			System.out.println(e.getMessage());
			fail();
		}
	}
}
