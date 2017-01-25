package vote;

import java.io.Serializable;
import java.util.ArrayList;

public class FileSendObject implements Serializable{
	private static final long serialVersionUID = 5817338911028258547L;
	private ArrayList<String[]> voter_list;
	private VoteInfo vote_info;

	public FileSendObject(ArrayList<String[]> vl,VoteInfo  vi){
		voter_list = vl;
		vote_info = vi;
	}

	public ArrayList<String[]> getVoter_list() {
		return voter_list;
	}

	public void setVoter_list(ArrayList<String[]> voter_list) {
		this.voter_list = voter_list;
	}

	public VoteInfo getVote_info() {
		return vote_info;
	}

	public void setVote_info(VoteInfo vote_info) {
		this.vote_info = vote_info;
	}
}
