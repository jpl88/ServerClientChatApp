import java.io.PrintWriter;
import java.net.ServerSocket;
import java.util.HashSet;

/**
 * 
 * @author justinlee
 */
public class Server {
	
	private static final int PORT = 500026;
	
	private static HashSet<String> names = new HashSet<String>();
	
	private static HashSet<PrintWriter> writers = new HashSet<PrintWriter>();
	
	public static void main(String[] args){
		System.out.println("Chat server is now running");
		ServerSocket listener = new ServerSocket(PORT);
		try{
			while(true){
				new MessageHandler(listener.accept()).start();
			}
		}
		finally{
			listener.close();
		}
	}
}
