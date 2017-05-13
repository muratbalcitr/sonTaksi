package com.murat.murat.sontaxi;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.murat.murat.sontaxi.Haritaprocess.TaksiCagir;

import java.util.ArrayList;

public class Taksi_yerleri extends AppCompatActivity {

    Spinner SrcViewsehirAra;
    ListView lvDuraklar;
    ArrayList<String> illerList;
    ArrayList<Taksiduraklari> taksiduraklariList;
    FirebaseAuth frAuth;
    TextView tvNumber;
    String tiklanansehir;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_taksi_yerleri);

        SrcViewsehirAra = (Spinner) findViewById(R.id.spnSehirler);
        tvNumber = (TextView) findViewById(R.id.tvNumber);
        lvDuraklar = (ListView) findViewById(R.id.lvDuraklar);

        frAuth = FirebaseAuth.getInstance();

        //SehirDuraklari();
        tumDuraklar();

    }


    private void tumDuraklar() {
        Resources res = getResources();
        String[] illerListesi = res.getStringArray(R.array.sehirler);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, illerListesi);
        adapter.notifyDataSetChanged();
        SrcViewsehirAra.setAdapter(adapter);
        Log.d("adapter.", "başarılı");

    }

    private void SehirDuraklari(String gelenSehir) {
        taksiduraklariList = new ArrayList<>();
        DatabaseReference dbref = FirebaseDatabase.getInstance().getReference().child("taksiIlleri").child(gelenSehir);
        //   dbref.push().setValue("asdasdas");
        dbref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot dsp : dataSnapshot.getChildren()) {
                    Taksiduraklari td = new Taksiduraklari();
                    td.durakAdi = dsp.getKey().toString();
                    td.durakNumarasi = dsp.getValue().toString();
                    taksiduraklariList.add(td);
                    Log.d("taksiduraklariList.", "başarılı");
                }
                TaksiListesiAdapter tAdaptor = new TaksiListesiAdapter(Taksi_yerleri.this, taksiduraklariList);
                tAdaptor.notifyDataSetChanged();
                lvDuraklar.setAdapter(tAdaptor);
                Log.d("tAdaptor.", "başarılı");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
    String posted_by;
    public void btnAra(View view) {
        String uri = "tel:" + tvNumber.getText();
        Intent intent = new Intent(Intent.ACTION_CALL);
        intent.setData(Uri.parse(uri));
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
       startActivity(intent);
    }

    public void sehirara(View view) {
        tiklanansehir = SrcViewsehirAra.getSelectedItem().toString();
        SehirDuraklari(tiklanansehir);
    }
    public void durakEkle(View view) {
        Intent i = new Intent(Taksi_yerleri.this,DurakEkleme.class);
        startActivity(i);
    }
}