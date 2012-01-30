package org.simpletracking.www;

import com.google.android.c2dm.C2DMBaseReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.util.Log;
 
public class C2DMReceiver extends C2DMBaseReceiver {
    public C2DMReceiver(){
        super("simpletrackingorg@gmail.com");
    }
 
    @Override
    public void onRegistered(Context context, String registrationId) {
        Log.w("onRegistered", registrationId);
        SimpleTrackingService.sendPushId(registrationId);
        SimpleTrackingService.pushId = registrationId;
        Log.e ("gps","Send PushID: " + SimpleTrackingService.pushId);
    }
 
    @Override
    public void onUnregistered(Context context) {
    	Log.e ("gps","onUnregistered");        
    }
 
    @Override
    public void onError(Context context, String errorId) {
        Log.w("onError", errorId);
    }
 
    @Override
    protected void onMessage(Context context, Intent receiveIntent) 
    {
        String data = receiveIntent.getStringExtra("message");
        if(data != null)
        {        
            Log.e("gps","C2DMReceiver: " + data);
            
            if (data.contentEquals("getPosition"))
            {
            	//Активировать сервис получения координат
            	Log.e("gps","Need Activate GPS");            	    		
            	SimpleTrackingService.gpsNeedActivate = true;	
            }            
        }
    }
}