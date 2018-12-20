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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import Linkos.RTC.Message.AXS.Axs;
import Linkos.RTC.Message.GenericOuterClass;


public class MainActivity extends AppCompatActivity {

   private static Socket s;
    private static ServerSocket ss;
    private static InputStreamReader isr;
    private static BufferedReader br;
    private static PrintWriter pw;
    private static DatagramSocket ds;
    String message = "gui";
    private static String ip = "10.1.7.230";
    myTask mt = new myTask();
    DatagramSocket dss;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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
                while (true) {
                    try {
                        mt.send(dss, ip, mt.makeProto());
                        //mt.recieveUDP();
                        mt.parseProto(mt.recieveUDP());

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        Thread.sleep(3000);
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
            dss = new DatagramSocket(8009);
        } catch (SocketException e) {
            e.printStackTrace();
        }

        return dss;
    }
    public void send(DatagramSocket ds, String ip, byte[] os){


        try {
            DatagramPacket dp = new DatagramPacket(os, os.length, InetAddress.getByName(ip), 8009);
            ds.send(dp);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public byte[] recieveUDP() throws IOException {
        //DatagramSocket rds = dss;
        byte[] buf = new byte[dss.getSendBufferSize()];
        DatagramPacket rdp = new DatagramPacket(buf, buf.length);
      //  while (true) {

                dss.receive(rdp);
                Log.i("rdplength", String.valueOf(rdp.getLength()));
                buf = new byte[rdp.getLength()];
                DatagramPacket rdp2 = new DatagramPacket(buf, buf.length);
                dss.receive(rdp2);
                //rdp.setLength(buf.length);
                byte[] data = rdp2.getData();


                Log.i("datalength", String.valueOf(data.length));
                Log.i("recieveddata", bytesToHex(data));
                return data;

            //Log.i("nl", String.valueOf(rdp2.getData().length));
            //String txt = "fuck";
            //return  data;
        //}
        //return  data;
        /*byte[] data = rdp.getData();

            try {
                rds.receive(rdp);
                //txt = "recieved";
                Log.i("Data recieved", data.toString());

            } catch (IOException e) {
                e.printStackTrace();
            }
            return data;*/


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

        GenericOuterClass.MREQ.Builder mmrqB = GenericOuterClass.MREQ.newBuilder();
        mmrqB.setMd5A(1);
        mmrqB.setMd5B(1);
        mmrqB.setMd5C(2);
        mmrqB.setMd5D(3);
        mmrqB.setPriority(1);
        gocB.setMreq(mmrqB);

        byte[] os = gocB.build().toByteArray();

        return os;


    }


    public void parseProto(byte[] incoming) throws InvalidProtocolBufferException {


        Log.i("byte", bytesToHex(incoming));



        GenericOuterClass.Generic input = GenericOuterClass.Generic.parseFrom(incoming);
        int gotmid = input.getMid();
        Log.i("MID", String.valueOf(gotmid));
        GenericOuterClass.SREP statusReport = GenericOuterClass.SREP.parseFrom(input.toByteArray());

        //statusReport.g
        Log.i("gotready", String.valueOf(statusReport.getReady()) + String.valueOf(statusReport.getBusy()));


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
