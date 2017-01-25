package vote;

import java.io.Serializable;
import java.util.ArrayList;

import javax.crypto.SealedObject;

public class VoteInfo implements Serializable {
	public static final String SESSION_NAME = "voteinfo";
	private static final long serialVersionUID = 1L;
	private String emailAddress;
	private String title;
	private String startTime;
	private String endTime;
	private ArrayList<String> candidates;
	private ArrayList<String> candEmail;
	private ArrayList<ArrayList<String>> representatives;
	private String pswd;
	private String electionID; // election index
	private String electionKey; // election serial
	private boolean pledge;
	private int totalNum;
	private static int numCandidates;
	private SealedObject dbMessage;

	public VoteInfo() {
		numCandidates = 0;
		candidates = new ArrayList<String>();
		representatives = new ArrayList<ArrayList<String>>();
		candEmail = new ArrayList<String>();
		
	}

	public void setEmail(String s) {
		this.emailAddress = s;
	}

	public void setTitle(String s) {
		this.title = s;
	}

	public void setStartTime(String s) {
		this.startTime = s;
	}

	public void setEndTime(String s) {
		this.endTime = s;
	}

	public void addCandidate(String s) {
		candidates.add(s);
		numCandidates ++;
		representatives.add(new ArrayList<String>());
	}

	public void addCandEmail(String s){
		candEmail.add(s);
	}

	public void addRepresentative(String s) {
		representatives.get(numCandidates-1).add(s);
	}

	public void setPswd(String s) {
		this.pswd = s;
	}
	
	public void setElectionID(String i){
		this.electionID = i;
	}

	public void setUsePledgePage(boolean b){
		this.pledge = b;
	}
	
	public void setTotalNum(int n){
		this.totalNum = n;
	}
	
	public String getEmail() {
		return emailAddress;
	}

	public String getTitle() {
		return title;
	}

	public String getStartTime() {
		return startTime;
	}

	public String getEndTime() {
		return endTime;
	}

	public ArrayList<String> getCandidates() {
		return candidates;
	}

	public ArrayList<String> getCandEmails(){
		return candEmail;
	}

	public ArrayList<ArrayList<String>> getRepresentatives() {
		return representatives;
	}

	public String getPswd() {
		return pswd;
	}
		
	public String getElectionID(){
		return electionID;
	}

	public boolean getUsePledgePage(){
		return pledge;
	}
	
	public int getTotalNum(){
		return totalNum;
	}
	
	public void setElectionKey(String s){
		electionKey = s;
	}
	
	public String getElectionKey(){
		return electionKey;
	}

	public SealedObject getDbMessage() {
		return dbMessage;
	}

	public void setDbMessage(SealedObject dbMessage) {
		this.dbMessage = dbMessage;
	}
	
	public void setCandList(ArrayList<String> _list){
		this.candidates = _list;
	}
	
	public void setReprList(ArrayList<ArrayList<String>> _list){
		this.representatives = _list;
	}
	
	public void setCandEmailList(ArrayList<String> _list){
		this.candEmail = _list;
	}
}


