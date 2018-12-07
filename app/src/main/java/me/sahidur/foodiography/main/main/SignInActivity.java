package me.sahidur.foodiography.main.main;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.transition.Explode;
import android.util.Log;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import me.sahidur.foodiography.R;
import me.sahidur.foodiography.main.utils.Constants;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import java.util.Arrays;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;



public class SignInActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    private static final int RC_SIGN_IN = 9001;
    private static String TAG = "SignInActivity:";
    @Bind(R.id.sign_in_button)
    Button signInGoogle;
    @Bind(R.id.coordinator)
    RelativeLayout vCoordinatorLayout;

    @Bind(R.id.sign_in_button_fb)
    Button signInFacebook;

    ProgressDialog mProgressDialog;

    private FirebaseAuth mFAuth;
    private GoogleApiClient mGoogleApiClient;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private FirebaseUser mFUser;
    private CallbackManager callbackManager;
    AuthCredential credential;
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    private void setupWindowAnimations() {
        Explode slide;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            slide = new Explode();
            slide.setDuration(1000);
            getWindow().setExitTransition(slide);
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity_sign_in);

        SharedPreferences settings = getSharedPreferences("firstTime", MODE_PRIVATE);
        boolean firstTime = settings.getBoolean("first_launch", true); //0 is the default valu

        if(firstTime){
            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean("first_launch", false);
            editor.apply();
        }

        ButterKnife.bind(this);
        setupWindowAnimations();

        mFAuth = FirebaseAuth.getInstance();

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage(getString(R.string.signing_in));
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        FacebookSdk.sdkInitialize(this.getApplicationContext());
        callbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
            }

            @Override
            public void onError(FacebookException error) {
                Toast.makeText(getApplicationContext(), "" + error.getMessage(), Toast.LENGTH_LONG).show();
                alertLoginFailed("facebook");
            }
        });

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                mFUser = firebaseAuth.getCurrentUser();
                if (mFUser != null) {
                    Intent intent = new Intent(SignInActivity.this, MainActivity.class);
                    intent.putExtra("prov", prov);
                    startActivity(intent);
                    SignInActivity.this.finish();
                } else {
                    Log.d(Constants.TAG_PARENT, TAG + "onAuthStateChanged:userLogout");
                }
            }
        };
    }

    private void handleFacebookAccessToken(AccessToken token) {
        Log.d(TAG, "handleFacebookAccessToken:" + token);
        mProgressDialog.show();
        credential = FacebookAuthProvider.getCredential(token.getToken());
        mFAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (!task.isSuccessful()) {
                            Log.w(Constants.TAG_PARENT, TAG + "signInWithCredential", task.getException());
                            showToast(getString(R.string.auth_failed));
                        }
                        prov = "Facebook";
                        mProgressDialog.dismiss();

                    }
                });
    }

    @OnClick(R.id.sign_in_button_fb)
    void loginWithFacebook(){
        signInFacebook();
    }

    @OnClick(R.id.sign_in_button)
    void setSignInGoogle() {
        mProgressDialog.show();
        signInGoogle();
    }

    private void signInFacebook() {
        LoginManager
                .getInstance()
                .logInWithReadPermissions(
                        this,
                        Arrays.asList("public_profile", "user_friends", "email")
                );
    }


    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e(Constants.TAG_PARENT, TAG + "onConnectionFailed:" + connectionResult);
        showToast(getString(R.string.play_error));
    }

    private void signInGoogle() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onStart() {
        super.onStart();
        mFAuth.addAuthStateListener(mAuthStateListener);
    }

    String prov;
    public void alertLoginFailed(final String cred) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setMessage(R.string.login_failed);
        alertDialog.setPositiveButton(R.string.try_again, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if(cred.equals("google")){
                    signInGoogle();
                }else if(cred.equals("facebook")){
                    signInFacebook();
                }

                dialog.dismiss();
            }
        });
        alertDialog.setNegativeButton(R.string.quit_app, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                finish();
            }
        });
        alertDialog.setCancelable(false);
        alertDialog.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
                prov = "Google";
            } else {
                alertLoginFailed("google");
            }
        }else{
            callbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {

        mProgressDialog.show();

        credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mFAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (!task.isSuccessful()) {
                            Log.w(Constants.TAG_PARENT, TAG + "signInWithCredential", task.getException());
                            showToast(getString(R.string.auth_failed));
                        }

                        mProgressDialog.dismiss();
                    }
                });
    }

    protected void showToast(String text) {
        Snackbar snackbar = Snackbar
                .make(vCoordinatorLayout, text, Snackbar.LENGTH_LONG);
        snackbar.show();
    }
}
