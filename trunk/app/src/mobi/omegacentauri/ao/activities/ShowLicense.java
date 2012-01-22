/*
 * Copyright (C) 2008-2009 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package mobi.omegacentauri.ao.activities;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.text.Html;
import mobi.omegacentauri.ao.R;

public class ShowLicense extends PreferenceActivity {
    @Override
    protected void onCreate(Bundle icicle) {
    	Resources r = getResources();
        super.onCreate(icicle);
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle(r.getString(R.string.license_title));
        alertDialog.setMessage(Html.fromHtml(r.getString(R.string.license)));
        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, r.getString(android.R.string.ok), 
        	new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {finish();} });
        alertDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            public void onCancel(DialogInterface dialog) {finish();} });
        alertDialog.show();
    }
}
