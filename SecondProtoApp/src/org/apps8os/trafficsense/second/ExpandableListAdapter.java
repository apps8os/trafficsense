package org.apps8os.trafficsense.second;

import java.util.ArrayList;

import org.apps8os.trafficsense.core.Route;
import org.apps8os.trafficsense.core.RouteConstants;
import org.apps8os.trafficsense.core.Segment;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckedTextView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class ExpandableListAdapter extends BaseExpandableListAdapter {
	//private final Route mRoute;
	private final ArrayList<Segment> mSegmentList;
	private LayoutInflater mInflater;
	private Activity mActivity;

	private ArrayList<View> mSegmentViews = new ArrayList<View>();

	//private ArrayList <ArrayList<View>> mWaypointViews = new ArrayList<ArrayList<View>>();

	public ExpandableListAdapter(Activity act, Route route) {
		mActivity = act;
		//mRoute = route;
		mInflater = act.getLayoutInflater();
		mSegmentList = route.getSegmentList();
	}

	@Override
	public Object getChild(int groupPosition, int childPosition) {
		return mSegmentList.get(groupPosition).getWaypointList()
				.get(childPosition).getWaypointName()
				+ ", "
				+ mSegmentList.get(groupPosition).getWaypointList()
						.get(childPosition).getWaypointTime();
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return 0;
	}

	@Override
	public View getChildView(int groupPosition, final int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {

		final String children = (String) getChild(groupPosition, childPosition);
		TextView text = null;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.waypoint_layout, null);
		}
		//mWaypointViews.add(index, object);
		text = (TextView) convertView.findViewById(R.id.checkedTextView);
		text.setText(children);
		//highlightCurrentWaypoint(convertView);

		convertView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Toast.makeText(mActivity, children, Toast.LENGTH_SHORT).show();
			}
		});
		return convertView;
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		return mSegmentList.get(groupPosition).getWaypointList().size();
		//return groups.get(groupPosition).children.size();
	}

	@Override
	public Object getGroup(int groupPosition) {
		return mSegmentList.get(groupPosition) + ", "
				+ mSegmentList.get(groupPosition).getSegmentStartTime();
		//return groups.get(groupPosition);
	}

	@Override
	public int getGroupCount() {
		return mSegmentList.size();
		//return groups.size();
	}

	@Override
	public void onGroupCollapsed(int groupPosition) {
		super.onGroupCollapsed(groupPosition);
	}

	@Override
	public void onGroupExpanded(int groupPosition) {
		super.onGroupExpanded(groupPosition);
	}

	@Override
	public long getGroupId(int groupPosition) {
		return 0;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded,
			View convertView, ViewGroup parent) {

		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.segment_layout, null);
		}
		mSegmentViews.add(convertView);
		int hslSegMode = mSegmentList.get(groupPosition).getSegmentType();
		CheckedTextView textview = (CheckedTextView) convertView
				.findViewById(R.id.checkedTextView);

		textview.setText(mSegmentList.get(groupPosition).getSegmentMode()
				+ ", " + mSegmentList.get(groupPosition).getSegmentStartTime());
		textview.setChecked(isExpanded);
		ImageView imageView = (ImageView) convertView
				.findViewById(R.id.GroupImage);

		switch (hslSegMode) {
		case RouteConstants.WALKING:
			imageView.setImageResource(R.drawable.walking_icon_small);
			break;
		case RouteConstants.FERRY:
			imageView.setImageResource(R.drawable.ferry_icon_small);
			break;
		case RouteConstants.TRAMS:
			imageView.setImageResource(R.drawable.tram_icon_small);
			break;
		case RouteConstants.CONMUTER_TRAINS:
			break;
		case RouteConstants.METRO:
			break;
		default:
			imageView.setImageResource(R.drawable.bus_icon_small);
			break;
		}

		return convertView;
	}

	@Override
	public boolean hasStableIds() {
		return false;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return false;
	}

	/*
	public ArrayList<View> getSegmentViewList() {
		return mSegmentViews;
	}

	public ArrayList<View> getWaypointViewList() {
		return mWaypointViews;
	}
	*/
	
	/*
	//public void highlightCurrentWaypoint(int groupPosition,int childPosition, View convertView, ViewGroup parent){
	public void highlightCurrentWaypoint(View convertView) {
		System.out.println("DBG hightlightCurrentWaypoint");

		TextView text = null;
		ArrayList<View> al = mWaypointViews.get(0);
		System.out.println("SIZEEE:" + mSegmentViews.size());
		mRoute.getSegmentList().get(1).getWaypointList().get(0);
		mSegmentViews.get().findViewById(R.id.checkedTextView)
				.setBackgroundColor(Color.CYAN);
		View v = al.get(0);
		TextView p = (TextView) mWaypointViews.get(1).get(1);
		text = (TextView) p.findViewById(R.id.checkedTextView);
		p.setBackgroundColor(Color.CYAN);
		text = (TextView) convertView.findViewById(R.id.checkedTextView);
		text.setBackgroundColor(Color.CYAN);

	}
	*/

} 
