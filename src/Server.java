import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Server {
	
	private static final int PORT = 50026;
	private static ArrayList<String> usernames = new ArrayList<String>();
	private static ArrayList<PrintWriter> writers = new ArrayList<PrintWriter>();
	private static ArrayList<String> busyUsernames = new ArrayList<String>();
	
	public static void main(String[] args){
		try{
			ServerSocket listener = new ServerSocket(PORT);
			System.out.println("ServerSocekt is a sucess");
			InetAddress IP=InetAddress.getLocalHost();
			System.out.println(IP.getHostAddress());
			try{
				while (true) new MessageHandler(listener.accept()).start();
			}
			finally{
				listener.close();
			}
		}
		catch(IOException e){
			System.out.println("Server: " + e.getMessage());
		}
		
	}
	
	private static class MessageHandler extends Thread{
		
		private String name;
        private Socket socket;
        private BufferedReader in;
        private PrintWriter out;

        public MessageHandler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try{
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);
                while (true) {
                    out.println("SUBMITNAME");
                    name = in.readLine();
                    if (name == null) return;
                    synchronized (usernames) {
                        if (!usernames.contains(name)) {
                            usernames.add(name);
                            break;
                        }
                    }
                }
                out.println("NAMEACCEPTED");
                writers.add(out);
                synchronized (busyUsernames){
                	int userIndex = -1;
                    while (true) {
    					String input = in.readLine();
    					if (input == null)
    						return;
    					else if (input.contains("NEWCHATREQUEST")) {
    						userIndex = usernames.lastIndexOf(input.substring(14));
    						if (userIndex != -1) {
    							if (!(busyUsernames.contains(input.substring(14)) || busyUsernames.contains(usernames.get(writers.lastIndexOf(out))))) {
    								busyUsernames.add(input.substring(14));
    								busyUsernames.add(usernames.get(writers.lastIndexOf(out)));
    								writers.get(userIndex).println("CHATINITIALIZED " + input.substring(14));
    								out.println("CHATINITIALIZED " + input.substring(14));
    							} 
    							else {
    								out.println("FAILEDCHATINITIALIZE User is busy.");
    							}
    						} 
    						else {
    							out.println("FAILEDCHATINITIALIZE User doesn't exits.");
    						}
                    	}
    					else if(input.contains("EXITCHATREQUEST")){
    						writers.get(userIndex).println("EXITCHATREQUEST");
    						out.println("EXITCHATREQUEST");
    						while(busyUsernames.contains(name)){
    	                		busyUsernames.remove(name);
    	                	} 
    						while(busyUsernames.contains(usernames.get(userIndex))){
    							busyUsernames.remove(usernames.get(userIndex));
    						}
    					}
    					else if(input.contains("CHATMESSAGE")){
    						writers.get(userIndex).println("CHATMESSAGE " + name + ": " + input.substring(11));
    						out.println("CHATMESSAGE " + name + ": " + input.substring(11));
    					}
    					System.out.println(input);
    					System.out.println(usernames.toString());
    					System.out.println(busyUsernames.toString());
                    }
                }
            } 
            catch (IOException e) {
                System.out.println(e);
            } 
            finally {
                if (name != null){
                	usernames.remove(name); 	
                }
                if (out != null) writers.remove(out);
                try{
                	socket.close();
                }
                catch (IOException e) {
                	System.out.println("MessageHandler: " + e.getMessage());
                }
            }
        }
		
	}  //end class MessageHandler

}  //end class Server
