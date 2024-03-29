// Copyright 2008 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package mobi.omegacentauri.ao;

import mobi.omegacentauri.ao.activities.DynamicStarMapActivity;
import mobi.omegacentauri.ao.control.AstronomerModel;
import mobi.omegacentauri.ao.control.AstronomerModelImpl;
import mobi.omegacentauri.ao.control.ZeroMagneticDeclinationCalculator;
import mobi.omegacentauri.ao.layers.EclipticLayer;
import mobi.omegacentauri.ao.layers.GridLayer;
import mobi.omegacentauri.ao.layers.HorizonLayer;
import mobi.omegacentauri.ao.layers.LayerManager;
import mobi.omegacentauri.ao.layers.MeteorShowerLayer;
import mobi.omegacentauri.ao.layers.NewConstellationsLayer;
import mobi.omegacentauri.ao.layers.NewMessierLayer;
import mobi.omegacentauri.ao.layers.NewStarsLayer;
import mobi.omegacentauri.ao.layers.PlanetsLayer;
import mobi.omegacentauri.ao.layers.SkyGradientLayer;
import mobi.omegacentauri.ao.util.MiscUtil;
import mobi.omegacentauri.ao.util.OsVersions;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.Calendar;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

/**
 * The main Stardroid Application class.
 *
 * @author John Taylor
 */
public class StardroidApplication extends Application {
  private static final String TAG = MiscUtil.getTag(StardroidApplication.class);
  private static final String PREVIOUS_APP_VERSION_PREF = "previous_app_version";
  private static final String NONE = "Clean install";
  private static final String UNKNOWN = "Unknown previous version";
  private static final boolean DEBUG = true;
  // The Application class is a singleton, so treat it as such, with static
  // fields.  This is necessary so that the content provider can access the
  // things it needs; there seems to be no easy way for a ContentProvider
  // to access its Application object.
  private static AstronomerModel model;
  private static LayerManager layerManager;
  private static ExecutorService backgroundExecutor;
  
  public void log(String s) {
	  if (DEBUG)
		  Log.i("OpenStarMap", s);
  }

  @Override
  public void onCreate() {
    Log.d(TAG, "StardroidApplication: onCreate");
    super.onCreate();

    Log.i(TAG, "OS Version: " + android.os.Build.VERSION.RELEASE
        + "(" + android.os.Build.VERSION.SDK + ")");
    String versionName = getVersionName();
    Log.i(TAG, "Sky Map version " + versionName + " build " + getVersion());
    backgroundExecutor = new ScheduledThreadPoolExecutor(1);
    // This populates the default values from the preferences XML file. See
    // {@link DefaultValues} for more details.
    PreferenceManager.setDefaultValues(this, R.xml.preference_screen, false);

    AssetManager assetManager = this.getAssets();
    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
    Resources resources = this.getResources();
    // Start the LayerManager initializing
    getLayerManager(assetManager, preferences, resources, this);
    Log.d(TAG, "StardroidApplication: -onCreate");
  }

  @Override
  public void onTerminate() {
    super.onTerminate();
  }

  /**
   * Returns the version string for Sky Map.
   */
  public String getVersionName() {
    PackageManager packageManager = getPackageManager();
    try {
      PackageInfo info = packageManager.getPackageInfo(this.getPackageName(), 0);
      return info.versionName;
    } catch (NameNotFoundException e) {
      Log.e(TAG, "Unable to obtain package info");
      return "Unknown";
    }
  }

  /**
   * Returns the build number for Sky Map.
   */
  public int getVersion() {
    PackageManager packageManager = getPackageManager();
    try {
      PackageInfo info = packageManager.getPackageInfo(this.getPackageName(), 0);
      return info.versionCode;
    } catch (NameNotFoundException e) {
      Log.e(TAG, "Unable to obtain package info");
      return -1;
    }
  }

  /**
   * Get the catalog.
   * This should return relatively quickly, with the catalogs initializing
   * themselves on background threads.
   */
  public static synchronized LayerManager getLayerManager(AssetManager assetManager,
                                                          SharedPreferences preferences,
                                                          Resources resources,
                                                          Context context) {
    if (layerManager == null) {
      Log.i(TAG, "Initializing LayerManager");
      layerManager = new LayerManager(preferences, getModel());
      layerManager.addLayer(new NewStarsLayer(assetManager, resources));
      layerManager.addLayer(new NewMessierLayer(assetManager, resources));
      layerManager.addLayer(new NewConstellationsLayer(assetManager, resources));
      layerManager.addLayer(new PlanetsLayer(getModel(), resources, preferences));
      layerManager.addLayer(new MeteorShowerLayer(getModel(), resources));
      layerManager.addLayer(new GridLayer(resources, 24, 19));
      layerManager.addLayer(new HorizonLayer(getModel(), resources));
      layerManager.addLayer(new EclipticLayer(resources));
      layerManager.addLayer(new SkyGradientLayer(getModel(), resources));
      // layerManager.addLayer(new IssLayer(resources, getModel()));
      layerManager.initialize();
    } else {
      Log.i(TAG, "LayerManager already initialized.");
    }
    return layerManager;
  }

  /**
   * Return the model.
   */
  public static synchronized AstronomerModel getModel() {
    if (model == null) {
      model = new AstronomerModelImpl(new ZeroMagneticDeclinationCalculator());
    }
    return model;
  }

  /**
   * Schedules this runnable to run as soon as possible on a background
   * thread.
   *
   * @param runnable
   */
  // TODO(johntaylor): the idea, and I'm not sure yet whether it's a good one,
  // is to centralize the management of background threads so we don't have
  // them scattered all over the app.  We can then control how many threads
  // are spawned, perhaps having a VIP service for extra important runnables
  // that we'd prefer not to queue, as well as providing convenience functions
  // to facilitate callbacks on the UI thread.
  public static void runInBackground(Runnable runnable) {
    backgroundExecutor.submit(runnable);
  }
}
