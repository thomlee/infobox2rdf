package hk.hku.cs.data.preprocessor;

import static org.junit.Assert.*;
import org.junit.*;

public class MagicWordCleanserTest {
	private MagicWordCleanser cleanser;
	
	@Before
	public void setUp() {
		 this.cleanser = new MagicWordCleanser();
		 this.cleanser.setMagicWord("NOTOC");
		 this.cleanser.setMagicWord("NOGALLERY");
		 this.cleanser.setMagicWord("START");
	}
 	
	@Test
	public void testCleanse() {
		String testStr = "foo __NOTOC__bar__notMagicWord__f__START__oe";
		testStr = this.cleanser.cleanse(testStr);
		assertEquals("foo bar__notMagicWord__foe", testStr);
	}
	
	@Test
	public void testCleanseWithNowiki() {
		String testStr = "foo __NOTOC__bar<nowiki>__NOGALLERY__</nowiki>f__NOGALLERY__oe";
		testStr = this.cleanser.cleanse(testStr);
		assertEquals("foo bar<nowiki>__NOGALLERY__</nowiki>foe", testStr);
	}
}
