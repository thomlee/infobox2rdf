package hk.hku.cs.data.preprocessor;

import static org.junit.Assert.*;
import org.junit.Test;

public class WikiMarkupCleanserTest {
	@Test
	public void testCleanse() {
		String testStr = "<ref>test 0</ref> '''test 1''' ~~~~~''''test 2''''<pre>test 3</pre>" +
				"<ref name=\"id\">test 4</ref>__NOTOC__<nowiki><nowiki>test 5</nowiki></nowiki>" +
				"__START__";
		testStr = WikiMarkupCleanser.cleanse(testStr);
		assertEquals("test 0 test 1 test 2 test 3 test 4<nowiki>test 5", testStr);
	}
}
