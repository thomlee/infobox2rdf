package hk.hku.cs.data.preprocessor;

import java.io.*;
import java.util.*;

class SimpleXMLWriter {
	private static final int DEFAULT_INDENT = 2;
	private PrintStream printStream;
	private LinkedList<String> openedTags;
	private int indent;
	private String unitIndentSpace;
	private String currentIndentSpaces;
	private boolean isLastTagWriteStart;
	
	SimpleXMLWriter(OutputStream outputStream) {
		this(outputStream, DEFAULT_INDENT);
	}
	
	SimpleXMLWriter(OutputStream outputStream, int indent) {
		this.printStream = new PrintStream(outputStream);
		this.openedTags = new LinkedList<String>();
		this.indent = indent;
		this.setIndent(indent);
		this.currentIndentSpaces = "";
		this.isLastTagWriteStart = false;
	}
	
	private void setIndent(int indent) {
		this.unitIndentSpace = "";
		for (int i = 0; i < indent; i++) {
			this.unitIndentSpace += " ";
		}
	}
	
	void writeStartTag(String tagName) {
		if (this.isLastTagWriteStart) {
			this.currentIndentSpaces += this.unitIndentSpace;
			this.printStream.print("\n" + this.currentIndentSpaces + "<" + tagName + ">");
		}
		else {
			this.printStream.print(this.currentIndentSpaces + "<" + tagName + ">");
		}
		
		this.openedTags.addLast(tagName);
		this.isLastTagWriteStart = true;
	}
	
	void writeText(String text) {
		if (!this.openedTags.isEmpty()) {
			String formattedText = this.formatText(text);
			this.printStream.print(formattedText);
		}
	}
	
	private String formatText(String inputStr) {
		String outputStr = inputStr.replaceAll("&", "&amp;");
		outputStr = outputStr.replaceAll("<", "&lt;");
		outputStr = outputStr.replaceAll(">", "&gt;");
		
		return outputStr;
	}
	
	void writeEndTag() {
		if (this.openedTags.isEmpty()) {
			return;
		}
		
		if (!isLastTagWriteStart) {
			this.currentIndentSpaces = this.currentIndentSpaces.substring(0,
					this.currentIndentSpaces.length() - this.indent);
			this.printStream.println(this.currentIndentSpaces + "</" + this.openedTags.removeLast() + ">");
		}
		else {
			this.printStream.println("</" + this.openedTags.removeLast() + ">");		
		}
		
		this.isLastTagWriteStart = false;
	}
	
	void writeEndTag(String tagName) {
		if (this.openedTags.isEmpty()) {
			return;
		}
		
		if (!tagName.equals(this.openedTags.getLast())) {
			return;
		}
		
		if (!isLastTagWriteStart) {
			this.currentIndentSpaces = this.currentIndentSpaces.substring(0,
					this.currentIndentSpaces.length() - this.indent);
			this.printStream.println(this.currentIndentSpaces + "</" + this.openedTags.removeLast() + ">");
		}
		else {
			this.printStream.println("</" + this.openedTags.removeLast() + ">");		
		}
		
		this.isLastTagWriteStart = false;
	}
	
	void writeTag(String tagName) {
		if (this.isLastTagWriteStart) {
			this.currentIndentSpaces += this.unitIndentSpace;
			this.printStream.println("\n" + this.currentIndentSpaces + "<" + tagName + "/>");
		}
		else {
			this.printStream.println(this.currentIndentSpaces + "<" + tagName + "/>");
		}
		
		this.isLastTagWriteStart = false;
	}
	
	void close() {
		this.printStream.close();
	}
}
