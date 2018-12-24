package ru.linkos.wifi_connector;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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
   private static Socket s;
    private static ServerSocket ss;
    private static InputStreamReader isr;
    private static BufferedReader br;
    private static PrintWriter pw;
    private static DatagramSocket ds;
    int priority = 5;
    String message = "gui";
    private static String ip = "10.1.7.230";
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




    public void checkWifi(View view){

        final TextInputLayout tw = findViewById(R.id.textInputLayout);
        final String ip = tw.getEditText().getText().toString();


        final Thread mthr = new Thread(new Runnable() {
            @Override
            public void run() {
                while (send) {
                    try {
                        mt.send(dss, ip, mt.makeProto());
                        mt.recieveUDP();
                       // switch (mt.parseProto(mt.recieveUDP())){
                         //   case 1: //Log.w("Connection", "Device is busy");
                           // priority = 1;
                            mt.send(dss, ip, mt.makeCreq());
                            mt.parseCrep(mt.recieveUDP());
                            mt.send(dss, ip, mt.makeSreq());
                            mt.parseSrep(mt.recieveUDP());
                            mt.send(dss,ip,mt.makeMreq());
                            mt.parseMrep(mt.recieveUDP());

                           // mt.parseSrep(mt.recieveUDP());
                           // send = false;
                           // break;

                           /* case 2:  // Log.w("Connection", "Device is busy");
                                priority = 1;
                                mt.send(dss, ip, mt.makeCreq());
                                mt.parseCrep(mt.recieveUDP());
                                mt.send(dss, ip, mt.makeSreq());
                                mt.parseSrep(mt.recieveUDP());
                                mt.parseSrep(mt.recieveUDP());
                                mt.send(dss,ip,mt.makeMreq());
                              //  mt.parseSrep(mt.recieveUDP());
                                //send = false;
                             //   break;
                        }*/

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






    }

class myTask extends AsyncTask<Void, Void, Void> {

    @Override
    protected Void doInBackground(Void... voids) {

        return null;
    }
    public DatagramSocket connect(){

        try {
            dss = new DatagramSocket(40050);
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
              //  Log.i("rdplength", String.valueOf(rdp.getLength()));

                byte[] data = rdp.getData();
                byte[] dataout = new byte [rdp.getLength()];
                System.arraycopy(data, 0, dataout, 0, rdp.getLength());


              //  Log.i("datalength", String.valueOf(data.length));

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


       // Log.i("byte", bytesToHex(incoming));



        GenericOuterClass.Generic input = GenericOuterClass.Generic.parseFrom(incoming);
        int gotmid = input.getMid();
        //Log.i("MID", String.valueOf(gotmid));
        //Log.i("hasSrep", String.valueOf(input.hasSrep()));
        GenericOuterClass.SREP srep = input.getSrep();


        //statusReport.g
       // Log.i("gotready", String.valueOf(srep.getReady()) + String.valueOf(srep.getBusy()));
        int statusOK = 0;
        if (srep.getReady()){
            statusOK = 1;
            if (srep.getBusy()){
                statusOK = 2;
            }
        }
      //  Log.i("Generic device status", String.valueOf(statusOK));
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
          //  Log.i("X positions: ", xposmin + "\t" + xposmax);
           // Log.i("Y positions: ", yposmin + "\t" + yposmax);
            byte[] hash = MessageDigest.getInstance("MD5").digest(incoming);
            dataList.clear();
            for (int i = 0; i < hash.length; i += 4) {
                int floatBits = hash[i] & 0xFF |
                        (hash[i + 1] & 0xFF) << 8 |
                        (hash[i + 2] & 0xFF) << 16 |
                        (hash[i + 3] & 0xFF) << 24;
                //float Data = Float.intBitsToFloat(floatBits);
//int intData = Math.round(Data);
dataList.add(floatBits);

//checksum  = mt.byteArrayToInt(MessageDigest.getInstance("MD5").digest(incoming));


            }
            //Log.i("Checksum", String.valueOf(dataList.size()));

        }
    }

    public void parseSrep(byte[] bytes) throws InvalidProtocolBufferException {


        GenericOuterClass.Generic input = GenericOuterClass.Generic.parseFrom(bytes);
        if (input.hasSrep()){
            GenericOuterClass.SREP srep = input.getSrep();
            Axs.SREP axsSrep = srep.getAxs();

            curXpos = axsSrep.getXposition();
            curYpos = axsSrep.getYposition();
           // Log.i("Status", String.valueOf(input.getMrep().getReady()) +  String.valueOf(input.getMrep().getBusy()));
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

    public byte[] makeMreq()  {

        GenericOuterClass.Generic.Builder gocB = GenericOuterClass.Generic.newBuilder();
        gocB.getDefaultInstanceForType();
        gocB.setMid(1398292803);
        GenericOuterClass.MREQ.Builder mreq = GenericOuterClass.MREQ.newBuilder();

        mreq.setMd5A(dataList.get(0));
        Log.i("MD5A", String.valueOf(dataList.get(0)));
        mreq.setMd5B(dataList.get(1));
        Log.i("MD5B", String.valueOf(dataList.get(1)));
        mreq.setMd5C(dataList.get(2));
        Log.i("MD5C", String.valueOf(dataList.get(2)));
        mreq.setMd5D(dataList.get(3));
        Log.i("MD5D", String.valueOf(dataList.get(3)));
        mreq.setPriority(0);


        Axs.MREQ.Builder axsMreq = Axs.MREQ.newBuilder();
       // axsMreq.setXposition(0);
        axsMreq.setXspeed(xspdmax/10);
       // axsMreq.setYposition(0);
        axsMreq.setYspeed(0);
        //axsMreq.build();
        mreq.setAxs(axsMreq.build());
        gocB.setMreq(mreq.build());
       byte[] mreqpacket = gocB.build().toByteArray();
        return mreqpacket;

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
            Log.i ("Status:", String.valueOf(mrep.getReady()) + "\t" + String.valueOf(mrep.getBusy()));
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

    dss =  mt.connect();


}

}
