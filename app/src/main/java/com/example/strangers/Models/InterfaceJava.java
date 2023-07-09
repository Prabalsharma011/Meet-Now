package com.example.strangers.Models;

import android.webkit.JavascriptInterface;

import com.example.strangers.Activity.callActivity;

public class InterfaceJava {
    callActivity callActivity;
    public InterfaceJava(callActivity callActivity){
        this.callActivity=callActivity;
    }

    @JavascriptInterface
    public void onPeerConnected(){
        callActivity.onPeerConnected();
    }
}
