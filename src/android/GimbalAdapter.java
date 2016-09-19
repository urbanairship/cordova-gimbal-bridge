/*
 Copyright 2009-2015 Urban Airship Inc. All rights reserved.
 
 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions are met:
 
 1. Redistributions of source code must retain the above copyright notice, this
 list of conditions and the following disclaimer.
 
 2. Redistributions in binary form must reproduce the above copyright notice,
 this list of conditions and the following disclaimer in the documentation
 and/or other materials provided with the distribution.
 
 THIS SOFTWARE IS PROVIDED BY THE URBAN AIRSHIP INC ``AS IS'' AND ANY EXPRESS OR
 IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
 EVENT SHALL URBAN AIRSHIP INC OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.urbanairship.cordova.gimbal;

import com.urbanairship.Logger;
import com.urbanairship.UAirship;
import com.urbanairship.location.RegionEvent;

import com.gimbal.android.Gimbal;
import com.gimbal.android.PlaceManager;
import com.gimbal.android.PlaceEventListener;
import com.gimbal.android.Place;
import com.gimbal.android.Visit;

public class GimbalAdapter {
	
	private static final String K_SOURCE = "Gimbal";
	
	private static GimbalAdapter _instance = new GimbalAdapter();
	public static GimbalAdapter getInstance(){
		return _instance;
	}
	
	private Boolean _started = false;
	private PlaceManager _placeManager = PlaceManager.getInstance();
	
	private PlaceEventListener _placeEventListener = new PlaceEventListener(){
		public void onVisitStart(Visit visit) {
			Logger.debug("Entered a Gimbal Place: " + visit.place.name + " on the following date: " + visit.arrivalDate);
			
			reportPlaceEventToAnalytics(visit.place, RegionEvent.BOUNDARY_EVENT_ENTER);
		}
		
		public void onVisitEnd(Visit visit) {
			Logger.debug("Exited a Gimbal Place: " + visit.place.name + " Entrance date:" + visit.arrivalDate + " Exit Date:" + visit.departureDate);
			
			reportPlaceEventToAnalytics(visit.place, RegionEvent.BOUNDARY_EVENT_EXIT);
		}
	}
	
	private GimbalAdapter(){
		_placeManager.addListener(_placeEventListener);
	}
	
	public void startAdapter(){
		if (_started) {
			return;
		}
		_started = true;
		
		_placeManager.startMonitoring();
		
		Logger.debug("Started Gimbal Adapter.");
	}
	
	public void stopAdapter(){
		if (!_started) {
			return;
		}
		_started = false;
		
		_placeManager.stopMonitoring();
		
		Logger.debug("Stopped Gimbal Adapter.");
	}
	
	private void reportPlaceEventToAnalytics(Place place, int boundaryEvent){
		RegionEvent regionEvent = new RegionEvent(place.identifier, K_SOURCE, boundaryEvent);
		UAirship.shared().analytics.addEvent(regionEvent);
	}
}