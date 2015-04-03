package com.harmonie.castorev2;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class EnvironementsAdapter extends ArrayAdapter<Environements> {

        public EnvironementsAdapter(Context context, List<Environements> list_exp) {
            super(context, R.layout.environnement_item, list_exp);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            convertView = LayoutInflater.from(this.getContext()).inflate(R.layout.environnement_item, null);
            ((TextView) convertView.findViewById(R.id.env)).setText(this.getItem(position).ENV_NAME);
            return convertView;
        }
}


