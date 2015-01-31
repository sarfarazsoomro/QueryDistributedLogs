/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package project5a;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author ssoomro
 */
public class Project5a {
    public static void main(String[] args) {
        File folder = new File("logs");
        File[] listOfFiles = folder.listFiles();
        String line = "";
        BufferedReader r;
        String[] temp;
        ArrayList<Message> msgArr = new ArrayList<Message>();
        App app = new App();
        int ln = 1;
        
        int fcount = 1;
        ExecUnit tmpExU;
        Message tmpMsg;
        String id;
        int ts;
        String event;
        String from;
        String to;
        String msg;
        String desc;
        
        try {
            for (File f : listOfFiles) {
                ln=1;
                r = new BufferedReader(new FileReader(f));
                tmpExU = new ExecUnit( getNodeId( f.getName().substring(0, f.getName().length()-4) ) );
                System.out.println("file "+fcount++);
                while( (line = r.readLine()) != null ) {
                    if( ln%2==0 ) {
                        line = trimLabel(line, "INFO");
                        temp = line.split("\\|");
                        id = getNodeId(trimLabel(temp[0].trim(), "id"));
                        ts = Integer.parseInt(trimLabel(temp[1].trim(), "ts"));
                        event = trimLabel(temp[2].trim(), "event");
                        from = "";
                        to = "";
                        if( event.equals("SEND") ) {
                            from = id;
                            to = getNodeId(trimLabel(temp[3].trim(), "to/from"));
                        } else if( event.equals("RECEIVE") ) {
                            to = id;
                            from = getNodeId(trimLabel(temp[3].trim(), "to/from"));
                        }
                        msg = trimLabel(temp[4].trim(), "msg");
                        desc = trimLabel(temp[5].trim(), "desc");
                        
                        tmpMsg = new Message(id, from, to, ts, msg, desc, event);                        
                        tmpExU.addMsg(tmpMsg);
                        
                        //msgArr.add(tmpMsg);
                        //may need to deal with duplicates, since sent/recieve pair
                        //of msgs end up in the logs of both the actors communicating 
                        //via msgs
                    }
                    ln++;
                }
                r.close();
                app.add(tmpExU);
            }
            //Collections.sort(msgArr);
            //System.out.println(msgArr.toString());
            //mscGen generated here
//            MscGen msGen = new MscGen(msgArr);
//            System.out.println(msGen.generateMscString());
            
            //keep asking for queries one after another
            Boolean moreQuery = true;
            
            while(moreQuery) {
                String query = getUserQuery();
                Query q = new Query(query);
                System.out.println(q.toString());
                processQueryResults(app.query(q));
                //ask for more
                moreQuery = inputYesNo();
            }
        } catch (IOException ex) {
            Logger.getLogger(Project5a.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    static String trimLabel(String str, String lbl) {
        return str.substring(lbl.length()+1).trim();
    }
    
    static String getNodeId(String str) {
        String[] tmp;
        tmp = str.split("\\@");
        return tmp[1];
    }
    
    static String getUserQuery() {
        String q = "";
        System.out.println("Enter query: ");

        //  open up standard input
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        try {
            q = br.readLine();
        } catch (IOException ioe) {
            System.out.println("IO error trying to read your name!");
            System.exit(1);
        }
        return q;
    }
    
    static Boolean inputYesNo() {
        Boolean yn = true;
        System.out.println("Enter another query [y/n]?: ");

        //  open up standard input
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        try {
            String s = br.readLine();
            if(s.equals("y") == false) {
                yn = false;
            }
        } catch (IOException ioe) {
            System.out.println("IO error trying to read your name!");
            System.exit(1);
        }
        return yn;
    }

    private static void processQueryResults(ArrayList<QueryElementMatchInstance> queryResult) {
        if( queryResult.size()>0 ) {
            MscGenQuery mscGen = new MscGenQuery(queryResult);
            generatePNG(writeFile(mscGen.generateMscString()));
        } else {
            System.out.println("No result found");
        }
    }

    private static void generatePNG(String fname) {
        try {
            String command = "mscgen.exe -T png -i ./out/"+fname+" -o ./outpng/"+fname+".png";
            Process child = Runtime.getRuntime().exec(command);
            try {
                child.waitFor();
            } catch (InterruptedException ex) {
                Logger.getLogger(Project5a.class.getName()).log(Level.SEVERE, null, ex);
            }
            command = "rundll32 \"C:\\Program Files\\Windows Photo Viewer\\PhotoViewer.dll\", ImageView_Fullscreen D:\\Docs\\UFL\\Dropbox\\DOS-COP5616\\project5a\\outpng\\"+fname+".png";
            child = Runtime.getRuntime().exec(command);

        } catch (IOException ex) {
            Logger.getLogger(Project5a.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static String writeFile(String str) {
        String fname = "out"+str.hashCode()+".msc";
        try {
          FileWriter fstream = new FileWriter("out/"+fname);
          BufferedWriter out = new BufferedWriter(fstream);
          out.write(str);
          out.close();
        } catch (Exception e){//Catch exception if any
            System.err.println("Error: " + e.getMessage());
        }
        return fname;
    }
}