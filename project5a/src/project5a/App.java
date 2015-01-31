/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package project5a;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author ssoomro
 */
public class App {
    ArrayList<ExecUnit> execUnits = new ArrayList<ExecUnit>();
    Map<MessageType, ArrayList<ExecUnit>> msgToExU = new HashMap<MessageType, ArrayList<ExecUnit>>();
    Map<String, ExecUnit> strToExU = new HashMap<String, ExecUnit>();
    
    public void add(ExecUnit ex) {
        execUnits.add(ex);
        strToExU.put(ex.nodeId, ex);
        //create a mapping of msgType to nodes, that would be map of what 
        //msgTypes have been received and sent by what nodes.
        //This is just aggregating information for better efficiency for query
        //processing
        for( MessageType msgType : ex.msgTypeSentRec ) {
            if( msgToExU.containsKey(msgType) == false ) {
                msgToExU.put(msgType, new ArrayList<ExecUnit>());
            }
            //possibility of adding the same exUnit to the list twice so first
            //check if the exUnit is already present in the list
            if( msgToExU.get(msgType).contains(ex) == false)
                msgToExU.get(msgType).add(ex);
        }
    }
    
    //will get all the exec units  that have the required messages in them.
    //uses the map we created earlier for msg=>exu mappings.
    public ArrayList<ExecUnit> findExecUnitsWithMsgs(ArrayList<MessageType> msgReq) {
        ArrayList<ExecUnit> qualifyingExecUnits = new ArrayList<ExecUnit>();
        qualifyingExecUnits = msgToExU.get(msgReq.get(0));
        
        for(int i=1; i<msgReq.size(); i++) {
            qualifyingExecUnits.retainAll(msgToExU.get(msgReq.get(i)));
        }
        
        return qualifyingExecUnits;
    }

