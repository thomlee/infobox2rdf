package hk.hku.cs.data.preprocessor;

import java.util.regex.*;

class WikiMarkupCleanser {
	private static Pattern unaryWikiMarkupPattern;
	private static Pattern referencePattern;
	private static HtmlStyleTagCleanser htmlStyleTagCleanser;
	private static MagicWordCleanser magicWordCleanser;
	
	static {
		unaryWikiMarkupPattern = getUnaryWikiMarkupPattern();
		referencePattern = getReferencePattern();
		initWikiTagMarkup();
		initMagicWords();
	}
	
	private static Pattern getUnaryWikiMarkupPattern() {
		return Pattern.compile("'{3,5}|~{3,5}");
	}
	
	private static Pattern getReferencePattern() {
		return Pattern.compile("<ref(\\s*name=[^\\s]*)?>|</ref>");
	}
	
	private static void initWikiTagMarkup() {
		htmlStyleTagCleanser = new HtmlStyleTagCleanser();
		htmlStyleTagCleanser.setMarkup("nowiki", false);
		htmlStyleTagCleanser.setMarkup("pre", true);
		htmlStyleTagCleanser.setMarkup("math", false);
		htmlStyleTagCleanser.setMarkup("html", false);
		htmlStyleTagCleanser.setMarkup("gallery", true);
		htmlStyleTagCleanser.setMarkup("onlyinclude", false);
		htmlStyleTagCleanser.setMarkup("noinclude", false);
		htmlStyleTagCleanser.setMarkup("includeonly", false);
	}
	
	private static void initMagicWords() {
		magicWordCleanser = new MagicWordCleanser();
		magicWordCleanser.setMagicWord("NOTOC");
		magicWordCleanser.setMagicWord("FORCETOC");
		magicWordCleanser.setMagicWord("TOC");
		magicWordCleanser.setMagicWord("NOEDITSECTION");
		magicWordCleanser.setMagicWord("NEWSECTONLINK");
		magicWordCleanser.setMagicWord("NONEWSECTONLINK");
		magicWordCleanser.setMagicWord("NOGALLERY");
		magicWordCleanser.setMagicWord("HIDDENCAT");
		magicWordCleanser.setMagicWord("NOCONTENTCONVERT");
		magicWordCleanser.setMagicWord("NOCC");
		magicWordCleanser.setMagicWord("NOTITLECONVERT");
		magicWordCleanser.setMagicWord("NOTC");
		magicWordCleanser.setMagicWord("START");
		magicWordCleanser.setMagicWord("END");
		magicWordCleanser.setMagicWord("INDEX");
		magicWordCleanser.setMagicWord("NOINDEX");
		magicWordCleanser.setMagicWord("STATICREDIRECT");
	}
	
	static String cleanse(String inputStr) {
		String cleansedStr = removeUnaryWikiMarkup(inputStr);
		cleansedStr = removeReference(cleansedStr);
		cleansedStr = magicWordCleanser.cleanse(cleansedStr);
		
		return htmlStyleTagCleanser.cleanse(cleansedStr);
	}
	
	private static String removeUnaryWikiMarkup(String inputStr) {
		Matcher matcher = unaryWikiMarkupPattern.matcher(inputStr);
		return removeWikiMarkup(inputStr, matcher);
	}
	
	private static String removeReference(String inputStr) {
		Matcher matcher = referencePattern.matcher(inputStr);
		return removeWikiMarkup(inputStr, matcher);
	}
	
	private static String removeWikiMarkup(String inputStr, Matcher matcher) {
		NowikiRanges nowikiRanges = NowikiRanges.getNowikiRanges(inputStr);
		StringBuilder outputStr = new StringBuilder(inputStr);
		int offset = 0;

		while (matcher.find()) {
			int wikiMarkupStartPos = matcher.start();
			int wikiMarkupEndPos = matcher.end();

			if (!nowikiRanges.isInRange(wikiMarkupStartPos, wikiMarkupEndPos)) {
				int removalStartPos = wikiMarkupStartPos - offset;
				int removalEndPos = wikiMarkupEndPos - offset;
				outputStr.delete(removalStartPos, removalEndPos);
				offset += (removalEndPos - removalStartPos);
			}
		}

		return outputStr.toString();
	}
}
