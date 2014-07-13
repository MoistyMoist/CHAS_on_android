package com.sla.codurs.chas.utils;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.sla.codurs.chas.R;
import com.sla.codurs.chas.model.Address;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Moistyburger on 13/7/14.
 */
public class AddressAdapter extends ArrayAdapter<Address> {

    Context context;
    ArrayList<Address> data;

    public AddressAdapter(Context context, int resourceId,
                                 ArrayList<Address> items) {
        super(context, resourceId, items);
        this.context = context;
        this.data=items;

    }

    /*private view holder class*/
    private class ViewHolder {

        TextView txtTitle;
        TextView txtDesc;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        Address rowItem = data.get(position);

        LayoutInflater mInflater = (LayoutInflater) context
                .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.address_list_layout, null);
            holder = new ViewHolder();
            holder.txtDesc = (TextView) convertView.findViewById(R.id.title);
           // holder.txtTitle = (TextView) convertView.findViewById(R.id.title);
           // holder.imageView = (ImageView) convertView.findViewById(R.id.icon);
            convertView.setTag(holder);
        } else
            holder = (ViewHolder) convertView.getTag();

        holder.txtDesc.setText(rowItem.getTitle());
      //  holder.txtTitle.setText(rowItem.getTitle());
       // holder.imageView.setImageResource(rowItem.getImageId());

        return convertView;
    }
}
