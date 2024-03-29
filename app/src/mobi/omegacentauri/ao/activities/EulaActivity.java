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

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Animation.AnimationListener;

import mobi.omegacentauri.ao.R;
import mobi.omegacentauri.ao.util.MiscUtil;
/**
 * Shows a splash screen, then launch the next activity.
 */
public class EulaActivity extends Activity {
  private final static String TAG = MiscUtil.getTag(EulaActivity.class);
    
  @Override
  public void onCreate(Bundle savedInstanceState) {
    Log.d(TAG, "SplashScreen onCreate");
    super.onCreate(savedInstanceState);
    setContentView(R.layout.splash);
    finish();
    
    final View graphic = findViewById(R.id.splash);
    Animation fadeAnimation = AnimationUtils.loadAnimation(this, R.anim.fadeout);
    fadeAnimation.setAnimationListener(new AnimationListener() {
      public void onAnimationEnd(Animation arg0) {
        Log.d(TAG, "SplashScreen.Animation onAnimationEnd");
        graphic.setVisibility(View.INVISIBLE);
        finish();
      }

      public void onAnimationRepeat(Animation arg0) {
      }

      public void onAnimationStart(Animation arg0) {
        Log.d(TAG, "SplashScreen.Animcation onAnimationStart");
      }
    });
    graphic.startAnimation(fadeAnimation);
  }
  
  @Override
  public void onPause() {
    Log.d(TAG, "SplashScreen onPause");
    super.onPause();
  } 
  
  @Override
  public void onDestroy() {
    Log.d(TAG, "SplashScreen onDestroy");
    super.onDestroy();
  }
  
  @Override
  public void finish() {
    Log.d(TAG,"SplashScreen finish called");
    Intent intent = new Intent(this, DynamicStarMapActivity.class);
    startActivity(intent);
    super.finish();
  }
}
