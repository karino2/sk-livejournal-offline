package com.googlecode.karino.db;

import java.util.StringTokenizer;


import com.googlecode.karino.BlogPostEditor;
import com.googlecode.karino.db.ImageEmbedSpan;

import de.timroes.base64.Base64;

import android.content.Context;
import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.CharacterStyle;
import android.text.style.StyleSpan;
import android.text.style.URLSpan;
import android.util.Log;
import android.webkit.WebView;

/**
 * Utility class for handling buffers and spans. The main reason it's so messy
 * is that I hope to offload the whole rich text editing business to an external
 * application as soon as there's a good rich text / html editor available for
 * android.
 * 
 * @author juhak
 * 
 */

public class SpannableBufferHelper {

	private final String TAG = "SpannableBufferHelper";
	private final int URL_SPAN_TYPE = 65534;
	private final int HTML_SPAN_TYPE = 65533;
	private final int IMG_SPAN_TYPE = 65532;
	private final int OPEN_BOLD = 1;
	private final int OPEN_ITALIC = 2;
	private final int CLOSE_BOLD = 3;
	private final int CLOSE_ITALIC = 4;
	private boolean isBoldEnabled = false;
	private boolean isItalicEnabled = false;
	private int boldStart;
	private int italicStart;
	private int boldEnd;
	private int italicEnd;

	private Context c = null;

	public void debugWriteBuffer(String title, Spannable seq) {
		SpannableStringBuilder ssb = new SpannableStringBuilder(seq);
		Object[] spans = ssb.getSpans(0, ssb.length(), StyleSpan.class);
		Object[] urls = ssb.getSpans(0, ssb.length(), URLSpan.class);
		Object[] htmls = ssb.getSpans(0, ssb.length(), HTMLEmbedSpan.class);
		Object[] allobjs = ssb.getSpans(0, ssb.length(), Object.class);
		Object[] imgs = ssb.getSpans(0, ssb.length(), ImageEmbedSpan.class);
		for (int i = 0; i < allobjs.length; i++) {
			String onespan = allobjs[i].getClass().getName();
		}
		for (int i = 0; i < spans.length; i++) {
			StyleSpan current = (StyleSpan) (spans[i]);
			boolean isBold = current.getStyle() == Typeface.BOLD;
			boolean isItalic = current.getStyle() == Typeface.ITALIC;
		}
		for (int i = 0; i < urls.length; i++) {
			URLSpan current = (URLSpan) (urls[i]);
			Log.d(TAG, "Urls[" + i + "], SpanStart="
					+ ssb.getSpanStart(current) + "SpanEnd="
					+ ssb.getSpanEnd(current) + " flags:"
					+ ssb.getSpanFlags(current) + " URL:" + current.getURL());
		}
		for (int i = 0; i < htmls.length; i++) {
			HTMLEmbedSpan current = (HTMLEmbedSpan) (htmls[i]);
			Log.d(TAG, "HTML[" + i + "], SpanStart="
					+ ssb.getSpanStart(current) + "SpanEnd="
					+ ssb.getSpanEnd(current) + " flags:"
					+ ssb.getSpanFlags(current) + " Markup:"
					+ current.getHtml());
		}
		for (int i = 0; i < imgs.length; i++) {
			ImageEmbedSpan current = (ImageEmbedSpan) (imgs[i]);
			Log.d(TAG, "Images[" + i + "], SpanStart="
					+ ssb.getSpanStart(current) + "SpanEnd="
					+ ssb.getSpanEnd(current) + " flags:"
					+ ssb.getSpanFlags(current) + " Src:" + current.getSrc());
		}
		int next = 0;
		int total = 0;
		int iteration = 0;
		StyleSpan curTran = null;
		if (ssb != null) {
			long failsafe = 0;
			next = ssb.nextSpanTransition(0, ssb.length(), StyleSpan.class);
			total += next;
			while (total < ssb.length()) {
				try {
					curTran = (StyleSpan) spans[iteration++];
				} catch (ArrayIndexOutOfBoundsException ae) {
					Log
							.d(TAG,
									"More transitions while looking at buffer than returned by getSpans!");
				} catch (ClassCastException ce) {
					Log
							.d(TAG,
									"Getspans returned an object which is not a StyleSpan!");
				}
				Log.d(TAG, "Transition at index:" + total);
				next = ssb.nextSpanTransition(total, ssb.length() - total,
						StyleSpan.class);
				total += next;
				if ((failsafe++) > ssb.length()) {
					break;
				}
			}
		} else {
			Log.d(TAG, "Buffer is null.");
		}
	}

