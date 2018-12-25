package ru.linkos.wifi_connector;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Layout;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.protobuf.InvalidProtocolBufferException;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import Linkos.RTC.Message.AXS.Axs;
import Linkos.RTC.Message.GenericOuterClass;
import Linkos.RTC.Message.Range;


public class MainActivity extends AppCompatActivity {
boolean xPosLoop;
boolean yPosLoop;
double xposmin;
    double xposmax;
    double yposmin;
    double yposmax;
    double curXpos;
    double curYpos;
    double xspdmax;
    double yspdmax;
    List <Integer> dataList;
    boolean send;
    boolean targetspeedx;
    boolean targetspeedy;
    boolean targetpositivity;
   private static Socket s;
    private static ServerSocket ss;
    private static InputStreamReader isr;
    private static BufferedReader br;
    private static PrintWriter pw;
    private static DatagramSocket ds;
    int priority = 5;
    String message = "gui";
    private static String ip = "10.1.7.230";
    int stationport;
    myTask mt = new myTask();
    DatagramSocket dss;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        send = true;
        setContentView(R.layout.activity_main);
        dataList = new ArrayList<>();
        ConnectivityManager cm = (ConnectivityManager) getSystemService(this.CONNECTIVITY_SERVICE);
        NetworkInfo nw = null;
        if (cm != null) {
            nw = cm.getActiveNetworkInfo();

            if (nw.isConnected()) {
                Toast.makeText(MainActivity.this, "Connected", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(MainActivity.this, "Not Connected", Toast.LENGTH_LONG).show();
            }
        }
        else {
            Toast.makeText(MainActivity.this, "cm null", Toast.LENGTH_LONG).show();
        }
    }




    @SuppressLint({"ClickableViewAccessibility", "SetTextI18n"})
    public void checkWifi(View view){

        targetspeedx = false;
        targetspeedy = false;
        targetpositivity = false;
        View starter = findViewById(R.id.startingLayour);
        starter.setVisibility(View.INVISIBLE);
        View controls = findViewById(R.id.buttonLayout);
        controls.setVisibility(View.VISIBLE);
        final Thread mthr = new Thread(new Runnable() {
            @Override
            public void run() {
                while (send) {
                    try {
                        mt.send(dss, ip, mt.makeProto());
                        mt.recieveUDP();

                            mt.send(dss, ip, mt.makeCreq());
                            mt.parseCrep(mt.recieveUDP());
                            mt.send(dss, ip, mt.makeSreq());
                            mt.parseSrep(mt.recieveUDP());
                            Log.i("But", String.valueOf(targetpositivity) + String.valueOf(targetspeedx) + String.valueOf(targetspeedy));
                            mt.send(dss,ip,mt.makeMreq(2, targetspeedx, targetspeedy, targetpositivity));
                            mt.parseMrep(mt.recieveUDP());



                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (NoSuchAlgorithmException e) {
                        e.printStackTrace();
                    }
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            }
        });
        mthr.setDaemon(true);
        mthr.start();
        final TextView curPosit = findViewById(R.id.curPositions);

        final Handler switchHandler = new Handler();
        final Runnable runnable = new Runnable() {

            public void run() {
                curPosit.setText("X: " + String.valueOf(curXpos) + "\n" + "Y:" + String.valueOf(curYpos));
                        switchHandler.postDelayed(this, 1000);}

        };
        switchHandler.removeCallbacks(runnable);
        switchHandler.postDelayed(runnable, 1000);





        Button up = findViewById(R.id.upButton);
        Button right = findViewById(R.id.rightButton);
        Button down = findViewById(R.id.downButton);
        Button left = findViewById(R.id.leftButton);

        up.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch(event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        // PRESSED
                        targetspeedy = true;
                        targetpositivity = false;

                        return true; // if you want to handle the touch event
                    case MotionEvent.ACTION_UP:
                        // RELEASED
                        targetspeedy = false;

                        return true; // if you want to handle the touch event
                }
                return false;
            }
        });
        down.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch(event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        // PRESSED
                        targetspeedy = true;
                        targetpositivity = true;
                        return true; // if you want to handle the touch event
                    case MotionEvent.ACTION_UP:
                        // RELEASED
                        targetspeedy = false;
                        return true; // if you want to handle the touch event
                }
                return false;
            }
        });
        left.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch(event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        // PRESSED
                        targetspeedx = true;
                        targetpositivity = false;
                        return true; // if you want to handle the touch event
                    case MotionEvent.ACTION_UP:
                        // RELEASED
                        targetspeedx = false;
                        return true; // if you want to handle the touch event
                }
                return false;
            }
        });
        right.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch(event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        // PRESSED
                        targetspeedx = true;
                        targetpositivity = true;
                        return true; // if you want to handle the touch event
                    case MotionEvent.ACTION_UP:
                        // RELEASED
                        targetspeedx = false;
                        return true; // if you want to handle the touch event
                }
                return false;
            }
        });


        /*while (true) {

        }*/


    }

