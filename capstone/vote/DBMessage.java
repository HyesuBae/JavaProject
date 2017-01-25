package vote;

import java.io.Serializable;

public class DBMessage implements Serializable{
	private static final long serialVersionUID = 1L;
	private String electionID;
	private String electionKey;
	private Object obj;

	public String getElectionID() {
		return electionID;
	}
	public void setElectionID(String electionID) {
		this.electionID = electionID;
	}
	public String getElectionKey() {
		return electionKey;
	}
	public void setElectionKey(String electionKey) {
		this.electionKey = electionKey;
	}
	public Object getObj() {
		return obj;
	}
	public void setObj(Object obj) {
		this.obj = obj;
	}
	
	
}
