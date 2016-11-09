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
	
	BufferedReader inputReader;
    PrintWriter outputWriter;
    JFrame frame = new JFrame("Client Message GUI.");
    JTextField textField = new JTextField(40);
    JTextArea messageArea = new JTextArea(10, 40);
    
    private boolean inChat = false;
    
    public Client(){
        textField.setEditable(false);
        messageArea.setEditable(false);
        frame.getContentPane().add(textField, "Center");
        frame.getContentPane().add(new JScrollPane(messageArea), "North");
        frame.pack();
        
        textField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	if(textField.getText().equals("Start Chat")){
            		String chatPartener = getChatPartener();
            		outputWriter.println("NEWCHATREQUEST" + chatPartener);
            	}
            	else if(textField.getText().equals("Exit Chat")){
            		outputWriter.println("EXITCHATREQUEST");
            	}
            	else if (inChat){
            		outputWriter.println("CHATMESSAGE" + textField.getText());
            	}
            	textField.setText("");
            }
        });
    }
    
    private String getServerAddress() {
        return JOptionPane.showInputDialog(
            frame,
            "Enter IP Address of the Server:",
            "Chat Setup",
            JOptionPane.QUESTION_MESSAGE);
    }
    
    private String getPortNumber() {
        return JOptionPane.showInputDialog(
            frame,
            "Enter Port Number of the Server:",
            "Chat Setup",
            JOptionPane.QUESTION_MESSAGE);
    }
    
    private String getName() {
        return JOptionPane.showInputDialog(
            frame,
            "Choose a screen name:",
            "SChat Setup",
            JOptionPane.PLAIN_MESSAGE);
    }
    
    private String getChatPartener(){
    	return JOptionPane.showInputDialog(
    		frame,
    		"Choose a user to chat with.",
    		"Chat Setup",
    		JOptionPane.PLAIN_MESSAGE);
    }
    
    private void run(){
    	try{
			// Make connection and initialize streams
			String serverAddress = getServerAddress();
			Integer portNum = Integer.parseInt(getPortNumber());
			Socket socket = new Socket(serverAddress, portNum);
			inputReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			outputWriter = new PrintWriter(socket.getOutputStream(), true);
			while (true) {
				String line = inputReader.readLine();
				if (line.startsWith("SUBMITNAME")) outputWriter.println(getName());
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
    
    public static void main(String[] agrs){
    	Client c = new Client();
    	c.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        c.frame.setVisible(true);
        c.run();
    }
    
}
