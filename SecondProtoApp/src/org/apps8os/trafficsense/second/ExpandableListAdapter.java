package org.apps8os.trafficsense.second;

import java.util.ArrayList;
import java.util.HashMap;

import org.apps8os.trafficsense.core.Route;
import org.apps8os.trafficsense.core.RouteConstants;
import org.apps8os.trafficsense.core.Segment;

import android.app.Activity;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckedTextView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * The expandable sub-lists of waypoints in schematic view. 
 */
public class ExpandableListAdapter extends BaseExpandableListAdapter {

	private final Route mRoute;
	/**
	 * List of segments in the journey.
	 */
	private final ArrayList<Segment> mSegmentList;
	/**
	 * Layout inflater from our parent Activity. 
	 */
	private LayoutInflater mInflater;
	/**
	 * A reference to the schematic view.
	 */
	private Activity mActivity;
	/**
	 * Holds the pool of convertView(s).
	 */
	private HashMap<View, HashMap<Integer,Integer>> mConvertViewPool;
	/**
	 * For reverse loop-up of mConvertViewPool.
	 */
	private HashMap<HashMap<Integer,Integer>,View> mReverseConvertViewMap;
	private final static int SEGMENT_ITEM_VIEW = 99999;
	/**
	 * Previously highlighed waypoint.
	 */
	private View mPrevWptConvertView;

	public ExpandableListAdapter(Activity act, Route route) {
		mActivity = act;
		mRoute = route;
		mInflater = act.getLayoutInflater();
		mSegmentList = route.getSegmentList();
		mConvertViewPool = new HashMap<View, HashMap<Integer,Integer>>();
		mReverseConvertViewMap = new HashMap<HashMap<Integer,Integer>,View>();
		mPrevWptConvertView = null;
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

		// TODO refactor
		if (convertView != null) {
			if (mPrevWptConvertView == convertView) {
				mPrevWptConvertView = null;
			}
			if (mConvertViewPool.containsKey(convertView)) {
				// A recycled convertView, delete its current entry
				HashMap<Integer,Integer> value = mConvertViewPool.get(convertView);
				mConvertViewPool.remove(convertView);
				mReverseConvertViewMap.remove(value);
			} else {
				// Got it from somewhere ?!
				System.out.println("DBG ExpListAdp.getChildView convertView from somewhere?");
			}
		} else {
			// It is null, we have to inflate it.
			convertView = mInflater.inflate(R.layout.waypoint_layout, null);
		}
		// Add it to pool
		HashMap<Integer,Integer> viewPosition = new HashMap<Integer,Integer>();
		viewPosition.put(groupPosition, childPosition);
		mConvertViewPool.put(convertView, viewPosition);
		mReverseConvertViewMap.put(viewPosition, convertView);
		
		// Update its text
		final String children = (String) getChild(groupPosition, childPosition);
		TextView text = (TextView) convertView.findViewById(R.id.checkedTextView);
		text.setText(children);

		// Set its color
		if(mRoute.getCurrentIndex() == groupPosition &&
			mRoute.getCurrentSegment().getCurrentIndex() == childPosition) {
			convertView.setBackgroundColor(Color.RED);
			mPrevWptConvertView = convertView;
		} else {
			convertView.setBackgroundColor(Color.CYAN);
		}
		
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
	}

	@Override
	public Object getGroup(int groupPosition) {
		return mSegmentList.get(groupPosition) + ", "
				+ mSegmentList.get(groupPosition).getSegmentStartTime();
	}

	@Override
	public int getGroupCount() {
		return mSegmentList.size();
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
		return groupPosition;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded,
			View convertView, ViewGroup parent) {

		// TODO refactor
		if (convertView != null) {
			if (mPrevWptConvertView == convertView) {
				mPrevWptConvertView = null;
			}
			if (mConvertViewPool.containsKey(convertView)) {
				// A recycled convertView, delete its current entry
				HashMap<Integer,Integer> value = mConvertViewPool.get(convertView);
				mConvertViewPool.remove(convertView);
				mReverseConvertViewMap.remove(value);
			} else {
				// Got it from somewhere ?!
				System.out.println("DBG ExpListAdp.getGroupView convertView from somewhere?");
			}
		} else {
			// It is null, we have to inflate it.
			convertView = mInflater.inflate(R.layout.segment_layout, null);
		}
		// Add it to pool
		HashMap<Integer,Integer> viewPosition = new HashMap<Integer,Integer>();
		viewPosition.put(groupPosition, SEGMENT_ITEM_VIEW);
		mConvertViewPool.put(convertView, viewPosition);
		mReverseConvertViewMap.put(viewPosition, convertView);

		// Populate it
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
		case RouteConstants.CONMUTER_TRAINS:
		case RouteConstants.TRAMS:
			imageView.setImageResource(R.drawable.tram_icon_small);
			break;
		case RouteConstants.METRO:
			imageView.setImageResource(R.drawable.metro_icon_small);
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

	public void highlighWaypoint(int segment, int waypoint) {
		HashMap<Integer,Integer> viewPosition = new HashMap<Integer,Integer>();
		viewPosition.put(segment, waypoint);
		// Un-highligh previous waypoint
		if (mPrevWptConvertView != null) {
			TextView text = (TextView) mPrevWptConvertView.findViewById(R.id.checkedTextView);
			text.setBackgroundColor(Color.CYAN);
		}
		// Highlight current waypoint
		View convertView = mReverseConvertViewMap.get(viewPosition);
		mPrevWptConvertView = convertView;
		if (convertView != null) {
			TextView text = (TextView) convertView.findViewById(R.id.checkedTextView);
			text.setBackgroundColor(Color.RED);
		}
	}

} 
