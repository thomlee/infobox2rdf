package hk.hku.cs.data.preprocessor;

import static org.junit.Assert.*;
import org.junit.Test;

public class HtmlTagCleanserTest {
	@Test
	public void testCleanse() {
		String testStr = "<h3>foo</h3>-->--><br /><!--comment--><!--comment2--><ol >bar<c>" +
				"--><!--comment3--><s>foe</s><!--comment again";
		testStr = HtmlTagCleanser.cleanse(testStr);
		assertEquals(" foo   bar<c>foe", testStr);
	}

	@Test
	public void testCleanseWithNowiki() {
		String testStr = "<center>foo</center><nowiki><b>bar</b><!--c1--><br />-->" +
				"<!--c2</nowiki><s>foe</s><!--c3-->--><nowiki><sup>bla</sup><!--c4</nowiki>" +
				"--><!--c5<nowiki><strike>foo</strike></nowiki>--><!--c6";
		testStr = HtmlTagCleanser.cleanse(testStr);
		String expectedStr = "foo<nowiki><b>bar</b><!--c1--><br />--><!--c2</nowiki>" +
				"foe<nowiki><sup>bla</sup><!--c4</nowiki>";
		assertEquals(expectedStr, testStr);
	}
}