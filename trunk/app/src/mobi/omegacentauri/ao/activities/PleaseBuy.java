package mobi.omegacentauri.ao.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Build;

public class PleaseBuy {
	public static final String APPSTORE = "Appstore";
	public static final String MARKET = "Market";
	
	public static String detect(Context c) {
		PackageManager pm = c.getPackageManager();
				
		String installer = pm.getInstallerPackageName(c.getPackageName());
		
		if (installer != null && installer.equals("com.android.vending")) 
			return MARKET;
		
		if (Build.MODEL.equalsIgnoreCase("Kindle Fire")) 
			return APPSTORE;

		try {
			if (pm.getPackageInfo("com.amazon.venezia", 0) != null) 
				return APPSTORE;
		} catch (NameNotFoundException e) {
		}
		
		return MARKET;
	}
	

	
	public PleaseBuy(final Context c, boolean always) {
		final String market = detect(c);
		
		if (!always) {
			SharedPreferences p = c.getSharedPreferences("PleaseBuy", 0);
			int v;
			try {
				v = c.getPackageManager()
					.getPackageInfo(c.getPackageName(),0).versionCode;
			} catch (NameNotFoundException e) {
				v = 0;
			}
			if (p.getInt("version", 0) == v) {
				return;
			}
			SharedPreferences.Editor ed = p.edit();
			ed.putInt("version", v);
			ed.commit();
		}
		
        AlertDialog alertDialog = new AlertDialog.Builder(c).create();

        alertDialog.setTitle("Other applications?");
        
        alertDialog.setMessage("If you liked this, you may like our LunarMap moon maps and ScreenDim dimmer!  Visit " +market + "?");
        
        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, 
        		"See other apps", 
        	new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            	Intent i = new Intent(Intent.ACTION_VIEW);
            	i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            	if (market.contains("arket"))
            		i.setData(Uri.parse("market://search?q=pub:\"Omega Centauri Software\""));
            	else
            		i.setData(Uri.parse("http://www.amazon.com/gp/mas/dl/android?p=mobi.omegacentauri.ScreenDim.Full&showAll=1"));            		
            	c.startActivity(i);
            } });
        alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, 
        		"Not now", 
        	new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {} });
        alertDialog.show();				
	}
}
