/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package project5a;

import java.util.ArrayList;

/**
 * this class will primarily be used to represent all the msgs per file
 * since a file in our case is per each actor/thread, this would represent all
 * the msg activity as an actor/node/thread i.e. all the msgs received/sent
 * at this node/thread/actor
 * @author ssoomro
 */
public class ExecUnit {
   ArrayList<Message> msgArr = new ArrayList<Message>();
   String nodeId;
   ArrayList<MessageType> msgTypeSentRec = new ArrayList<MessageType>();
   int cntMsgRec = 0;
   int cntMsgSent = 0;
   
   public ExecUnit(String id) {
       nodeId = id;
   }
   
   public void addMsg(Message m) {
       if( m.event.equals("RECEIVE") ) {
           cntMsgRec++;
       } else if( m.event.equals("SEND") ) {
           cntMsgSent++;
       }
       
      if( msgTypeSentRec.contains( m.msgTypeObj ) == false ) {
           msgTypeSentRec.add(m.msgTypeObj);
       }
       msgArr.add(m);
   }
   
   public int recMsgCnt() {
       return cntMsgRec;
   }
   
   public int sentMsgCnt() {
       return cntMsgSent;
   }

    public Boolean hasMsgType(MessageType msgType_) {
        return msgTypeSentRec.contains(msgType_);
    }
    
    public boolean chkMsgTypesPresent(ArrayList<MessageType> mts) {
        boolean check = true;
        for(int i=0;i<mts.size();i++) {
            check = check && hasMsgType(mts.get(i));
            if( !check ) {
                break;
            }
        }
        return check;
    }
    
    @Override 
    public boolean equals(Object o) {
        if(o instanceof ExecUnit) {
            ExecUnit me = (ExecUnit)o;
            if( this.nodeId.equals(me.nodeId) ) {
                return true;
            }
            return false;
        }
        return false;
    }
    
    @Override
    public int hashCode(){
        return (nodeId).hashCode();
    }    
}