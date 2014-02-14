package com.example.uiprototype;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.graphics.drawable.*;
import android.app.Activity;
import android.view.Menu;

public class MainActivity extends Activity {
	private ImageView mImage;
	private Drawable mDrawable;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mImage = new ImageView(this);
		mImage.setImageResource(R.id.imageView1);
		setContentView(R.layout.activity_main);
		RelativeLayout rl = (RelativeLayout) findViewById(R.id.scrollLayout);
		rl.addView(mImage);
		ImageView image2 = new ImageView(this);
		image2.setImageResource(R.id.imageView1);
		rl.addView(image2, 0, 0);
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
