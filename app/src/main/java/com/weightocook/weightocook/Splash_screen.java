package com.weightocook.weightocook;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;

/**
 * Created by Micolichek's on 9/20/2015.
 */
 
public class Splash_screen extends Activity {
    private Thread mSplashThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_layout);
        final Splash_screen sPlashScreen = this;

        mSplashThread =  new Thread(){

            @Override
            public void run(){
                try {
                    synchronized(this){
                        wait(5000);
                    }
                }
                catch(InterruptedException ex){

                }

                finish();

                Intent intent = new Intent();
                intent.setClass(sPlashScreen, HomeActivity.class);
                startActivity(intent);
            }
        };
        mSplashThread.start();
    }

    @Override
    public boolean onTouchEvent(MotionEvent evt)
    {
        if(evt.getAction() == MotionEvent.ACTION_DOWN)
        {
            synchronized(mSplashThread){
                mSplashThread.notifyAll();
            }
        }
        return true;
    }
}
