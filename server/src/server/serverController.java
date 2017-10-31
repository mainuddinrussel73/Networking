package server;

import java.awt.Desktop;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Stack;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.stage.FileChooser;

/**
 *
 * @author main
 */
public class serverController implements Initializable {
    
    
    //for javafx initilizations
    @FXML
    private TextArea servercon;

    @FXML
    private TextArea onlinecon;

    @FXML
    private Button start;

    @FXML
    private Button online;

    @FXML
    private Button end;
    
    @FXML
    private Button file;
    
    ///end initializations
    
    //for server variables
    
    Stack <Pair> clientOutputStreams;
    ArrayList<String> users;
    Pair p;
    DataOutputStream dout;
    DataInputStream din;
    
    BufferedReader reader;
    Socket sock;
    Socket extra;
    PrintWriter writer;
    InputStream is;
    FileOutputStream fos;
    BufferedOutputStream bos;
    boolean isreceivemood = false;
    boolean isrenew = false;
    int port = 2222;
    int port1 = 2225;
    ServerSocket serverSock ;
    ServerSocket serverSock1 ;
    ServerSocket other;
    int i = 0;
    boolean doListen = false;
    long MAX_SIZE = 1000000;
    long file_size = 0;
    RandomAccessFile ramin;
    RandomAccessFile ramout;
    String dir=null;
    String extention;
    String fname;
    Vector v;
    int Size=0,y=0;
    String extentions = null;
    String username = null;
    File filename = null;
    String toSend = null;
    String ReqUser = null;
    String senderName;
    boolean isDone = false;
    
    
    String checksum="";
    String payload="";
    int[] received = {0,0,0,0,0,0,0,0};
    int[] temp = {0,0,0,0,0,0,0,0};
    int chunkSize = 1024;
    public String Destuff(String res){
         int counter=0;
         String out="";
         for(int i=0;i<res.length();i++)
         {
                   
             if(res.charAt(i) == '1')
             {
                            

                 counter++;
                 out = out + res.charAt(i);
                           
             }
             else
             {
                out = out + res.charAt(i);
                counter = 0;
             }
             if(counter == 5)
             {
                if((i+2)!=res.length())
                    out = out + res.charAt(i+2);
                else
                    out=out + '1';
                    i=i+2;
                    counter = 1;
                }
             } 
         return out;
   }
    public int[] addBinary(int[] a,int[] b) {
               
                int firstbit=0 ;
                int[] sum = {0,0,0,0,0,0,0,0};
                int carry = 0;
                
                
                for(int i = 7; i >= 0; i--){
                    int add = a[i] + b[i] + carry;
                    sum[i] = add % 2;
                    carry = (int)add / 2;
                    //System.out.println(carry);
                }

                while(carry == 1){
                    int[] c = {0,0,0,0,0,0,0,1};
                    if(sum[7]==0){sum[7] = sum[7]+carry;break;}
                    else {
                        carry=0;
                        for(int i = 7; i >= 0; i--){
                            int add = sum[i] + c[i] + carry;
                            sum[i] = add % 2;
                            carry = (int)add / 2;
                            //System.out.println(carry);
                        }
                      
                    }
                }
                  
                return sum;

    }
    //////////////////////////////////////////////////////////////
    public class Pair {
        String name;
        private DataInputStream din;
        private DataOutputStream dout;
        
