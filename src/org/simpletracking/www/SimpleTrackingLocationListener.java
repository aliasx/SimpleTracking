package org.simpletracking.www;
/* The part of SimpleTracking open source project. Web: http://simpletracking.org */
import java.text.SimpleDateFormat;
import java.util.Date;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;

public class SimpleTrackingLocationListener implements LocationListener {

	 //GPS------------------------------------------------
	public SimpleTrackingLocationListener()
	{
		Log.e("gps","SimpleTrackingLocationListener create");
        //Инициализация http post
        httpSender = new SimpleTrackingSender();

	}
		@Override
		public void onLocationChanged(Location loc)
		{		
			Log.e("gps","Coords!");
			currentLat = loc.getLatitude();
			currentLon = loc.getLongitude();
			currentAlt = loc.getAltitude();
			currentSpeed = loc.getSpeed();//Скорость в м/с, надо не забывать об этом!
			currentAccuracy = loc.getAccuracy();
			
			final long currentTime = loc.getTime();
			
			SimpleTrackingService.currentLat = currentLat;
			SimpleTrackingService.currentLon = currentLon;
			SimpleTrackingService.currentAlt = currentAlt;
			SimpleTrackingService.currentSpeed = currentSpeed;			
			SimpleTrackingService.currentAccuracy = currentAccuracy;			
			SimpleTrackingService.currentTime = currentTime;
						
			//Проверка на пройденное время, если прошло больше чем указано в настроках 
			if (((currentTime - prevTime) > (SimpleTrackingService.sendTimeValue*1000)) && (currentAccuracy < SimpleTrackingService.sendAccuracy))
			{
				Log.e("gps","Location time! " + lastLat + " " + prevTime);
	    		prevTime = currentTime;
	    		//То проверить изменилось ли положение
	    		if ((currentLat != lastLat) || (currentLon != lastLon))
	     	   	{
	    			Log.e("gps","Location send packet");
	     		   //Отправить координаты    	                            		       	                            		   	     		   
	     		   new Thread(new Runnable() {
	     			   public void run() {
	     				  if (httpSender.sendPosition(SimpleTrackingService.imei, currentLat, currentLon, currentAlt, currentSpeed, currentTime))
	     				  {
	     					SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	     					SimpleTrackingService.lastTime = dateFormat.format(new Date(currentTime)); 
	     					SimpleTrackingService.cntPackets++;		                	
	     				  }
	     				  else
	     				  {
	     					 SimpleTrackingService.cntErrorPackets++;			             
	     				  }
	     				 lastLat = currentLat;
	     				 lastLon = currentLon;
	     			   }
	     		   }).start();	     		        		   
	     		   //Если включен режим Push, то надо остановить сервис как только пришли нормальные координаты
	     		   if (SimpleTrackingService.usePush)
	     		   {    			
	     			  SimpleTrackingService.gpsOff();	     			  
	     		   }
	     	   	}
			}
	 	   }
		@Override
		public void onProviderDisabled(String provider)
		{				
		}
		@Override
		public void onProviderEnabled(String provider)
		{		
		}
		@Override
		public void onStatusChanged(String provider, int status, Bundle extras)
		{	
		}
		//-------------------------------------------------------
		  
		    private double currentLat = 0;
		    private double currentLon = 0;
		    private double currentAlt = 0;
		    private double currentSpeed = 0;
		    private double currentAccuracy = 0;
		    private double lastLat = 0;
		    private double lastLon = 0;
		    private long prevTime = 0;
		    
		    private SimpleTrackingSender httpSender;
}
