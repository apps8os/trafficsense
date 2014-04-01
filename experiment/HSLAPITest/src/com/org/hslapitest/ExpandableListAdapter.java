package com.org.hslapitest;

import java.util.ArrayList;

import android.app.Activity;
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
	  private boolean isInFront = false;
	  public ExpandableListAdapter(Activity act, Route route) {
		    activity = act;
		    this.route = route;
		    inflater = act.getLayoutInflater();
		    this.SegmentList = route.getSegmentList();
		  }

		  @Override
		  public Object getChild(int groupPosition, int childPosition) {
			 return SegmentList.get(groupPosition).getWaypointList().get(childPosition).getWaypointName()+ ", " + SegmentList.get(groupPosition).getWaypointList().get(childPosition).getWaypointTime();
		   // return groups.get(groupPosition).children.get(childPosition);
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
		      convertView = inflater.inflate(R.layout.listrow_details, null);
		    }
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
		      convertView = inflater.inflate(R.layout.listrow_group, null);
		    }
		   	    
		    TextView lblListHeader = (TextView) convertView
                    .findViewById(R.id.checkedTextView);
           
            lblListHeader.setText(SegmentList.get(groupPosition).getSegmentMode() + ", " + SegmentList.get(groupPosition).getSegmentStartTime() + "                                            ");

            ImageView imageView = (ImageView) convertView.findViewById(R.id.GroupImage);
            imageView.setImageResource(R.drawable.walking_icon_small);

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
		  
		  
		  public void ChangeColor() {
			    isInFront = true;
			}
		} 

