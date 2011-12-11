package sk.pack.db;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.text.style.ImageSpan;
import android.util.Log;
import sk.pack.R;

/**
 * This produces the small "html" image to indicate that a buffer that's opened
 * in entry editor has a HTML span attached to it.
 * 
 * @author juhak
 */

public class HTMLEmbedSpan extends ImageSpan {

	private static final String TAG = "HTMLEmbedSpan";
	private String myHtml = null;
	private Context parentRef = null;

	public HTMLEmbedSpan(String html, Context caller) {
		super(caller, R.drawable.html_icon);
		this.myHtml = html;
		this.parentRef = caller;
	}

	@Override
	public Drawable getDrawable() {
		// Resources.getResources doesn't seem to work?
		Context context = parentRef;
		Resources resources = null;
		if (context != null) {
			resources = context.getResources();
		} else {
			return null;
		}
		Drawable d = resources.getDrawable(R.drawable.html_icon);
		d.setVisible(true, true);
		d.setAlpha(255);
		d.setBounds(new Rect(0, 0, d.getIntrinsicWidth(), d
				.getIntrinsicHeight()));
		return d;
	}

	public String getHtml() {
		return myHtml;
	}

	public void setHtml(String myHtml) {
		this.myHtml = myHtml;
	}

}