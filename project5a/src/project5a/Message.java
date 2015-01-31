package project5a;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author ssoomro
 */
public class Message implements Comparable<Message>{
    String id;
    String from;
    String to;
    int ts;
    String msg;
    String msgType;
    String desc;
    String event;
    MessageType msgTypeObj;
    
    
    public Message() {
        
    }
    
    public Message(String id_, String from_, String to_, int ts_, String msg_, String desc_, String event_) {
        id = id_;
        from = from_;
        to = to_;
        ts = ts_;
        msg = msg_;
        desc = desc_;
        event = event_;
        msgType = getMessageType(msg);
        msgTypeObj = new MessageType(event, msgType);
    }
    
    @Override
    public String toString() {
        String str = "";
        str += "NodeID:"+id+"\n";
        str += "Timestamp:"+ts+"\n";
        str += "Event:"+event+"\n";
        str += "From:"+from+"\n";
        str += "To:"+to+"\n";
        str += "Message:"+msg+"\n";
        str += "MessageType:"+msgType+"\n";
        str += "desc:"+desc+"\n";
        return str;
    }

    @Override
    public int compareTo(Message o) {
        if( this.ts > o.ts ) {
            return 1;
        } else if(this.ts < o.ts) {
            return -1;
        } else
            return 0;
    }

    private String getMessageType(String msg_) {
        if( msg_.indexOf("(") != -1 )
            return msg_.substring(0, msg_.indexOf("("));
        else 
            return msg_;
    }
}

