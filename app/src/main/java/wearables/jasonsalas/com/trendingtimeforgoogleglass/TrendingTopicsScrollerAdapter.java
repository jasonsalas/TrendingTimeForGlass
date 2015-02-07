package wearables.jasonsalas.com.trendingtimeforgoogleglass;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.google.android.glass.widget.CardBuilder;
import com.google.android.glass.widget.CardScrollAdapter;

import java.util.ArrayList;
import java.util.List;

public class TrendingTopicsScrollerAdapter extends CardScrollAdapter {

    private Activity activity;
    private List<Object> items;

    public TrendingTopicsScrollerAdapter(Activity activity) {
        this.activity = activity;
        this.items = new ArrayList<Object>();
    }

    public TrendingTopicsScrollerAdapter(Activity activity, List<Object> items) {
        this.activity = activity;
        this.items = items;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int i) {
        return items.get(i);
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        Object item = items.get(i);
        if(item instanceof CardBuilder) {
            return ((CardBuilder) item).getView(view, viewGroup);
        } else if(item instanceof View) {
            return (View) item;
        } else if(item instanceof Integer) {
            return activity.getLayoutInflater().inflate((Integer) item, viewGroup);
        } else {
            throw new ClassCastException("Unable to create view from " + item.getClass());
        }
    }

    @Override
    public int getPosition(Object o) {
        int index = items.indexOf(o);
        return index < 0 ? AdapterView.INVALID_POSITION : index;
    }

    public TrendingTopicsScrollerAdapter add(Object item) {
        if(item == null) {
            throw new NullPointerException("Unable to add null card!");
        } else if(item instanceof View || item instanceof CardBuilder || item instanceof Integer) {
            items.add(item);
        } else {
            throw new ClassCastException("Unable to add item of type " + item.getClass());
        }
        return this;
    }
}