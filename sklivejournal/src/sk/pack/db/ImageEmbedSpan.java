package sk.pack.db;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.text.style.ImageSpan;
import android.util.Log;
import sk.pack.R;

/**
 * This produces the small "IMG" image to indicate that a buffer that's opened
 * in entry editor has a image span attached to it.
 * 
 * @author juhak
 */

public class ImageEmbedSpan extends ImageSpan {
	private static final String TAG = "ImageEmbedSpan";
	private String mySrc = null;
	private Context parentRef = null;

	public ImageEmbedSpan(String src, Context caller) {
		super(caller, R.drawable.img_icon);
		this.mySrc = src;
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
		Drawable d = resources.getDrawable(R.drawable.img_icon);
		d.setVisible(true, true);
		d.setAlpha(255);
		d.setBounds(new Rect(0, 0, d.getIntrinsicWidth(), d
				.getIntrinsicHeight()));
		return d;
	}

	public String getSrc() {
		return mySrc;
	}

	public void setSrc(String mySrc) {
		this.mySrc = mySrc;
	}

}