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
package mobi.omegacentauri.ao.activities;

import mobi.omegacentauri.ao.R;
import mobi.omegacentauri.ao.activities.util.ActivityLightLevelChanger;
import mobi.omegacentauri.ao.activities.util.ActivityLightLevelManager;
import mobi.omegacentauri.ao.util.MiscUtil;
import mobi.omegacentauri.ao.util.OsVersions;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.res.ObbScanner;
import android.location.Address;
import android.location.Geocoder;
import android.os.Build;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceGroup;
import android.preference.PreferenceManager;
import android.preference.Preference.OnPreferenceChangeListener;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Edit the user's preferences.
 */
public class EditSettingsActivity extends PreferenceActivity {
  /**
   * These must match the keys in the preference_screen.xml file.
   */
  private static final String LONGITUDE = "longitude";
  private static final String LATITUDE = "latitude";
  private static final String LOCATION = "location";
  private static final String TAG = MiscUtil.getTag(EditSettingsActivity.class);
private static final CharSequence LOCATION_SETTINGS = "location_settings";
  private Geocoder geocoder;
  private ActivityLightLevelManager activityLightLevelManager;

  @SuppressLint("NewApi")
@Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    activityLightLevelManager = new ActivityLightLevelManager(
        new ActivityLightLevelChanger(this, null),
        PreferenceManager.getDefaultSharedPreferences(this));
    addPreferencesFromResource(R.xml.preference_screen);
    Preference editPreference = findPreference(LOCATION);
    if ( OsVersions.isKindle1() || (Build.VERSION.SDK_INT >= 9 && ! Geocoder.isPresent())) {
    	PreferenceGroup parent = (PreferenceGroup)findPreference(LOCATION_SETTINGS);
    	parent.removePreference(editPreference);
    }
    else {
    	geocoder = new Geocoder(this);
	    // TODO(johntaylor) if the lat long prefs get changed manually, we should really
	    // reset the placename to "" too.
	    editPreference.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
	
	      public boolean onPreferenceChange(Preference preference, Object newValue) {
	        Log.d(TAG, "Place to be updated to " + newValue);
	        boolean success = setLatLongFromPlace(newValue.toString());
	        return success;
	      }
	    });
    }
  }

  @Override
  public void onStart() {
    super.onStart();
  }

  @Override
  public void onResume() {
    super.onResume();
    activityLightLevelManager.onResume();
  }

  @Override
  public void onPause() {
    super.onPause();
    updatePreferences();
    activityLightLevelManager.onPause();
  }

  /**
   * Updates preferences on singletons, so we don't have to register
   * preference change listeners for them.
   */
  private void updatePreferences() {
    Log.d(TAG, "Updating preferences");
  }

  protected boolean setLatLongFromPlace(String place) {
    List<Address> addresses = new ArrayList<Address>();
    try {
      addresses = geocoder.getFromLocationName(place, 1);
    } catch (IOException e) {
      Log.e(TAG, ""+e);
      Toast.makeText(this, getString(R.string.location_unable_to_geocode), Toast.LENGTH_LONG).show();
      return false;
    }
    if (addresses.size() == 0) {
      showNotFoundDialog(place);
      return false;
    }
    // TODO(johntaylor) let the user choose, but for now just pick the first.
    Address first = addresses.get(0);
    setLatLong(first.getLatitude(), first.getLongitude());
    return true;
  }

  private void setLatLong(double latitude, double longitude) {
    EditTextPreference latPreference = (EditTextPreference) findPreference(LATITUDE);
    EditTextPreference longPreference = (EditTextPreference) findPreference(LONGITUDE);
    latPreference.setText(Double.toString(latitude));
    longPreference.setText(Double.toString(longitude));
    String message = String.format(getString(R.string.location_place_found), latitude, longitude);
    Log.d(TAG, message);
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
  }

  private void showNotFoundDialog(String place) {
    String message = String.format(getString(R.string.location_not_found), place);
    AlertDialog.Builder dialog = new AlertDialog.Builder(this)
        .setTitle(R.string.location_not_found_title)
        .setMessage(message)
        .setPositiveButton(android.R.string.ok, new OnClickListener() {
          public void onClick(DialogInterface dialog, int which) {
            dialog.dismiss();
          }
        });
    dialog.show();
  }
}
