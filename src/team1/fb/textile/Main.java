package team1.fb.textile;

import java.util.ArrayList;
import java.util.Calendar;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;


public class Main extends Activity {
	private String albumname;
	 private void listalbums(Spinner m)
	    {
	    	ArrayList<String> albums = new ArrayList<String>();
	    	
	// need to add name for each album a user has 
	    	albums.add("x");
	    	ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item,albums);
	    	adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	        m.setAdapter(adapter);
	    }
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		
	    super.onCreate(savedInstanceState);
	    
	    alarm = new TimeReciver();
	    setContentView(R.layout.profile);
	    
	    ActionBar actionbar = getActionBar();
		actionbar.hide();
	    
	    Button change=(Button) findViewById(R.id.setButton);
	    
	    change.setOnClickListener( new OnClickListener() {
			public void onClick(View v) {
	    		
	    		  RadioGroup x= (RadioGroup) findViewById(R.id.select);
	    		  RadioButton c =(RadioButton)findViewById(x.getCheckedRadioButtonId());
	    		
	    		//  Toast.makeText(getApplicationContext(), c.getText().toString(), Toast.LENGTH_LONG).show();
	    		  		if (c.getText().toString().equals("Daily"))
	    		  		{
	    		  			Daily();
	    		  		
	    		  		}else{
	    		  		if (c.getText().toString().equals("Weekly"))
	    		  		{
	    		  			Weekly();
	    		  		}else{
	    		  		if (c.getText().toString().equals("Never"))
	    		  		{end();
	    		  		}else
	    		  		{end();}
	    		  		}}
	    	}});
	    
	   
	}
	


	private TimeReciver alarm;

	private boolean week=false;

	private boolean day=false;
 public void startTimer(Boolean x, Boolean y, String s, Integer d) {
    Context context = this.getApplicationContext();
    if(alarm != null){
    alarm.CancelAlarm(context);
    
   alarm.SetAlarm(context,x,y,s,d);
    }else{
    	alarm = new TimeReciver();
  	alarm.SetAlarm(context,x,y,s,d);

    }
   }
   
   public void cancel(){
    Context context = this.getApplicationContext();
    if(alarm != null){
   alarm.CancelAlarm(context);
    }
   }
   

 void Daily()
 {
	 if (week)
		 end();
	 day=true;
	 week =false;
	 
startTimer(true,false,null,9);
 }
 void Weekly()
 {
	 if (day)
	 {end();}
	 day=false;
	 week =true;
	 startTimer(false,true,null,Calendar.getInstance().get(Calendar.DAY_OF_WEEK));}
 void end()
 {day=false;
 week =false;
 cancel();
 }





	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		
		getMenuInflater().inflate(R.menu.gridtiles, menu);
		
		return true;
	}
	

}
