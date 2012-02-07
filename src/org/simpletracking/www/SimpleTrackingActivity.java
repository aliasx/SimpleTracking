package org.simpletracking.www;
/* The part of SimpleTracking open source project. Web: http://simpletracking.org */
import com.google.android.c2dm.C2DMessaging;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.telephony.TelephonyManager;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.preference.PreferenceManager;
import android.provider.Settings.Secure;

public class SimpleTrackingActivity extends Activity {
    /** Called when the activity is first created. */
    
	/*@Override    
    //Настройка меню
    public boolean onCreateOptionsMenu(Menu menu) {
    	 menu.add(1,1,0,"Настройки");         
    	 menu.add(1,2,0,"Выход");
         return true;
    }*/
    //Вызов пункта меню настроек
    /*public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
        case 1:
        	startActivity(new Intent(getBaseContext(),SimpleTrackingPrefs.class));
            return true;
        case 2:
        	if (alwaysOn) { wl.release(); }
        	
			C2DMessaging.unregister(this);
        	mNotificationManager.cancelAll();
        	stopService(new Intent(SimpleTrackingActivity.this,SimpleTrackingService.class));
        	finish();
        	//System.runFinalizersOnExit(true);
        	//System.exit(0);
            return true;
            
        default:
            return super.onOptionsItemSelected(item);
        }
    }*/
    
    @Override 
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        readPrefs();
        initPower();
        initTray();

        //Настройка основных кнопок
        Button btExit = (Button) findViewById(R.id.btExit);
        btExit.setOnClickListener(new OnClickListener(){
	    	public void onClick(View arg0) {
		    		if (alwaysOn) { wl.release(); }		        	
					C2DMessaging.unregister(getBaseContext());
		        	mNotificationManager.cancelAll();
		        	stopService(new Intent(SimpleTrackingActivity.this,SimpleTrackingService.class));
		        	finish();
	    		}
	    	});	        
        Button btSettings = (Button) findViewById(R.id.btSettings);
        btSettings.setOnClickListener(new OnClickListener(){
	    	public void onClick(View arg0) {
	    			startActivity(new Intent(getBaseContext(),SimpleTrackingPrefs.class));
	    		}
	    	});	
         
        //Получение и вывод imei
        TextView tvImei = (TextView) findViewById(R.id.tvImei);
        imei = getImei();
        tvImei.setText(imei);
        
        //Вывод версии
        TextView tvVersion = (TextView) findViewById(R.id.tvVersion);
        try 
        {
			tvVersion.setText(getPackageManager().getPackageInfo(getPackageName(), 0).versionName);
		} 
        catch (NameNotFoundException e) 
		{
		}
                                               
        startService(new Intent(SimpleTrackingActivity.this,SimpleTrackingService.class));
    	
        //Свернуть приложение
    	if (runMinimized) { this.moveTaskToBack(true); }
    }
    
    @Override
    public void onStart()
    {
    	super.onStart();
    	TextView tvLat = (TextView) findViewById(R.id.tvLat);
        TextView tvLon = (TextView) findViewById(R.id.tvLon);
    	TextView tvAccuracy = (TextView) findViewById(R.id.tvAccuracy);
    	TextView tvPackets = (TextView) findViewById(R.id.tvPackets);
    	TextView tvErrorPackets = (TextView) findViewById(R.id.tvErrorPackets);
    	TextView tvLastTime = (TextView) findViewById(R.id.tvLastTime);
    			
    	tvLat.setText("" + SimpleTrackingService.currentLat);
    	tvLon.setText("" + SimpleTrackingService.currentLon);
    	tvAccuracy.setText("" + SimpleTrackingService.currentAccuracy);
    	tvPackets.setText("" + SimpleTrackingService.cntPackets);    	  
    	tvErrorPackets.setText("" + SimpleTrackingService.cntErrorPackets);
    	tvLastTime.setText("" + SimpleTrackingService.lastTime);

    }
    
    //Обработка нажатия кнопки Back (вместо выхода - свернуть приложение)
    public boolean onKeyDown(int keyCode, KeyEvent event)  
    {
         if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0)
         {
            this.moveTaskToBack(true);
            return true;
         }
        return super.onKeyDown(keyCode, event);
    }

    //Чтение настроек
    public void readPrefs()
    {
    	SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        alwaysOn = preferences.getBoolean("alwaysOn", false);        
        runMinimized = preferences.getBoolean("runMinimized", false);
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

    //Менеджер питания
    public void initPower()
    {
    	if (alwaysOn)
    	{
	    	pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
	    	wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, "SimpleTracking");
	    	wl.acquire();
    	}
    }
    //Инициализация значка в трее
    public void initTray()
    {
	    String ns = Context.NOTIFICATION_SERVICE;
	    mNotificationManager = (NotificationManager) getSystemService(ns);
	    
	    int icon = R.drawable.icon_bar;        // icon from resources
	    CharSequence tickerText = this.getString(R.string.app_name);              // ticker-text
	    long when = System.currentTimeMillis();         // notification time
	    Context context0 = getApplicationContext();      // application Context
	    CharSequence contentTitle = this.getString(R.string.app_name);  // expanded message title
	    CharSequence contentText = "";      // expanded message text
	
	    Intent notificationIntent = new Intent(this, SimpleTrackingActivity.class);
	    notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
	    PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
	
	    // the next two lines initialize the Notification, using the configurations above
	    notification = new Notification(icon, tickerText, when);
	    notification.setLatestEventInfo(context0, contentTitle, contentText, contentIntent);
	    notification.flags |= Notification.FLAG_NO_CLEAR;
	    
	    mNotificationManager.notify(NOTIFICATION_ID, notification);
    }
    
    static String imei = "";
    //Запускать свернутым
    private static boolean runMinimized= false;
    //Всегда включен
    private static boolean alwaysOn = false;   
         
    private static final int NOTIFICATION_ID = 1;
    public Notification notification;
    private NotificationManager mNotificationManager;
    
    private static PowerManager pm;
    private static PowerManager.WakeLock wl;
    
    static Handler updateHandler;
    static Handler serviceHandler;
}