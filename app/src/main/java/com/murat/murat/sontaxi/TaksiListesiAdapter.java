package com.murat.murat.sontaxi;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by murat on 04.04.2017.
 */

public class TaksiListesiAdapter extends BaseAdapter {

    private LayoutInflater layoutInflater;
    private ArrayList<Taksiduraklari> duraklar;

    public TaksiListesiAdapter(Activity activity, ArrayList<Taksiduraklari> tdurakList) {
        //XML'i alıp View a çevirecek inflater örnekleyelim
        layoutInflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        //gösterilecek listede bu şekilde
        duraklar = tdurakList;
    }

    @Override
    public int getCount() {
        return duraklar.size();
    }

    @Override
    public Taksiduraklari getItem(int position) {
        return duraklar.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    String posted_by;
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        final View satirView;
        satirView = layoutInflater.inflate(R.layout.listitem, null);
        TextView tvDurakAdi = (TextView) satirView.findViewById(R.id.tvDurakAdi);
        TextView tvDurakNumarasi = (TextView) satirView.findViewById(R.id.tvNumber);
        Button btnAra = (Button) satirView.findViewById(R.id.btnAra);
        Taksiduraklari tdrk = duraklar.get(position);
        tvDurakAdi.setText(tdrk.getDurakAdi().toString());
        tvDurakNumarasi.setText(tdrk.getDurakNumarasi().toString());
        return satirView;
    }
}
