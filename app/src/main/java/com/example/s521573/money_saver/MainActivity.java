package com.example.s521573.money_saver;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.spark.submitbutton.SubmitButton;

public class MainActivity extends AppCompatActivity {

    private SubmitButton mStartBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mStartBtn = (SubmitButton) findViewById(R.id.main_start_btn);

        mStartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AsyncTask<String,String,String> startBtn = new AsyncTask<String, String, String>() {
                    @Override
                    protected String doInBackground(String... strings) {
                        try{
                            Thread.sleep(3100);
                        }catch (InterruptedException e){
                            e.printStackTrace();
                        }
                        return "finish";
                    }

                    @Override
                    protected void onPostExecute(String s) {
                        if (s.equals("finish")){
                            Intent management = new Intent(MainActivity.this,ManagementActivity.class);
                            startActivity(management);
                            Toast.makeText(MainActivity.this, "Welcome to Swell", Toast.LENGTH_SHORT).show();
                        }
                    }
                };
                mStartBtn.startAnimation();
                startBtn.execute();
            }
        });
    }
}
