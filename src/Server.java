import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Server {
	
	private static final int PORT = 50026;
	private static ArrayList<String> usernames = new ArrayList<String>();
	private static ArrayList<PrintWriter> writers = new ArrayList<PrintWriter>();
	
	public static void main(String[] args){
		try{
			ServerSocket listener = new ServerSocket(PORT);
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
                while (true) {
                    String input = in.readLine();
                    if (input == null) return;
                    for (PrintWriter writer : writers) {
                        writer.println("MESSAGE " + name + ": " + input);
                    }
                }
            } 
            catch (IOException e) {
                System.out.println(e);
            } 
            finally {
                if (name != null) usernames.remove(name);
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
