package team1.fb.textile;
/* Mosaic app v1.2 by Mobile Programming Team 1
 * Authors: Angela Gibbens
 *          Benjamin Roche
 *          Richard Korn
 *          Rick Tilley
 *          
 * Date: 7/14/2013
 * Description:
 * Imports all your facebook friend's profile pictures, then lets you
 * draw a mosaic with their pictures as tiles.  Also has some cookie-cutter
 * options, and will post your custom mosaics to facebook or save
 * them to your phone.  Please be patient while first loading data.
 */
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.util.Pair;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.model.GraphUser;
import com.facebook.widget.ProfilePictureView;

public class Gridtiles extends Activity {

	private Button buttonLoginLogout;
	Session session;
	Bitmap mIcon1 = null;
	protected mozaic moze;
	Bitmap publishing;
	
	public ProfilePictureView profilePic;
	ArrayList<String> userIds = new ArrayList<String>();
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_gridtiles);
		ActionBar actionbar = getActionBar();
		actionbar.hide();
		moze = new mozaic();
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
	            	TextView textView = (TextView) findViewById(R.id.textView);
	                textView.setText("Number of friends: " + users.size());
	                String id = new String();
	                int i = 0;
	                new DownloadFilesTask().execute(users);
            	}
            	else
            		Log.i("testing","unable to retreive users!");
            }
		});
		}
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
		// *** Change maxprofiles for testing or speed
		public static final int MAXPROFILES = 512;
		// *******************************************

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
/*
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
			*/
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
			palettelistener(tile);
			while (it.hasNext())
			{
				friend = it.next();
				tile = new ImageView(getApplicationContext());
				tile.setImageBitmap(friend.profpic);
				galpicid[galpicidn++] = id = getValidId(galpicidn);
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
				  /*
				  Button btn = (Button) findViewById(R.id.painteron);
				  btn.setText("Erase Mode");
*/
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
			drawingmode = true;
			smiley = new int [width][height];
			facewidth = width; faceheight = height;
			for (int y = 0; y < width; y++)
			{  
				for (int x = 0; x < height; x++)
				{  
					smiley[x][y] = count++;
					if (count > galpicidn)
						count = 1;
				}
			}
		}
		
		public void makesmiley()
		{
			if (facewidth > 0 & faceheight > 0)
				makesmiley(facewidth,faceheight);
		}
		
		public void makesmiley(int width, int height)
		{  
		     int roll = 1;
			 double rth;
			 int count = 1;
			 Random radical = new Random();
			 int widthx = width*dx;
			 int heightx = height*dy;
			 int rx = -1*widthx/2;
			 int ry = -1*heightx/2;	 
			 double r = (widthx*widthx/4.0)*0.8;
			 
			ImageView image = (ImageView) findViewById(R.id.imageView1);
			Bitmap bigpic = ((BitmapDrawable)image.getDrawable()).getBitmap();
			Canvas panorama = new Canvas(bigpic);
			Paint clearpaint = new Paint();
			clearpaint.setXfermode(new PorterDuffXfermode(Mode.CLEAR));

			Toast.makeText(getApplicationContext(), "Cookie Cutter Working", Toast.LENGTH_LONG).show();
				
			facewidth = width; faceheight = height;
			for (int y = 0; y < widthx; y++, ry++)
			{   rx = -1*widthx/2;
				for (int x = 0; x < heightx; x++, rx++)
				{  
					//roll = radical.nextInt(800);
				//	smiley[x][y] = radical.nextInt(435);		// for all
					// circle
					rth = rx*rx+ry*ry;
					if (rth < r)
					{  // test left eye
						int zx = x-(3*widthx/10);
						int zy = y-(heightx/3);
					   rth = zx*zx+zy*zy;
					   if (rth > r/16)
					   {  // right eye
						   zx = x-(7*widthx/10);
						   rth = zx*zx+zy*zy;
						   if (rth > r/16)
						   {  rth = (rx*rx+ry*ry) - (4*r/7);
						      rth = rth * rth;
						   	  if (rth > r*r/128 | ry < 0)
						   		  roll = 1; // dummy call, don't draw on smiley
						   	  else
						   		  panorama.drawPoint(x,y,clearpaint);
						   }
						   else
							   panorama.drawPoint(x,y,clearpaint);
					   }
					   else
						   panorama.drawPoint(x, y, clearpaint);
					}
					else
						panorama.drawPoint(x, y, clearpaint);
				}
			}
			image.setImageBitmap(bigpic);
			Log.i("cookie", "smiley has been cut");

		}

		public void maketriangle(int width, int height)
		{  
			boolean inside = false;
			 int widthx = width*dx;
			 int heightx = height*dy;
			 int rx = -1*widthx/2;
			 int ry = -1*heightx/2;	 
			 double m[] = {0.0,0.0,0.0};
			 double b[] = {0.0,0.0,0.0};
			 
			 // triangle slopes of 3 sides
			 m[0] = -1 * (heightx/2.0) / (widthx/4.0);
			 m[1] = -m[0];	m[2] = 0;
			 b[0] = b[1] = -1*heightx/2.0;
			 b[2] = -b[0];
			 
			 Log.i("dims", Double.toString(m[0])+"+"+Double.toString(b[0]));
			 Log.i("dims", Double.toString(m[1])+"+"+Double.toString(b[1]));
			 Log.i("dims", Double.toString(m[2])+"+"+Double.toString(b[2]));
			 
			ImageView image = (ImageView) findViewById(R.id.imageView1);
			Bitmap bigpic = ((BitmapDrawable)image.getDrawable()).getBitmap();
			Canvas panorama = new Canvas(bigpic);
			Paint clearpaint = new Paint();
			clearpaint.setXfermode(new PorterDuffXfermode(Mode.CLEAR));

			Toast.makeText(getApplicationContext(), "Cookie Cutter Working", Toast.LENGTH_LONG).show();
				
			facewidth = width; faceheight = height;
			for (int y = 0; y < widthx; y++, ry++)
			{   rx = -1*widthx/2;
				for (int x = 0; x < heightx; x++, rx++)
				{  
					if ((ry > (m[0]*rx+b[0]))
					  & (ry > (m[1]*rx+b[1]))
					  & (ry < b[2]))
						inside = true;
					
					if (!inside)
						panorama.drawPoint(x,y,clearpaint);
					else
						inside = false;
				}
			}
			image.setImageBitmap(bigpic);
			Log.i("cookie", "triangle has been cut");

		}
	
		class dumbPair
		{
			public	double first,second;
			dumbPair()
			{
				
			}
		}

		protected dumbPair toPolar(dumbPair cart, dumbPair p)
		{
			p.first = Math.sqrt(cart.first*cart.first+cart.second*cart.second);
			p.second = Math.tan(cart.second/cart.first);
			return p;
		}

		protected dumbPair toCart(dumbPair polar, dumbPair c)
		{
			c.first = polar.first * Math.cos(polar.second);
			c.second = polar.first * Math.sin(polar.second);
			return c;
		}
		
		
