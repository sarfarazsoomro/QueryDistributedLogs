/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package project5a;

import java.util.ArrayList;

/**
 *
 * @author ssoomro
 */
public class Query {
    String q;
    int maxResults=0;
    ArrayList<QueryElement> qStruct = new ArrayList<QueryElement>();
    //contains all the msg types required in the query, usefull for checking
    //if all these msgs are present in the system as well or not
    ArrayList<MessageType> msgTypes = new ArrayList<MessageType>();
    
    public Query(String q_) {
        q = q_;
        parseQuery(q);
    }

    private void parseQuery(String q_) {
        if(q_.indexOf("$")>0) {
            String globalOptions = q_.substring(q_.indexOf("$")+1).trim();
            if( globalOptions.startsWith("l") ) {
                maxResults = Integer.parseInt(globalOptions.substring(1));
            }
            q_ = q_.substring(0, q_.indexOf("$")).trim();
        }
                
        String[] exUs = q_.trim().split("\\|");
        if(exUs.length > 0) {
            //create an entry in the qStruct for each section of query
            //this each section represents the ops on each execution unit
            for(int i = 0; i<exUs.length; i++) {
                //qStruct.add(new ArrayList<MessageType>());
                qStruct.add(new QueryElement());
                //now each section would specify what needs to be checked for
                //that exec. unit. So now we have to split each section now
                String[] secs = exUs[i].trim().split(" ");
                //check if options were specified, the options are specified using
                //;(semicolon). eg.: >m1 >m2 ;no,ni|>m3 m4> ;o,ni
                //the options are specified for each query element
                //the options are
                //ordered o (default)
                //not ordered no
                //interleaved i
                //not interleaved ni (default)
                if(secs[secs.length-1].startsWith(";")) {
                    String options = secs[secs.length-1].substring(1).trim();
                    qStruct.get(i).parseOptions(options);
                    
                    //get rid of the options part so only required msgs are left 
                    //in the array
                    String[] tempSecs= new String[secs.length-1];
                    System.arraycopy(secs, 0, tempSecs, 0, secs.length-1);
                    secs = tempSecs;
                }
                
                //now process the regular msgs required
                if( secs.length > 0 ) {
                    for (int j=0; j<secs.length; j++) {
                        MessageType tmpQE = parseMessageTypeFromString(secs[j].trim());
                        qStruct.get(i).addMsg(tmpQE);
                        if( msgTypes.contains(tmpQE) == false ) {
                            msgTypes.add(tmpQE);
                        }
                    }
                }
            }
        }
    }
    
    private MessageType parseMessageTypeFromString(String qe) {
        MessageType qEv;
        if( qe.indexOf(">") == 0 ) {
            //it's a receive event
            qEv = new MessageType("RECEIVE", qe.substring(1));
        } else if( qe.indexOf(">") == qe.length()-1 ) {
            //it's a send event
            qEv = new MessageType("SEND", qe.substring(0, qe.length()-1));
        } else
            qEv = null;
        return qEv;
    }
    
    @Override
    public String toString() {
        String str = "";
        for(int i=0; i<qStruct.size(); i++) {
            System.out.println("ExU "+(i+1));
            System.out.println(qStruct.get(i).toString());
        }
        return str;
    }
    
    public int numDistinctExecUnits() {
        return qStruct.size();
    }
}
