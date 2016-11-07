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

/**
 * 
 * @author justinlee
 *
 */
public class Client {
	
	BufferedReader inputReader;
    PrintWriter outputWriter;
    JFrame frame = new JFrame("Client Message GUI.");
    JTextField textField = new JTextField(40);
    JTextArea messageArea = new JTextArea(10, 40);
    
    public Client(){
        textField.setEditable(false);
        messageArea.setEditable(false);
        frame.getContentPane().add(textField, "Center");
        frame.getContentPane().add(new JScrollPane(messageArea), "North");
        frame.pack();
        
        textField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                outputWriter.println(textField.getText());
                textField.setText("");
            }
        });
    }
    
    private String getServerAddress() {
        return JOptionPane.showInputDialog(
            frame,
            "Enter IP Address of the Server:",
            "Welcome to the Chatter",
            JOptionPane.QUESTION_MESSAGE);
    }
    
    private String getName() {
        return JOptionPane.showInputDialog(
            frame,
            "Choose a screen name:",
            "Screen name selection",
            JOptionPane.PLAIN_MESSAGE);
    }
    
    private void run(){
    	try{
        // Make connection and initialize streams
        String serverAddress = getServerAddress();
        Socket socket = new Socket(serverAddress, 9001);
        inputReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        outputWriter = new PrintWriter(socket.getOutputStream(), true);

        // Process all messages from server, according to the protocol.
        while (true) {
            String line = inputReader.readLine();
            if (line.startsWith("SUBMITNAME")) {
                outputWriter.println(getName());
            } else if (line.startsWith("NAMEACCEPTED")) {
                textField.setEditable(true);
            } else if (line.startsWith("MESSAGE")) {
                messageArea.append(line.substring(8) + "\n");
            }
        }
    	}
    	catch(IOException e){
    		System.out.println("Client: " + e.getMessage());
    	}
    }
    
    public static void main(String[] agrs){
    	Client c = new Client();
    	c.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        c.frame.setVisible(true);
        c.run();
    }
    
}
