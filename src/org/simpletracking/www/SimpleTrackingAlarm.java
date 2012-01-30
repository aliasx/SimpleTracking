package org.simpletracking.www;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;

public class SimpleTrackingAlarm extends BroadcastReceiver {
	 
	 @Override
	 public void onReceive(Context context, Intent intent) {		 
		 if (SimpleTrackingService.controlHandler != null)
		 {
			//Log.e ("gps","alarm!!!");
		   try {
	      		if (SimpleTrackingService.usePush)
	      		{
	      			if (!SimpleTrackingService.isGpsOn)
	      			{
	      				SimpleTrackingService.lastGpsTime = SystemClock.elapsedRealtime();
	      			}
	      			if(SimpleTrackingService.gpsNeedActivate)
	      			{
	      				SimpleTrackingService.controlHandler.sendEmptyMessage(SimpleTrackingService.SEND_PUSH_GPSON);
	      			}
	      			//Если больше 60 секунд был включен GPS, то отключить его
	      			if ((SystemClock.elapsedRealtime() - SimpleTrackingService.lastGpsTime) > 120000)
	      			{
	      				SimpleTrackingService.controlHandler.sendEmptyMessage(SimpleTrackingService.SEND_PUSH_GPSOFF);
	      			}    
	      		}
	      		else//Для режима без Push
	      		{
	           	 //Если GPS не активен
	      			if (!SimpleTrackingService.isGpsOn)
	      			{
	      				//Если таймер больше чем время отключения GPS, то надо включить
	      				if ((SystemClock.elapsedRealtime() - SimpleTrackingService.lastGpsTime) > (SimpleTrackingService.gpsOnTimeValue*1000))
	      				{	                       					
	      					SimpleTrackingService.lastGpsTime = SystemClock.elapsedRealtime();
	      					SimpleTrackingService.controlHandler.sendEmptyMessage(SimpleTrackingService.SEND_GPSON);			
	      					Log.e("gps", "AlarmTimer GPS on");			
	      				}
	      			}	
	      			//Если GPS активен
	      			else		
	      				{
	      					//Если таймер больше чем время отключения GPS, то надо включить
	      					if ((SystemClock.elapsedRealtime() - SimpleTrackingService.lastGpsTime) > (SimpleTrackingService.gpsOffTimeValue*1000))
	      					{	                       						
	      						SimpleTrackingService.controlHandler.sendEmptyMessage(SimpleTrackingService.SEND_GPSOFF);
	      						SimpleTrackingService.lastGpsTime = SystemClock.elapsedRealtime();
	      						Log.e("gps", "AlarmTimer GPS off");
	      					}
	      				}				                            	   
	          		}  	       	   	       	   	          	    		     
		    } 
		   catch (Exception e) 
		   {	     
		     e.printStackTrace();	 
		   }
		 }
	 }	 
}