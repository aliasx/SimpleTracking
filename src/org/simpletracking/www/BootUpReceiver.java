package org.simpletracking.www;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

  
//Класс для автозапуска приложения
public class BootUpReceiver extends BroadcastReceiver{

    @Override
    public void onReceive(Context context, Intent intent) {
            Intent i = new Intent(context, SimpleTrackingActivity.class);  
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i);  
    }

}