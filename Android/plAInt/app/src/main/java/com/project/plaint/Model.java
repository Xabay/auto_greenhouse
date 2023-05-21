package com.project.plaint;

import android.os.Build;

import androidx.annotation.RequiresApi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;

import com.jjoe64.graphview.series.DataPoint;


@RequiresApi(api = Build.VERSION_CODES.O)
public class Model {

    //Class variables
    private Socket sock;

    //Globally stored instance
    private static Model instance;

    //Description: Instance getter
    public static Model getInstance() {
        return instance;
    }

    //Description: Instance setter
    //Parameters:  m - Instance to set to
    public static void setInstance(Model m) {
        instance = m;
    }

    //Description: Constructor, sets up TCP connection on socket
    //Exceptions:  Exception is thrown if unable to set up connection within given timeout
    public Model(String ip,  int port) throws Exception {
        boolean[] nocon = {false};
        Thread conT = new Thread(() -> {
            try {
                sock = new Socket();
                InetAddress inetAddress = InetAddress.getByName(ip);
                SocketAddress socketAddress = new InetSocketAddress(inetAddress, port);
                sock.connect(socketAddress, 5000);
            } catch (IOException e) {
                nocon[0] = true;
            }
        });

        conT.start();
        conT.join();

        if(nocon[0]) throw (new Exception());
    };


    //Description: Query module state
    //Params:      attr - Which corresponding module state to query, 'c' if current control mode is to be queried
    //Return:      Module state
    //Exceptions:  Exception thrown when connection loss is identified
    public boolean queryMan(String attr) throws Exception {
        String ans = contactPI("q;" + attr);
        if(ans.equals("nocon")) throw (new Exception("no query"));
        return (Integer.parseInt(ans) == 1);
    }

    //Description: Toggle module state
    //Params:      attr     - Attribute controlled by target module
    //             toggleTo - State to toggle to
    //Exceptions:  Exception thrown when connection loss is identified
    public void toggleMan(String attr, boolean toggleTo) throws Exception {
        String ans = contactPI("t;" + attr + ";" + (toggleTo ? "1": "0"));
        if(ans.equals("nocon")) throw (new Exception("no toggle"));
    }

    //Description: Switch control mode
    //Params:      isManual - Mode to switch to (true if manual, false if automatic)
    //Exceptions:  Exception is thrown when connection loss is identified
    public void switchMode(boolean isManual) throws Exception {
        String ans = contactPI("c;" + String.valueOf(isManual ? 1:0));
        if(ans.equals("nocon")) throw (new Exception("no switch"));
    }

    //Description: Get data for graph
    //Params:      attr - Attribute for which data should be collected
    //Returns:     Array of DataPoints, with x-s and y-s being measured times and measured values
    //Exceptions:  Exception is thrown when connection loss is identified
    public DataPoint[] getPlotData(String attr) throws Exception {
        String ans = contactPI("p;" + attr);

        if(ans.equals("nocon")) throw (new Exception("no plot"));
        String[] times_s =  ans.split(";")[0].split(",");
        String[] vals_s = ans.split(";")[1].split(",");
        DataPoint[] points = new DataPoint[vals_s.length];



        for(int i=0; i < vals_s.length; ++i) {
            points[i] = new DataPoint(Float.parseFloat(times_s[i]), Integer.parseInt(vals_s[i]) );
        }
        return points;
    }

    //Description: Get currently used goal value
    //Params:      attr - Attribute for which to get goal value
    //Returns:     The goal value
    //Exceptions:  Exception is thrown when connection loss is identified
    public int getAttr(String attr) throws Exception {
        String ans = contactPI( "g;" + attr);
        if(ans.equals("nocon")) throw (new Exception("no getstate"));
        return Integer.parseInt(ans);
    }

    //Description: Get last measured value
    //Params:      attr - Attribute for which to get current measurement
    //Returns:     The measured value
    //Exceptions:  Exception is thrown when connection loss is identified
    public int getVal(String attr) throws Exception {
        String ans = contactPI("v;" + attr);
        if(ans.equals("nocon")) throw (new Exception("no getval"));
        return Integer.parseInt(ans);
    }

    //Description: Set goal value to be used
    //Params:      attr - Attribute for which to set goal value
    //             goal - Goal value to be set
    //Exceptions:  Exception is thrown when connection loss is identified.
    public void setAttr(String attr, int goal) throws Exception {
        String ans = contactPI("s;" + attr + ";" + String.valueOf(goal));
        if(ans.equals("nocon")) throw (new Exception("no setval"));
    }

    //Description: Close TCP connection
    public void closeConection() {
        try {
            sock.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    //Description: Method used by previous methods to communicate through TCP socket
    //Params:      toSend - Message to send to raspberry PI
    //Returns:     The answer received to the message
    //             "nocon" if the connection is no longer functional
    private String contactPI(String toSend) {
        String[] message = {""};


        Thread comT = new Thread(() -> {
            try {
                OutputStreamWriter dout = new OutputStreamWriter(sock.getOutputStream(), StandardCharsets.UTF_8);
                BufferedReader din = new BufferedReader( new InputStreamReader(sock.getInputStream(), StandardCharsets.UTF_8));
                dout.write(toSend);
                dout.flush();
                //while (!din.ready()) {}
                int read_byte = din.read();
                if (read_byte == -1) {
                    return;
                }
                message[0] += (char) read_byte;
                while(din.ready()) {
                    message[0] +=  (char) din.read();
                }
            } catch (IOException i) {
                message[0] = "nocon";
            }
        });

        comT.start();
        try {
            comT.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (message[0].equals("")) message[0] = "nocon";
        return message[0];
    }

}
