package com.app.twiittooo;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;

import io.fabric.sdk.android.Fabric;


public class LoginActivity extends AppCompatActivity {

    // Note: Your consumer key and secret should be obfuscated in your source code before shipping.
    private static final String TWITTER_KEY = "kpMRkzLmXFARlVwMbxecfv2em";
    private static final String TWITTER_SECRET = "02iSrzWtIp5qJDqzFy4DwfdfsR9oPHGz4pfThtSC6iTgHD99iF";
    private TwitterLoginButton loginButton;


    public static final String PREFERENCES = "myPrefs" ;
    public static final String PREF_USER_NAME = "usernamekey";
    public static final String PREF_PASSWORD = "passwordKey";
    public static final String PREF_loggedIn = "loginKey";
    SharedPreferences sharedpreferences;
    Boolean isLoggedin = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(!firsTimeCheck()) {

            TwitterAuthConfig authConfig = new TwitterAuthConfig(TWITTER_KEY, TWITTER_SECRET);
            Fabric.with(this, new Twitter(authConfig));
            setContentView(R.layout.activity_login);

            loginButton = (TwitterLoginButton) findViewById(R.id.twitter_login_button);
            if (isOnline()) {
                loginButton.setCallback(new Callback<TwitterSession>() {
                    @Override
                    public void success(Result<TwitterSession> result) {
                        // The TwitterSession is also available through:
                        // Twitter.getInstance().core.getSessionManager().getActiveSession()
                        TwitterSession session = result.data;
                        // TODO: Remove toast and use the TwitterSession's userID
                        // with your app's user model
                        String msg = "@" + session.getUserName() + " logged in! (#" + session.getUserId() + ")";
                        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();

                        //save the user credential in device settings
                        //TODO: get user password from session
                        saveCredentials(session.getUserName(), "");

                        sharedpreferences = getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedpreferences.edit();
                        editor.putBoolean(PREF_loggedIn, true);
                        editor.commit();

                        Intent i = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(i);
                    }

                    @Override
                    public void failure(TwitterException exception) {
                        Log.d("TwitterKit", "Login with Twitter failure", exception);
                    }
                });

            }
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        firsTimeCheck();
    }

    private boolean firsTimeCheck() {
        Log.i("in Method","------first time check------");
        sharedpreferences = getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
        isLoggedin = sharedpreferences.getBoolean(PREF_loggedIn, false);
        if(isLoggedin){
            //TODO: login to the server to make sure that the user didn't change his credentials from the web
            Intent i = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(i);
        }
        return isLoggedin;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Make sure that the loginButton hears the result from any
        // Activity that it triggered.
        loginButton.onActivityResult(requestCode, resultCode, data);
    }


    //TODO: change this method to broadcast receiver
    public boolean isOnline() {
        // check if device is connected to the internet
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }


    protected void saveCredentials(String username, String password){
        sharedpreferences = getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putString(PREF_USER_NAME, username);
        editor.putString(PREF_PASSWORD, password);
        editor.commit();

    }

    protected String retrieveCredentials() {
        sharedpreferences = getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
        return sharedpreferences.getString(PREF_USER_NAME,"");
    }

    public  void clearCredentials(){
        sharedpreferences = getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.clear();
        editor.commit();
    }
}
