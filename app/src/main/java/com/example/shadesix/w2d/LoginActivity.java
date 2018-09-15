package com.example.shadesix.w2d;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.shadesix.w2d.Models.ModelLogin;
import com.example.shadesix.w2d.Utils.Constant;
import com.example.shadesix.w2d.Utils.Utilities;
import com.google.gson.Gson;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import cz.msebera.android.httpclient.Header;

public class LoginActivity extends AppCompatActivity {
    EditText uid1,uid2,uid3,uid4,password, username;
    Button go;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        uid1 = (EditText) findViewById(R.id.edt_uderid1);
        uid2 = (EditText) findViewById(R.id.edt_uderid2);
        uid3 = (EditText) findViewById(R.id.edt_uderid3);
        uid4 = (EditText) findViewById(R.id.edt_uderid4);
        password = (EditText) findViewById(R.id.password);
        username = (EditText) findViewById(R.id.username);
        go = (Button) findViewById(R.id.go);

        uid1.setFilters(new InputFilter[]{new InputFilter.AllCaps()});
        uid3.setFilters(new InputFilter[]{new InputFilter.AllCaps()});

        //if user already logged in,starts Landing Activity
        if(!Utilities.getFromeUserDefault(LoginActivity.this,Constant.DRIVER_ID).equals("")){
            startActivity(new Intent(LoginActivity.this,StartEndActivity.class));
            finish();
        }


        //first edittext value checking and moving to next edittext
        uid1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(uid1.getText().toString().length()==2)
                    uid2.requestFocus();
            }
            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        //second edittext value checking and moving to next edittext
        uid2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(uid2.getText().toString().length()==2)
                    uid3.requestFocus();
            }
            @Override
            public void afterTextChanged(Editable editable) {}
        });

        //third edittext value checking and moving to next edittext
        uid3.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(uid3.getText().toString().length()==2)
                    uid4.requestFocus();
            }
            @Override
            public void afterTextChanged(Editable editable) {}
        });

        //fourth edittext value checking and moving to next edittext
        uid4.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(uid4.getText().toString().length()==4)
                    username.requestFocus();
            }
            @Override
            public void afterTextChanged(Editable editable) {}
        });

        //button calls loginuser() to verify credentials and log in or show error
        go.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginuser();
            }
        });
    }

    private void loginuser() {

        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.add("vehicle_id",uid1.getText().toString()+uid2.getText().toString()+uid3.getText().toString()
                +uid4.getText().toString());
        params.add("username",username.getText().toString());
        params.add("password",password.getText().toString());
        client.setTimeout(20*10000);
        client.post(Constant.BASE_URL+"login",params,new LoginActivity.ResponseHandlingClass());

    }

    public class ResponseHandlingClass extends AsyncHttpResponseHandler {

        @Override
        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
            ModelLogin model = new Gson().fromJson(new String(responseBody),ModelLogin.class);
            if(model.success == 1){
                //if success,saves values to shared prefs and starts landing activity
                Utilities.saveToUserDefault(LoginActivity.this,Constant.VEHICLE_CAPACITY,model.vehicle_capacity);
                Utilities.saveToUserDefault(LoginActivity.this,Constant.VEHICLE_ID,model.vehicle_id);
                Utilities.saveToUserDefault(LoginActivity.this,Constant.AUTH_TOKEN,model.auth_token);
                Utilities.saveToUserDefault(LoginActivity.this,Constant.DRIVER_ID,model.driver_id);

                startActivity(new Intent(LoginActivity.this,StartEndActivity.class));
                finish();
            }
            else{
                Toast.makeText(LoginActivity.this,"Username and password does not match!",Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
            Toast.makeText(LoginActivity.this,"Seems like your network connectivity is down or very slow!",Toast.LENGTH_SHORT).show();
        }
    }
}
