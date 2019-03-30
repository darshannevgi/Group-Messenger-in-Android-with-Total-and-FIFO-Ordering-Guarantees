package edu.buffalo.cse.cse486586.groupmessenger2;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

/**
 * GroupMessengerActivity is the main Activity for the assignment.
 * 
 * @author stevko
 *
 */
public class GroupMessengerActivity extends Activity {
    static final String TAG = GroupMessengerActivity.class.getSimpleName();
    static final String[] REMOTE_PORTS = {"11108","11112","11116", "11120","11124" };
    static final int SERVER_PORT = 10000;
    int recAgreedMsg = 0;

    //Mycode
PriorityQueue<PQElement>  pq = new PriorityQueue<PQElement>(100,new PQElementComparator());
int sentMsgSeqNo = 1;
int fifoQueue[] = new int[5];
HashMap<String, HashMap<String,String>> msgRecevicedProposalMap = new HashMap<String, HashMap<String,String>>();
int deliverySeq_no = 0;
int highestProposalNumber = 0;
//TODO and Assumption that each process will send only one proposal
int [] proposalMap = new int[]{-1,-1,-1,-1};
    private String myPortNumber;
// TODO data strurcture for FIFO ordering (Process Seq_no table)

enum msgType {ACTUAL_MSG, PROPOSED_PRIORITY, AGREED_PRIORITY,};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_messenger);

        //MyCode
        /*
         * Calculate the port number that this AVD listens on.
         * It is just a hack that I came up with to get around the networking limitations of AVDs.
         * The explanation is provided in the PA1 spec.
         */
        TelephonyManager tel = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        String portStr = tel.getLine1Number().substring(tel.getLine1Number().length() - 4);
        final String myPort = String.valueOf((Integer.parseInt(portStr) * 2));
        myPortNumber =myPort;

        try {
            /*
             * Create a server socket as well as a thread (AsyncTask) that listens on the server
             * port.
             *
             * AsyncTask is a simplified thread construct that Android provides. Please make sure
             * you know how it works by reading
             * http://developer.android.com/reference/android/os/AsyncTask.html
             */
            ServerSocket serverSocket = new ServerSocket(SERVER_PORT);
            new ServerTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, serverSocket);
        } catch (IOException e) {
            /*
             * Log is a good way to debug your code. LogCat prints out all the messages that
             * Log class writes.
             *
             * Please read http://developer.android.com/tools/debugging/debugging-projects.html
             * and http://developer.android.com/tools/debugging/debugging-log.html
             * for more information on debugging.
             */
            Log.e(TAG, "Can't create a ServerSocket");
        }

        final TextView tv = (TextView) findViewById(R.id.textView1);
        tv.setMovementMethod(new ScrollingMovementMethod());

        /*
         * Registers OnPTestClickListener for "button1" in the layout, which is the "PTest" button.
         * OnPTestClickListener demonstrates how to access a ContentProvider.
         */
        findViewById(R.id.button1).setOnClickListener(
                new OnPTestClickListener(tv, getContentResolver()));
        final EditText editText = (EditText) findViewById(R.id.editText1);

        findViewById(R.id.button4).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String msg = editText.getText().toString() + "\n";
                editText.setText(""); // This is one way to reset the input box.
                tv.append("\t" + msg); // This is one way to display a string.
                TextView remoteTextView = (TextView) findViewById(R.id.textView2);
                remoteTextView.append("\n");

                /*
                 * Note that the following AsyncTask uses AsyncTask.SERIAL_EXECUTOR, not
                 * AsyncTask.THREAD_POOL_EXECUTOR as the above ServerTask does. To understand
                 * the difference, please take a look at
                 * http://developer.android.com/reference/android/os/AsyncTask.html
                 */
                new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, msg, myPort);
            }
        });
    }

    /***
     * ServerTask is an AsyncTask that should handle incoming messages. It is created by
     * ServerTask.executeOnExecutor() call in SimpleMessengerActivity.
     *
     * Please make sure you understand how AsyncTask works by reading
     * http://developer.android.com/reference/android/os/AsyncTask.html
     *
     * @author stevko
     *
     */
    private class ServerTask extends AsyncTask<ServerSocket, String, Void> {

        @Override
        protected Void doInBackground(ServerSocket... sockets) {
            ServerSocket serverSocket = sockets[0];
            int seq_no = 0;
            while(true)
            {
                try {

/*
                    //Working code
                    Log.e(TAG, "Inside While");
                    Socket client = serverSocket.accept();
                    BufferedReader br = new BufferedReader(new InputStreamReader(client.getInputStream()));
                    String msg;
                    Log.e(TAG, Hello"Before Reading");
                    while ((msg = br.readLine()) != null) {
                        Log.e(TAG, "After Reading");
                        publishProgress(msg);
                        br.close();
                        break;
                    }
                    ContentValues keyValueToInsert = new ContentValues();
                    // inserting <”key-to-insert”, “value-to-insert”>
                    Log.e(TAG, "Inserting Key:" + seq_no);
                    Log.e(TAG, "Inserting Value:" + msg);
                    keyValueToInsert.put("key" , seq_no+"");
                    keyValueToInsert.put("value", msg);
                    Uri mUri = buildUri("content", "edu.buffalo.cse.cse486586.groupmessenger2.provider");
                    Log.e(TAG, "URI created with value" + mUri);
                    if(mUri == null)
                        Log.e(TAG, "Wrong URI formation failed");
                    Log.e(TAG, "Inserting Value" + mUri);
                    Uri mUri1 = getContentResolver().insert(mUri,    // assume we already created a Uri object with our provider URI
                            keyValueToInsert);
                    Log.e(TAG, "Insertion URI created with value" + mUri1);
                    if(mUri1 == null)
                        Log.e(TAG, "Insertion failed");
                    seq_no++;
                    Log.e(TAG, "Insertion Successful");
                    PrintWriter pw = new PrintWriter(client.getOutputStream(),true);
                    Log.e(TAG, "PrintWriter Object Created");
                    pw.println("Hi");*/

                    Socket client = serverSocket.accept();
                    BufferedReader br = new BufferedReader(new InputStreamReader(client.getInputStream()));
                    String msg;
                   // Log.e(TAG, "Before Reading");
                    while ((msg = br.readLine()) != null) {
                     //   Log.e(TAG, "After Reading");
                     //   Log.e(TAG, "Insertion Successful");
                        /*PrintWriter pw = new PrintWriter(client.getOutputStream(),true);
                    //    Log.e(TAG, "PrintWriter Object Created");
                        pw.println("Hi");*/
                        if(msg.charAt(0) == '0')
                        {
                            Log.e(TAG, "Publishing Message to UI");
                            publishProgress(msg.substring(7));
                            handleActualMsg(msg);


                        }
                        else if(msg.charAt(0) == '1')
                        {
                            handleProposedPriorityMsg(msg);
                        }
                        else if(msg.charAt(0) == '2')
                        {
                            handleAgreedPriorityMsg(msg);
                        }

                        br.close();

                        break;
                    }

                }catch (IOException e)
                {
                    Log.e(TAG, "IO Exception Occured at Server");
                }
            }
        }

        /**
         * buildUri() demonstrates how to build a URI for a ContentProvider.
         *
         * @param scheme
         * @param authority
         * @return the URI
         */
        private Uri buildUri(String scheme, String authority) {
            Uri.Builder uriBuilder = new Uri.Builder();
            uriBuilder.authority(authority);
            uriBuilder.scheme(scheme);
            return uriBuilder.build();
        }

        protected void onProgressUpdate(String...strings) {
            /*
             * The following code displays what is received in doInBackground().
             */
            String strReceived = strings[0].trim();
            TextView remoteTextView = (TextView) findViewById(R.id.textView2);
            remoteTextView.append(strReceived + "\t\n");
            TextView localTextView = (TextView) findViewById(R.id.textView1);
            localTextView.append("\n");

            /*
             * The following code creates a file in the AVD's internal storage and stores a file.
             *
             * For more information on file I/O on Android, please take a look at
             * http://developer.android.com/training/basics/data-storage/files.html
             */

            String filename = "SimpleMessengerOutput";
            String string = strReceived + "\n";
            FileOutputStream outputStream;

            try {
                outputStream = openFileOutput(filename, Context.MODE_PRIVATE);
                outputStream.write(string.getBytes());
                outputStream.close();
            } catch (Exception e) {
                Log.e(TAG, "File write failed");
            }

            return;
        }
    }




    /***
     * ClientTask is an AsyncTask that should send a string over the network.
     * It is created by ClientTask.executeOnExecutor() call whenever OnKeyListener.onKey() detects
     * an enter key press event.
     *
     * @author stevko
     *
     */
    private class ClientTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... msgs) {
            /*try {
                for (int  i = 0 ; i < 5;i++) {
                    Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                            Integer.parseInt(REMOTE_PORTS[i]));
                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    String inputLine;

                    String msgToSend = msgs[0];
                    PrintWriter pw = new PrintWriter(socket.getOutputStream(), true);
                    pw.println(msgToSend);
                    while ((inputLine = in.readLine()) != null) {
                        if (inputLine.equals("Hi")) {
                            socket.close();
                            pw.close();
                            in.close();
                        }
                    }
                }

            } catch (UnknownHostException e) {
                Log.e(TAG, "ClientTask UnknownHostException");
            } catch (IOException e) {
                Log.e(TAG, "ClientTask socket IOException");
            }
*/
            sendActualMsg(msgs[0]);
            return null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_group_messenger, menu);
        return true;
    }

    void sendMsgToAll(String msg)
    {
        try {
            //Log.e(TAG, "Sending Message" + msg +  "to all process");
            for (int  i = 0 ; i < 5;i++) {
                Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                        Integer.parseInt(REMOTE_PORTS[i]));
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String inputLine;

                String msgToSend = msg;
                PrintWriter pw = new PrintWriter(socket.getOutputStream(), true);
                pw.println(msgToSend);

                /*while ((inputLine = in.readLine()) != null) {
                    if (inputLine.equals("Hi")) {
                        socket.close();
                        pw.close();
                        in.close();
                    }
                }*/
            }

        } catch (UnknownHostException e) {
            Log.e(TAG, "ClientTask UnknownHostException");
        } catch (IOException e) {
            Log.e(TAG, "ClientTask socket IOException");
        }
    }

    void sendMsgToProcess(String msg,int portNumber)
    {
        try {
              //  Log.e(TAG, "Sending Message" + msg +  "to process" + portNumber);
                Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                        portNumber);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String inputLine;

                String msgToSend = msg;
                PrintWriter pw = new PrintWriter(socket.getOutputStream(), true);
                pw.println(msgToSend);
   /*             while ((inputLine = in.readLine()) != null) {
                    if (inputLine.equals("Hi")) {
                        socket.close();
                        pw.close();
                        in.close();
                    }
                }*/

        } catch (UnknownHostException e) {
            Log.e(TAG, "ClientTask UnknownHostException");
        } catch (IOException e) {
            Log.e(TAG, "ClientTask socket IOException");
        }
    }

    void sendActualMsg(String msg)
    {
        //sample Actual msg  "0
       // Log.e(TAG, "Sending Msg" + msg);
        String msgId = sentMsgSeqNo + "";
        int i = msgId.length();
        while(i < 5)
        {
            msgId = "0" + msgId;
            i++;
        }
        msgId = msgId + portNumberToIndex(Integer.parseInt(myPortNumber));
        sentMsgSeqNo++;
        //fifoQueue[portNumberToIndex(Integer.parseInt(myPortNumber))]++;
        msgRecevicedProposalMap.put(msgId, new HashMap<String, String>());
        String finalmsg = 0+msgId + msg;
        sendMsgToAll(finalmsg);
       // Log.e(TAG, "Sending Final Msg" + finalmsg);
        // send finalmsg to all processes;
    }

    void sendProposalMsg(int proposalNumber, String receiverPortNo , String msg_id)
    {
        // get portNumber of sender
        //calculate proposal number
        String finalmsg = 1 + msg_id + portNumberToIndex(Integer.parseInt(myPortNumber))+  proposalNumber;
       // Log.e(TAG, "Sending Proposal Msg" + finalmsg);
        sendMsgToProcess(finalmsg, Integer.parseInt(receiverPortNo));
        //send finalmsg receiverPortNo
    }

    void sendAgreedMsg(String msgId, int proposer, int agreedPrio)
    {
        String finalmsg = 2 + msgId + proposer + agreedPrio;
       // Log.e(TAG, "Sending Agreed Msg" + finalmsg);
        sendMsgToAll(finalmsg);
        //send finalmsg to all processes
    }



    private void handleActualMsg(String msg)
    {
        Log.e(TAG, "Handling Actual Msg " + msg);
        String  msgId  =  msg.substring(1,7);
        highestProposalNumber++;
        PQElement pe = new PQElement(msg.substring(7),false,msgId,highestProposalNumber+"",portNumberToIndex(Integer.parseInt(myPortNumber))+"",false);
        pq.add(pe);
        String receiver_Port =  REMOTE_PORTS[Integer.parseInt(msg.substring(6,7))];
        sendProposalMsg(highestProposalNumber,receiver_Port, msgId);
    }

    private void handleProposedPriorityMsg(String msg) {
        Log.e(TAG, "Handling Proposed Msg" + msg);
        String  proposalNumber  =  msg.substring(8);
        String proposer = msg.substring(7,8);

        String msg_id = msg.substring(1,7);
        HashMap<String,String> processProposalMap = msgRecevicedProposalMap.get(msg_id);
        processProposalMap.put(proposer,proposalNumber);
        int agreed = 0;
        int maxProposer = 0;
        if(processProposalMap.size() == 5)
        {
            Iterator<Map.Entry<String, String>> itr = processProposalMap.entrySet().iterator();
            while(itr.hasNext())
            {
                Map.Entry<String, String> elememt = itr.next();
                int agg =  Integer.parseInt(elememt.getValue());
                int prop =  Integer.parseInt(elememt.getKey());
                if(agg > agreed)
                {
                    agreed = agg;
                    maxProposer = prop;
                }
                else if(agg == agreed)
                {
                    if(prop > maxProposer)
                        maxProposer = prop;
                }
            }
            sendAgreedMsg(msg_id,maxProposer,agreed);
            //send agreed priority to all processes
        }
        else
        msgRecevicedProposalMap.put(msg_id,processProposalMap);
    }

    private void handleAgreedPriorityMsg(String msg) {
        Log.e(TAG, "Agreed Msg Count" + (++recAgreedMsg));
        Log.e(TAG, "Agreed Msg " + msg);
        String  proposer  =  msg.substring(7);
        String  agreedPrio  =  msg.substring(8);
        highestProposalNumber = Math.max (highestProposalNumber, Integer.parseInt(agreedPrio));
        String msg_id = msg.substring(1,7);
        Log.e(TAG, "Making Deliverable " + msg_id);
        Iterator<PQElement> itr = pq.iterator();
        while(itr.hasNext())
        {
            PQElement element = itr.next();
            if(element.msgId.equals(msg_id))
            {
                Log.e(TAG, "Found Msg and made it Deliverable " + msg_id);
                pq.remove(element);
                PQElement newElement = new PQElement(element.msg,true,element.msgId,agreedPrio,proposer,false);
                pq.add(newElement);
                break;
            }
            //do Something with Each element
        }
        while(!pq.isEmpty() && pq.peek().isDeliverable)
        {
           String msgId =  pq.peek().msgId;
           String senderPort =  msgId.substring(5);
            String msgNumber =  msgId.substring(0,5);
            Log.e(TAG, "SenderPortNumber: " + senderPort + " MsgNumber : " + msgNumber);
            int current =fifoQueue[Integer.parseInt(senderPort)];
            int received = Integer.parseInt(msgNumber);
            Log.e(TAG, "Current: " + current + " Received : " + received);
           if(current == (received - 1))
            {
                fifoQueue[Integer.parseInt(senderPort)]++;
                deliverMsg(pq.poll().msg);
            }
            else
           {
               Log.e(TAG, "Buffering Msg" + msg);
               break;
           }

        }
        //search for msg entry in PQ , update proposed/Agreed entry(Key of PQ) which will shuffle PQ order
        //Mark msg as deliverable
        //if msg reach to top of queue deliverMsg()

    }

    private void deliverMsg(String msg)
    {
        Log.e(TAG, "Delivering Msg" + msg);
        ContentValues keyValueToInsert = new ContentValues();

        // inserting <”key-to-insert”, “value-to-insert”>
       // Log.e(TAG, "Inserting Key:" + deliverySeq_no);
       // Log.e(TAG, "Inserting Value:" + msg);
        keyValueToInsert.put("key" , deliverySeq_no+"");
        keyValueToInsert.put("value", msg);
        Uri mUri = buildUri("content", "edu.buffalo.cse.cse486586.groupmessenger2.provider");
        //Log.e(TAG, "URI created with value" + mUri);
        if(mUri == null)
            Log.e(TAG, "Wrong URI formation failed");
       // Log.e(TAG, "Inserting Value" + mUri);
        Uri mUri1 = getContentResolver().insert(mUri,    // assume we already created a Uri object with our provider URI
                keyValueToInsert);
       // Log.e(TAG, "Insertion URI created with value" + mUri1);
        if(mUri1 == null)
            Log.e(TAG, "Insertion failed");
        deliverySeq_no++;

    }
    private int portNumberToIndex(int portNumber)
    {

        for(int i = 0 ; i < REMOTE_PORTS.length;i++)
        {
            if(Integer.parseInt(REMOTE_PORTS[i]) == portNumber)
                return i;
        }
        return 0;

    }

    private Uri buildUri(String scheme, String authority) {
        Uri.Builder uriBuilder = new Uri.Builder();
        uriBuilder.authority(authority);
        uriBuilder.scheme(scheme);
        return uriBuilder.build();
    }


}

class PQElement
{
    boolean isDeliverable;
    String msgId;
    String proposedPriority, msgIdProposer;
    boolean isAgreed;
    String msg;

    public PQElement(String msg, boolean isDeliverable, String msgId, String proposedPriority, String msgIdProposer, boolean isAgreed) {

        this.msg = msg;
        this.isDeliverable = isDeliverable;
        this.msgId = msgId;
        this.proposedPriority = proposedPriority;
        this.msgIdProposer = msgIdProposer;
        this.isAgreed = isAgreed;
    }
}
//My Name is Darshan
class PQElementComparator implements Comparator<PQElement>
{
    @Override
    public int compare(PQElement e1, PQElement e2)
    {
        String a1= e1.proposedPriority;
        String a2= e2.proposedPriority;
       // Log.e("PQElementComparator", "Compared " + a1 + "And " + a2);
        //System.out.println("A2"+a2 + " A1" + a1);
        if(!a1.equals(a2))
            return Integer.parseInt(a1) - Integer.parseInt(a2) ;
        return Integer.parseInt(e1.msgIdProposer) - Integer.parseInt(e2.msgIdProposer);
    }
    
}


