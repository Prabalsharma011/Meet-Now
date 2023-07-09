package com.example.strangers.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.strangers.Models.User;
import com.example.strangers.R;
import com.example.strangers.databinding.ActivityMainBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;
    FirebaseAuth auth;
    FirebaseDatabase database;
    long coins;
    // for permission
    String[] permissions=new String[]{Manifest.permission.CAMERA,Manifest.permission.RECORD_AUDIO};
    private int requestcode=1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        auth=FirebaseAuth.getInstance();

        database=FirebaseDatabase.getInstance();
        FirebaseUser currentuser= auth.getCurrentUser();
        database.getReference().child("profiles").child(currentuser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user=snapshot.getValue(User.class);
                coins=user.getCoins();
                binding.coins.setText("You have: "+coins);
                Glide.with(MainActivity.this).load(currentuser.getPhotoUrl()).into(binding.profileimage);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        binding.find.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(permissiongranted()) {


                    if (coins > 5) {
                        startActivity(new Intent(MainActivity.this, connectingActivity.class));
                        Toast.makeText(MainActivity.this, "Call Finding...", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(MainActivity.this, "Insufficient Coins", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    askpermission();
                }
            }
        });

    }

    void askpermission(){
        ActivityCompat.requestPermissions(this,permissions,requestcode);
    }
    private boolean permissiongranted(){
        for(String permission : permissions){
            if(ActivityCompat.checkSelfPermission(this,permission)!= PackageManager.PERMISSION_GRANTED){
                return false;
            }
        }
        return  true;
    }
}