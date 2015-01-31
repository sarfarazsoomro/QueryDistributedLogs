/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package project5a;

/**
 *
 * @author ssoomro
 */
public class MessageType {
    String eventType;
    String msgType;
    
    public MessageType(String et, String mt) {
        eventType = et;
        msgType = mt;
    }

    @Override
    public String toString() {
        return eventType+":"+msgType;
    }
    
    @Override 
    public boolean equals(Object o) {
        if(o instanceof MessageType) {
            MessageType me = (MessageType)o;
            if( this.eventType.equals(me.eventType) && this.msgType.equals(me.msgType) ) {
                return true;
            }
            return false;
        }
        return false;
    }
    
    @Override
    public int hashCode(){
        return (eventType+msgType).hashCode();
    }
}
