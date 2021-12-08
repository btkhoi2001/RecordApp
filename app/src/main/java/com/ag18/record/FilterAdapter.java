package com.ag18.record;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

public class FilterAdapter extends BaseAdapter {
    private Context context;
    private int layout;
    private List<Filter> filterList;

    public FilterAdapter(Context context, int layout, List<Filter> filterList) {
        this.context = context;
        this.layout = layout;
        this.filterList = filterList;
    }

    @Override
    public int getCount() {
        return filterList.size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view = inflater.inflate(layout, null);

        TextView tv_filter_name = (TextView) view.findViewById(R.id.tv_filter_name);
        TextView tv_icon = (TextView) view.findViewById(R.id.tv_icon);

        Filter tv = filterList.get(i);

        tv_filter_name.setText(tv.getName());
        tv_icon.setText(tv.getIcon());

        return view;
    }
}
