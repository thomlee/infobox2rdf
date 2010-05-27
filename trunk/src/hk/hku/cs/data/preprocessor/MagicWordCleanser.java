package hk.hku.cs.data.preprocessor;

import java.util.*;
import java.util.regex.*;

public class MagicWordCleanser {
	private Set<String> recognizedMagicWords;
	private Pattern magicWordPattern;
	
	MagicWordCleanser() {
		this.recognizedMagicWords = new HashSet<String>();
		this.magicWordPattern = Pattern.compile("__([A-Z]+)__");
	}
	
	void setMagicWord(String magicWord) {
		this.recognizedMagicWords.add(magicWord);
	}
	
	String cleanse(String inputStr) {
		NowikiRanges nowikiRanges = NowikiRanges.getNowikiRanges(inputStr);
		StringBuilder outputStr = new StringBuilder(inputStr);
		Matcher matcher = this.magicWordPattern.matcher(inputStr);
		int magicWordStartPos;
		int magicWordEndPos;
		int offset = 0;
		
		while (matcher.find()) {
			magicWordStartPos = matcher.start(1);
			magicWordEndPos = matcher.end(1);
			
			if (nowikiRanges.isInRange(magicWordStartPos, magicWordEndPos)) {
				continue;
			}
			
			String magicWord = inputStr.substring(magicWordStartPos, magicWordEndPos);
			if (this.isRecognized(magicWord)) {
				int removalStartPos = matcher.start() - offset;
				int removalEndPos = matcher.end() - offset;
				outputStr.delete(removalStartPos, removalEndPos);
				offset += (removalEndPos - removalStartPos);
			}
		}
		
		return outputStr.toString();
	}
	
	private boolean isRecognized(String magicWord) {
		return this.recognizedMagicWords.contains(magicWord);
	}
}
