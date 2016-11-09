import java.io.PrintWriter;

public class Chat {
	private String username1;
	private String username2;
	private PrintWriter pw1;
	private PrintWriter pw2;
	
	public Chat(String username1, String username2, PrintWriter pw1, PrintWriter pw2){
		this.username1 = username1;
		this.username2 = username2;
		this.pw1 = pw1;
		this.pw2 = pw2;
	}
	
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
	
	public boolean equals(Chat c){
		if(this.username1.equals(c.getUsername1()) && 
				this.username2.equals(c.getUsername2()) && 
				this.pw1.equals(c.getPW1()) && this.pw2.equals(c.getPW2())){
			return true;
		}
		return false;
	}
}
