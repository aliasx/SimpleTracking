package org.simpletracking.www;
/* The part of SimpleTracking open source project. Web: http://simpletracking.org */

import java.util.Timer;
import java.util.TimerTask;
import com.google.android.c2dm.C2DMessaging;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

public class SimpleTrackingService extends Service {
	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}
	@Override
	public void onCreate() {
		super.onCreate();
		//Toast.makeText(this,"Сервис запущен.", Toast.LENGTH_LONG).show();
		Log.e("gps","Create Service");
		readPrefs();
		Log.e("gps","Read prefs");
		imei = getImei();
		Log.e("gps","Get IMEI.");
		
		stListener = new SimpleTrackingLocationListener();
        locManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE); 
        
        //Управляющий хендлер
        controlHandler = new Handler() {        	 
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                	if (msg.what == SEND_PUSH_GPSON)
                	{
                		Log.e("gps","Activate GPS in AlarmTimer");
      					gpsNeedActivate = false;
      					gpsOn();
                	}
                	if (msg.what == SEND_PUSH_GPSOFF)
                	{
          				isGpsOn = false;    			
          				gpsOff();
          			   //Отправляем последние известные координаты			   
          			   if ((currentLat != 0) && (currentLon != 0))
          			   {
          			    	new Thread(new Runnable() {
          						   public void run() {				   				                				   
          											   SimpleTrackingSender httpSender = new SimpleTrackingSender();
          								               httpSender.sendPosition(
          								            		 imei, 
          								            		currentLat, 
          								            		currentLon, 
          								            		currentAlt, 
          								            		currentSpeed, 
          								            		currentTime
          								            		   				  );
          								               httpSender = null;
          								               Log.e ("gps","AlarmTimer Send old coordinates");
          						   }
          					   }).start();	 				   
          			   }
                	}
                	if (msg.what == SEND_GPSON)
                	{
                		gpsOn();
                	}
                	
                	if (msg.what == SEND_GPSOFF)
                	{
                		gpsOff();
                	}   	
                	
            }
        };
		
        if (usePush)
        {   
        	pushId =  C2DMessaging.getRegistrationId(this);         
            //Инициализация C2DM
            if(pushId == "")
            {
            	C2DMessaging.register(this, "simpletrackingorg@gmail.com");
            }
            else
            {
            	sendPushId(pushId);
            }
            //В аларме находится контроль длительности опроса GPS в режиме push
            createGpsTimer();
        }
        else
        {
	        //Если отключать GPS, то запускать таймер запуска сервиса, если нет - запускать сразу сервис
	        if (gpsOff)
	        {
	        	createGpsTimer();  
	        }
	        else
	        {
	            //Инициализация GPS	    		       
	            gpsOn();
	        }
        }
        
	}
		
	 
	private void createGpsTimer()
	{
		
		alarmManager= (AlarmManager) getSystemService(Context.ALARM_SERVICE);
	    Intent intent = new Intent(this, SimpleTrackingAlarm.class);
	    pendingIntent = PendingIntent.getBroadcast(this, 654376, intent, PendingIntent.FLAG_CANCEL_CURRENT);
	    alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 1000, pendingIntent);

		/*
		
    	gpsHandler = new Handler();
    	gpsTimer = new Timer();	    	
    	gpsTimer.schedule(new TimerTask() { 
               public void run() { 
                       gpsHandler.post(new Runnable() { 
                               public void run() {
                            	 //Для режима push надо сделать проверку работы GPS. Если GPS больше 60 секунд работал, то его отключить
                           		if (usePush)
                           		{
                           			if (!isGpsOn)
                           			{
                           			   lastGpsTime = SystemClock.elapsedRealtime();
                           			}
                           			if(gpsNeedActivate)
                           			{
                           				Log.e("gps","Activate GPS in Timer");
                           				gpsNeedActivate = false;
                           				gpsOn();
                           			}
                           			//Если больше 60 секунд был включен GPS, то отключить его
                           			if ((SystemClock.elapsedRealtime() - lastGpsTime) > 120000)
                           			{
                           			   isGpsOn = false;    			
                           			   gpsOff();
                           			   //Отправляем последние известные координаты			   
                           			   if ((currentLat != 0) && (currentLon != 0))
                           			   {
                           			    	new Thread(new Runnable() {
                           						   public void run() {				   				                				   
                           											   SimpleTrackingSender httpSender = new SimpleTrackingSender();
                           								               httpSender.sendPosition(
                           								            		   					imei, 
                           								            		   					currentLat, 
                           								            		   					currentLon, 
                           								            		   					currentAlt, 
                           								            		   					currentSpeed, 
                           								            		   					currentTime
                           								            		   				  );
                           								               httpSender = null;
                           								               Log.e ("gps","Send old coordinates");
                           						   }
                           					   }).start();	 				   

                           			   }
                           			}    
                           		}
                           		else//Для режима без Push
                           		{
	                            	 //Если GPS не активен
	                       			if (!isGpsOn)
	                       			{
	                       				//Если таймер больше чем время отключения GPS, то надо включить
	                       				if ((SystemClock.elapsedRealtime() - lastGpsTime) > (gpsOnTimeValue*1000))
	                       				{	                       					
	                       					lastGpsTime = SystemClock.elapsedRealtime();
	                       					gpsOn();			
	                       					Log.e("gps", "Timer GPS on");			
	                       				}
	                       			}	
	                       			//Если GPS активен
	                       			else		
	                       				{
	                       					//Если таймер больше чем время отключения GPS, то надо включить
	                       					if ((SystemClock.elapsedRealtime() - lastGpsTime) > (gpsOffTimeValue*1000))
	                       					{	                       						
	                       						gpsOff();
	                       						lastGpsTime = SystemClock.elapsedRealtime();
	                       						Log.e("gps", "Timer GPS off");
	                       					}
	                       				}				                            	   
	                           		}  
                            	   
                            	   
                               } 
                       }); 
               } 
       }, 1000,1000);*/ 		
	}
	//Активировать GPS
	public static void gpsOn()
	{		
		try
		{
			locManager.requestLocationUpdates( LocationManager.GPS_PROVIDER, 0, 0, stListener);
			isGpsOn = true;
			Log.e ("gps","GPS On");
		}
		catch (Exception e)
		{			
			Log.e ("gps","GPS On ERROR");
		}
		
	}
	//Деактивировать GPS
	public static void gpsOff()
	{		
		try
		{
			locManager.removeUpdates(stListener);
			isGpsOn = false;
			Log.e ("gps","GPS Off");
		}
		catch (Exception e)
		{			
			Log.e ("gps","GPS Off ERROR");
		}
	}
	
  //Получить IMEI устройства
    public String getImei()
    {    	
    	TelephonyManager telephonyManager = (TelephonyManager)getSystemService(TELEPHONY_SERVICE);
    	String result = ""; 
    	result = telephonyManager.getDeviceId();    	
    	//Если не возможно получить IMEI, то получить AndroidID
    	if (result == null)
    	{
    		result = Secure.getString(getContentResolver(), Secure.ANDROID_ID);
    	}    	
    	//Если не возможно получить AndroidID, то использовать WiFi 
    	if (result == null)
    	{
    		WifiManager wifiManager = (WifiManager)getSystemService(Context.WIFI_SERVICE);
    		result = wifiManager.getConnectionInfo().getMacAddress();
    	}
    	return result; 
    }
    //Чтение настроек
    public void readPrefs()
    {
    	SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
    	serverName = preferences.getString("serverName", "server.simpletracking.org:460");
    	useSsl = preferences.getBoolean("useSsl", false);
        gpsOff = preferences.getBoolean("gpsOff", false);        
        gpsOnTimeValue = Integer.parseInt(preferences.getString("gpsOnTime", "60"));
        gpsOffTimeValue = Integer.parseInt(preferences.getString("gpsOffTime", "60"));
        sendTimeValue = Integer.parseInt(preferences.getString("sendTime", "20"));
        sendAccuracy = Integer.parseInt(preferences.getString("sendAccuracy", "30"));
        usePush = preferences.getBoolean("usePush", false);
    }
	
    public static void sendPushId(final String pushId)
    {
    	new Thread(new Runnable() {
			   public void run() {				   
	                SimpleTrackingSender httpSender = new SimpleTrackingSender();
	                httpSender.sendPushId(imei, pushId);
	                httpSender = null;
	                Log.e ("gps","Send PushID: " + pushId);
			   }
		   }).start();	   
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e("gps", "Received start id " + startId + ": " + intent);
        return START_STICKY;
    }
    /*@Override
	public void onLowMemory()
    {
    	super.onLowMemory();
    	Log.e ("gps","Service onLowMemory");
    	closeAll();    	
    }*/
    
	@Override
	public void onDestroy() {
		super.onDestroy();
		
		Log.e ("gps","Service onDestroy");
		closeAll();				
	}
	public void closeAll()
	{
		try {
			Log.e ("gps","Try cancel alarm");
			pendingIntent.cancel();
			alarmManager.cancel(pendingIntent);
		}
		catch (Exception e)
		{			
			Log.e ("gps","Error cancel alarm");
		}
		alarmManager = null;
		pendingIntent = null;
		
		locManager.removeUpdates(stListener);
		locManager = null;
		stListener = null;
		
		Log.e("gps", "Service destroy");
	}

	static SimpleTrackingLocationListener stListener;
	static LocationManager locManager;	
	
	//private Handler gpsHandler;
	//private Timer gpsTimer;
	
	//Перенесено из Activity
	static String imei = "";    
    static String serverName = "";
    //Использовать SSL
    static boolean useSsl = true;
    //Отключать GPS
    static boolean gpsOff = false;
    //Через сколько отправлять данные
    static int sendTimeValue = 20;
    //С какой точностью отправлять данные
    static int sendAccuracy = 30;

    //Через сколько включить GPS
    static int gpsOnTimeValue = 60;
    //Через сколько выключить GPS
    static int gpsOffTimeValue = 60;
    //Использовать PUSH для запросов
    static boolean usePush= false;
    
    static String pushId = "";
         
    static int cntPackets = 0;
    static int cntErrorPackets = 0;
    static double currentLat = 0;
    static double currentLon = 0;
    static double currentAlt = 0;
    static double currentSpeed = 0;
    static double currentAccuracy = 0;
    static long currentTime = 0;
    
    static String lastTime = "";
                  
    public static boolean isGpsOn = true; 
    public static long lastGpsTime = 0;
    public static boolean gpsNeedActivate = false;
    AlarmManager alarmManager;
    PendingIntent pendingIntent;
    static Handler controlHandler;
    final static int SEND_PUSH_GPSON = 1001;
    final static int SEND_PUSH_GPSOFF = 1002;
    final static int SEND_GPSON = 1003;
    final static int SEND_GPSOFF = 1004;
}
