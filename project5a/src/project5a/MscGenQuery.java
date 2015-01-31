/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package project5a;

import java.util.ArrayList;
import java.util.Random;

/**
 *
 * @author sarfaraz
 */
public class MscGenQuery {
    ArrayList<QueryElementMatchInstance> queryResult;
    ArrayList<String> nodeList;
    String[] colors = {"gray", "black", "maroon", "red", "orange", "olive", "green", "aqua"};
    
    public MscGenQuery(ArrayList<QueryElementMatchInstance> qR) {
        queryResult = qR;
        nodeList = new ArrayList<String>();
        for(QueryElementMatchInstance qi : queryResult) {
            for(Message m: qi.matchingMessages) {
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
    }
    
    public String generateMscString() {
        StringBuilder sb = new StringBuilder();
        //start with the msg envelope
        sb.append("msc { \n");
        sb.append( implode(",", nodeList)+";" );
        sb.append("\n");
        sb.append("\n");
        Random rand = new Random();
        for(QueryElementMatchInstance qi : queryResult) {
            String color = colors[rand.nextInt(colors.length)];
            for(Message m : qi.matchingMessages) {
                sb.append(m.from+"=>"+m.to);
                sb.append(" "+"[label=\""+m.ts+"-"+m.msgType+"\", textcolor=\""+color+"\", linecolor=\""+color+"\"]");
                sb.append(";");
                sb.append("\n");
            }
            sb.append("---;");
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
