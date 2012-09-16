package fr.openstreetmap.watch.util;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * "SAX-like" simple and fast XML writing
 * @author Clément Stenac
 */
public class SimpleXMLWriter {
	public SimpleXMLWriter(Writer writer) {
		this.writer = writer;
		curTagIsClosed = true;
		elementsStack = new Stack<String>();
		curElementAttrNames = new ArrayList<String>();
		curElementAttrValues = new ArrayList<String>();
	}

	public SimpleXMLWriter xmlDeclaration(String charset) throws IOException {
		writer.write("<?xml version=\"1.0\" encoding=\"" + charset + "\" ?>\n");
		return this;
	}

	public SimpleXMLWriter entity(String name) throws IOException {
		/* Finish opening tag for previous level */
		finishWritingCurrentTag(true);
		curTagIsClosed = false;
		curElementIsEmpty = true;
		
		/* Start this level */
		writer.write("<"); writer.write(name);
		elementsStack.add(name);
		
		return this;
	}

	public SimpleXMLWriter attr(String attr, String value) throws IOException  {
		if (attr == null || value == null) {
			throw new IOException("Trying to write illegal XML attribute  attr=" + attr + " val=" + value);
		}
		curElementAttrNames.add(attr);
		curElementAttrValues.add(value);
		return this;
	}

	public SimpleXMLWriter attr(String attr, int value) throws IOException {
		return attr(attr, String.valueOf(value));
	}
	public SimpleXMLWriter attr(String attr, long value) throws IOException {
		return attr(attr, String.valueOf(value));
	}

	public SimpleXMLWriter endEntity() throws IOException {
		if(elementsStack.empty()) {
			throw new IOException("Trying to pop below last stack level");
		}
		String name = elementsStack.pop();
		if (curElementIsEmpty) {
			writeCurrentTagAttrs();
			writer.write("/>");
		} else {
			writer.write("</"); writer.write(name);	writer.write(">");
		}
		curElementIsEmpty = false;
		curTagIsClosed = true;
		return this;
	}

	/**
	 * Close this writer. It does not close the underlying 
	 * writer, but does throw an exception if there are 
	 * as yet unclosed tags.
	 */
	public void close() throws IOException {
		if(!elementsStack.empty()) {
			throw new IOException("Writer closed but stack not empty, stack top is " + elementsStack.pop());
		}
		writer.flush();
	}


	public SimpleXMLWriter text(String text) throws Exception {
		return textUnsafe(text, false);
	}

	public SimpleXMLWriter text(long value) throws Exception {
		return textUnsafe(String.valueOf(value), true);
	}

	public SimpleXMLWriter text(int value) throws Exception {
		return textUnsafe(String.valueOf(value), true);
	}
	
	public SimpleXMLWriter textUnsafe(String text, boolean skipEscaping) throws IOException {
		finishWritingCurrentTag(false);
		curElementIsEmpty = false;
		if (skipEscaping) {
			writer.write(text);
		} else {
			escape(writer, text);
		}
		return this;
	}



	private void finishWritingCurrentTag(boolean entity) throws IOException {
		if (!curTagIsClosed) {
			writeCurrentTagAttrs();
			curTagIsClosed = true;
			if (entity) writer.write(">");
			else writer.write(">");
		}
	}
	private void writeCurrentTagAttrs() throws IOException {
		if (curElementAttrNames.size() > 0) {
			for (int i = 0; i < curElementAttrNames.size(); i++) {
				writer.write(" ");
				writer.write(curElementAttrNames.get(i));
				writer.write("=\"");
				escape(writer, curElementAttrValues.get(i));
				writer.write("\"");
			}
			curElementIsEmpty = false;
			curElementAttrNames.clear();
			curElementAttrValues.clear();
		}
	}

	public static void escape(Writer outWriter, String input) throws IOException {
		int len = input.length();
		for (int i = 0; i < len; i++) {
			char c = input.charAt(i);
			switch (c) {
			case '<': outWriter.write("&lt;"); break;
			case '>': outWriter.write("&gt;"); break;
			case '\'': outWriter.write("&apos;"); break;
			case '\"': outWriter.write("&quot;"); break;
			case '&': outWriter.write("&amp;"); break;
			default:			
				if (c >= ' ' || c == '\t' || c== '\r' || c == '\n') {
					outWriter.write(c);
				}
			}
		}
	}

	private Writer writer;
	private Stack<String> elementsStack;
	private List<String> curElementAttrNames;
	private List<String> curElementAttrValues;
	private boolean curElementIsEmpty;
	private boolean curTagIsClosed;
}