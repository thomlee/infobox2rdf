package hk.hku.cs.data.preprocessor;

import java.util.*;
import java.util.regex.*;

class HtmlStyleTagCleanser {
	private Set<String> recognizedMarkups;
	private Set<String> lineBreakMarkups;
	private Pattern markupPattern;

	HtmlStyleTagCleanser() {
		this.recognizedMarkups = new HashSet<String>();
		this.lineBreakMarkups = new HashSet<String>();
		this.markupPattern = Pattern.compile("<(\\w+)\\s*?[^\\s]*?/?>|</(\\w+)\\s*?>");
	}
	
	void setMarkup(String markup, boolean isLineBreaking) {
		this.recognizedMarkups.add(markup);
		if (isLineBreaking) {
			this.lineBreakMarkups.add(markup);
		}
	}
	
	String cleanse(String inputStr) {
		NowikiRanges nowikiRanges = NowikiRanges.getNowikiRanges(inputStr);
		StringBuilder outputStr = new StringBuilder(inputStr);
		Matcher matcher = this.markupPattern.matcher(inputStr);
		int markupStartPos;
		int markupEndPos;
		int offset = 0;
		
		while (matcher.find()) {
			markupStartPos = matcher.start(1);
			if (markupStartPos >= 0) {
				markupEndPos = matcher.end(1);
			}
			else {
				markupStartPos = matcher.start(2);
				markupEndPos = matcher.end(2);
			}
			
			if (nowikiRanges.isInRange(markupStartPos, markupEndPos)) {
				continue;
			}

			String markup = inputStr.substring(markupStartPos, markupEndPos);
			if (this.isRecognized(markup)) {
				int removalStartPos = matcher.start() - offset;
				int removalEndPos = matcher.end() - offset;
				if (this.isLineBreakMarkup(markup)) {
					outputStr.replace(removalStartPos, removalEndPos, " ");
					offset += (removalEndPos - removalStartPos - 1);
				}
				else {
					outputStr.delete(removalStartPos, removalEndPos);
					offset += (removalEndPos - removalStartPos);
				}
			}
		}

		return outputStr.toString();
	}

	private boolean isRecognized(String markup) {
		return this.recognizedMarkups.contains(markup);
	}
	
	private boolean isLineBreakMarkup(String markup) {
		return this.lineBreakMarkups.contains(markup);
	}
}