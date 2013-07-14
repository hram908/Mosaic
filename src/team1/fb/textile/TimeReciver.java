package team1.fb.textile;



import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

public class TimeReciver extends BroadcastReceiver {

final public static String ONE_TIME = "onetime";static int x=0;
 public boolean day= false;  public boolean week =false; public int Dow=0;
 public String albumname=null;
@Override
public void onReceive(Context context, Intent intent) {

  
         Bundle extras = intent.getExtras().getBundle("buns");
         StringBuilder msgStr = new StringBuilder();
         
         if(extras == null){
         msgStr.append("One time Timer : ");
         }
         Format formatter = new SimpleDateFormat("hh:mm:ss a");
         msgStr.append(formatter.format(new Date()));
       if(extras!=null)
         {day=extras.getBoolean("day",false);
         week=extras.getBoolean("week",false);
         albumname=extras.getString("name", null);
         Dow=extras.getInt("dow",9);
        }
        if (day)
        {
        	 
        	change_pic(context); 
         }
         if (week && Dow == Calendar.getInstance().get(Calendar.DAY_OF_WEEK))
         {
        	 
         change_pic(context);       //Toast.makeText(context, "same day", Toast.LENGTH_LONG).show();
         }
    //  Toast.makeText(context, ""+day+week, Toast.LENGTH_LONG).show();
         
 
}
public void SetAlarm(Context context, Boolean Day, Boolean Week, String name,Integer dow)
    {

        AlarmManager alarm=(AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent intents = new Intent(context,TimeReciver.class);
     // intent.setFlags(Intent.FLAG_RECEIVER_REPLACE_PENDING) ;       
        Bundle bun=new Bundle();
        bun.putBoolean(ONE_TIME, Boolean.TRUE);
       bun.putBoolean("day",Day);
       bun.putBoolean("week", Week);
       bun.putString("name", name);
       bun.putInt("dow", dow);
     intents.putExtra("buns", bun);
     PendingIntent  pi = PendingIntent.getBroadcast(context, 0, intents, PendingIntent.FLAG_UPDATE_CURRENT|Intent.FLAG_RECEIVER_REPLACE_PENDING);
x=0;
        Format formatter = new SimpleDateFormat("hh:mm:ss a");
        StringBuilder msgStr = new StringBuilder();
        msgStr.append(formatter.format(new Date()));
       // Toast.makeText(context, " "+Day+Week, Toast.LENGTH_LONG).show();
        //After after 30 seconds
        alarm.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), (AlarmManager.INTERVAL_DAY) , pi);
    }

    public void CancelAlarm(Context context)
    {
        Intent intent = new Intent(context, TimeReciver.class);
        PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent,PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(sender);
       
}
    public void change_pic(Context context){
    
    	//albumname should be the name album in the spinner;  x should be the number of times run 

		if ( x > 0 ){
	    	NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
	        Notification notification = new Notification(R.drawable.ic_launcher,
	        		"Mosaic Reminder",
	                System.currentTimeMillis());
	        CharSequence contentTitle = "Mosaic Reminder";
	        CharSequence contentText = "Click to create an updated Mosaic";
	        Intent notificationIntent = new Intent(context, Gridtiles.class);
	        PendingIntent contentIntent = PendingIntent.getActivity(
	                context,0,notificationIntent,PendingIntent.FLAG_UPDATE_CURRENT);
	        
	        notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
	        notification.flags = Notification.FLAG_AUTO_CANCEL | Notification.FLAG_ONLY_ALERT_ONCE;
	        nm.notify(1, notification);
			
    		x++;
	    		//change pic
		}
    		
	}
}   
