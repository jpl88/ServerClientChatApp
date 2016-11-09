import java.io.PrintWriter;

public class Chat {
	//Private fields.
	private String username1;
	private String username2;
	private PrintWriter pw1;
	private PrintWriter pw2;
	
	//Constructor method.
	public Chat(String username1, String username2, PrintWriter pw1, PrintWriter pw2){
		this.username1 = username1;
		this.username2 = username2;
		this.pw1 = pw1;
		this.pw2 = pw2;
	}
	
	//GETTERS & SETTERS
	public String getUsername1(){
		return username1;
	}
	
	public String getUsername2(){
		return username2;
	}
	
	public PrintWriter getPW1(){
		return pw1;
	}
	
	public PrintWriter getPW2(){
		return pw2;
	}

}
