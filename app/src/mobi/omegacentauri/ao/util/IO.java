package mobi.omegacentauri.ao.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

public class IO {
	  private static final String TAG = MiscUtil.getTag(OsVersions.class);

	  static private String getStreamFile(InputStream stream) {
		BufferedReader reader;
		try {
			reader = new BufferedReader(new InputStreamReader(stream));

			String text = "";
			String line;
			while (null != (line=reader.readLine()))
				text = text + line;
			return text;
		} catch (IOException e) {
			Log.e(TAG, ""+e);
			return "";
		}
	}
	
	static public String getAssetFile(Context context, String assetName) {
		try {
			return getStreamFile(context.getAssets().open(assetName));
		} catch (IOException e) {
			Log.e(TAG, ""+e);
			return "";
		}
	}
	
	static public String getURLFile(String url) {
		try {
			return getStreamFile((new URL(url)).openStream());
		} catch (MalformedURLException e) {
			Log.e(TAG, ""+e);
			return "";
		} catch (IOException e) {
			Log.e(TAG, ""+e);
			return "";
		}
	}

}
