package com.example.tiltball;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.widget.FrameLayout;

public class TiltBallActivity extends Activity {
	
	BallView ballView = null;
	Handler RedrawHandler = new Handler();
	Timer tmr = null;
	TimerTask tsk = null;
	int scrWidth, scrHeight;
	android.graphics.PointF ballPos, ballSpd;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);  //Hides title bar
		getWindow().setFlags(0xFFFFFFFF, LayoutParams.FLAG_FULLSCREEN|LayoutParams.FLAG_KEEP_SCREEN_ON);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tilt_ball);
		//create pointer to main screen
		final FrameLayout mainView = (android.widget.FrameLayout)findViewById(R.id.main_view);
		
		//get screen dimensions
		Display display = getWindowManager().getDefaultDisplay();
		scrWidth = display.getWidth();
		scrHeight = display.getHeight();
		ballPos = new android.graphics.PointF();
		ballSpd = new android.graphics.PointF();
		
		//create variables for ball position and speed
		ballPos.x = scrWidth/2;
		ballPos.y = scrHeight/2;
		ballSpd.x = 0;
		ballSpd.y = 0;
		
		//create initial ball
		ballView = new BallView(this,ballPos.x,ballPos.y,5);
		mainView.addView(ballView); //adds ball to main screen
		ballView.invalidate(); //call onDraw in BallView
		
		//listener to accelerometer
		((SensorManager)getSystemService(Context.SENSOR_SERVICE)).registerListener(
	    		new SensorEventListener() {    
	    			@Override  
	    			public void onSensorChanged(SensorEvent event) {  
	    			    //set ball speed based on phone tilt (ignore Z axis)
	    				ballSpd.x = -event.values[0];
	    				ballSpd.y = event.values[1];
	    				//timer event will redraw ball
	    			}

					@Override
					public void onAccuracyChanged(Sensor sensor, int accuracy) {
						// TODO Auto-generated method stub
						
					}
	    		},
	    		((SensorManager)getSystemService(Context.SENSOR_SERVICE))
	        	.getSensorList(Sensor.TYPE_ACCELEROMETER).get(0), SensorManager.SENSOR_DELAY_NORMAL);
	        		
	        //listener for touch event 
	        mainView.setOnTouchListener(new android.view.View.OnTouchListener() {
		        public boolean onTouch(android.view.View v, android.view.MotionEvent e) {
		        	//set ball position based on screen touch
		        	ballPos.x = e.getX();
		        	ballPos.y = e.getY();
	    			//timer event will redraw ball
		        	return true;
		        }}); 
	
	}
	
	//listener for menu button on phone
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add("Exit"); //only one menu item
        return super.onCreateOptionsMenu(menu);
    }

  //listener for menu item clicked
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	// Handle item selection    
    	if (item.getTitle() == "Exit") //user clicked Exit
    		finish(); //will call onPause
   		return super.onOptionsItemSelected(item);    
    }
    
    @Override
    public void onPause() //app moved to background, stop background threads
    {
    	tmr.cancel(); //kill\release timer (our only background thread)
    	tmr = null;
    	tsk = null;
    	super.onPause();
    }
    
    @Override
    public void onResume() //app moved to foreground (also occurs at app startup)
    {
        //create timer to move ball to new position
        tmr = new Timer(); 
        tsk = new TimerTask() {
			public void run() {
				//if debugging with external device, 
				//  a cat log viewer will be needed on the device
				android.util.Log.d(
				    "TiltBall","Timer Hit - " + ballPos.x + ":" + ballPos.y);
			    //move ball based on current speed
				ballPos.x += ballSpd.x;
				ballPos.y += ballSpd.y;
				//if ball goes off screen, reposition to opposite side of screen
				if (ballPos.x > scrWidth) ballPos.x=0;
				if (ballPos.y > scrHeight) ballPos.y=0;
				if (ballPos.x < 0) ballPos.x=scrWidth;
				if (ballPos.y < 0) ballPos.y=scrHeight;
				//update ball class instance
				ballView.X = ballPos.x;
				ballView.Y = ballPos.y;
				//redraw ball. Must run in background thread to prevent thread lock.
				RedrawHandler.post(new Runnable() {
				    public void run() {	
					   ballView.invalidate();
				  }});
			}}; // TimerTask

        tmr.schedule(tsk,10,10); //start timer
        super.onResume();
    }
    
    @Override
    public void onDestroy() //main thread stopped
    {
    	super.onDestroy();
    	System.runFinalizersOnExit(true); //wait for threads to exit before clearing app
    	android.os.Process.killProcess(android.os.Process.myPid());  //remove app from memory 
    }
    
    //listener for config change. 
    //This is called when user tilts phone enough to trigger landscape view
    //we want our app to stay in portrait view, so bypass event 
    @Override 
    public void onConfigurationChanged(Configuration newConfig)
	{
       super.onConfigurationChanged(newConfig);
	}

}