/*		
		protected Pair<Double, Double> toPolar(Pair<Double, Double> cart)
		{
			Pair <Double, Double> p;
			double r, th;
			r = Math.sqrt(cart.first*cart.first+cart.second*cart.second);
			th = Math.tan(cart.second/cart.first);
			p = Pair.create(r, th);
			return p;
		}

		protected Pair<Double, Double> toCart(Pair<Double, Double> polar)
		{
			Pair <Double, Double> c;
			double x, y;
			x = polar.first * Math.cos(polar.second);
			y = polar.first * Math.sin(polar.second);
			c = Pair.create(x,y);
			return c;
		}
	*/	
		
		public void makesun(int width, int height)
		{  
			boolean inside = false;
			 int widthx = width*dx;
			 int heightx = height*dy;
			 int rx = -1*widthx/2;
			 int ry = -1*heightx/2;	 
			 dumbPair cart = new dumbPair();
			 dumbPair polar = new dumbPair();
			 double r;
			 double th;
			 double scale = 2.0/widthx;
			 double compval;
			 
			ImageView image = (ImageView) findViewById(R.id.imageView1);
			Bitmap bigpic = ((BitmapDrawable)image.getDrawable()).getBitmap();
			Canvas panorama = new Canvas(bigpic);
			Paint clearpaint = new Paint();
			clearpaint.setXfermode(new PorterDuffXfermode(Mode.CLEAR));

			Toast.makeText(getApplicationContext(), "Cookie Cutter Working", Toast.LENGTH_LONG).show();
				
			facewidth = width; faceheight = height;
			for (int y = 0; y < widthx; y++, ry++)
			{   rx = -1*widthx/2;
				for (int x = 0; x < heightx; x++, rx++)
				{  
					cart.first = (double)rx;  cart.second = (double)ry;
					polar = toPolar(cart, polar);
					r = polar.first * scale; th = polar.second;
					compval = Math.sin(th*10.0);
					if (r < (0.9+(compval/10.0)))
					 inside = true;
					
					if (!inside)
						panorama.drawPoint(x,y,clearpaint);
					else
						inside = false;
				}
			}
			image.setImageBitmap(bigpic);
			Log.i("cookie", "moon has been cut");

		}		

		public void makeflower(int width, int height)
		{  
			boolean inside = false;
			 int widthx = width*dx;
			 int heightx = height*dy;
			 int rx = -1*widthx/2;
			 int ry = -1*heightx/2;	 
			 dumbPair cart = new dumbPair();
			 dumbPair polar = new dumbPair();
			 double r;
			 double th;
			 double scale = 2.0/widthx;
			 double compval;
			 
			ImageView image = (ImageView) findViewById(R.id.imageView1);
			Bitmap bigpic = ((BitmapDrawable)image.getDrawable()).getBitmap();
			Canvas panorama = new Canvas(bigpic);
			Paint clearpaint = new Paint();
			clearpaint.setXfermode(new PorterDuffXfermode(Mode.CLEAR));

			Toast.makeText(getApplicationContext(), "Cookie Cutter Working", Toast.LENGTH_LONG).show();
				
			facewidth = width; faceheight = height;
			for (int y = 0; y < widthx; y++, ry++)
			{   rx = -1*widthx/2;
				for (int x = 0; x < heightx; x++, rx++)
				{  
					cart.first = (double)rx;  cart.second = (double)ry;
					polar = toPolar(cart, polar);
					r = polar.first * scale; th = polar.second;
					compval = Math.sin(th) * Math.cos(th);
					if (r < (0.4+compval))
					 inside = true;
					
					if (!inside)
						panorama.drawPoint(x,y,clearpaint);
					else
						inside = false;
				}
			}
			image.setImageBitmap(bigpic);
			Log.i("cookie", "moon has been cut");

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
				if (drawidx == -1)
				{
					// clear the block
				}
		}
		public Bitmap redraw()
		{
			if (facewidth > 0 & faceheight > 0)
			{
				Iterator<tiles> it = grid.iterator();
				Bitmap bigpic = Bitmap.createBitmap(facewidth*dx, faceheight*dy, Bitmap.Config.ARGB_8888);
				Canvas panorama = new Canvas(bigpic);
				Rect src, dest;
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
				publishing = bigpic;
				return bigpic;
			}
			else
				return null;
		}
		
		public Bitmap makemozaic(int width, int height)
		{	
			if (drawingmode == false | smiley == null)
				makeblock(width, height);		// init blocks first time
			facewidth = width;
			faceheight = height;
			return redraw();
		}
		
		public void cookie(int cidx)
		{  
		  if (facewidth > 0 & faceheight > 0)
		  {
				Log.i("cookie", "which cutter?");
			switch (cidx)
			{
			  case 1: makesmiley(facewidth, faceheight); break;
			  case 2: makesun(facewidth, faceheight); break;
			  case 3: maketriangle(facewidth, faceheight); break;
			  case 4: makeflower(facewidth, faceheight); break;
			  default: break;
			}
		  }
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
		 	    if (i == moze.MAXPROFILES){break;} //uncomment to cap at i
		 	    i++;
	 	    }
	 	    return null;
	     }

	     protected void onProgressUpdate(Integer... progress) {
				Log.i("testing", "progress..");

	     }
	     
	     protected void onPostExecute(Void v) {
	    	ImageView pane = (ImageView) findViewById(R.id.imageView1);
    		moze.gallery(0,0);
    		pane.setImageBitmap(moze.makemozaic(10,10));
    		pane.setOnTouchListener(new OnTouchListener() {
    			  @Override
    			  public boolean onTouch(View v, MotionEvent event)
    			  {
    				  ImageView pane = (ImageView) findViewById(R.id.imageView1);
    				  int touchX = (int) event.getX();
    				  int touchY = (int) event.getY();
    				  Log.i("scale", Float.toString(moze.scale));
    				  int x = (int) Math.round((moze.scale * touchX - moze.dx) / (moze.dx * moze.scale ));
    				  int y = (int) Math.round((moze.scale * touchY - moze.dy) / (moze.dy * moze.scale));
    				  Log.i("touched", Integer.toString(x) + ", " + Integer.toString(y));
    				  moze.settile(x,y);
    				  return true;
    			  }
    			});
	  		Spinner smileit = (Spinner) findViewById(R.id.cookiecutter);
	 		smileit.setOnItemSelectedListener(new OnItemSelectedListener() {
	 		    @Override
	 		    public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
	 		        moze.cookie(position);
	 		    }

	 		    @Override
	 		    public void onNothingSelected(AdapterView<?> parentView) {
	 		        // your code here
	 		    }

	 		});
     	   // pane.setImageBitmap(result);
	     }
	 }
	
	/*
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.gridtiles, menu);
		return true;
	}	
*/
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
	  super.onActivityResult(requestCode, resultCode, data);
	  Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);
	}
	
	public void remindClick(View v){

		Intent myIntent = new Intent(this, Main.class);
		
		startActivity(myIntent);
	}
}
