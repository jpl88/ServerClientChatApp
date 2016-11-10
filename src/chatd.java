import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Objects;

public class chatd {
	
	//Private Fields
	private static final int PORT = 50026;
	private static ArrayList<String> usernames = new ArrayList<String>();
	private static ArrayList<PrintWriter> writers = new ArrayList<PrintWriter>();
	private static ArrayList<Chat> chats = new ArrayList<Chat>();
	
	//Main method
	public static void main(String[] args){
		try{
			//Setup Socket.
			ServerSocket listener = new ServerSocket(PORT);
			System.out.println("ServerSocekt is a sucess");
			//Print IP adress of the server
			InetAddress IP=InetAddress.getLocalHost();
			System.out.println(IP.getHostAddress());
			//Create thread every time socket ouptputs data.
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
		
		//Private fields
		private String name;
        private Socket socket;
        private BufferedReader inputReader;
        private PrintWriter outputWriter;

        //Constructor method.
        public MessageHandler(Socket socket) {
            this.socket = socket;
        }
        
        //Helper method to find the first chat the user is a member of.
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
        
        //Run method for the thread
        public void run() {
            try{
            	//Create input and output interpreters/writers.
                inputReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                outputWriter = new PrintWriter(socket.getOutputStream(), true);
                
                //If has failed to enter unique username.
                boolean hasFailed = false;
                
                //Continuously prompt for unique username until correct.
                while (true) {
                	
                	//First time trying for unique username.
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
                	//After failing typing in username.
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
                //Accept name and add the writer to the writers list.
                outputWriter.println("NAMEACCEPTED");
                synchronized (writers){
                	writers.add(outputWriter);
                }
                //Handle messages sent from client.
                while(true){
                	String input = inputReader.readLine();
                	if(input == null) continue;
                	//Handles new chat requests from in a chat.
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
                	//Handles a new chat request from a listener.
                	else if(input.startsWith("NEWCHATREQUEST")){
                		synchronized (usernames){
                			if(usernames.stream().anyMatch(t -> t.equals(input.substring(14)))){
                				synchronized (chats){
                        			synchronized (writers){
                        				//Create chat if none of them are in chat.
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
                	//Handles a message in a chat
                	else if(input.startsWith("CHATMESSAGE")){
                		Chat temp = findMyChat(name);
                		if(temp != null){
                			temp.getPW1().println("CHATMESSAGE " + name + ": " + input.substring(11));
                			temp.getPW2().println("CHATMESSAGE " + name + ": " + input.substring(11));
                		}	
                	}
                	//Handles an exit chat request.
                	else if(input.startsWith("EXITCHATREQUEST")){
                		Chat temp = findMyChat(name);
                		if(temp != null){
                			temp.getPW1().println("EXITCHATREQUEST");
                			temp.getPW2().println("EXITCHATREQUEST");
                		}
                		chats.remove(temp);
                	}
                	//Handles closing of chat window.
                	else if(input.startsWith("EXITREQUEST")){
                		synchronized (usernames) {
                			if(name != null){
                				usernames.remove(name);
                			}	
                		}
                		synchronized (writers){
                			if(writers != null){
                				writers.remove(outputWriter);
                			}
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
