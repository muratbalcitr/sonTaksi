package com.murat.murat.sontaxi.Login_pages;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import com.murat.murat.sontaxi.R;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class register extends AppCompatActivity implements View.OnClickListener {
    private EditText adsoyad, username, email, password, passwordRepeat;
    private Button register, cancel;
    private RadioButton taksici, musteri;
    private ProgressDialog progressBar;

    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //firebase doğrulama başlatılıyor.
        firebaseAuth = FirebaseAuth.getInstance();

        adsoyad = (EditText) findViewById(R.id.etAdsoyad);
        username = (EditText) this.findViewById(R.id.etUsername);
        email = (EditText) this.findViewById(R.id.etEmail);
        password = (EditText) this.findViewById(R.id.etPassword);
        passwordRepeat = (EditText) this.findViewById(R.id.etPasswordRepeat);

        taksici = (RadioButton) findViewById(R.id.rbtnTaksici);
        musteri = (RadioButton) findViewById(R.id.rbtnMusteri);
        musteri.toggle();
        cancel = (Button) this.findViewById(R.id.btnCancel);
        register = (Button) this.findViewById(R.id.btnRegister);
        //attaching listener to button
        cancel.setOnClickListener(this);
        register.setOnClickListener(this);

    }

    private void cancel() {
        Intent i = new Intent(register.this, LoginActivity.class);
        startActivity(i);

    }

    private void register() {

        progressBar = new ProgressDialog(this);
        progressBar.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressBar.setIndeterminate(true);
        progressBar.setMessage("Hacı kayıt yapıyorum burada bekle biraz :)..");

        final String RadSoyad = adsoyad.getText().toString().trim();
        final String Rusername = username.getText().toString().trim();
        final String Remail = email.getText().toString().trim();
        final String Rpassword = password.getText().toString().trim();
        final String RPasswordRepeat = passwordRepeat.getText().toString().trim();
        final String Rmusteri = musteri.getText().toString().trim();
        final String Rtaksici = taksici.getText().toString().trim();


        if (Rusername.matches("") && Rusername.length() < 5) {
            Toast.makeText(register.this, "Kullanıcı adını boş olamaz ve en az 5 harfli olacak  ", Toast.LENGTH_SHORT).show();
        }
        if (TextUtils.isEmpty(Remail)) {
            Toast.makeText(register.this, "e-mail'i boş bırakmayınız!", Toast.LENGTH_SHORT).show();
        }
        if (TextUtils.isEmpty(Rpassword)) {
            Toast.makeText(register.this, "parola giriniz ", Toast.LENGTH_SHORT).show();
        }
        if (TextUtils.isEmpty(RPasswordRepeat)) {
            Toast.makeText(register.this, "parola giriniz", Toast.LENGTH_SHORT).show();
        }

        if (!(Rpassword.equals(RPasswordRepeat))) {
            Toast.makeText(register.this, "parolalar uyuşmuyor", Toast.LENGTH_SHORT).show();
        }
        if (Rpassword.length() < 6) {
            Toast.makeText(register.this, "parola en az 6 karakter olmalı", Toast.LENGTH_SHORT).show();
        }

        progressBar.show();
        if (Rpassword.length() >= 6 && Rpassword.equals(RPasswordRepeat) && Rusername.length() >= 5 && !Remail.matches("")) {


            //creating a new user
            firebaseAuth.createUserWithEmailAndPassword(Remail, Rpassword)
                    .addOnCompleteListener(register.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {

                            //checking if success
                            if (task.isSuccessful()) {

                                FirebaseUser fbUser = task.getResult().getUser();//kullanıcılara erişiyoruz
                                String userId = fbUser.getUid();//her kulanıcnın uid sini alıyoruz

                                if (musteri.isChecked()) {
                                    //display some message here

                                    DatabaseReference dbref = FirebaseDatabase.getInstance().getReference().child("musteriler");//eğer üyeler tablosu yoka kendi oluşturuyor.

                                    dbref.child(userId).child("adsoyad").setValue(RadSoyad);
                                    dbref.child(userId).child("username").setValue(Rusername);
                                    dbref.child(userId).child("email").setValue(Remail);
                                    dbref.child(userId).child("musteri").setValue(Rmusteri);

                                    fbUser.sendEmailVerification().addOnCompleteListener(register.this, new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            Toast.makeText(register.this, "kayıt başarılı mail adresini kontrol ediniz", Toast.LENGTH_LONG).show();
                                           /* Intent i = new Intent(register.this, LoginActivity.class);
                                            startActivity(i);*/
                                        }
                                    });


                                } else {


                                    DatabaseReference dbref = FirebaseDatabase.getInstance().getReference().child("taksici");//eğer üyeler tablosu yoka kendi oluşturuyor.

                                    dbref.child(userId).child("adsoyad").setValue(RadSoyad);
                                    dbref.child(userId).child("username").setValue(Rusername);
                                    dbref.child(userId).child("email").setValue(Remail);
                                    dbref.child(userId).child("taksici").setValue(Rtaksici);

                                    fbUser.sendEmailVerification().addOnCompleteListener(register.this, new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            Toast.makeText(register.this, "kayıt başarılı mail adresini kontrol ediniz", Toast.LENGTH_LONG).show();
                                           /* Intent i = new Intent(register.this, LoginActivity.class);
                                            startActivity(i);*/

                                        }
                                    });

                                }


                            } else {
                                Toast.makeText(register.this, "kayıt hatası", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
        }
        progressBar.hide();

    }

    @Override
    public void onClick(View v) {

        if (v == register) {
            register();
        } else if (v == cancel) {
            cancel();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

}