    public ArrayList<QueryElementMatchInstance> query(Query q) {
        //first step is to check whether or not the app has as many distint exec.
        //units as required by the query.
        ArrayList<QueryElementMatchInstance> queryResults = new ArrayList<QueryElementMatchInstance>();
        //an array list of QueryElementMatchInstance for keeping the results
        ArrayList<QueryElementMatchInstance> tmpQueryResults = new ArrayList<QueryElementMatchInstance>();
        if( execUnits.size() >= q.numDistinctExecUnits() ) {
            //second step would be to check whether or not app has all the msg types
            //as required by the query
            //to do this, we check the keys in the msgToExU variable for each of the
            //msgType required in the query
            boolean step2Check = chkMsgTypesPresent(q.msgTypes);
                    
            //continue only if the step2Check is passed
            if( step2Check ) {
                //all the above checks can be made on the aggregate data and doesn't need
                //a lot of processing. Plus the query doesn't need to be processed if
                //the above checks fail
                
                //now we know the system could possibly have the result for the 
                //query
                
                ArrayList<ExecUnit> qualifyingEXUs = new ArrayList<ExecUnit>();
                //for each of query's exec units, query apps exec units
                for(int i=0;i<q.qStruct.size();i++) {
                    //the query results cascade from one query element to the other
                    //for each query element, we need to only look into those exec
                    //units which have the required msgs in them,
                    //for this we created a map of msgs=>exec units, we can use 
                    //this map to find the qualifying exec units. 
                    
                    //for first query element, find the exec units that have the 
                    //required msgs in them,
                    qualifyingEXUs = findExecUnitsWithMsgs(q.qStruct.get(i).msgTypes);
                    if(i>0) {
                        //for the following query elements, find out all the exec
                        //units that are approachable by each of the match from the previous
                        //query element's matches. then check if these exec units have 
                        //the required msgs for this query element's requirement
                        ArrayList<ExecUnit> dstExU = new ArrayList<ExecUnit>();
                        ArrayList<Integer> delIndices = new ArrayList<Integer>();
                        for(int x=0;x<queryResults.size();x++) {
                            dstExU.add(queryResults.get(x).dst);
                            if( qualifyingEXUs.contains(queryResults.get(x).dst) == false ) {
                                delIndices.add(x);
                            }
                        }
                        
                        //from all the exunits that can be reached from the query
                        //results, only keep the ones that would have msgs of the 
                        //current query element
                        qualifyingEXUs.retainAll(dstExU);
                        
                        //some of the query results from previous query element 
                        //can now be invalid becuase the dest doesn't have the required
                        //msgs so clean up the query results before proceeding
                        for(int x=0;x<queryResults.size();x++){
                            if( delIndices.contains(x)==false ){
                                tmpQueryResults.add(queryResults.get(x));
                            }
                        }
                        queryResults = tmpQueryResults;
                        tmpQueryResults = new ArrayList<QueryElementMatchInstance>();
                    }
                    
                    if( i==0 ) {
                        //now for each of query's exec unit,
                        // query each exec unit in the app, but if the query requires 
                        // exec. units to be distinct, then don't query the exec. unit
                        // which has already been queried for this result of the query
                        ArrayList<ArrayList<Message>> tM = new ArrayList<ArrayList<Message>>();
                        for(int j=0; j<qualifyingEXUs.size();j++) {
                            tM = new ArrayList<ArrayList<Message>>();
                            //check if this exec unit has the msg types we are looking
                            //for
    //                        if(qualifyingEXUs.get(j).chkMsgTypesPresent(q.qStruct.get(i).msgTypes)) {
                                //all the msgs as required by this query are present 
                                //in this exec unit
                                //now we need to check whether they are present in the 
                                //required order and interleaving ?
                                ArrayList<MessageType> mtr = q.qStruct.get(i).msgs;
                                if(q.qStruct.get(i).interleaved==true && q.qStruct.get(i).ordered==false) {
                                    //interleaving is allowed but msgs have to appear in the required
                                    //order    
                                    tM = queryNotOrderedInterleaved(mtr, qualifyingEXUs.get(j).msgArr, 0);
                                } else if(q.qStruct.get(i).interleaved==false && q.qStruct.get(i).ordered==false) {
                                    //case where no interleaving is allowed but msgs
                                    //can appear as a bunch in any order, i.e. the 
                                    //required msgs should all appear together
                                    tM = queryNotOrderedNotInterleaved(mtr, qualifyingEXUs.get(j).msgArr, 0);
                                } else if(q.qStruct.get(i).interleaved==false && q.qStruct.get(i).ordered==true) {
                                    //case where no interleaving is allowed and msgs should be in order
                                    //means that the msgs have to appear in this particular order each only 
                                    //occuring once (or as much as the quantifier permits) and followed by 
                                    //none other than the one specified in the query.
                                    tM = queryOrderedNotInterleaved(mtr, qualifyingEXUs.get(j).msgArr, 0);
                                } else if(q.qStruct.get(i).interleaved==true && q.qStruct.get(i).ordered==true) {
                                    //case where interleaving of the required messages is allowed
                                    //and the required messages also need to be present in that particular order
                                    //somehow the quantifier for the required messages will 
                                    //define how many times the required message may interleave
                                    //(i.e. repeat).
                                    tM = queryOrderedInterleaved(mtr, qualifyingEXUs.get(j).msgArr, 0);
                                }
    //                        }
                            QueryElementMatchInstance qemi = new QueryElementMatchInstance(qualifyingEXUs.get(j));
                            //limit the number of search results
                            int n=tM.size();
                            if(q.qStruct.get(i).n > 0 && q.qStruct.get(i).n <=n) {
                                n=q.qStruct.get(i).n;
                            }
                            for(int x=0;x<n;x++) {
                                qemi.addMessages(tM.get(x));
                                qemi.dst = getExecUnitByString(qemi.dstStr);
                                queryResults.add(qemi);
                                qemi = new QueryElementMatchInstance(qualifyingEXUs.get(j));
                            }
                        }
                    }
                    else {
                        //go through the query results and increase the results
                        for( int x=0; x<queryResults.size();x++ ) {
                            QueryElementMatchInstance qe = queryResults.get(x);
                            ExecUnit qeDst = qe.dst;
                            ArrayList<MessageType> mtr = q.qStruct.get(i).msgs;
                            ArrayList<ArrayList<Message>> tM = new ArrayList<ArrayList<Message>>();
                            if(q.qStruct.get(i).interleaved==true && q.qStruct.get(i).ordered==false) {
                                //interleaving is allowed but msgs have to appear in the required
                                //order    
                                tM = queryNotOrderedInterleaved(mtr, qeDst.msgArr, qe.lastTs);
                            } else if(q.qStruct.get(i).interleaved==false && q.qStruct.get(i).ordered==false) {
                                //case where no interleaving is allowed but msgs
                                //can appear as a bunch in any order, i.e. the 
                                //required msgs should all appear together
                                tM = queryNotOrderedNotInterleaved(mtr, qeDst.msgArr, qe.lastTs);
                            } else if(q.qStruct.get(i).interleaved==false && q.qStruct.get(i).ordered==true) {
                                //case where no interleaving is allowed and msgs should be in order
                                //means that the msgs have to appear in this particular order each only 
                                //occuring once (or as much as the quantifier permits) and followed by 
                                //none other than the one specified in the query.
                                tM = queryOrderedNotInterleaved(mtr, qeDst.msgArr, qe.lastTs);
                            } else if(q.qStruct.get(i).interleaved==true && q.qStruct.get(i).ordered==true) {
                                //case where interleaving of the required messages is allowed
                                //and the required messages also need to be present in that particular order
                                //somehow the quantifier for the required messages will 
                                //define how many times the required message may interleave
                                //(i.e. repeat).
                                tM = queryOrderedInterleaved(mtr, qeDst.msgArr, qe.lastTs);
                            }
                            //limit the number of search results
                            int n=tM.size();
                            if(q.qStruct.get(i).n > 0 && q.qStruct.get(i).n <=n) {
                                n=q.qStruct.get(i).n;
                            }                            
                            for(int xX=0;xX<n;xX++) {
                                QueryElementMatchInstance tmpQemi = new QueryElementMatchInstance(qe.src);
                                tmpQemi.dst = qe.dst;
                                tmpQemi.dstStr = qe.dstStr;
                                tmpQemi.lastTs = qe.lastTs;
                                tmpQemi.matchingMessages.addAll(qe.matchingMessages);
                                //tmpQemi.matchingMessages = qe.matchingMessages;
                                tmpQemi.addMessages(tM.get(xX));
                                tmpQemi.dst = getExecUnitByString(tmpQemi.dstStr);
                                tmpQueryResults.add(tmpQemi);
                            }
                        }
                        queryResults = tmpQueryResults;
                        tmpQueryResults = new ArrayList<QueryElementMatchInstance>();
                    }
                }
            } else {
                System.out.println("The messages required by the query were not "
                        + "found travelling in the system");
            }
        } else {
            System.out.println("Not enough distinct exec. units as required by query");
        }
        if( q.maxResults > 0 && q.maxResults < queryResults.size()) {
            for(int z=0;z<q.maxResults;z++) {
                tmpQueryResults.add(queryResults.get(z));
            }
            queryResults = tmpQueryResults;
        }
        return queryResults;
    }
    
