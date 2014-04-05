package org.apps8os.trafficsense.second;

import java.util.ArrayList;

import org.apps8os.trafficsense.core.Route;
import org.apps8os.trafficsense.core.Segment;




import android.app.Activity;
import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.BackgroundColorSpan;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckedTextView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class ExpandableListAdapter extends BaseExpandableListAdapter{
	  private final Route route;
	  private final ArrayList<Segment> SegmentList;
	  public LayoutInflater inflater;
	  public Activity activity;
	  
	  private ArrayList <View> segment_Views = new ArrayList <View>();
	  private ArrayList <ArrayList<View>> waypoint_Views = new ArrayList <ArrayList<View>>();

	  public ExpandableListAdapter(Activity act, Route route) {
	    activity = act;
	    this.route = route;
	    inflater = act.getLayoutInflater(); 
	    this.SegmentList = route.getSegmentList();
	  }

	  @Override
	  public Object getChild(int groupPosition, int childPosition) {
		 return SegmentList.get(groupPosition).getWaypointList().get(childPosition).getWaypointName()+ ", " + SegmentList.get(groupPosition).getWaypointList().get(childPosition).getWaypointTime();
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
	      convertView = inflater.inflate(R.layout.waypoint_layout, null);
	    }
	    //waypoint_Views.add(index, object);
	    text = (TextView) convertView.findViewById(R.id.checkedTextView);
	    text.setText(children);
	    convertView.setOnClickListener(new OnClickListener() {
	      @Override
	      public void onClick(View v) {
	        Toast.makeText(activity, children,
	            Toast.LENGTH_SHORT).show();
	      }
	    });
	    return convertView;
	  }

	  @Override
	  public int getChildrenCount(int groupPosition) {
		  return SegmentList.get(groupPosition).getWaypointList().size();
	    //return groups.get(groupPosition).children.size();
	  }

	  @Override
	  public Object getGroup(int groupPosition) {
		return SegmentList.get(groupPosition) + ", " + SegmentList.get(groupPosition).getSegmentStartTime();
	    //return groups.get(groupPosition);
	  }

	  @Override
	  public int getGroupCount() {
		return SegmentList.size();
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
	      convertView = inflater.inflate(R.layout.segment_layout, null);
	    }
	    segment_Views.add(convertView);
	   int mode = SegmentList.get(groupPosition).getSegmentType();
	   CheckedTextView textview = (CheckedTextView) convertView.findViewById(R.id.checkedTextView);
	    
	    textview.setText(SegmentList.get(groupPosition).getSegmentMode() + ", " + SegmentList.get(groupPosition).getSegmentStartTime());
	    textview.setChecked(isExpanded);
	    ImageView imageView = (ImageView) convertView.findViewById(R.id.GroupImage);
	   //textview.setBackgroundColor(Color.BLACK);

	    
	    
	    if(mode == 0){
	    	 imageView.setImageResource(R.drawable.walking_icon_small);
	    }else
	    if(mode == 7){
	    	 imageView.setImageResource(R.drawable.ferry_icon_small);
	    }else
	    if(mode == 2){
	    	 imageView.setImageResource(R.drawable.tram_icon_small);
	    }else
	    	 if(mode == 1 || mode == 3 || mode == 4 || mode == 5 || mode== 22){
		    	 imageView.setImageResource(R.drawable.bus_icon_small);
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
	  public ArrayList<View> getSegmentViewList(){
		  return segment_Views;
	  }
	  
	  public ArrayList<View> getWaypointViewList(){
		  return waypoint_Views;
	  }*/
	  
//		public void highlightCurrentWaypoint(){
//		
//	}
	  
	} 



