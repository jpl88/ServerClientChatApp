import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Client {
	
	//Private Fields
	private BufferedReader inputReader;
    private PrintWriter outputWriter;
    private JFrame frame = new JFrame("Client Message GUI.");
    private JTextField textField = new JTextField(40);
    private JTextArea messageArea = new JTextArea(10, 40);
    private boolean inChat = false;
    
    //Constructor Method
    public Client(){
    	//GUI setup
        textField.setEditable(false);
        messageArea.setEditable(false);
        frame.getContentPane().add(textField, "Center");
        frame.getContentPane().add(new JScrollPane(messageArea), "North");
        frame.pack();
        
        //Action listener for when gui is closed.
        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
               outputWriter.println("EXITREQUEST");
            }
        });
        
        //Action listener for when message is entered in text field.
        textField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	//Handles commands to the server.
            	if (inChat){
            		//Start new chat while in a chat.
            		if(textField.getText().equals("Start Chat")){
            			String newChatPartener = getChatPartener();
            			outputWriter.println("NEWCHATREQUESTINCHAT" + newChatPartener);
            		}
            		//Exit chat while in chat.
            		else if(textField.getText().equals("Exit Chat")){
                		outputWriter.println("EXITCHATREQUEST");
                	}
            		//While in chat default to message.
            		else{
            			outputWriter.println("CHATMESSAGE" + textField.getText());
            		}	
            	}
            	//Exit chat when outside of chat.
            	else if(textField.getText().equals("Exit Chat")){
            		messageArea.append("Your not in a chat\n");
            	}
            	//Start chat when  currently not in chat.
            	else if(textField.getText().equals("Start Chat")){
            		String chatPartener = getChatPartener();
            		outputWriter.println("NEWCHATREQUEST" + chatPartener);
            	}
            	//If not in chat print to own message area.
            	else{
            		messageArea.append("You aren't currently in a chat with anybody:\n" + textField.getText() + "\nwas not sent and bounced back\n");
            	}
            	//Reset to blank text.
            	textField.setText("");
            }
        });
    }
    
    //Method that prompts for server address.
    private String getServerAddress() {
        return JOptionPane.showInputDialog(
            frame,
            "Enter IP Address of the Server:",
            "Chat Setup",
            JOptionPane.QUESTION_MESSAGE);
    }
    
    //Method that prompts for port number.
    private String getPortNumber() {
        return JOptionPane.showInputDialog(
            frame,
            "Enter Port Number of the Server:",
            "Chat Setup",
            JOptionPane.QUESTION_MESSAGE);
    }
    
    //Method that prompts for username.
    private String getName() {
        return JOptionPane.showInputDialog(
            frame,
            "Choose a user name:",
            "Chat Setup",
            JOptionPane.PLAIN_MESSAGE);
    }
    
    //Method that prompts for a correct username.
    private String retryName(){
    	return JOptionPane.showInputDialog(
    		frame,
    		"You failed to input a unque user name. Please try again:",
    		"Chat Setup",
    		JOptionPane.PLAIN_MESSAGE);
    }
    
    //Method that prompts for starting a chat.
    private String getChatPartener(){
    	return JOptionPane.showInputDialog(
    		frame,
    		"Choose a user to chat with.",
    		"Chat Setup",
    		JOptionPane.PLAIN_MESSAGE);
    }
    
    //Method that runs and communicates with the server.
    private void run(){
    	try{
			//Create Socket connection
			String serverAddress = getServerAddress();
			Integer portNum = Integer.parseInt(getPortNumber());
			Socket socket = new Socket(serverAddress, portNum);
			//Create input and output interpreters/writers.
			inputReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			outputWriter = new PrintWriter(socket.getOutputStream(), true);
			
			//Handles server messages and respons accordingly.
			while (true) {
				String line = inputReader.readLine();
				if (line.startsWith("SUBMITNAME")) outputWriter.println(getName());
				else if(line.startsWith("FAILEDSUBMITNAME")) outputWriter.println(retryName());
				else if (line.startsWith("NAMEACCEPTED"))textField.setEditable(true);
				else if (line.startsWith("CHATINITIALIZED")){
					messageArea.append("Chat initialized." + line.substring(15) + "\n");
					inChat = true;
				}
				else if (line.startsWith("FAILEDCHATINITIALIZE")) messageArea.append(line.substring(21) + "\n");
				else if(line.startsWith("CHATMESSAGE")) messageArea.append(line.substring(12) + "\n");
				else if(line.startsWith("EXITCHATREQUEST")){
					messageArea.append("Exited Chat.\n");
					inChat = false;
				}
			}	
    	}
    	catch(IOException e){
    		System.out.println("Client Error: " + e.getMessage());
    	}
    	
    }
    
    //Main method for running client.
    public static void main(String[] agrs){
    	Client c = new Client();
        c.frame.setVisible(true);
        c.run();
    }
    
}