	/**
	 * This converts the spannable buffer to XHTML String. This currently
	 * replaces: TypeFace.BOLD -> <b>text</b> TypeFace.ITALIC -> <i>text</i>
	 * TypeFace.BOLD_ITALIC -> <b><i>text</i></b> URLSpan -> <a href =
	 * "URL">text</a>
	 * 
	 * @param seq
	 * @return String
	 */

	public String SpannableToXHTML(Spannable seq) {
		SpannableStringBuilder ssb = new SpannableStringBuilder(seq);
		// get the text to result and start inserting markup
		Object[] spans;
		String str = "";
		isBoldEnabled = false;
		isItalicEnabled = false;
		for (int i = 0; i < seq.length(); i++) {
			spans = ssb.getSpans(i, i + 1, StyleSpan.class);
			boolean bold = false;
			boolean italic = false;
			boolean normal = false;
			for (int j = 0; j < spans.length; j++) {
				int style = ((StyleSpan) spans[j]).getStyle();
				if (style == android.graphics.Typeface.BOLD)
					bold = true;
				if (style == android.graphics.Typeface.ITALIC)
					italic = true;
				if (style == android.graphics.Typeface.NORMAL)
					normal = true;
			}
			if (!normal) {
				if ((bold) && (!isBoldEnabled)) {
					isBoldEnabled = true;
					str = str + "<b>";
				}
				if ((italic) && (!isItalicEnabled)) {
					isItalicEnabled = true;
					str = str + "<i>";
				}
			}
			if (normal) {
				if (isItalicEnabled) {
					isItalicEnabled = false;
					str = str + "</i>";
				}
				if (isBoldEnabled) {
					isBoldEnabled = false;
					str = str + "</b>";
				}
			}
			if ((isItalicEnabled) && (!italic)) {
				isItalicEnabled = false;
				str = str + "</i>";
			}
			if ((isBoldEnabled) && (!bold)) {
				isBoldEnabled = false;
				str = str + "</b>";
			}
			str = str + seq.charAt(i);
		}
		if (isItalicEnabled)
			str = str + "</i>";
		if (isBoldEnabled)
			str = str + "</b>";

		Object[] urls = ssb.getSpans(0, ssb.length(), URLSpan.class);
		Object[] htmls = ssb.getSpans(0, ssb.length(), HTMLEmbedSpan.class);
		Object[] imgs = ssb.getSpans(0, ssb.length(), ImageEmbedSpan.class);
		/*
		 * for (int i = 0; i < spans.length; i++) { StyleSpan current =
		 * (StyleSpan) (spans[i]); int totalDisplacement =
		 * insertMarkupAndFixSpans(current.getStyle(),
		 * ssb.getSpanStart(current), ssb.getSpanEnd(current), ssb); }
		 */
		for (int i = 0; i < urls.length; i++) {
			URLSpan current = (URLSpan) (urls[i]);
			int totalDisplacement = insertLinksAndFixSpans(current.getURL(),
					ssb.getSpanStart(current), ssb.getSpanEnd(current), ssb);
		}
		for (int i = 0; i < htmls.length; i++) {
			HTMLEmbedSpan current = (HTMLEmbedSpan) (htmls[i]);
			int totalDisplacement = insertHTMLAndFixSpans(current.getHtml(),
					ssb.getSpanStart(current), ssb);
		}
		for (int i = 0; i < imgs.length; i++) {
			ImageEmbedSpan current = (ImageEmbedSpan) (imgs[i]);
			int totalDisplacement = insertImgsAndFixSpans(current.getSrc(), ssb
					.getSpanStart(current), ssb);
		}
		Log.d(TAG, "SpannableToXHTML returns: " + ssb.toString());
		// WebView webview = findViewById("WebView");
		// webview.loadDataWithBaseURL(null, ssb.toString(), "text/html",
		// "UTF-8", "about:blank");
		// webview.
		return /* ssb.toString(); */str;
	}

