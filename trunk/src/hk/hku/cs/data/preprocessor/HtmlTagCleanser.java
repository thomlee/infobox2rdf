package hk.hku.cs.data.preprocessor;

class HtmlTagCleanser {
	private static int htmlCommentStartMarkupLen;
	private static int htmlCommentEndMarkupLen;
	private static HtmlStyleTagCleanser htmlStyleTagCleanser;

	static {
		htmlCommentStartMarkupLen = (new String("<!--")).length();
		htmlCommentEndMarkupLen = (new String("-->")).length();
		initHtmlMarkups();
	}

	private static void initHtmlMarkups() {
		htmlStyleTagCleanser = new HtmlStyleTagCleanser();
		htmlStyleTagCleanser.setMarkup("b", false);
		htmlStyleTagCleanser.setMarkup("big", false);
		htmlStyleTagCleanser.setMarkup("blockquote", true);
		htmlStyleTagCleanser.setMarkup("br", true);
		htmlStyleTagCleanser.setMarkup("caption", false);
		htmlStyleTagCleanser.setMarkup("center", false);
		htmlStyleTagCleanser.setMarkup("cite", false);
		htmlStyleTagCleanser.setMarkup("code", false);
		htmlStyleTagCleanser.setMarkup("dd", true);
		htmlStyleTagCleanser.setMarkup("del", false);
		htmlStyleTagCleanser.setMarkup("div", true);
		htmlStyleTagCleanser.setMarkup("dl", true);
		htmlStyleTagCleanser.setMarkup("dt", true);
		htmlStyleTagCleanser.setMarkup("em", false);
		htmlStyleTagCleanser.setMarkup("font", false);
		htmlStyleTagCleanser.setMarkup("hr", true);
		htmlStyleTagCleanser.setMarkup("h1", true);
		htmlStyleTagCleanser.setMarkup("h2", true);
		htmlStyleTagCleanser.setMarkup("h3", true);
		htmlStyleTagCleanser.setMarkup("h4", true);
		htmlStyleTagCleanser.setMarkup("h5", true);
		htmlStyleTagCleanser.setMarkup("h6", true);
		htmlStyleTagCleanser.setMarkup("i", false);
		htmlStyleTagCleanser.setMarkup("ins", false);
		htmlStyleTagCleanser.setMarkup("li", true);
		htmlStyleTagCleanser.setMarkup("ol", true);
		htmlStyleTagCleanser.setMarkup("p", true);
		htmlStyleTagCleanser.setMarkup("rb", false);
		htmlStyleTagCleanser.setMarkup("rp", false);
		htmlStyleTagCleanser.setMarkup("rt", false);
		htmlStyleTagCleanser.setMarkup("ruby", false);
		htmlStyleTagCleanser.setMarkup("s", false);
		htmlStyleTagCleanser.setMarkup("small", false);
		htmlStyleTagCleanser.setMarkup("span", false);
		htmlStyleTagCleanser.setMarkup("strike", false);
		htmlStyleTagCleanser.setMarkup("strong", false);
		htmlStyleTagCleanser.setMarkup("sub", false);
		htmlStyleTagCleanser.setMarkup("sup", false);
		htmlStyleTagCleanser.setMarkup("table", true);
		htmlStyleTagCleanser.setMarkup("td", true);
		htmlStyleTagCleanser.setMarkup("th", true);
		htmlStyleTagCleanser.setMarkup("tr", true);
		htmlStyleTagCleanser.setMarkup("tt", false);
		htmlStyleTagCleanser.setMarkup("u", false);
		htmlStyleTagCleanser.setMarkup("ul", true);
		htmlStyleTagCleanser.setMarkup("var", false);
	}
	
	static String cleanse(String inputStr) {
		String cleansedStr = removeHtmlComment(inputStr);
		cleansedStr = removeHtmlCommentEndMarkup(cleansedStr);
		
		return htmlStyleTagCleanser.cleanse(cleansedStr);
	}
	
	private static String removeHtmlComment(String inputStr) {
		NowikiRanges nowikiRanges = NowikiRanges.getNowikiRanges(inputStr);
		StringBuilder outputStr = new StringBuilder(inputStr);
		int commentStartPos = 0;
		int commentEndPos = 0;
		int offset = 0;
		
		while ((commentStartPos = inputStr.indexOf("<!--", commentStartPos)) >= 0) {
			int commentStartMarkupEndPos = commentStartPos + htmlCommentStartMarkupLen;
			if (nowikiRanges.isInRange(commentStartPos, commentStartMarkupEndPos)) {
				commentStartPos = commentStartMarkupEndPos;
				continue;
			}
			
			commentEndPos = inputStr.indexOf("-->", commentStartPos);
			if (commentEndPos > 0) {
				outputStr.delete(commentStartPos - offset,
						commentEndPos + htmlCommentEndMarkupLen - offset);
				offset += commentEndPos + htmlCommentEndMarkupLen - commentStartPos;
				commentStartPos = commentEndPos + htmlCommentEndMarkupLen; 
			}
			else {
				outputStr.delete(commentStartPos - offset, inputStr.length() - offset);
				break;
			}
		}
		
		return outputStr.toString();
	}
	
	private static String removeHtmlCommentEndMarkup(String inputStr) {
		NowikiRanges nowikiRanges = NowikiRanges.getNowikiRanges(inputStr);
		StringBuilder outputStr = new StringBuilder(inputStr);
		int commentEndPos = 0;
		int offset = 0;
		
		while ((commentEndPos = inputStr.indexOf("-->", commentEndPos)) >= 0) {
			if (!nowikiRanges.isInRange(commentEndPos, commentEndPos)) {
				outputStr.delete(commentEndPos - offset,
						commentEndPos + htmlCommentEndMarkupLen - offset);
				offset += htmlCommentEndMarkupLen;
			}
			
			commentEndPos += htmlCommentEndMarkupLen;
		}
		
		return outputStr.toString();
	}
}