package edu.buffalo.cse.cse486586.groupmessenger2;

import android.util.Log;

import java.util.HashMap;

public class Testing {
    enum msgType {PROPOSED_PRIORITY, AGREED_PRIORITY,ACTUAL_MSG};
    public static void main(String args[])
    {

/*        int msg = msgType.ACTUAL_MSG.ordinal() ;
        System.out.print(msg);
        int sentMsgSeqNo=23;
        String msgSeq = sentMsgSeqNo + "";
        int i = msgSeq.length();
        while(i<5)
        {
            msgSeq = "0" + msgSeq;
            i++;
        }*/
        //sendActualMsg("Hello");
        System.out.print(Integer.parseInt("0001"));
    }
    static void sendActualMsg(String msg)
    {
        int sentMsgSeqNo = 1;
        //sample Actual msg  "0
       // Log.e(TAG, "Sending Msg" + msg);
        //Log.e(TAG, "Sending Msg" + msg);
        String msgId = sentMsgSeqNo + "";
        int i = msgId.length();
        while(i < 5)
        {
            msgId = "0" + msgId;
            i++;
        }
        msgId = msgId + 11108;
        sentMsgSeqNo++;
        //fifoQueue[portNumberToIndex(Integer.parseInt(myPortNumber))]++;
       // msgRecevicedProposalMap.put(msgId, new HashMap<String, String>());
        String finalmsg = 0+msgId + msg;
        //sendMsgToAll(finalmsg);
        System.out.print(finalmsg);
        //Log.e(TAG, "Sending Final Msg" + finalmsg);
        // send finalmsg to all processes;

    }

}