    //checks if the given msg types are also present in the system
    //used for step2Check
    private boolean chkMsgTypesPresent(ArrayList<MessageType> mts) {
        boolean check = true;
        for(int i=0;i<mts.size();i++) {
            check = check && msgToExU.containsKey(mts.get(i));
            if( !check ) {
                break;
            }
        }
        return check;
    }

    //given a sequence of msgTypes finds whether all of them are present in msgArr
    //such that they occur in the order as specified and no interleaving of any
    //other type of msgs occur between them
    private ArrayList<ArrayList<Message>> queryOrderedNotInterleaved(ArrayList<MessageType> msgsRequired, ArrayList<Message> msgArr, int ts) {
        ArrayList<ArrayList<Message>> msgsArr = new ArrayList<ArrayList<Message>>();
        ArrayList<Message> tmpMsgsArr = new ArrayList<Message>();
        
        if( msgsRequired.size() > 1 ) {
            MessageType current = msgsRequired.get(0);
            int nextCtr=1;
            for(int i=0; i<msgArr.size(); i++) {
                if(msgArr.get(i).ts >= ts && msgArr.get(i).msgTypeObj.equals(current)) {
                    tmpMsgsArr.add(msgArr.get(i));
                    if( nextCtr < msgsRequired.size() ) {
                        current = msgsRequired.get(nextCtr);
                        nextCtr++;
                    } else {
                        msgsArr.add(tmpMsgsArr);
                        tmpMsgsArr = new ArrayList<Message>();
                        System.out.println("Success");
                        current=msgsRequired.get(0);
                        nextCtr=1;
                    }
                } else {
                    //do the search again if the chain of matches is broken
                    //reset the current and next
                    current=msgsRequired.get(0);
                    nextCtr=1;
                    tmpMsgsArr.clear();
                }
            }
        } else {
            for(int i=0;i<msgArr.size();i++) {
                if(msgArr.get(i).ts >= ts && msgArr.get(i).msgTypeObj.equals(msgsRequired.get(0))) {
                    tmpMsgsArr.add(msgArr.get(i));
                    msgsArr.add(tmpMsgsArr);
                    tmpMsgsArr = new ArrayList<Message>();
                }
            }
        }
        return msgsArr;
    }

