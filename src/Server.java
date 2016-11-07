import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;

/**
 * 
 * @author justinlee
 */
public class Server {
	
	private static final int PORT = 50026;
	
	private static HashSet<String> usernames = new HashSet<String>();
	
	private static HashSet<PrintWriter> writers = new HashSet<PrintWriter>();
	
	public static void main(String[] args){
		
		try{
			ServerSocket listener = new ServerSocket(PORT);
			try{
				System.out.println("Chat server is now running");
				
				while (true) {
					new MessageHandler(listener.accept()).start();
				}
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
		
		private String username;
		private Socket socket;
		private BufferedReader inputReader;
		private PrintWriter outputWriter;
		
		public MessageHandler(Socket socket){
			this.socket = socket;
		}
		
		@Override
		public void run(){
			try{
				inputReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				outputWriter = new PrintWriter(socket.getOutputStream(), true);
				while (true) {
					outputWriter.println("Attempting to submit username.");
	                username = inputReader.readLine();
	                if (username == null) {
	                    throw new NullPointerException();
	                }
	                synchronized (usernames) {
	                    if (!usernames.contains(username)) {
	                        usernames.add(username);
	                        break;
	                    }
	                }
				}
				outputWriter.println("Username accepted.");
                writers.add(outputWriter);
                while (true) {
                    String input = inputReader.readLine();
                    if (input == null) {
                        throw new NullPointerException();
                    }
                    for (PrintWriter writer : writers) {
                        writer.println("MESSAGE " + username + ": " + input);
                    }
                }
			}
			catch(NullPointerException e){
				System.out.println(e.getMessage());
			}
			catch(IOException e){
				System.out.println(e.getMessage());
			}
			finally{
				if (username != null) {
                    usernames.remove(username);
                }
                if (outputWriter != null) {
                    writers.remove(outputWriter);
                }
                try {
                    socket.close();
                } catch (IOException e) {
                }
			}
		}
		
	}  //end class MessageHandler

}  //end class Server
