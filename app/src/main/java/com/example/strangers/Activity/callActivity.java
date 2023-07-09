package com.example.strangers.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.strangers.Models.InterfaceJava;
import com.example.strangers.Models.User;
import com.example.strangers.R;
import com.example.strangers.databinding.ActivityCallBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.NotNull;

import java.util.UUID;

public class callActivity extends AppCompatActivity {
    ActivityCallBinding binding;
    String uniqueId = "";
    FirebaseAuth auth;
    String username = "";
    String friendsUsername = "";

    boolean isPeerConnected = false;

    DatabaseReference firebaseRef;

    boolean isAudio = true;
    boolean isVideo = true;
    String createdBy;

    boolean pageExit = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCallBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        auth = FirebaseAuth.getInstance();
        firebaseRef = FirebaseDatabase.getInstance().getReference().child("users");

        username = getIntent().getStringExtra("username");
        String incoming = getIntent().getStringExtra("incoming");
        createdBy = getIntent().getStringExtra("createdBy");

//        friendsUsername = "";
//
//        if(incoming.equalsIgnoreCase(friendsUsername))
//            friendsUsername = incoming;

        friendsUsername = incoming;

        setupWebView();

        binding.micbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isAudio = !isAudio;
                callJavaScriptFunction("javascript:toggleAudio(\""+isAudio+"\")");
                if(isAudio){
                    binding.micbutton.setImageResource(R.drawable.btn_unmute_normal);
                } else {
                    binding.micbutton.setImageResource(R.drawable.btn_mute_normal);
                }
            }
        });

        binding.videobutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isVideo = !isVideo;
                callJavaScriptFunction("javascript:toggleVideo(\""+isVideo+"\")");
                if(isVideo){
                    binding.videobutton.setImageResource(R.drawable.btn_video_normal);
                } else {
                    binding.videobutton.setImageResource(R.drawable.btn_video_muted);
                }
            }
        });

        binding.endcall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });


    }


    void setupWebView() {
        binding.webview.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onPermissionRequest(PermissionRequest request) {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    request.grant(request.getResources());
                }
            }
        });

        binding.webview.getSettings().setJavaScriptEnabled(true);
        binding.webview.getSettings().setMediaPlaybackRequiresUserGesture(false);
        binding.webview.addJavascriptInterface(new InterfaceJava(this), "Android");

        loadVideoCall();
    }

    public void loadVideoCall() {
        String filePath = "file:android_asset/call.html";
        binding.webview.loadUrl(filePath);

        binding.webview.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                initializePeer();
            }
        });
    }


    void initializePeer() {
        uniqueId = getUniqueId();

        callJavaScriptFunction("javascript:init(\"" + uniqueId + "\")");

        if(createdBy.equalsIgnoreCase(username)) {
            if(pageExit)
                return;
            firebaseRef.child(username).child("connId").setValue(uniqueId);
            firebaseRef.child(username).child("isAvailable").setValue(true);

            binding.loadinggroup.setVisibility(View.GONE);
            binding.controlls.setVisibility(View.VISIBLE);

            FirebaseDatabase.getInstance().getReference()
                    .child("profiles")
                    .child(friendsUsername)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                            User user = snapshot.getValue(User.class);

                            Glide.with(callActivity.this).load(user.getProfile())
                                    .into(binding.profile);
                            binding.name.setText(user.getName());
                            binding.city.setText(user.getCity());

                        }

                        @Override
                        public void onCancelled(@NonNull @NotNull DatabaseError error) {

                        }
                    });

        } else {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    friendsUsername = createdBy;
                    FirebaseDatabase.getInstance().getReference()
                            .child("profiles")
                            .child(friendsUsername)
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                                    User user = snapshot.getValue(User.class);

                                    Glide.with(callActivity.this).load(user.getProfile())
                                            .into(binding.profile);
                                    binding.name.setText(user.getName());
                                    binding.city.setText(user.getCity());

                                }

                                @Override
                                public void onCancelled(@NonNull @NotNull DatabaseError error) {

                                }
                            });
                    FirebaseDatabase.getInstance().getReference()
                            .child("users")
                            .child(friendsUsername)
                            .child("connId")
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                                    if(snapshot.getValue() != null) {
                                        sendCallRequest();
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull @NotNull DatabaseError error) {

                                }
                            });
                }
            }, 3000);
        }

    }

    public void onPeerConnected(){
        isPeerConnected = true;
    }

    void sendCallRequest(){
        if(!isPeerConnected) {
            Toast.makeText(this, "You are not connected. Please check your internet.", Toast.LENGTH_SHORT).show();
            return;
        }

        listenConnId();
    }

    void listenConnId() {
        firebaseRef.child(friendsUsername).child("connId").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if(snapshot.getValue() == null)
                    return;

               binding.loadinggroup.setVisibility(View.GONE);
                binding.controlls.setVisibility(View.VISIBLE);
                String connId = snapshot.getValue(String.class);
                callJavaScriptFunction("javascript:startCall(\""+connId+"\")");
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }

    void callJavaScriptFunction(String function){
        binding.webview.post(new Runnable() {
            @Override
            public void run() {
                binding.webview.evaluateJavascript(function, null);
            }
        });
    }

    String getUniqueId(){
        return UUID.randomUUID().toString();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        pageExit = true;
        firebaseRef.child(createdBy).setValue(null);
        finish();
    }
}