    //given a sequence of message types, finds whether these messages occur in the
    //msgArr in the required order while allowing interleaving of messages other 
    //than the ones required.
    private ArrayList<ArrayList<Message>> queryOrderedInterleaved(ArrayList<MessageType> msgsRequired, ArrayList<Message> msgArr, int ts) {
        ArrayList<ArrayList<Message>> msgsArr = new ArrayList<ArrayList<Message>>();
        ArrayList<Message> tmpMsgsArr = new ArrayList<Message>();
  
        if( msgsRequired.size() > 1 ) {
            MessageType current = msgsRequired.get(0);
            int nextCtr=1;
            for(int i=0; i<msgArr.size(); i++) {
                if(msgArr.get(i).ts >= ts && msgArr.get(i).msgTypeObj.equals(current)) {
                    tmpMsgsArr.add(msgArr.get(i));
                    if( nextCtr < msgsRequired.size() ) {
                        current = msgsRequired.get(nextCtr);
                        nextCtr++;
                    } else {
                        //have found atleast one match
                        System.out.println("Match++ o,i");
                        msgsArr.add(tmpMsgsArr);
                        tmpMsgsArr = new ArrayList<Message>();
                        current=msgsRequired.get(0);
                        nextCtr=1;
                    }
                }
            }
        } else {
            for(int i=0;i<msgArr.size();i++) {
                if(msgArr.get(i).ts >= ts && msgArr.get(i).msgTypeObj.equals(msgsRequired.get(0))) {
                    tmpMsgsArr.add(msgArr.get(i));
                    msgsArr.add(tmpMsgsArr);
                    tmpMsgsArr = new ArrayList<Message>();
                }
            }
        }
        
        return msgsArr;
    }

    //required msgs appearing as a bunch in any order
    private ArrayList<ArrayList<Message>> queryNotOrderedNotInterleaved(ArrayList<MessageType> msgsRequired, ArrayList<Message> msgArr, int ts) {
        ArrayList<ArrayList<Message>> msgsArr = new ArrayList<ArrayList<Message>>();
        ArrayList<Message> tmpMsgsArr = new ArrayList<Message>();
        
        ArrayList<MessageType> record = new ArrayList<MessageType>();
        
        for(int i=0;i<msgArr.size();i++) {
            //if this message is the required type, we worry about it, 
            //otherwise it's just interference, in which case we clear the 
            //recorded msgs since this will no more be a valid sequence
            if(msgArr.get(i).ts >= ts && msgsRequired.contains(msgArr.get(i).msgTypeObj) ) {
                //if this type of message is already detected, it means that we 
                //were already somewhere in between in search sequence,
                //and since this message showed up again before search finished,
                //so we have to start over search again
                //but if it's not present we can add it to the record to proceed
                //with the search
                if( record.contains(msgArr.get(i).msgTypeObj) == false ) {
                    record.add(msgArr.get(i).msgTypeObj);
                    //
                    tmpMsgsArr.add(msgArr.get(i));
                } else {
                    record.clear();
                    record.add(msgArr.get(i).msgTypeObj);
                    //
                    tmpMsgsArr.clear();
                    tmpMsgsArr.add(msgArr.get(i));
                }
                if( record.size()==msgsRequired.size() ) {
                    System.out.println("match ++");
                    record.clear();
                    //have a successful match instance
                    msgsArr.add(tmpMsgsArr);
                    tmpMsgsArr = new ArrayList<Message>();
                }
            } else {
                record.clear();
                tmpMsgsArr.clear();
            }
        }
        return msgsArr;
    }
    
    private ArrayList<ArrayList<Message>> queryNotOrderedInterleaved(ArrayList<MessageType> msgsRequired, ArrayList<Message> msgArr, int ts) {
        ArrayList<ArrayList<Message>> msgsArr = new ArrayList<ArrayList<Message>>();
        ArrayList<Message> tmpMsgsArr = new ArrayList<Message>();
        
        ArrayList<MessageType> record = new ArrayList<MessageType>();
        
        for(int i=0;i<msgArr.size();i++) {
            //if this message is the required type, we worry about it, 
            //otherwise it's just interference
            if(msgArr.get(i).ts >= ts && msgsRequired.contains(msgArr.get(i).msgTypeObj) ) {
                //if this type of message is already detected, it means that we 
                //were already somewhere in between in search sequence,
                //and since this message showed up again before search finished,
                //so we have to start over search again
                //but if it's not present we can add it to the record to proceed
                //with the search
                if( record.contains(msgArr.get(i).msgTypeObj) == false ) {
                    record.add(msgArr.get(i).msgTypeObj);
                    //
                    tmpMsgsArr.add(msgArr.get(i));
                } else {
                    record.clear();
                    record.add(msgArr.get(i).msgTypeObj);
                    //
                    tmpMsgsArr.clear();
                    tmpMsgsArr.add(msgArr.get(i));
                }
                if( record.size()==msgsRequired.size() ) {
                    System.out.println("match ++");
                    record.clear();
                    //have a successful match instance
                    msgsArr.add(tmpMsgsArr);
                    tmpMsgsArr = new ArrayList<Message>();
                }
            }
        }
        return msgsArr;
    }

    private ExecUnit getExecUnitByString(String exStr) {
        return strToExU.get(exStr);
    }
}