	/*
	 * public Spannable removeSpans(int sStart,int sEnd, Spannable seq){
	 * SpannableStringBuilder ssb = new SpannableStringBuilder(seq); Object[]
	 * spans; for (int i = 0; i < seq.length(); i++) { spans = ssb.getSpans(i,
	 * i+1, StyleSpan.class); boolean bold=false; boolean italic=false; for (int
	 * j = 0; j < spans.length; j++) { int style = ((StyleSpan)
	 * spans[j]).getStyle(); if (style==android.graphics.Typeface.BOLD){ bold =
	 * true; if (!isBoldEnabled) { isBoldEnabled = true; str=str+"<b>"; } } if
	 * (style==android.graphics.Typeface.ITALIC){ italic=true; if
	 * (!isItalicEnabled) { isItalicEnabled = true; str=str+"<i>"; } } } if
	 * ((isItalicEnabled)&&(!italic)) { isBoldEnabled = false; str=str+"</i>"; }
	 * if ((isBoldEnabled)&&(!bold)) { isBoldEnabled = false; str=str+"</b>"; }
	 * str=str+seq.charAt(i); } }
	 */

	private int insertMarkupAndFixSpans(int style, int startoffset,
			int endoffset, SpannableStringBuilder ssb) {
		String starttag = null;
		String endtag = null;
		switch (style) {
		case android.graphics.Typeface.BOLD:
			starttag = "<b>";
			endtag = "</b>";
			break;
		case android.graphics.Typeface.ITALIC:
			starttag = "<i>";
			endtag = "</i>";
			break;
		case android.graphics.Typeface.BOLD_ITALIC:
			starttag = "<i><b>";
			endtag = "</b></i>";
			break;
		case android.graphics.Typeface.NORMAL:
			starttag = "";
			endtag = "";
		}
		ssb.insert(startoffset, starttag);
		ssb.insert(endoffset + starttag.length(), endtag);
		/*
		 * Object [] spans = ssb.getSpans(0, ssb.length(), StyleSpan.class);
		 * for(int i = 0; i < spans.length; i++) { StyleSpan current =
		 * (StyleSpan)(spans[i]); int oldstart = ssb.getSpanStart(current); int
		 * oldend = ssb.getSpanEnd(current); if(oldstart < startoffset) { //
		 * change was before this point, no change } else if((oldstart >=
		 * startoffset) && (oldstart < endoffset)) { //the old start of span was
		 * between the start and end tag, we need to move it forward
		 * ssb.setSpan(current, oldstart+starttag.length(), , flags) } }
		 */
		return starttag.length() + endtag.length();
	}

	private int insertLinksAndFixSpans(String URL, int startoffset,
			int endoffset, SpannableStringBuilder ssb) {
		String starttag = "<a href = \'" + URL + "\'>";
		String endtag = "</a>";
		ssb.insert(startoffset, starttag);
		ssb.insert(endoffset + starttag.length(), endtag);
		return starttag.length() + endtag.length();
	}

	private int insertHTMLAndFixSpans(String html, int startoffset,
			SpannableStringBuilder ssb) {
		ssb.insert(startoffset, html);
		return html.length();
	}

	private int insertImgsAndFixSpans(String imgsrc, int startoffset,
			SpannableStringBuilder ssb) {
		String html = "<img src = \"" + imgsrc + "\"/>";
		ssb.insert(startoffset, html);
		return html.length();
	}

	/**
	 * Reverses the process of SpannableToXHTML and supports the same tags/span
	 * transitions.
	 * 
	 * @param xhtml
	 * @return
	 */