        Socket ss;
        public Pair(DataInputStream l, DataOutputStream r){
            this.din = l;
            this.dout = r;
            
        }
        public DataInputStream getL(){ return din; }
        public DataOutputStream getR(){ return dout; }
        public String getU(){return name;}
        public Socket getS() {return ss;}
        public void setL(DataInputStream l){ this.din = l; }
        public void setR(DataOutputStream r){ this.dout = r; }
        public void setU(String name){this.name = name;}
        public void setS(ServerSocket s) {try {
            ss = s.accept();
            } catch (IOException ex) {
                //Logger.getLogger(serverController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    ////////////////////////////////////////////////////////////////
    public void ListenThread(String username) {
        receiveFile receiveFiles = new receiveFile(username);
        Thread filereceiver = new Thread(receiveFiles);
        if(doListen==true){
         
         filereceiver.start();
        }else {
            doListen = false;
            try {
                Thread.sleep(500);
            } catch (InterruptedException ex) {
               // Logger.getLogger(serverController.class.getName()).log(Level.SEVERE, null, ex);
            }
            receiveFiles.terminate();
        }
    }
    public class ClientHandler implements Runnable	
   {
      
      
       
      public ClientHandler() 
       {
            //client = user;
            try 
            {
                
                //InputStreamReader isReader = new InputStreamReader(sock.getInputStream());
                //reader = new BufferedReader(isReader);
            }
            catch (Exception ex) 
            {
                servercon.setText(servercon.getText()+"Unexpected error... \n");
            }

       }


       @Override
       public void run() 
       {
            String  message,file = "File" ,resend = "Resend", connect = "Connect", disconnect = "Disconnect", chat = "Chat" ,done="End";
            String[] data;
            
            try 
            {   
               
                
                 while ( (message = din.readUTF()) != null ) 
                {
                    //servercon.setText(servercon.getText()+"Received: " + message + "\n");
                    data = message.split(":");
                    
                    

                    if (data[2].equals(connect)) 
                    {
                       if(searchUser(data[0])==false){
                            username = data[0];
                            p = new Pair(din,dout);
                            p.name = data[0];
                            p.ss = extra;
                            clientOutputStreams.push(p);
                            userAdd(data[0]);
                            tellEveryone((data[0] + ":" + data[1] + ":" + chat));
                       }
                       else{
                            
                            p = new Pair(din,dout);
                            p.name = data[0];
                            p.ss = extra;
                            clientOutputStreams.push(p);
                           //getUserTosend();
                           tellEveryone((data[0] + ":" + "You are aleady Logged in" + ":" + chat));
                           clientOutputStreams.pop();
                       }
                    } 
                    
                    else if(data[2].equals(done)){
                            doListen = false;
                            System.out.println("successs");
                            tellEveryone((data[0] + ":SUCCESSFULLY got all the files " + ":" + chat));
                            
                            readllfiles();
                            margefiles();
                            deleteFileas();
                            senderName = username;
                            
                            //username = toSend;
                            
                          
                    }
                    else if(data[2].equals("Ext")){
                        extentions = data[1];
                        System.out.println(extentions);
                    }
                    else if(data[2].equals(resend)){
                        
                    
                    }
                    else if (data[2].equals(disconnect)) 
                    {
                        tellEveryone((data[0] + ":has disconnected." + ":" + chat));
                        userRemove(data[0]);
                    } 
                    else if(data[2].equals("Final")){
                        String[] frames;
                        frames = data[1].split("~");
                        payload = Destuff(frames[2]);
                        System.out.println(frames[1]+"\n"+payload);
                        checksum = frames[1];
                        for(int i=0;i<8;i++){
                            received[i] = 0;
                            if(checksum.charAt(i)=='1')temp[i]=1;
                            else if(checksum.charAt(i)=='0')temp[i]=0;
                        }
                        
                        String strings;
                        int index = 0;
                        while (index < payload.length()) {
                            strings = payload.substring(index, Math.min(index + 8,payload.length()));
                            index += 8;
                            int[] arr = {0,0,0,0,0,0,0,0};
                            for(int y=0;y<8;y++){
                                if(strings.charAt(y)=='0')arr[y]=0;
                                else arr[y]=1;
                            }
                            received = addBinary(received,arr);
                        }
                        Thread.sleep(2); 
                       
                        received = addBinary(received,temp);
                        String s = Arrays.toString(received).replaceAll("\\[|\\]|,|\\s", "");
                        if(s.equals("11111111")){
                            tellEveryone((data[0] + ":all files are lossless" + ":" + chat));
                        }
                        
                        
                        
                    }
                    else if (data[2].equals(chat)) 
                    {
                        username = data[0];
                        getUserTosend();
                        if(data[1].equals("file")){
                            tellEveryone((data[0] + ":ok sent the file" + ":" + chat));
                            isreceivemood = true;
                            doListen = true;
                            ListenThread(data[0]);
                            
                        }
                        else if(data[1].equals("resend")){
                            //getExtentions();
                            username = toSend;
                            getUserTosend();
                            tellEveryone((data[0] + ":ok giving files" + ":" +extentions));
                            //
                            System.out.println("SErver Resend");
                            sendBack();
                        }
                        else if(data[1].contains("1405")){
                            boolean a = searchUser(data[1]);
                            System.out.println(a);
                            if(a==true){tellEveryone(data[0] + ":" + data[1] + ":" +"Start");toSend = data[1];}
                            else {tellEveryone(data[0] + ":" + data[1]+"Not Available" + ":" + "No");}

                        }
                        
                        else {
                            tellEveryone((message));
                        }
                    } 
                    
                    else 
                    {
                        servercon.setText(servercon.getText()+"No Conditions were met. \n");
                    }
                } 
             } 
             catch (Exception ex) 
             {
                try {
                    servercon.setText(servercon.getText()+din.readUTF()+"Lost a connection. \n");
                    ex.printStackTrace();
                } catch (IOException ex1) {
                    //Logger.getLogger(serverController.class.getName()).log(Level.SEVERE, null, ex1);
                }
                //ex.printStackTrace();
                clientOutputStreams.remove(writer);
             } 
	} 
    }
    public void  getUserTosend(){
        for(Pair t : clientOutputStreams){
            if(t.name.equals(username)){
                dout = t.dout;
                din =  t.din;
            }
        
        }
    
    }
    public boolean searchUser(String username){
        for(Pair e: clientOutputStreams){if(e.name.equals(username))return true;}return false;}
    public  List<File> splitFile(File file) throws IOException {
        List<File> filess = new ArrayList<>();
        try{
            ramin=new RandomAccessFile(file,"r");
				
            byte bt[]=new byte[chunkSize];
            int len=(int)ramin.length();
				
            int loops=(len / chunkSize);
            int remainingBytes=len-(loops*chunkSize);
				
            byte bt1[]=new byte[remainingBytes];
            boolean folder = new File("/home/main/serverchunk"+username).mkdir();
            for(int i=1;i<=loops+1;i++)
            {
                                    
					
                
                File files=new File("/home/main/serverchunk"+username,username+String.valueOf(i)+extention);
		ramout=new RandomAccessFile(files,"rw");
		if(i==loops+1)
		{
                    ramin.read(bt1);
                    ramout.write(bt1);
						//clientcon.setText(clientcon.getText()+files.toString()+"     <"+(int)ramout.length()/.0+" MB>\n");
						//scrollPane();
                    ramout.close();
                                                
		}
		else
		{
                    ramin.read(bt);
                    ramout.write(bt);
						//clientcon.setText(clientcon.getText()+files.toString()+"     <"+(int)ramout.length()/.0+" MB>\n");
						
                    ramin.seek(i*chunkSize);
                    ramout.close();
		}
                filess.add(files);
	}
				//clientcon.setText(clientcon.getText()+"\nFinished splitting "+file.toString()+"     <Original size> <"+(int)file.length()/.0+" MB>\n");
				
            ramin.close();
	}
	catch(FileNotFoundException e){}
	catch(IOException t){}
        return filess;
        
    }
    public void getExtentions(){
        File folder = new File("/home/main/rec"+username);
        File[] listOfFiles = folder.listFiles();
        int counter = 0;
        for (File file : listOfFiles) {
            if (file.isFile()) {
                counter++;
                String name = file.getAbsolutePath().substring(file.getAbsolutePath().lastIndexOf("/"));
                extentions = name.substring(name.indexOf("."));
                
                System.out.println(extentions);
                
            }
            
        }
    
    }
    public File[] sendllfiles(){
        

        File folder = new File("/home/main/rec/");
        File[] listOfFiles = folder.listFiles();
        int counter = 0;
        for (File file : listOfFiles) {
            if (file.isFile()) {
                counter++;
                String name = file.getAbsolutePath().substring(file.getAbsolutePath().lastIndexOf("/"));
                extentions = name.substring(name.indexOf("."));
                System.out.println(file.getName()+"Sent"+extentions);
            }
            
        }
        System.out.println(counter+"files");


        return listOfFiles;
    }
    public void sendBack(){
        Thread resend = new Thread(new resendBack());
        resend.start();
    
    }
    public class resendBack implements Runnable{
        public void copy(InputStream in, OutputStream out) throws IOException {
            byte[] buf = new byte[chunkSize];
            int len = 0;
            while ((len = in.read(buf)) != -1) {
                out.write(buf, 0, len);
            }
            servercon.setText(servercon.getText()+"Sent\n");
            //sentComplete = true;
        }
        public void sendFile(File f) throws IOException {


            try{
                System.out.println("Resend");
                if(findSocket(username).ss.isClosed())findSocket(username).ss = (serverSock1.accept());
                 System.out.println(extra.toString()); 
                InputStream in = new FileInputStream(f);
                 System.out.println("okk instream");
                OutputStream out = findSocket(username).ss.getOutputStream();
                System.out.println(in.toString()+";"+out.toString());
                copy(in, out);
                i++;
                isreceivemood = false;
                isrenew = true;
                out.close();
                in.close();
                
            }catch(IOException e){}




        }

        @Override
        public void run() {
               
                          
                           List<File> selectedFiles;
                            try {
                                
                                List<File> fls = readllfiles2();
                                selectedFiles = splitFile(fls.get(0));
                                System.out.println(selectedFiles.isEmpty());
                            
                            for(File f : selectedFiles){
                                try {
                                    sendFile(f);
                                    Thread.sleep(500);
                                } catch (IOException ex) {
                                   servercon.setText(servercon.getText()+"Can'e sent file");
                                } catch (InterruptedException ex) {
                                    //Logger.getLogger(clientController.class.getName()).log(Level.SEVERE, null, ex);
                                }
                                System.out.println(f.getName());
                               
                            }
                                tellEveryone("Server" + ":SUCCESSFUL:"+"End");
                            } catch (IOException ex) {
                                ///Logger.getLogger(serverController.class.getName()).log(Level.SEVERE, null, ex);
                            }
                            
                           
                        
                       
                
                    
        }
    }
    private void copyFileUsingStream(File source) throws IOException {
        InputStream is = null;
        OutputStream os = null;
        try {
            is = new FileInputStream(source);
            os = new FileOutputStream(filename);
            byte[] buffer = new byte[chunkSize];
            int length;
            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
        } finally {
            is.close();
            os.close();
        }
    }
    public class ServerStart implements Runnable 
    {
        @Override
        public void run() 
        {
            clientOutputStreams = new Stack();
            users = new ArrayList();  

            try 
            {
              serverSock = new ServerSocket(2222);
              serverSock1 = new ServerSocket(2225);

                while (true) 
                {
				sock = serverSock.accept();
                                extra = serverSock1.accept();
				//writer = new PrintWriter(sock.getOutputStream());
				

                                din=new DataInputStream(sock.getInputStream()); 
                                dout=new DataOutputStream(sock.getOutputStream());  
                                System.out.println("new user has been added");
                                
                                //clientOutputStreams.add(dOut);

				Thread listener = new Thread(new ClientHandler());
				listener.start();
                                servercon.setText(servercon.getText()+"Got a connection. \n");
                }
            }
            catch (Exception ex)
            {
                 servercon.setText(servercon.getText()+"Error making a connection. \n");
            }
        }
    }
    public Pair findSocket(String username){
        
        for(Pair p :clientOutputStreams){
            if(p.name.equals(username)){
                return p;
            }
        
        }
        return null;
    }
    public class receiveFile implements Runnable{
        String username;
        private volatile boolean running = true;
        
        //boolean folder = new File("/home/main/server"+username).mkdir();
        
        
        
        receiveFile(String username){
            this.username = username;
        }
        
        public void terminate() {
            running = false;
        }
        public void terminator(){
            tellEveryone(username+":"+"File Size Is More then 5MB"+":"+"Err");
        
        }
        public void copy(InputStream in, OutputStream out) throws IOException {
            byte[] buf = new byte[chunkSize];
            int len = 0;
            System.out.println("    =========   ");
            while ((len = in.read(buf)) != -1) {
                out.write(buf, 0, len);
            }
            //PrintStream printStream = new PrintStream(sock.getOutputStream());
            //printStream.print("JOIN #channel1\r\n");
            //printStream.flush( );
            System.out.println("DOne sending");
            
            tellEveryone(username+":Received:"+"Chat");
            servercon.setText(servercon.getText()+"Downloaded\n");
        }
        public void receivefile(){
             try{
                System.out.println("RECEIVER");
                if(findSocket(username).ss.isClosed()){findSocket(username).ss = serverSock1.accept();}
                 System.out.println(extra.toString()); 
                InputStream in = findSocket(username).ss.getInputStream();
                 System.out.println("okk instream");
                 
                OutputStream out = new FileOutputStream("/home/main/rec/"+"_"+i+extentions);
                System.out.println(in.toString()+";"+out.toString());
                copy(in, out);
                i++;
                isreceivemood = false;
                isrenew = true;
                out.close();
                in.close();
                
            }catch(IOException e){}
        
        }
        @Override
        public void run() 
        {
              
               while(running){
                   file_size+=chunkSize;
                   receivefile();
                   if(file_size>=MAX_SIZE){
                       deleteFileas();
                       terminator();
                       file_size = 0;
                       break;
                       
                       
                   }
                    
               }
            

        }
    }
    
    public void deleteFileas(){
    
        File dir = new File("/home/main/rec");
        if (dir.isDirectory()) 
        { 
            
            File[] children = dir.listFiles(); 
            for (int i=0; i<children.length; i++)
            {
                boolean a = children[i].delete();
            }
        }  
  // The directory is now empty or this is a file so delete it 
//  return dir.delete(); 

    }
    public void userAdd (String data) 
    {
        
        users.add(data);
        servercon.setText(servercon.getText()+"After " + data + " added. \n");
       

        
    }
    
    public void userRemove (String data) 
    {
        users.remove(data);
    }
    
    public void tellEveryone(String message) 
    {
	//Iterator it = clientOutputStreams.iterator();
        
        for (Pair t : clientOutputStreams) 
        {
            try 
            {
                //PrintWriter writer = (PrintWriter) it.next();
                
                //dIn.close();
                // write the message
               
                //p.getR().writeUTF(message);
               
                //p.getR().flush();
              if(t.name.equals(username)){ 
                t.dout.writeUTF(message);
                t.dout.flush();
              }
		//writer.println(message);
                //writer.flush();
		
                
               // ta_chat.setCaretPosition(ta_chat.getDocument().getLength());

            } 
            catch (Exception ex) 
            {
		servercon.setText(servercon.getText()+"Error telling everyone. \n");
            }
        }
        servercon.setText(servercon.getText()+"Sending:"  + message + "\n");
    }
    
    public void deleteFiles(){
        File folder = new File("/home/main/rec/");
        File[] listOfFiles = folder.listFiles();
        int counter = 0;
        for (File file : listOfFiles) {
            Matcher m = Pattern.compile("_[0-9]*").matcher(file.getAbsolutePath());
            if (m.matches()) {
               file.delete();
               System.out.println(file.getName()+"Deleted");
            }
            
            
        }
    
    }
    public File[] readllfiles(){
        

        File folder = new File("/home/main/rec/");
        File[] listOfFiles = folder.listFiles();
        int counter = 0;
        for (File file : listOfFiles) {
            if (file.isFile()) {
                counter++;
                System.out.println(file.getName());
            }
            
        }
        System.out.println(counter+"files");


        return listOfFiles;
    }
    public List<File> readllfiles2(){
        
        List<File> fls = new ArrayList<>();
        File folder = new File("/home/main/rec"+senderName);
        File[] listOfFiles = folder.listFiles();
        int counter = 0;
        for (File file : listOfFiles) {
            if (file.isFile()) {
                counter++;
                fls.add(file);
                System.out.println(file.getName());
            }
            
        }
        System.out.println(counter+"files");


        return fls;
    }
    public void margefiles() throws FileNotFoundException, IOException{
            File[] files = readllfiles();
            Arrays.sort(files, new Comparator<File>() {
            @Override
                public int compare(File o1, File o2) {
                    int n1 = extractNumber(o1.getName());
                    int n2 = extractNumber(o2.getName());
                    return n1 - n2;
                }

                private int extractNumber(String name) {
                    int i = 0;
                    try {
                        int s = name.indexOf('_')+1;
                        int e = name.lastIndexOf('.');
                        String number = name.substring(s, e);
                        i = Integer.parseInt(number);
                    } catch(Exception e) {
                        i = 0; // if filename does not match the format
                               // then default to 0
                    }
                    return i;
                }
            });

            try{
                boolean folder = new File("/home/main/rec"+username).mkdir();
                File fi = new File("/home/main/rec"+username,"some."+extentions);
		ramout=new RandomAccessFile(fi,"rw");
		double raminLength=(int)ramout.length()/(double)chunkSize;
		for(int i=0;i<files.length;i++)
		{
                    File f=(File)files[i];
                    ramin=new RandomAccessFile(f,"r");
                    int sze=(int)ramin.length();
					
                    byte byt[]=new byte[sze];
				
                    ramin.read(byt);
                    ramout.seek(Size);
                    Size=Size+sze;
                    ramout.write(byt);
                    ramin.close();
		}
		ramout.close();
				
				
            }
            catch(FileNotFoundException t){}
            catch(IOException o){}
    
    
            
    }
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
        start.setOnAction(new EventHandler(){

            @Override
            public void handle(Event t) {
                Thread starter = new Thread(new ServerStart());
                starter.start();

                servercon.setText(servercon.getText()+"Server started...\n");
            }
        });
        end.setOnAction(new EventHandler(){

            @Override
            public void handle(Event t) {
                try 
                {
                    Thread.sleep(2000);                 //5000 milliseconds is five second.
                } 
                catch(InterruptedException ex) {Thread.currentThread().interrupt();}

                tellEveryone("Server:is stopping and all users will be disconnected.\n:Chat");
                servercon.setText(servercon.getText()+"Server stopping... \n");

                
            }
        });
        online.setOnAction(new EventHandler(){

            @Override
            public void handle(Event t) {
                onlinecon.setText("\n Online users : \n");
                for (String current_user : users)
                {
                        onlinecon.setText(onlinecon.getText()+current_user);
                        onlinecon.setText(onlinecon.getText()+"\n");
                }   
            }
            
        
        });
        file.setOnAction(new EventHandler(){
            private Object FilenameUtils;

            @Override
            public void handle(Event t) {
               
                
                
                    FileChooser fileChooser = new FileChooser();
                    
                    
                    File file = fileChooser.showOpenDialog(null);
                    System.out.println(file);
                    filename = file;                  
                
            
            }
        });
    }    
    
}
