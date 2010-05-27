package hk.hku.cs.data.preprocessor;

import static org.junit.Assert.*;
import org.junit.*;

public class NowikiRangesTest {
	@Test
	public void testIsCompleteOverlap() {
		String testStr = "1<nowiki><pre>2</pre></nowiki>3<nowiki>4</nowiki><pre>5</pre><nowiki>6";
		NowikiRanges nowikiRanges = NowikiRanges.getNowikiRanges(testStr);
		assertTrue(nowikiRanges.isInRange(9, 21));
		assertFalse(nowikiRanges.isInRange(8, 21));
		assertFalse(nowikiRanges.isInRange(9, 22));
		assertTrue(nowikiRanges.isInRange(10, 20));
		
		assertTrue(nowikiRanges.isInRange(39, 40));
		assertFalse(nowikiRanges.isInRange(38, 40));
		assertFalse(nowikiRanges.isInRange(39, 41));
		
		assertTrue(nowikiRanges.isInRange(54, 55));
		assertFalse(nowikiRanges.isInRange(53, 55));
		assertFalse(nowikiRanges.isInRange(54, 56));
		
		assertFalse(nowikiRanges.isInRange(3, 8));
		assertFalse(nowikiRanges.isInRange(30, 31));
		assertFalse(nowikiRanges.isInRange(45, 52));
		
		assertTrue(nowikiRanges.isInRange(69, 70));
		assertFalse(nowikiRanges.isInRange(68, 70));
		assertFalse(nowikiRanges.isInRange(69, 71));
	}
}
