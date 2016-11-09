import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Objects;

public class Server {
	
	private static final int PORT = 50026;
	private static ArrayList<String> usernames = new ArrayList<String>();
	private static ArrayList<PrintWriter> writers = new ArrayList<PrintWriter>();
	private static ArrayList<Chat> chats = new ArrayList<Chat>();
	
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
        private BufferedReader inputReader;
        private PrintWriter outputWriter;

        public MessageHandler(Socket socket) {
            this.socket = socket;
        }

        
        private Chat findMyChat(String username){
        	synchronized (chats){
        		Objects.requireNonNull(username);
            	for(int i = 0; i < chats.size(); i++){
            		if(chats.get(i).getUsername1().equals(username) || chats.get(i).getUsername2().equals(username)){
            			return chats.get(i);
            		}
            	}
            	return null;
        	}
        }
        
        public void run() {
            try{
                inputReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                outputWriter = new PrintWriter(socket.getOutputStream(), true);
                boolean hasFailed = false;
                while (true) {
                	if(!hasFailed){
						outputWriter.println("SUBMITNAME");
						name = inputReader.readLine();
						if (name == null) return;
						synchronized (usernames) {
							if (!usernames.contains(name)) {
								usernames.add(name);
								break;
							}
							else{
								hasFailed = true;
							}
						}
                	}
                    else{
                    	outputWriter.println("FAILEDSUBMITNAME");
                    	name = inputReader.readLine();
						if (name == null) return;
						synchronized (usernames) {
							if (!usernames.contains(name)) {
								usernames.add(name);
								break;
							}
						}
                    }
                }
                outputWriter.println("NAMEACCEPTED");
                writers.add(outputWriter);
                while(true){
                	String input = inputReader.readLine();
                	if(input == null) continue;
                	else if(input.startsWith("NEWCHATREQUESTINCHAT")){
                		synchronized (usernames){
                			if(usernames.stream().anyMatch(t -> t.equals(input.substring(20)))){
                				synchronized (chats){
                        			synchronized (writers){
                        				//Person is in a chat that doesn't include you.
                        				System.out.println((chats.stream().anyMatch(t -> t.getUsername1().equals(input.substring(20))) && 
                        					!chats.stream().anyMatch(t -> t.getUsername2().equals(name))) || 
                        					(chats.stream().anyMatch(t -> t.getUsername2().equals(input.substring(20))) && 
                                			!chats.stream().anyMatch(t -> t.getUsername1().equals(name))));
                                			
                        				if((chats.stream().anyMatch(t -> t.getUsername1().equals(input.substring(20))) && 
                        					!chats.stream().anyMatch(t -> t.getUsername2().equals(name))) || 
                        					(chats.stream().anyMatch(t -> t.getUsername2().equals(input.substring(20))) && 
                                			!chats.stream().anyMatch(t -> t.getUsername1().equals(name)))){
                        					outputWriter.println("FAILEDCHATINITIALIZE User is busy.");
                        				}
                        				//Person is in a chat with you already.
                        				else if((chats.stream().anyMatch(t -> t.getUsername1().equals(input.substring(20))) && 
                            					chats.stream().anyMatch(t -> t.getUsername2().equals(name))) || 
                            					(chats.stream().anyMatch(t -> t.getUsername2().equals(input.substring(20))) && 
                                    			chats.stream().anyMatch(t -> t.getUsername1().equals(name)))){
                        					outputWriter.println("FAILEDCHATINITIALIZE User is busy.");
                        				}
                        				//Person is not in a chat
                        				else{
                        					Chat old = findMyChat(name);
                        					old.getPW1().println("EXITCHATREQUEST");
                                			old.getPW2().println("EXITCHATREQUEST");
                                			chats.remove(old);
                        					Chat temp = new Chat(input.substring(20), name, writers.get(usernames.lastIndexOf(input.substring(20))), outputWriter);
                                			chats.add(temp);
                                			temp.getPW1().println("CHATINITIALIZED " + temp.getUsername2());
                                			temp.getPW2().println("CHATINITIALIZED " + temp.getUsername1());
                        				}
                        			}
                        		}
                			}
                			else{
                				outputWriter.println("FAILEDCHATINITIALIZE User doesn't exist.");
                			}
                		}
                		
                	}
                	else if(input.startsWith("NEWCHATREQUEST")){
                		synchronized (usernames){
                			if(usernames.stream().anyMatch(t -> t.equals(input.substring(14)))){
                				synchronized (chats){
                        			synchronized (writers){
                        				if(!(chats.stream().anyMatch(t -> t.getUsername1().equals(input.substring(14))) || 
                                				chats.stream().anyMatch(t -> t.getUsername2().equals(input.substring(14))) || 
                                				chats.stream().anyMatch(t -> t.getUsername1().equals(name)) || 
                                				chats.stream().anyMatch(t -> t.getUsername2().equals(name)) || name.equals(input.substring(14)))){
                                			Chat temp = new Chat(input.substring(14), name, writers.get(usernames.lastIndexOf(input.substring(14))), outputWriter);
                                			chats.add(temp);
                                			temp.getPW1().println("CHATINITIALIZED " + temp.getUsername2());
                                			temp.getPW2().println("CHATINITIALIZED " + temp.getUsername1());
                                		}
                        				else {
                                			outputWriter.println("FAILEDCHATINITIALIZE User is busy.");
                                		}
                        			}
                        		}	
                			}
                			else{
                    			outputWriter.println("FAILEDCHATINITIALIZE User doesn't exist.");
                    		}
                		}	
                	}
                	else if(input.startsWith("CHATMESSAGE")){
                		Chat temp = findMyChat(name);
                		if(temp != null){
                			temp.getPW1().println("CHATMESSAGE " + name + ": " + input.substring(11));
                			temp.getPW2().println("CHATMESSAGE " + name + ": " + input.substring(11));
                		}	
                	}
                	else if(input.startsWith("EXITCHATREQUEST")){
                		Chat temp = findMyChat(name);
                		if(temp != null){
                			temp.getPW1().println("EXITCHATREQUEST");
                			temp.getPW2().println("EXITCHATREQUEST");
                		}
                		chats.remove(temp);
                	}
                	else if(input.startsWith("EXITREQUEST")){
                		synchronized (usernames) {
                			usernames.remove(name);
                		}
                		Chat temp = findMyChat(name);
                		temp.getPW1().println("EXITCHATREQUEST");
            			temp.getPW2().println("EXITCHATREQUEST");
                    	chats.remove(temp);
                	}
                		
                }
            } 
            catch (IOException e) {
                System.out.println(e.getMessage());
            } 
            finally {
                if (name != null){
                	usernames.remove(name); 
                	Chat temp = findMyChat(name);
                	chats.remove(temp);
                }
                if (outputWriter != null) writers.remove(outputWriter);
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
