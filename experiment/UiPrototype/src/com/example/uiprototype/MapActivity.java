package com.example.uiprototype;

import java.util.ArrayList;

import org.apps8os.trafficsense.TrafficsenseContainer;
import org.apps8os.trafficsense.android.Constants;
import org.apps8os.trafficsense.core.Route;
import org.apps8os.trafficsense.core.Segment;
import org.apps8os.trafficsense.core.Waypoint;
import org.apps8os.trafficsense.util.EmailCredential;
import org.apps8os.trafficsense.util.JourneyParser;

import com.google.android.gms.maps.*;
import com.google.android.gms.maps.LocationSource.OnLocationChangedListener;
import com.google.android.gms.maps.model.*;

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MapActivity extends Activity implements OnLocationChangedListener {
	private Route mRoute;
	private GoogleMap mMap;
	private GroundOverlay mPointerOverlay;
	private TrafficsenseContainer mContainer;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        mMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map_fragment)).getMap();
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                new LatLng(-18.142, 178.431), 2));

        // Polylines are useful for marking paths and routes on the map.
        mMap.addPolyline(new PolylineOptions().geodesic(true)
                .add(new LatLng(-33.866, 151.195))  // Sydney
                .add(new LatLng(-18.142, 178.431))  // Fiji
                .add(new LatLng(21.291, -157.821))  // Hawaii
                .add(new LatLng(37.423, -122.091))  // Mountain View
        );
        mMap.setMyLocationEnabled(true);
        mContainer = TrafficsenseContainer.getInstance();
        EmailCredential cred = new EmailCredential("trafficsense.aalto@gmail.com", "ag47)h(58P", "imap.gmail.com");
		mContainer.startJourneyTracker(Constants.SERVICE_TIME_ONLY, cred);
        /*/mPointerOverlay = mMap.addGroundOverlay(new GroundOverlayOptions()
	    	.image(BitmapDescriptorFactory.defaultMarker())
	    	.positionFromBounds(new LatLngBounds(new LatLng(0.0, 0.0), new LatLng(0.0, 0.0)))
	    	.transparency((float) 0.5));/*/
    }
    
    @Override
    protected void onResume() {
    	super.onResume();
    	mContainer.activityAttach(getApplicationContext());
    }
    
    @Override
    protected void onPause() {
    	super.onPause();
    	mContainer.activityDetach();
    }
    
    public void setRoute(Route route) {
		mRoute = route;
	}
    
    public void drawPointer() {
    	View w = getFragmentManager().findFragmentById(R.id.map_fragment).getView();
    	Projection proj = mMap.getProjection();
    	Point p = proj.toScreenLocation(new LatLng(-33.866, 151.195)); // Sydney)
    	//mPointerOverlay.
    }

	@Override
	public void onLocationChanged(Location location) {
		// TODO Auto-generated method stub
		
	}
}