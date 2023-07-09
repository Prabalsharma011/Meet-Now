package com.example.strangers.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.bumptech.glide.Glide;
import com.example.strangers.R;
import com.example.strangers.databinding.ActivityConnectingBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class connectingActivity extends AppCompatActivity {

    ActivityConnectingBinding binding;
    FirebaseAuth auth;
    FirebaseDatabase database;
    boolean isokay=false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityConnectingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth=FirebaseAuth.getInstance();
        FirebaseUser user=auth.getCurrentUser();
        database=FirebaseDatabase.getInstance();

        String  profile=user.getPhotoUrl().toString();
        Glide.with(this).load(profile).into(binding.profileimage);

        String username=auth.getUid();
        database.getReference().child("users").orderByChild("status").equalTo(0).limitToFirst(1).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.getChildrenCount()>0){
                    isokay=true;
                    // room available
                    for(DataSnapshot childsnap : snapshot.getChildren()){
                        database.getReference().child("users").child(childsnap.getKey()).child("incoming").setValue(username);
                        database.getReference().child("users").child(childsnap.getKey()).child("status").setValue(1);
                        Intent intent=new Intent(connectingActivity.this,callActivity.class);
                        intent.putExtra("username",username);
                        intent.putExtra("incoming",childsnap.child("incoming").getValue(String.class));
                        intent.putExtra("createdBy",childsnap.child("createdBy").getValue(String.class));
                        intent.putExtra("isAvailable",childsnap.child("isAvailable").getValue(Boolean.class));
                        startActivity(intent);
                        finish();
                    }

                }else{
                    // not available
                    HashMap<String,Object> map=new HashMap<>();
                    map.put("incoming",username);
                    map.put("createdBy",username);
                    map.put("isAvailable",true);
                    map.put("status",0);
                    database.getReference().child("users").child(username).setValue(map).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            database.getReference().child("users").child(username).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if(snapshot.child("status").exists()){
                                        if(snapshot.child("status").getValue(Integer.class)==1){

                                            if(isokay) return;

                                            isokay=true;
                                            Intent intent=new Intent(connectingActivity.this,callActivity.class);
                                            intent.putExtra("username",username);
                                            intent.putExtra("incoming",snapshot.child("incoming").getValue(String.class));
                                            intent.putExtra("createdBy",snapshot.child("createdBy").getValue(String.class));
                                            intent.putExtra("isAvailable",snapshot.child("isAvailable").getValue(Boolean.class));
                                            startActivity(intent);
                                            finish();
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });



    }
}