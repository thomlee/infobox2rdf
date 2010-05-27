package hk.hku.cs.data.preprocessor;

import static org.junit.Assert.*;
import org.junit.*;

public class HtmlStyleTagCleanserTest {
	private HtmlStyleTagCleanser cleanser;
	@Before
	public void setUp() {
		this.cleanser = new HtmlStyleTagCleanser();
		this.cleanser.setMarkup("a", false);
		this.cleanser.setMarkup("b", true);
		this.cleanser.setMarkup("c", false);
	}
	
	@Test
	public void testCleanse() {
		String testStr = "foo<a>bar</a></c><d>foe</d><b>bla</b>";
		testStr = this.cleanser.cleanse(testStr);
		assertEquals("foobar<d>foe</d> bla ", testStr);
	}

	@Test
	public void testCleanseWithNowiki() {
		String testStr = "<b>foo</b><nowiki><a>bar</a><c>foe</c></nowiki><a>foo2</a>" +
				"<nowiki><b>bar2</b></nowiki><c>foe2</c><nowiki><a>foo3</a>";
		testStr = this.cleanser.cleanse(testStr);
		String expectedStr = " foo <nowiki><a>bar</a><c>foe</c></nowiki>foo2" +
				"<nowiki><b>bar2</b></nowiki>foe2<nowiki><a>foo3</a>";
		assertEquals(expectedStr, testStr);
	}
}
