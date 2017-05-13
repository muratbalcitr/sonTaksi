package com.murat.murat.sontaxi.Login_pages;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.murat.murat.sontaxi.Haritaprocess.TaksiCagir;

import com.murat.murat.sontaxi.R;


public class LoginActivity extends AppCompatActivity {

    //defining view objects
    private EditText editTextEmail;
    private EditText editTextPassword;
    private FirebaseAuth frAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private DatabaseReference mRef;
    CallbackManager fcallbackManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(this);
        setContentView(R.layout.activity_login);
        //initializing firebase auth object
        frAuth = FirebaseAuth.getInstance();
        //initializing views
        editTextEmail = (EditText) findViewById(R.id.LoginEmailEditText);
        editTextPassword = (EditText) findViewById(R.id.LoginPasswordEditText);
        Button registerLink = (Button) this.findViewById(R.id.btnkayitol);
        Button buttonSignin = (Button) findViewById(R.id.btnHeyTaksi);
        Button button_login_facebook = (LoginButton) this.findViewById(R.id.login_button);

        FirebaseDatabase.getInstance().setPersistenceEnabled(true);

        if (!isNetworkAvailable()){

            final AlertDialog.Builder netcontrol = new AlertDialog.Builder(LoginActivity.this);
            netcontrol.setTitle("internetininiz kapalı");
            netcontrol.setMessage("Veriniz kapalı açmak istermisiniz?");

            netcontrol.setPositiveButton("EVET", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent dialogIntent = new Intent(android.provider.Settings.ACTION_DATA_ROAMING_SETTINGS);
                    dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(dialogIntent);
                    //startActivityForResult(new Intent(Settings.ACTION_DATA_ROAMING_SETTINGS), 1);
                }
            });
            netcontrol.setNegativeButton("Hayır", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });

            netcontrol.show();

        }
        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if(user!=null){
                    Toast.makeText(getApplicationContext(),"kullanıcı var",Toast.LENGTH_LONG).show();
                }

            }
        };

        buttonSignin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ProgressDialog pDialog = new ProgressDialog(LoginActivity.this);
                pDialog.setMessage("giriş yapılıyor lütfen bekleyiniz");
                pDialog.show();
                LoginUser();
                pDialog.hide();

            }
        });
        button_login_facebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                facebookLogin();
            }
        });

        registerLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent registerPage = new Intent(LoginActivity.this, register.class);
                startActivity(registerPage);

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Pass the activity result back to the Facebook SDK
        fcallbackManager.onActivityResult(requestCode, resultCode, data);
    }
    private void facebookLogin() {


        fcallbackManager = CallbackManager.Factory.create();
        LoginButton loginButton = (LoginButton) findViewById(R.id.login_button);
        loginButton.setReadPermissions("email", "public_profile");
        loginButton.registerCallback(fcallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d("başarılı", "facebook:onSuccess:" + loginResult);
                handleFacebookAccessToken(loginResult.getAccessToken());
                Intent i = new Intent(LoginActivity.this, TaksiCagir.class);
                startActivity(i);
            }

            @Override
            public void onCancel() {
                Log.d("giriş iptal", "facebook:onCancel");

            }

            @Override
            public void onError(FacebookException error) {
                Toast.makeText(getApplicationContext(), "facebooka bağlanırken bir hata oluştu", Toast.LENGTH_SHORT).show();
            }


        });
    }

    private void handleFacebookAccessToken(AccessToken accessToken) {
        AuthCredential credential = FacebookAuthProvider.getCredential(accessToken.getToken());
        frAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                Toast.makeText(getApplicationContext(),"başarılı",Toast.LENGTH_LONG).show();
            }
        });
    }

    private void LoginUser() {
        //getting email and password from edit texts
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        try {
         /*   Context context;
            context = null;
            ProgressDialog pDialog = new ProgressDialog(LoginActivity.this);
            pDialog.setMessage("giriş yapılıyor lütfen bekleyiniz");
            pDialog.show();
            progressDialog.setCancelable(false);*/
            //checking if email and passwords are empty
            if (TextUtils.isEmpty(email)) {
                Toast.makeText(LoginActivity.this, "mail adresini giriniz", Toast.LENGTH_LONG).show();
                return;
            }
            if (TextUtils.isEmpty(password)) {
                Toast.makeText(LoginActivity.this, "parolanızı giriniz", Toast.LENGTH_LONG).show();
                return;
            }
            if (!email.matches("") && !password.matches("")) {
                frAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        FirebaseUser fbUser = task.getResult().getUser();

                        if (task.isSuccessful()) {
                            if (fbUser.isEmailVerified()) {
                                Intent i = new Intent(LoginActivity.this, TaksiCagir.class);
                                startActivity(i);
                            } else {
                                Toast.makeText(LoginActivity.this, "doğrulanmamış e-mail lütfen doğrulayın", Toast.LENGTH_LONG).show();
                            }
                        } else {
                            Toast.makeText(getApplicationContext(), "Böyle bir kullanııcı bulunmamaktadır", Toast.LENGTH_LONG).show();
                            AlertDialog.Builder dialog = new AlertDialog.Builder(LoginActivity.this);
                            dialog.setTitle("Böyle bir kullanıcı bulunmamaktadır kayıt olmak istermisiniz ? ");
                            dialog.setCancelable(false);
                            dialog.setPositiveButton("EVET", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent i = new Intent(LoginActivity.this, register.class);
                                    startActivity(i);
                                }
                            });
                            dialog.setNegativeButton("İPTAL", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                        }
                    }

                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

   @Override
   public void onStart() {
       super.onStart();
       frAuth.addAuthStateListener(authStateListener);
   }

    @Override
    public void onStop() {
        super.onStop();
        if (authStateListener != null) {
            frAuth.removeAuthStateListener(authStateListener);
        }
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