	public Spannable XHTMLToSpannable(CharSequence xhtml) {
		SpannableStringBuilder ssb = new SpannableStringBuilder();
		int index = 0;
		isBoldEnabled = false;
		boldStart = 0;
		italicStart = 0;
		boldEnd = 0;
		italicEnd = 0;
		isItalicEnabled = false;
		for (int i = 0; i < xhtml.length(); i++) {
			boolean flag = false;
			int tag = isTag(xhtml, i);
			if (tag == OPEN_BOLD) {
				i = i + 2;
				if (!isBoldEnabled) {
					isBoldEnabled = true;
					boldStart = index;
				}
				flag = true;
			}
			if (tag == OPEN_ITALIC) {
				i = i + 2;
				if (!isItalicEnabled) {
					isItalicEnabled = true;
					italicStart = index;
				}
				flag = true;
			}
			if (tag == CLOSE_BOLD) {
				i = i + 3;
				if (isBoldEnabled) {
					isBoldEnabled = false;
					boldEnd = index;
				}
				flag = true;
				ssb.setSpan(new StyleSpan(Typeface.BOLD), boldStart, boldEnd,
						Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			}
			if (tag == CLOSE_ITALIC) {
				i = i + 3;
				if (isItalicEnabled) {
					isItalicEnabled = false;
					italicEnd = index;
				}
				flag = true;
				ssb.setSpan(new StyleSpan(Typeface.ITALIC), italicStart,
						italicEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			}
			if (!flag) {
				ssb.append(xhtml.charAt(i));
				index++;
			}
		}
		return ssb;
	}

	private int isTag(CharSequence xhtml, int index) {
		if (xhtml.charAt(index) != '<')
			return -1;
		try {
			String str = "" + xhtml.charAt(index) + xhtml.charAt(index + 1)
					+ xhtml.charAt(index + 2);
			if (str.equals("<b>"))
				return OPEN_BOLD;
			if (str.equals("<i>"))
				return OPEN_ITALIC;
			str = str + xhtml.charAt(index + 3);
			if (str.equals("</b>"))
				return CLOSE_BOLD;
			if (str.equals("</i>"))
				return CLOSE_ITALIC;
		} catch (Exception e) {
			return -1;
		}
		return -1;
	}

	/**
	 * This does not really serialize the parameter object, it just gets the
	 * spans from the SpannableString, and postfixes the content with this
	 * information.
	 * 
	 * @param xhtml
	 * @return
	 */
	
	public String getSerializedSequence(CharSequence xhtml) {
		SpannableStringBuilder ssb = new SpannableStringBuilder(xhtml);
		Object[] spans = ssb.getSpans(0, ssb.length(), CharacterStyle.class);
		StringBuilder sb = new StringBuilder("|"
				+ Base64.encode(xhtml.toString().getBytes()));
		for (int i = 0; i < spans.length; i++) {
			int type = 0;
			int start = 0;
			int end = 0;
			int flags = 0;
			CharacterStyle current = (CharacterStyle) (spans[i]);
			if (current instanceof StyleSpan) {
				StyleSpan currentstyle = (StyleSpan) current;
				type = currentstyle.getStyle();
				start = ssb.getSpanStart(currentstyle);
				end = ssb.getSpanEnd(currentstyle);
				flags = ssb.getSpanFlags(currentstyle);
				sb.append("|" + type + "," + start + "," + end + "," + flags);
			} else if (current instanceof URLSpan) {
				URLSpan currenturl = (URLSpan) current;
				type = URL_SPAN_TYPE;
				start = ssb.getSpanStart(currenturl);
				end = ssb.getSpanEnd(currenturl);
				flags = ssb.getSpanFlags(currenturl);
				sb.append("|" + type + "," + start + "," + end + "," + flags
						+ "," + currenturl.getURL());
			} else if (current instanceof HTMLEmbedSpan) {
				HTMLEmbedSpan htmlspan = (HTMLEmbedSpan) current;
				type = HTML_SPAN_TYPE;
				start = ssb.getSpanStart(htmlspan);
				end = start;
				flags = ssb.getSpanFlags(htmlspan);
				sb.append("|" + type + "," + start + "," + end + "," + flags
						+ "," + htmlspan.getHtml());
			} else if (current instanceof ImageEmbedSpan) {
				ImageEmbedSpan imgspan = (ImageEmbedSpan) current;
				type = IMG_SPAN_TYPE;
				start = ssb.getSpanStart(imgspan);
				end = start;
				flags = ssb.getSpanFlags(imgspan);
				sb.append("|" + type + "," + start + "," + end + "," + flags
						+ "," + imgspan.getSrc());
			}

		}
		return sb.toString();

	}

	/**
	 * Reverses the operation of getSerializedSequence.
	 * 
	 * @param encoded
	 * @return
	 */

	public CharSequence deSerializeStringToSequence(String encoded, Context c) {
		String[] splits = encoded.split("\\|");
		String contentBase64 = splits[1];
		this.c = c;
		int item = 2;
		byte[] content = null;
		content = Base64.decode(contentBase64);
		String res = new String(content);
		SpannableStringBuilder ssb = new SpannableStringBuilder(res);
		// try to parse styles only if there are some.
		if (splits.length > 2) {
			for (String istylestr = splits[item]; item < splits.length;) {
				StringTokenizer strTok = new StringTokenizer(istylestr, ",",
						false);
				int count = 0, style = -1, start = -1, flags = 0, end = 0;
				String contentOfSpan = null;
				while (strTok.hasMoreTokens()) {
					String part = strTok.nextToken();
					try {
						if (count == 0) {
							style = Integer.parseInt(part);
						} else if (count == 1) {
							start = Integer.parseInt(part);
						} else if (count == 2) {
							end = Integer.parseInt(part);
						} else if (count == 3) {
							flags = Integer.parseInt(part);
						} else if (count == 4) {
							contentOfSpan = part;
						}
					} catch (NumberFormatException e) {
						Log.d(TAG, "Exception when parsing spans at phase:"
								+ count + "/" + item);
					}
					count++;
				}
				CharacterStyle currentSpan = null;
				if (style == IMG_SPAN_TYPE) {
					currentSpan = new ImageEmbedSpan(contentOfSpan, c);
					// since these spans can't be of zero length, blogentry
					// editor inserts one empty character
					// and we +1 the end
					ssb.setSpan(currentSpan, start, end + 1, flags);
					Log.d(TAG,
							"Attached ImageEmbedSpan to buffer(start,end,flags):"
									+ start + "," + end + "(+1)," + flags);
				} else if (style == URL_SPAN_TYPE) {
					currentSpan = new URLSpan(contentOfSpan);
					ssb.setSpan(currentSpan, start, end, flags);
					Log.d(TAG, "Attached UrlSpan to buffer(start,end,flags):"
							+ start + "," + end + "," + flags);
				} else if (style == HTML_SPAN_TYPE) {
					currentSpan = new HTMLEmbedSpan(contentOfSpan, c);
					// since these spans can't be of zero length, blogentry
					// editor inserts one empty character
					// and we +1 the end
					ssb.setSpan(currentSpan, start, end + 1, flags);
					Log.d(TAG,
							"Attached HTMLEmbedSpan to buffer(start,end,flags):"
									+ start + "," + end + "(+1)," + flags);
				} else {
					currentSpan = new StyleSpan(style);
					ssb.setSpan(currentSpan, start, end, flags);
					Log.d(TAG, "Attached StyleSpan to buffer(start,end,flags):"
							+ start + "," + end + "," + flags);
				}
				try {
					istylestr = splits[++item];
				} catch (ArrayIndexOutOfBoundsException e) {
					// this will happen with last index
					Log.d(TAG, "End of parsing reached.");
					continue;
				}
			}
		} else {
			Log.d(TAG, "Entry does not contain styling information.");
		}
		return ssb;

	}

	/**
	 * Hell of an ugly hack, get rid of this immediately
	 * 
	 * @param c
	 */

	public void enableContextAwareness(Context c) {
		this.c = c;
	}
}