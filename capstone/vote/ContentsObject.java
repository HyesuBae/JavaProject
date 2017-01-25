package vote;

import java.io.Serializable;

public class ContentsObject implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String userSerial;
	private String email;
	private Object object;

	public ContentsObject(String userSerial, String email, Object object) {
		this.userSerial = userSerial;
		this.email = email;
		this.object = object;
	}

	public String getUserSerial() {
		return userSerial;
	}

	public void setUserSerial(String userSerial) {
		this.userSerial = userSerial;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public Object getObject() {
		return object;
	}

	public void setObject(Object object) {
		this.object = object;
	}

}
