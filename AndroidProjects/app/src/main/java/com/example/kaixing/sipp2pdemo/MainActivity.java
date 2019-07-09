package com.example.kaixing.sipp2pdemo;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class IpUtil {
    public static final String regexCIp = "^192\\.168\\.(\\d{1}|[1-9]\\d|1\\d{2}|2[0-4]\\d|25\\d)\\.(\\d{1}|[1-9]\\d|1\\d{2}|2[0-4]\\d|25\\d)$";
    //匹配A类地址
    public static final String regexAIp = "^10\\.(\\d{1}|[1-9]\\d|1\\d{2}|2[0-4]\\d|25\\d)\\.(\\d{1}|[1-9]\\d|1\\d{2}|2[0-4]\\d|25\\d)\\.(\\d{1}|[1-9]\\d|1\\d{2}|2[0-4]\\d|25\\d)$";
    //匹配B类地址
    public static final String regexBIp = "^172\\.(1[6-9]|2\\d|3[0-1])\\.(\\d{1}|[1-9]\\d|1\\d{2}|2[0-4]\\d|25\\d)\\.(\\d{1}|[1-9]\\d|1\\d{2}|2[0-4]\\d|25\\d)$";


    public static String getHostIp() {
        String hostIp;
        Pattern ip = Pattern.compile("(" + regexAIp + ")|" + "(" + regexBIp + ")|" + "(" + regexCIp + ")");
        Enumeration<NetworkInterface> networkInterfaces = null;
        try {
            networkInterfaces = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException e) {
            e.printStackTrace();
        }
        Log.d("进不去","fuck");
        InetAddress address;
        //while(true){
        while (networkInterfaces.hasMoreElements()) {
            NetworkInterface networkInterface = networkInterfaces.nextElement();
            Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
            while (inetAddresses.hasMoreElements()) {
                address = inetAddresses.nextElement();
                String hostAddress = address.getHostAddress();
                Matcher matcher = ip.matcher(hostAddress);
                if (matcher.matches()) {
                    hostIp = hostAddress;
                    return hostIp;
                }

            }
        }
        return null;
    }
}

public class MainActivity extends AppCompatActivity implements SipMessageListener{
    private Button mSendButton;
    private TextView mNameText;
    private TextView mPortText;
    private TextView mContentText;
    private Handler mHandler;
    private static SipLayerFacade sipLayerFacade;

    private String mUserName;
    private String mIpAddress;
    private int mPort;

    private final int SHOW_MESSAGE = 1;
    private final int SHOW_LOG = 2;

    public void processReceivedMessage(String sender, String message){
        new AppendMessageThread("received message", true).start();
        new AppendMessageThread(sender + ": " + message, false).start();
    }
    public void processError(String errorMessage){

    }
    public void processInfo(String infoMessage){

    }

    class AppendMessageThread extends Thread{
        private String string;
        private boolean isLog;
        public AppendMessageThread(String string, boolean isLog){
            this.string = string;
            this.isLog = isLog;
        }

        public void run(){
            appendOnView(string, isLog);
        }
    }

    public void appendOnView(final String string, boolean isLog) {
        if(mHandler == null) {
            mHandler = new Handler(Looper.getMainLooper()) {
                @Override
                public void handleMessage(Message message) {
                    TextView textView = (TextView)findViewById(R.id.chatView);
                    String str = (String)message.obj;
                    switch(message.what){
                        case SHOW_MESSAGE:
                            //textView = (TextView)findViewById(R.id.chatView);
                            textView.append("\n" +str);
                            break;
                        case SHOW_LOG:
                            //textView = (TextView)findViewById(R.id.chatView);
                            textView.append("\n"+"LOG  ::::::::  "+str);
                            break;
                    }
                }
            };
        }
        Message message = new Message();
        if(isLog == true){
            message.what = SHOW_LOG;
        }
        else{
            message.what = SHOW_MESSAGE;
        }
        message.obj = string;
        mHandler.sendMessage(message);
    }

    //get local IP
    /*
    public static String getLocalIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf
                        .getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        return inetAddress.getHostAddress().toString();
                    }
                }
            }
        } catch (SocketException ex) {
            Log.e("WifiPrefIpAddress", ex.toString());
        }

        return null;
    }*/


    public static String getAssembledSipAddress(String name, String ip, int port){
        return name+"@"+ip+":"+ Integer.toString(port);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSendButton = findViewById(R.id.sendButton);
        mNameText = (TextView)findViewById(R.id.nameText);
        mPortText = (TextView)findViewById(R.id.portText);
        mContentText = (TextView)findViewById(R.id.contentText);

        //mUserName = mNameText.getText().toString();
        //mIpAddress = getLocalIpAddress();
        //mPort = Integer.parseInt(mPortText.getText().toString());
        Log.d("sao","sao");

        //create the sip layer facade
        IpUtil ipUtil = new IpUtil();
        final String ip = ipUtil.getHostIp();
        Log.d("ip",ip);
        try{
            sipLayerFacade = new SipLayerFacade("jack", ip, 8070);
        }
        catch (Exception e){
            Log.d("exception","create sip facade");
        }

        //set button listener
        mSendButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                String msg = mContentText.getText().toString();
                new AppendMessageThread(msg, true).start();

                try{
                    sipLayerFacade.sendMessage(getAssembledSipAddress("jack", ip, 8070), msg);
                }
                catch (Exception e){
                    Log.d("exception","send message");
                }
                new AppendMessageThread("Message sent.", true).start();


            }
        });

    }
}
