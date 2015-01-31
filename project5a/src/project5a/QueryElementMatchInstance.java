/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package project5a;

import java.util.ArrayList;

/**
 *
 * @author sarfaraz
 */
public class QueryElementMatchInstance {
    ExecUnit src;
    ExecUnit dst;
    String dstStr;
    ArrayList<Message> matchingMessages;
    Integer lastTs;
    
    public QueryElementMatchInstance( ExecUnit s ) {
        matchingMessages = new ArrayList<Message>();
        src = s;
    }
    
    public void addMessage(Message m) {
        matchingMessages.add(m);
        if( m.event.equals("SEND") ) {
            dstStr = m.to;
        }
        lastTs = m.ts;
    }
    
    public void addMessages(ArrayList<Message> msgs) {
        for(int i=0; i<msgs.size();i++) {
            addMessage(msgs.get(i));
        }
    }
    
    @Override
    public String toString() {
        String str = "*** ExU:"+src.nodeId+" ***\n";
        for(int i=0; i<matchingMessages.size();i++) {
            str += matchingMessages.get(i).toString()+"\n";
        }
        return str+"*** End Match ***";
    }
}
