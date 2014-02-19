package com.example.uiprototype;

import java.util.ArrayList;

import org.apps8os.trafficsense.core.Route;
import org.apps8os.trafficsense.core.Segment;
import org.apps8os.trafficsense.core.Waypoint;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.graphics.drawable.*;
import android.app.Activity;
import android.view.Menu;
import android.view.ViewGroup.LayoutParams;

public class MainActivity extends Activity {
	private Route mRoute;
	private ArrayList<ArrayList> segmentList;
	private ArrayList<TextView> waypointList;
	private LinearLayout mLl;
	private ImageView mImage;
	private Drawable mDrawable;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mLl = (LinearLayout) findViewById(R.id.scrollLayout);	
	}
	
	public void initializeLayout() {
		for (int i = 0; i < segmentList.size(); i++) {
			ArrayList<TextView> wpList = segmentList.get(i);
			for (int j = 0; j < segmentList.get(i).size(); j++) {
				mLl.addView(wpList.get(j));
			}
		}
	}
	
	public void initializeList(Route route) {
		mRoute = route;
		segmentList = new ArrayList<ArrayList>();
		for (int i = 0; i < route.getSegmentList().size(); i++) {
			ArrayList<TextView> wpList = new ArrayList<TextView>();
			Segment seg = route.getSegment(i);
			for (int j = 0; j < seg.getWaypointList().size(); j++) {
				Waypoint wp = seg.getWaypoint(j);
				TextView text = new TextView(this);
				text.setText(wp.getWaypointName() + " " + wp.getWaypointTime());
				wpList.add(text);
			}
			segmentList.add(wpList);
		}
		initializeLayout();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
