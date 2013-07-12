package team1.fb.textile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

import java.util.Iterator;

import team1.fb.textile.Gridtiles.mozaic.tiles;

import android.app.ActionBar;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.*;
import com.facebook.model.*;
import com.facebook.widget.ProfilePictureView;

public class Gridtiles extends Activity {

	private Button buttonLoginLogout;
	Session session;
	Bitmap mIcon1 = null;
	protected mozaic moze;
	Bitmap publishing;
	
	public ProfilePictureView profilePic;
	ArrayList<String> userIds = new ArrayList<String>();
	//ArrayList<Bitmap> userPictures = new ArrayList<Bitmap>();
    //private Session.StatusCallback statusCallback = new SessionStatusCallback();
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_gridtiles);
		
		ActionBar actionbar = getActionBar();
		actionbar.hide();
		
		moze = new mozaic();
		
		//Settings.addLoggingBehavior(LoggingBehavior.INCLUDE_ACCESS_TOKENS);
		
		//profilePic = (ProfilePictureView) findViewById(R.id.profilePic);
		//profilePic = new ProfilePictureView(getApplicationContext());
		//profilePic.setEnabled(false);

		Log.i("testing", "booting");
		
		if(session == null) {
	    session = Session.openActiveSession(this, true, new Session.StatusCallback() {

		// callback when session changes state
		    @Override
		    public void call(Session session, SessionState state, Exception exception) {
				Log.i("testing", "is session open?");
		    	if (session.isOpened()) {
					Log.i("testing", "session is opened");
		    		Toast.makeText(getApplicationContext(), "Logged in", Toast.LENGTH_SHORT).show();
		    	}
			    else Log.i("testing", "not logged in :(");
		    	// make request to the /me API
		    	/*Request.executeMeRequestAsync(session, new Request.GraphUserCallback() {
		    	  // callback after Graph API response with user object
		    	  @Override
		    	  public void onCompleted(GraphUser user, Response response) {
		    		  Toast.makeText(getApplicationContext(), "Logged in?", Toast.LENGTH_SHORT).show();
		    	  }
		    	});*/
		    }
		});
		Log.i("testing", "booting2");
	    
	    Request.executeMeRequestAsync(session, new Request.GraphUserCallback() {

	        @Override
	        public void onCompleted(GraphUser user, Response response) {
	            if (user != null) {
	            	TextView welcome = (TextView) findViewById(R.id.tvWelcome);
	            	welcome.setText("Welcome, "+user.getFirstName());
	            }
	        }
	    });
		
	    Request.executeMyFriendsRequestAsync(session, new Request.GraphUserListCallback(){
            @Override 
            public void onCompleted(List<GraphUser> users, Response response){
            	if (users != null)
            	{
	            	Log.i("testing", "running myfriendsrequestasync");
	            	TextView textView = (TextView) findViewById(R.id.textView);
	                textView.setText("Number of friends: " + users.size());
	    			Log.i("testing", "running blah3");
	                String id = new String();
	                int i = 0;
	    			Log.i("testing", "go download");
	                new DownloadFilesTask().execute(users);
	    			Log.i("testing", "running blah99");
            	}
            	else
            		Log.i("testing","unable to retreive users!");
            }
		});
		}
	   // Log.i("Amount of ids", Integer.toString(userIds.size()));
	    //profilePic.setProfileId("richard.korn5");
	    //Request.executeGraphPathRequestAsync(session, graphPath, callback)
	    
		
		Log.i("testing", "done booting, phew");
		/*moze.loadpic(BitmapFactory.decodeResource(getResources(), R.drawable.pic1));
		moze.loadpic(BitmapFactory.decodeResource(getResources(), R.drawable.pic2));
		moze.loadpic(BitmapFactory.decodeResource(getResources(), R.drawable.pic3));
		moze.loadpic(BitmapFactory.decodeResource(getResources(), R.drawable.glowfish));*/
		
	}
	
	public void initialLogin (View v) {

	}
	
	public void publishMozaic (View v) {
		final String[] PERMISSION_ARRAY_WRITE = {"publish_stream"};
		final List<String> permissionListWrite = Arrays.asList(PERMISSION_ARRAY_WRITE);
		
		if(!session.getPermissions().containsAll(permissionListWrite)) {
			session.requestNewPublishPermissions(new Session.NewPermissionsRequest(this, permissionListWrite));
		    Log.i("Publish Permission: ","Requested");
		}
		
		if(session.getPermissions().containsAll(permissionListWrite)) {
		//Bitmap image = BitmapFactory.decodeResource(this.getResources(), R.drawable.pic1);
		 Request request = Request.newUploadPhotoRequest(Session.getActiveSession(), publishing, new Request.Callback() {
			@Override
			public void onCompleted(Response response) {
				//
				if (response.getError() == null){
					Toast.makeText(getApplicationContext(), "Posted!", Toast.LENGTH_SHORT).show();
					Log.i("fb", "Photo Posted");
				}
				else
				{
					Toast.makeText(getApplicationContext(), "An error occured while publishing :(", Toast.LENGTH_SHORT).show();
					Log.e("Posting Error:", response.getError().getErrorMessage());
				}
			}
		});
		 request.executeAsync();
		}
	}
	
	public void saveMozaic(View v){
		//Bitmap image = BitmapFactory.decodeResource(this.getResources(), R.drawable.pic1);
		String description = "Mosaic created by the app Textile.";
		//MediaStore.Images.Media.insertImage(getContentResolver(), publishing, "MosaicOfFriends" , description);
		File picDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
								+ "/Mosaic/");
		picDir.mkdirs();
		
		Calendar c = Calendar.getInstance();
		//System.out.println("Current time => " + c.getTime());
		SimpleDateFormat df = new SimpleDateFormat("MMMddyy");
		String date = df.format(c.getTime());
		//String date = new SimpleDateFormat("mmd").format(new Date());
		
		
		String fname = "Mosaic-"+ date +".jpg";
		File file = new File (picDir, fname);
		
		int i = 1;
		while (file.exists ()){
			file = new File (picDir, "Mosaic-"+ date + "(" + i + ").jpg");
			i++;
		}
		//if (file.exists ()) file.delete ();
		try {
		       FileOutputStream out = new FileOutputStream(file);
		       publishing.compress(Bitmap.CompressFormat.JPEG, 90, out);
		       out.flush();
		       out.close();
		       addImageGallery(file); // Notifies database that an image has been added to the gallery directory
		       Toast.makeText(getApplicationContext(), "Image saved to " 
		    		   + Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
		    		   + "/Mosaic/",
					     Toast.LENGTH_SHORT).show();
		       
		} catch (Exception e) {
		       e.printStackTrace();
		}
	}
	
	private void addImageGallery( File file ) {
	    ContentValues values = new ContentValues();
	    values.put(MediaStore.Images.Media.DATA, file.getAbsolutePath());
	    values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
	    getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
	}
	
	protected class mozaic {

		protected class tiles {
			Bitmap profpic;
			String name;
			tiles(Bitmap x)
			{
				profpic = x;
				name = "Mr X";
			}
			// datestamp needed
			// name of friend or id
			// optional tag or separate class for tags
			
		}
		
		ArrayList<tiles> grid;
		int [][] smiley;
		int gridi;
		int dx, dy;
		int facewidth, faceheight;
		int SCREENx, SCREENy;
		float scale;
		int [] galpicid;
		int galpicidn;
		int touchidx;
		boolean drawingmode;

		mozaic()
		{   int MAXdx = 255;
			int MINdx = 0;
			galpicidn = 0;
			touchidx = -1;
			gridi = 0;
			drawingmode = false;
			grid = new ArrayList<tiles>();
			smiley = null;
			scale = getBaseContext().getResources().getDisplayMetrics().density;
			float tilesize = 35.0f;
			dx = Math.round(tilesize * scale);
			dy = Math.round(tilesize * scale);
			Log.i("initMoz", "making mozaic");
			Log.i("tilesize", Float.toString(tilesize));
			Log.i("dx/dy", Integer.toString(dx)+"/"+Integer.toString(dy));
			// MAX checks for renderstamps to fit
			if (dx > MAXdx) {  dx = dy = MAXdx;	}
			else
				if (dx < MINdx) { dx = dy = MINdx;}
			
			facewidth = 0;
			faceheight = 0;
			Display display = getWindowManager().getDefaultDisplay();
			Point size = new Point();
			display.getSize(size);
			SCREENx = size.x;
			SCREENy = size.y;
			galpicid = null;

		}

		private int getValidId(int idoff)
		{
			int id = 200+idoff;
			// Returns a valid id that isn't in use
			View v = findViewById(id);  
			while (v != null){  
				v = findViewById(++id);  
			}	
			return id;
		} 
		
		public void gallery()
		{  
			gallery(!drawingmode);
		}
		
		public void gallery(boolean on)
		{   LinearLayout gal = (LinearLayout) findViewById(R.id.gallery);
			Button btn = (Button) findViewById(R.id.painteron);
			if (on)
			{  touchidx = 1;
				btn.setText("Erase Mode");
				gal.setVisibility(View.VISIBLE);
				drawingmode = true;
			}
			else
			{	
				btn.setText("Draw Mode");
				touchidx = -1;
				gal.setVisibility(View.VISIBLE);
				drawingmode = false;
			}
		}
		
		public void gallery(int bx, int by)
		{
			LinearLayout gal = (LinearLayout) findViewById(R.id.gallery);
			Iterator<tiles> it = grid.iterator();
			ImageView tile;
			tiles friend;
			int id;
			if (galpicid != null)
			{
				for (int i = 0; i < galpicidn; i++)
				{	tile = (ImageView) findViewById(galpicid[i]);
					if (tile != null)
					  gal.removeView(tile);
				}
				galpicid = null;
				galpicidn = 0;
			}
			galpicid = new int[grid.size()+1];
			LinearLayout.LayoutParams lparams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
			lparams.setMargins(2,2,2,2);
			// make empty box
			
			// make chosen palette
			tile = new ImageView(getApplicationContext());
			galpicid[galpicidn++] = id = getValidId(galpicidn);
			tile.setId(id);
			tile.setLayoutParams(lparams);
			tile.setImageBitmap(blackbox());
				// maybe make it look different
			gal.addView(tile);
//			Log.i("gallery", Integer.toString(grid.size()));
			palettelistener(tile);
			while (it.hasNext())
			{
				friend = it.next();
				tile = new ImageView(getApplicationContext());
				tile.setImageBitmap(friend.profpic);
//				Log.i("gallery", "getta id");
				galpicid[galpicidn++] = id = getValidId(galpicidn);
//				if 
//				Log.i("gallery", "seta id");
				tile.setId(id);
				tile.setLayoutParams(lparams);
				gal.addView(tile);
				palettelistener(tile);
			}
			
		}
		
// add listener to each profile pic thumbnail
private void palettelistener(ImageView tile)
{
		tile.setOnTouchListener(new OnTouchListener() {
			  @Override
			  public boolean onTouch(View v, MotionEvent event)
			  {
				  ImageView pane = (ImageView) v;
				  int i = pane.getId();
				  touchidx = -1;
				  for (int j = 0; j < galpicid.length; j++)
					  if (galpicid[j] == i)
					  {
						 touchidx = j;
						 ImageView image = (ImageView) findViewById(R.id.ppal);
						 if (j > 0 & j <= grid.size())
							 image.setImageBitmap((grid.get(j-1)).profpic);
						 else
							 image.setImageBitmap(blackbox());
					  }
				  
				  Button btn = (Button) findViewById(R.id.painteron);
				  btn.setText("Erase Mode");
				  return true;
			  }
			});
}
		
		
		public void loadpic(Bitmap x)
		{
			// pass jpg not bitmap get it into a bitmap shrink also.
			tiles thumb = new tiles(Bitmap.createScaledBitmap(x,  dx,  dy,  true));
			grid.add(thumb);
			gridi++;
			
		}
		
		public void settile(int x, int y)
		{  Log.i("settile", "set to "+Integer.toString(touchidx));
		Log.i("settile","uh"+Integer.toString(facewidth));
			if (x >= 0 & x < facewidth & y >= 0 & y < faceheight)
			{
				ImageView image = (ImageView) findViewById(R.id.imageView1);
				Bitmap bigpic = ((BitmapDrawable)image.getDrawable()).getBitmap();
				Canvas panorama = new Canvas(bigpic);

				if (touchidx > 0 & touchidx <= grid.size())
				{	smiley[x][y] = touchidx;
					drawtile(panorama, x, y);
					image.setImageBitmap(bigpic);
				}
				else
				{	Paint clearit = new Paint();
					clearit.setXfermode(new PorterDuffXfermode(Mode.CLEAR));
					panorama.drawRect(x*dx, y*dy, (x+1)*dx, (y+1)*dy, clearit);
					image.setImageBitmap(bigpic);
					smiley[x][y] = 0;
				}
			}
		}
		
		private Bitmap blackbox()
		{
			Paint myPaint = new Paint(); 
			myPaint.setColor(Color.BLACK);
			Bitmap bbox = Bitmap.createBitmap(dx,dy,Bitmap.Config.ARGB_8888);
			Canvas splat = new Canvas(bbox);
			splat.drawRect(0, 0,dx,dy,myPaint);
			return bbox;
		}

		public void makeblock()
		{
			if (facewidth > 0 & faceheight > 0)
				makeblock(facewidth,faceheight);
		}
		
		public void makeblock(int width, int height)
		{  
			 int count = 1;
			 
			smiley = new int [width][height];
			facewidth = width; faceheight = height;
			for (int y = 0; y < width; y++)
			{  
				for (int x = 0; x < height; x++)
				{  
					smiley[x][y] = count++;
					if (count > galpicidn)
						count = 0;
				}
			}
		}
		
		public void makesmiley()
		{
			if (facewidth > 0 & faceheight > 0)
				makesmiley(facewidth,faceheight);
		}
		
		public void makesmiley(int width, int height)
		{  int rx = -1*width/2;
		   int ry = -1*height/2;
		   int roll = 1;
			 double r = (width*width/4.0)*0.8;
	//		 double reye = r/8;
			 double rth;
			 int count = 1;
			 Random radical = new Random();
			 
			smiley = new int [width][height];
			facewidth = width; faceheight = height;
			for (int y = 0; y < width; y++, ry++)
			{   rx = -1*width/2;
				for (int x = 0; x < height; x++, rx++)
				{  
					//roll = radical.nextInt(800);
				//	smiley[x][y] = radical.nextInt(435);		// for all
//					smiley[x][y] = count++;
					if (count > galpicidn)
						count = 1;
					// circle
					rth = rx*rx+ry*ry;
					if (rth < r)
					{  // test left eye
						int dx = x-(4*width/10);
						int dy = y-(height/3);
					   rth = dx*dx+dy*dy;
					   if (rth > r/16)
					   {  // right eye
						   dx = x-(7*width/10);
						   rth = dx*dx+dy*dy;
						   if (rth > r/16)
						   {  rth = (rx*rx+ry*ry) - (4*r/7);
						      rth = rth * rth;
						   	  if (rth > r*r/128 | ry < 0)
							     smiley[x][y] = count++;
						   }
					   }
					}
					else
						smiley[x][y] = 0;
				}
			}
		}
		
		private void drawtile(Canvas portr, int x, int y)
		{
			Rect src, dest;
			int drawidx = smiley[x][y] - 1;
			if (drawidx >= 0 & drawidx < grid.size() )
			{
				tiles t = grid.get(drawidx);
				src = new Rect(0,0,dx,dy);
				dest = new Rect(src);
				dest.offset(x*dx, y*dy);
				portr.drawBitmap(t.profpic,  src,  dest, null);
			}
			else
				if (drawidx == 0)
				{
					// clear the block
				}
		}
		public Bitmap redraw()
		{
			if (facewidth > 0 & faceheight > 0)
			{
				Log.i("testing", "drawer");

				Iterator<tiles> it = grid.iterator();
				Bitmap bigpic = Bitmap.createBitmap(facewidth*dx, faceheight*dy, Bitmap.Config.ARGB_8888);
				Canvas panorama = new Canvas(bigpic);
				Rect src, dest;
				Log.i("testing", "starting drawing loop");
				for (int x = 0; x < facewidth; x++)
					for (int y = 0; y < faceheight; y++)
					{   
						if (smiley[x][y] != 0)
						{		

							if (!it.hasNext())
									it = grid.iterator();
							if (it.hasNext())
							{

								tiles t = it.next();
								src = new Rect(0,0,dx,dy);
								dest = new Rect(src);
								dest.offset(x*dx, y*dy);
								panorama.drawBitmap(t.profpic,  src,  dest, null);
							}
						}
					}
				Log.i("testing", "result returns");
				publishing = bigpic;
				return bigpic;
			}
			else
				return null;
		}
		
		public Bitmap makemozaic(int width, int height)
		{	
			makeblock(width, height);		// important
			facewidth = width;
			faceheight = height;
			return redraw();
		}
	
	private void renderstamp(Bitmap curtain, int tilex, int tiley, int width, int height)
	{
		int pixel1 = Color.rgb(3, 17, 5);
		int pixel2 = Color.rgb(tiley, 13, 23);
		int pixelp = Color.rgb(width, height, tilex);
		curtain.setPixel(0,0,pixelp);
		curtain.setPixel(1,0,pixel1);
		curtain.setPixel(2,0,pixel2);
	}

	private boolean loadstamp(Bitmap curtain)
	{
		int pixelp = curtain.getPixel(0, 0);
		int pixel1 = curtain.getPixel(1, 0);
		int pixel2 = curtain.getPixel(2, 0);
		if (Color.red(pixel1) == 3 && Color.green(pixel1) == 17 && Color.blue(pixel1) == 5 
				&& Color.green(pixel2) == 13 && Color.blue(pixel2) == 23)
		{
			dx = Color.blue(pixelp);
			dy = Color.red(pixel2);
			facewidth = Color.red(pixelp);
			faceheight = Color.green(pixelp);
			return true;
		}
		else
			return false;
		
	}
// *** end mozaic class	
  }
	private class DownloadFilesTask extends AsyncTask<List<GraphUser>, Integer, Void> {
	     protected Void doInBackground(List<GraphUser>... users) {
	    	 String imageURL;
	 	    Bitmap bitmap = null;
	 	    Log.i("TAG", "Loading Picture");
			Log.i("testing", "loading pictures");
	 	    
	 	    int i = 0;
	 	    for (GraphUser user : users[0]){           	
		 	    imageURL = "http://graph.facebook.com/"+user.getId()+"/picture?type=small";
		 	    try {
		 	        bitmap = BitmapFactory.decodeStream((InputStream)new URL(imageURL).getContent());
		 	    } catch (Exception e) {
		 	        Log.i("TAG", "Loading Picture FAILED");
		 	        e.printStackTrace();
		 	    }
		 	    moze.loadpic(bitmap);
		 	    if (i == 50){break;} //uncomment to cap at i
		 	    i++;
	 	    }
	 	    return null;
	     }

	     protected void onProgressUpdate(Integer... progress) {
				Log.i("testing", "progress..");

	     }
	     
	     protected void onPostExecute(Void v) {
	    	ImageView pane = (ImageView) findViewById(R.id.imageView1);
			Log.i("testing", "time to draw mozaic");
	    	//moze.loadpic(result);
    		moze.gallery(0,0);
    		pane.setImageBitmap(moze.makemozaic(10,10));
    		pane.setOnTouchListener(new OnTouchListener() {
    			  @Override
    			  public boolean onTouch(View v, MotionEvent event)
    			  {
    				  ImageView pane = (ImageView) findViewById(R.id.imageView1);
    				  int touchX = (int) event.getX();
    				  int touchY = (int) event.getY();
    				  int x = (int) Math.round((moze.scale * touchX - moze.dx) / (moze.dx *2 ));
    				  int y = (int) Math.round((moze.scale * touchY - moze.dy) / (moze.dy *2));
    				  Log.i("touched", Integer.toString(x) + ", " + Integer.toString(y));
    				  //moze.gallery(true);
    				  moze.settile(x,y);
    				  return true;
    			  }
    			});
    	  		Button drawon = (Button) findViewById(R.id.painteron);
    	 		drawon.setOnClickListener(new OnClickListener() {
    				@Override
    				public void onClick(View v)
    				{	
    					moze.gallery();
    				}
    			});

     	   // pane.setImageBitmap(result);
	     }
	 }
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.gridtiles, menu);
		return true;
	}	

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
	  super.onActivityResult(requestCode, resultCode, data);
	  Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);
	}
