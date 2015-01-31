/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package project5a;

import java.util.ArrayList;

/**
 * Corresponds to each query element in the query which is supposed to be an
 * execution unit in the system
 * @author sarfaraz
 */
public class QueryElement {
    int cntMsgSent;
    int cntMsgRec;
    //for keeping record of the distinct msg types required in this query element
    //this is useful to first query the corresponding ex.u in app for the 
    //presence of these message types
    ArrayList<MessageType> msgTypes;
    //true for ordered, false for unordered
    boolean ordered;
    //true if interleaving allowed, false if not
    boolean interleaved;
    //the actual messages required
    ArrayList<MessageType> msgs;
    
    //number of matches to look against this query elements
    //0 means unlimited, as much as possible
    int n=0;
    
    public QueryElement() {
        cntMsgSent=0;
        cntMsgRec=0;
        ordered=true;
        interleaved=false;
        msgTypes = new ArrayList<MessageType>();
        msgs = new ArrayList<MessageType>();
        n=0;
    }
    
    public void addMsg(MessageType mt) {
        if(mt.eventType.equals("SEND")) {
            cntMsgSent++;
        } else if(mt.eventType.equals("RECEIVE")) {
            cntMsgRec++;
        }
        
        if(msgTypes.contains(mt)==false) {
            msgTypes.add(mt);
        }
        
        msgs.add(mt);
    }

    void parseOptions(String options) {
        String[] optionsArr = options.split(",");
        for(String opt:optionsArr) {
            if(opt.equals("o")) {
                ordered=true;
            } else if(opt.equals("no")) {
                ordered=false;
            } else if(opt.equals("i")) {
                interleaved=true;
            } else if(opt.equals("ni")) {
                interleaved=false;
            } else if(opt.startsWith("l")) {
                n=Integer.parseInt(opt.substring(1));
            }
        }
    }
    
    @Override
    public String toString(){
        String str="";
        str += "ordered:"+(ordered?"true":"false");
        str += ", ";
        str += "interleaved:"+(interleaved?"true":"false");
        str += "\n";
        for(MessageType mt:msgs) {
            str += mt.toString()+"\n";
        }
        return str;
    }
}
