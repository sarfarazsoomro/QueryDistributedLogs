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
public class MscGen {
    ArrayList<Message> msgArr;
    ArrayList<String> nodeList;
    String[] colors = {"gray", "black", "maroon", "red", "orange", "olive", "green", "aqua"};
    
    public MscGen(ArrayList<Message> mA) {
        msgArr = mA;
        nodeList = new ArrayList<String>();
        for(Message m : msgArr) {
            if( nodeList.contains(m.id) == false ) {
                nodeList.add(m.id);
            }
            if( nodeList.contains(m.to) == false ) {
                nodeList.add(m.to);
            }
            if( nodeList.contains(m.from) == false ) {
                nodeList.add(m.from);
            }            
        }
    }
    
    public String generateMscString() {
        StringBuilder sb = new StringBuilder();
        //start with the msg envelope
        sb.append("msc { \n");
        sb.append( implode(",", nodeList)+";" );
        sb.append("\n");
        sb.append("\n");
        for(Message m : msgArr) {
            sb.append(m.from+"=>"+m.to);
            sb.append(" "+"[label=\""+m.ts+"-"+m.msgType+"\"]");
            sb.append(";");
            sb.append("\n");
        }
        //close msg envelope
        sb.append("}\n");
        return sb.toString();
    }
    
    public static String implode(String separator, ArrayList<String> data) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < data.size() - 1; i++) {
            sb.append(data.get(i));
            sb.append(separator);
        }
        sb.append(data.get(data.size() - 1));
        return sb.toString();
    }
}