/*	
	@Override
    public void onStart() {
        super.onStart();
        Session.getActiveSession().addCallback(statusCallback);
    }

    @Override
    public void onStop() {
        super.onStop();
        Session.getActiveSession().removeCallback(statusCallback);
    }
	
	private void updateView() {
        Session session = Session.getActiveSession();
        if (session.isOpened()) {
            buttonLoginLogout.setText("Logout");
            buttonLoginLogout.setOnClickListener(new OnClickListener() {
                public void onClick(View view) { onClickLogout(); }
            });
        } else {
            buttonLoginLogout.setText("Login");
            buttonLoginLogout.setOnClickListener(new OnClickListener() {
                public void onClick(View view) { onClickLogin(); }
            });
        }
    }
	private void onClickLogin() {
        Session session = Session.getActiveSession();
        if (!session.isOpened() && !session.isClosed()) {
            session.openForRead(new Session.OpenRequest(this).setCallback(statusCallback));
        } else {
            Session.openActiveSession(this, true, statusCallback);
        }
    }

    private void onClickLogout() {
        Session session = Session.getActiveSession();
        if (!session.isClosed()) {
            session.closeAndClearTokenInformation();
        }
    }
    
	private class SessionStatusCallback implements Session.StatusCallback {
        @Override
        public void call(Session session, SessionState state, Exception exception) {
            updateView();
        }
    }*/
	
}
