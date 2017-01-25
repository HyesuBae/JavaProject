package vote;

import java.io.Serializable;

public class LoginInfo implements Serializable{
	private static final long serialVersionUID = 1L;
	private String email;
	private String userSerial;
	
	public LoginInfo(String e,String s){
		email = e;
		userSerial = s;
	}
	
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getUserSerial() {
		return userSerial;
	}
	public void setUserSerial(String userSerial) {
		this.userSerial = userSerial;
	}
}
