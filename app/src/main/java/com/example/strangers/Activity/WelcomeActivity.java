package com.example.strangers.Activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.strangers.R;
import com.google.firebase.auth.FirebaseAuth;

public class WelcomeActivity extends AppCompatActivity {
    Button b;
    FirebaseAuth auth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        b=findViewById(R.id.button);
        auth=FirebaseAuth.getInstance();

        if(auth.getCurrentUser()!=null){
            Intent intent=new Intent(WelcomeActivity.this,MainActivity.class);
            startActivity(intent);
         }
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            getintentactivity();

            }
        });
    }

    void getintentactivity(){
        Intent intent=new Intent(WelcomeActivity.this,LoginActivity.class);
        startActivity(intent);
    }
}