class myTask extends AsyncTask<Void, Void, Void> {

    @Override
    protected Void doInBackground(Void... voids) {

        return null;
    }
    public DatagramSocket connect(String targetPort){

        try {
            dss = new DatagramSocket(Integer.decode(targetPort));
        } catch (SocketException e) {
            e.printStackTrace();
        }

        return dss;
    }
    public void send(DatagramSocket ds, String ip, byte[] os){


        try {
            DatagramPacket dp = new DatagramPacket(os, os.length, InetAddress.getByName(ip), 40050);
            ds.send(dp);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public byte[] recieveUDP() throws IOException {

        byte[] buf = new byte[dss.getSendBufferSize()];
        DatagramPacket rdp = new DatagramPacket(buf, buf.length);


                dss.receive(rdp);


                byte[] data = rdp.getData();
                byte[] dataout = new byte [rdp.getLength()];
                System.arraycopy(data, 0, dataout, 0, rdp.getLength());




                return dataout;





    }


    public byte[] makeProto () throws IOException {

        GenericOuterClass.Generic.Builder gocB = GenericOuterClass.Generic.newBuilder();

        gocB.getDefaultInstanceForType();
        gocB.setMid(1398292803);


        GenericOuterClass.SREQ.Builder srqB = GenericOuterClass.SREQ.newBuilder();
        srqB.getDefaultInstanceForType();
        gocB.setSreq(srqB);
        srqB.clear();
        gocB.setSreq(srqB);



        byte[] os = gocB.build().toByteArray();

        return os;


    }


    public int parseProto(byte[] incoming) throws InvalidProtocolBufferException {






        GenericOuterClass.Generic input = GenericOuterClass.Generic.parseFrom(incoming);
        int gotmid = input.getMid();

        GenericOuterClass.SREP srep = input.getSrep();



        int statusOK = 0;
        if (srep.getReady()){
            statusOK = 1;
            if (srep.getBusy()){
                statusOK = 2;
            }
        }

        return statusOK;
    }


    public byte[] makeCreq() {

        GenericOuterClass.Generic.Builder gocB = GenericOuterClass.Generic.newBuilder();

        gocB.getDefaultInstanceForType();
        gocB.setMid(1398292803);
        GenericOuterClass.CREQ.Builder creq = GenericOuterClass.CREQ.newBuilder();
        creq.getDefaultInstanceForType();
        gocB.setCreq(creq);

        byte[] creqpacket = gocB.build().toByteArray();
        return creqpacket;

    }

    public void parseCrep(byte[] incoming) throws InvalidProtocolBufferException, NoSuchAlgorithmException {
        GenericOuterClass.Generic input = GenericOuterClass.Generic.parseFrom(incoming);
        if (input.hasCrep()) {
            GenericOuterClass.CREP crep = input.getCrep();
            Axs.CREP AxsCrep = crep.getAxs();

            xPosLoop = AxsCrep.getXpositionLoop();
            yPosLoop = AxsCrep.getYpositionLoop();
            Range.range_d rangex = AxsCrep.getXposition();
            Range.range_d rangey = AxsCrep.getYposition();
            Range.range_d spdx = AxsCrep.getXspeed();
            Range.range_d spdy = AxsCrep.getYspeed();
            xposmin = rangex.getMin();
            xposmax = rangex.getMax();
            yposmin = rangey.getMin();
            yposmax = rangey.getMax();
            xspdmax = spdx.getMax();
            yspdmax = spdy.getMax();

            byte[] hash = MessageDigest.getInstance("MD5").digest(incoming);
            dataList.clear();
            for (int i = 0; i < hash.length; i += 4) {
                int floatBits = hash[i] & 0xFF |
                        (hash[i + 1] & 0xFF) << 8 |
                        (hash[i + 2] & 0xFF) << 16 |
                        (hash[i + 3] & 0xFF) << 24;

dataList.add(floatBits);




            }


        }
    }

    public void parseSrep(byte[] bytes) throws InvalidProtocolBufferException {


        GenericOuterClass.Generic input = GenericOuterClass.Generic.parseFrom(bytes);
        if (input.hasSrep()){
            GenericOuterClass.SREP srep = input.getSrep();
            Axs.SREP axsSrep = srep.getAxs();

            curXpos = axsSrep.getXposition();
            curYpos = axsSrep.getYposition();

            Log.i ("Cur pos:", curXpos + "\t" + curYpos);
        }
    }

    public byte[] makeSreq() {

        GenericOuterClass.Generic.Builder gocB = GenericOuterClass.Generic.newBuilder();

        gocB.getDefaultInstanceForType();
        gocB.setMid(1398292803);
        GenericOuterClass.SREQ.Builder sreq = GenericOuterClass.SREQ.newBuilder();
        sreq.getDefaultInstanceForType();
        gocB.setSreq(sreq);

        byte[] sreqpacket = gocB.build().toByteArray();
        return sreqpacket;


    }

    public byte[] makeMreq(int Kspeed, boolean x, boolean y, boolean up)  {

        GenericOuterClass.Generic.Builder gocB = GenericOuterClass.Generic.newBuilder();
        gocB.getDefaultInstanceForType();
        gocB.setMid(1398292803);
        GenericOuterClass.MREQ.Builder mreq = GenericOuterClass.MREQ.newBuilder();

        mreq.setMd5A(dataList.get(0));
       // Log.i("MD5A", String.valueOf(dataList.get(0)));
        mreq.setMd5B(dataList.get(1));
        //Log.i("MD5B", String.valueOf(dataList.get(1)));
        mreq.setMd5C(dataList.get(2));
        //Log.i("MD5C", String.valueOf(dataList.get(2)));
        mreq.setMd5D(dataList.get(3));
        //Log.i("MD5D", String.valueOf(dataList.get(3)));
        mreq.setPriority(0);


        Axs.MREQ.Builder axsMreq = Axs.MREQ.newBuilder();
        double xpseed = xspdmax / Kspeed;
        double yspeed = yspdmax / Kspeed;
        if (x){
            if (up){
                axsMreq.setXspeed(xpseed);
            }
            else {
                axsMreq.setXspeed(-xpseed);
            }
        }
        else {
            axsMreq.setXspeed(0);
        }
        if (y){
            if (up){
                axsMreq.setYspeed(yspeed);
            }
            else {
                axsMreq.setYspeed(-yspeed);
            }
        }
        else {
            axsMreq.setYspeed(0);
        }

        mreq.setAxs(axsMreq.build());
        gocB.setMreq(mreq.build());
        return gocB.build().toByteArray();

    }





    public int byteArrayToInt(byte[] b)
    {
        return   b[3] & 0xFF |
                (b[2] & 0xFF) << 8 |
                (b[1] & 0xFF) << 16 |
                (b[0] & 0xFF) << 24;
    }

    public void parseMrep(byte[] bytes) throws InvalidProtocolBufferException {



        GenericOuterClass.Generic input = GenericOuterClass.Generic.parseFrom(bytes);
        if (input.hasMrep()){
            GenericOuterClass.SREP mrep = input.getMrep();


            // Log.i("Status", String.valueOf(input.getMrep().getReady()) +  String.valueOf(input.getMrep().getBusy()));
          //  Log.i ("Status:", String.valueOf(mrep.getReady()) + "\t" + String.valueOf(mrep.getBusy()));
        }
        else {
            Log.i ("Status:", "No mrep");
        }

    }
}

    private final static char[] hexArray = "0123456789ABCDEF".toCharArray();
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }
public void setConnection(View view){

        TextInputLayout portInput = findViewById(R.id.portInput);
        TextInputLayout ipInput = findViewById(R.id.textInputLayout);
        ip = ipInput.getEditText().getText().toString();

    dss =  mt.connect(portInput.getEditText().getText().toString());


}

}
