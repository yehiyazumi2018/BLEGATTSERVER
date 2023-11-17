//Yehiya 27.09.19
package com.example.blegattserver;

import android.content.Context;
import android.os.Build;
import android.util.ArraySet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import androidx.annotation.RequiresApi;
import java.util.List;
import java.util.Set;


public class ListAdapter extends BaseAdapter {
    private static final String TAG = "ListAdapter";
    List<PrintText> people;
    Context context;
    LayoutInflater layoutInflater;
    Set<View> viewSet;

    @RequiresApi(api = Build.VERSION_CODES.M)
    public ListAdapter(Context context, List<PrintText> people){
        this.context=context;
        this.people=people;
        this.viewSet = new ArraySet<View>();
    }


    @Override
    public int getCount() {
        return people.size();
    }

    @Override
    public Object getItem(int i) {
        return people.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        PersonViewHolder personViewHolder;

        if(view==null){
            layoutInflater = LayoutInflater.from(this.context);
            view=layoutInflater.inflate(R.layout.layout_list,null);
            personViewHolder=new PersonViewHolder();
            personViewHolder.textViewName = (TextView)view.findViewById(R.id.list_text);
            view.setTag(personViewHolder);
        }else{
            personViewHolder = (PersonViewHolder)view.getTag();
        }
        final PrintText printText = people.get(position);
        personViewHolder.textViewName.setText(printText.getName());
        viewSet.add(view);
        Log.i(TAG,"Index: "+position+" : "+view+", Set Size: "+ viewSet.size());
        return view;
    }


    private static class PersonViewHolder{
        public TextView textViewName;
    }
}
