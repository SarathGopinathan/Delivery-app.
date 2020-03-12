package com.example.shadesix.w2d;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.shadesix.w2d.Models.ModelStartEnd;
import com.example.shadesix.w2d.Utils.Constant;
import com.example.shadesix.w2d.Utils.Utilities;
import com.google.gson.Gson;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.ResponseHandlerInterface;

import cz.msebera.android.httpclient.Header;

public class StartEndActivity extends AppCompatActivity {

    Button startEnd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_end);

        startEnd = (Button) findViewById(R.id.btn_start_end);

        startEnd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                startEndAPI();

            }
        });
    }

    private void startEndAPI() {

        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.add("status","1");
        params.add("latitude","60.1234");
        params.add("longitude","62.1234");
        client.addHeader("auth-token", Utilities.getFromeUserDefault(StartEndActivity.this,Constant.AUTH_TOKEN));
        client.setTimeout(20*10000);
        client.post(Constant.BASE_URL+"track/onoff",params,new StartEndActivity.ResponseHandlingClass());

    }

    public class ResponseHandlingClass extends AsyncHttpResponseHandler {

        @Override
        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
            ModelStartEnd model = new Gson().fromJson(new String(responseBody),ModelStartEnd.class);

            if(model.success == 1){

                startActivity(new Intent(StartEndActivity.this,LandingActivity.class));
                finish();

            }
            else{

                startActivity(new Intent(StartEndActivity.this,LandingActivity.class));
                finish();

            }
        }

        @Override
        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
            Toast.makeText(StartEndActivity.this,"Seems like your network connectivity is down or very slow!",Toast.LENGTH_SHORT).show();
        }
    }
}
