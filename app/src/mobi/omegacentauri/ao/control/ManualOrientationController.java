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

package mobi.omegacentauri.ao.control;

import mobi.omegacentauri.ao.control.AstronomerModel.Pointing;
import mobi.omegacentauri.ao.units.GeocentricCoordinates;
import mobi.omegacentauri.ao.units.Matrix33;
import mobi.omegacentauri.ao.units.Vector3;
import mobi.omegacentauri.ao.util.Geometry;
import mobi.omegacentauri.ao.util.MiscUtil;

import android.util.Log;

/**
 * Allows user-input elements such as touch screens and trackballs to move the
 * map.
 *
 * @author John Taylor
 */
public class ManualOrientationController extends AbstractController {
  private static final String TAG = MiscUtil.getTag(ManualOrientationController.class);
  
  private boolean altAz = true;

  @Override
  public void start() {
    // Nothing to do
  }

  @Override
  public void stop() {
    // Nothing to do
  }

  /**
   * Moves the astronomer's pointing right or left.
   *
   * @param radians the angular change in the pointing in radians (only
   * accurate in the limit as radians tends to 0.)
   */
  public void changeRightLeft(float radians) {
    // TODO(johntaylor): Some of the Math in here perhaps belongs in
    // AstronomerModel.
    if (!enabled) {
      return;
    }
    
    Pointing pointing = model.getPointing();
    GeocentricCoordinates pointingXyz = pointing.getLineOfSight();
    GeocentricCoordinates topXyz = pointing.getPerpendicular();

    if (altAz) {
    	float degrees = radians * 180f / (float)Math.PI;
        Matrix33 rotation = Geometry.calculateRotationMatrix(degrees, model.getZenith());
        model.setPointing(Geometry.matrixVectorMultiply(rotation, pointingXyz),
        		Geometry.matrixVectorMultiply(rotation, topXyz));
        return;
    }
    
    Vector3 horizontalXyz = Geometry.vectorProduct(pointingXyz, topXyz);
    Vector3 deltaXyz = Geometry.scaleVector(horizontalXyz, radians);

    Vector3 newPointingXyz = Geometry.addVectors(pointingXyz, deltaXyz);
    newPointingXyz.normalize();

    model.setPointing(newPointingXyz, topXyz);
  }

  /**
   * Moves the astronomer's pointing up or down.
   *
   * @param radians the angular change in the pointing in radians (only
   * accurate in the limit as radians tends to 0.)
   */
  public void changeUpDown(float radians) {
    if (!enabled) {
      return;
    }
    // Log.d(TAG, "Scrolling up down");
    Pointing pointing = model.getPointing();
    GeocentricCoordinates pointingXyz = pointing.getLineOfSight();
    // Log.d(TAG, "Current view direction " + viewDir);
    GeocentricCoordinates topXyz = pointing.getPerpendicular();

    if (altAz) {
    	float currentAngle = (float)Math.acos((double)Geometry.cosineSimilarity(pointingXyz, model.getZenith())); 
    	
    	Log.d(TAG, "currentAngle "+currentAngle+" rotation="+radians);
    	
    	Vector3 zenith = model.getZenith();
    	
    	if (radians < 0 &&  currentAngle <= -radians) {
    		model.setPointing(zenith, topXyz);    		
    	}
    	else if (radians > 0 && currentAngle >= (float)Math.PI-radians) {
    		model.setPointing(Geometry.scaleVector(zenith, -1f), topXyz);    		    		
    	}
    	else {
        	float degrees = radians * 180f / (float)Math.PI;

        	Vector3 rotationVector = Geometry.vectorProduct(pointingXyz,
        			topXyz);
        	
	        Matrix33 rotation = Geometry.calculateRotationMatrix(degrees, 
	        		rotationVector);
	        
	        model.setPointing(Geometry.matrixVectorMultiply(rotation, pointingXyz),
	        		Geometry.matrixVectorMultiply(rotation, topXyz));
    	}
        
        return;
    }
    
    Vector3 deltaXyz = Geometry.scaleVector(topXyz, -radians);
    Vector3 newPointingXyz = Geometry.addVectors(pointingXyz, deltaXyz);
    newPointingXyz.normalize();

    Vector3 deltaUpXyz = Geometry.scaleVector(pointingXyz, radians);
    Vector3 newUpXyz = Geometry.addVectors(topXyz, deltaUpXyz);
    newUpXyz.normalize();

    model.setPointing(newPointingXyz, newUpXyz);

    resetRotation();
  }

  /**
   * Rotates the astronomer's view.
   */
  public void rotate(float degrees) {
    if (!enabled) {
      return;
    }
    Log.d(TAG, "Rotating by " + degrees);
    Pointing pointing = model.getPointing();
    GeocentricCoordinates pointingXyz = pointing.getLineOfSight();

    Matrix33 rotation = Geometry.calculateRotationMatrix(degrees, pointingXyz);

    GeocentricCoordinates topXyz = pointing.getPerpendicular();

    Vector3 newUpXyz = Geometry.matrixVectorMultiply(rotation, topXyz);
    newUpXyz.normalize();

    model.setPointing(pointingXyz, newUpXyz);
  }


  /**
   * Reset rotation.
   */
  public void resetRotation() {
    Log.d(TAG, "Unrotating");
    
    if (altAz) {
	    Pointing pointing = model.getPointing();
	
	    GeocentricCoordinates pointingXyz = pointing.getLineOfSight();
	    /* TODO: fix if too close to zenith */
	    Vector3 right = Geometry.vectorProduct(pointingXyz, model.getZenith());
	    
	    if (Geometry.scalarProduct(right, right)<1e-15) {
	    	/* too close */
	    	return;
	    }
	    
	    right.normalize();
	    Vector3 up = Geometry.vectorProduct(right, pointingXyz);
	    
	    model.setPointing(pointingXyz, up); 
    }
  }
  
  public void setAltAz(boolean altAz) {
	  this.altAz = altAz;
	  
	  if (altAz && enabled) {
		  resetRotation();
	  }
  }
}

