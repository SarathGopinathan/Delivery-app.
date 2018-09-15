package com.example.shadesix.w2d;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

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
                startActivity(new Intent(StartEndActivity.this,LandingActivity.class));
                finish();
            }
        });

    }
}
