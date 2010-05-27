package hk.hku.cs.data.preprocessor;

import java.util.*;
import java.util.regex.*;

class NowikiRanges {
	private static final String NOWIKI_TAG = "(<nowiki\\s*?>).*?(</nowiki\\s*?>)|(<nowiki\\s*?>).*";
	private static final String PRE_TAG = "(<pre\\s*?>).*?(</pre\\s*?>)|(<pre\\s*?>).*";
	private static final Pattern NOWIKI_TAG_PATTERN = Pattern.compile(NOWIKI_TAG);
	private static final Pattern PRE_TAG_PATTERN = Pattern.compile(PRE_TAG);
	private TreeMap<Integer, Integer> ranges;
	
	static NowikiRanges getNowikiRanges(String inputStr) {
		NowikiRanges nowikiRanges = new NowikiRanges();
		Set<Pattern> nowikiPatterns = getNowikiPatterns();
		
		for (Pattern nowikiPattern : nowikiPatterns) {
			Matcher matcher = nowikiPattern.matcher(inputStr);
			int nowikiStartPos;
			int nowikiEndPos;
			
			while (matcher.find()) {
				nowikiStartPos = matcher.end(1);
				if (nowikiStartPos >= 0) { 
					nowikiEndPos = matcher.start(2);
				}
				else {
					nowikiStartPos = matcher.end(3);
					nowikiEndPos = inputStr.length();
				}
				
				if (!nowikiRanges.isInRange(nowikiStartPos, nowikiEndPos)) {
					nowikiRanges.setRange(nowikiStartPos, nowikiEndPos);
				}
			}
		}
		
		return nowikiRanges;
	}
	
	private NowikiRanges() {
		this.ranges = new TreeMap<Integer, Integer>();
	}
	
	private static Set<Pattern> getNowikiPatterns() {
		Set<Pattern> nowikiPatterns = new LinkedHashSet<Pattern>();
		nowikiPatterns.add(NOWIKI_TAG_PATTERN);
		nowikiPatterns.add(PRE_TAG_PATTERN);
		
		return nowikiPatterns;
	}
	
	private void setRange(int startPos, int endPos) {
		this.ranges.put(startPos, endPos);
	}
	
	boolean isInRange(int startPos, int endPos) {
		Map.Entry<Integer, Integer> range = this.ranges.floorEntry(startPos);
		
		if (range != null) {
			if (range.getValue() >= endPos) {
				return true;
			}
		}
		
		return false;
	}
}
