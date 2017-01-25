package server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SealedObject;

import vote.ContentsObject;
import vote.DBMessage;
import vote.FileSendObject;
import vote.LoginInfo;
import vote.Message;
import vote.VoteInfo;
import access.filesystem.ConnectionInfo;
import access.filesystem.StateInfo;
import cipher.ServerKeys;
import communication.ObjectComm;

public class VerificationServer implements Runnable {
	private static final String VOTE_OVER = "vote over";
	private static final String UNKNOWN = "unknown";
	
	private static boolean isMaintenance;
	private static boolean isWebComplete, isVebComplete, isDbComplete;
	private static String dbIp, webIp, vebIp;
	private static int dbPort, webPort, vebPort, verifPort;

	private Socket clientSocket;
	private String clientAddr;
	private Message msg;
	private DataToDB dataToDB;

	private ObjectComm objComm = new ObjectComm();
	private FindFromDB findFromDB = new FindFromDB();

	private VerificationServer(Socket _clientSoc) {
		InetAddress inetaddr = _clientSoc.getInetAddress();
		clientAddr = inetaddr.getHostAddress();
		if(clientAddr.equals(ConnectionInfo.getWebIP())){
			clientAddr="WEB server";
		}else if(clientAddr.equals(ConnectionInfo.getVebIP())){
			clientAddr="VEB server";
		}else if(clientAddr.equals(ConnectionInfo.getDbIP())){
			clientAddr="DB server";
		}else{
			clientAddr = UNKNOWN;
		}
		
		System.out.print("* * * * * * * * ");
		System.out.print("access from " + clientAddr);
		System.out.println(" * * * * * * * *");
		clientSocket = _clientSoc;
		dataToDB = new DataToDB();
	}

	public static void main(String[] args) {
		KeyTimer timer = new KeyTimer();
		dbIp = ConnectionInfo.getDbIP();
		webIp = ConnectionInfo.getWebIP();
		vebIp = ConnectionInfo.getVebIP();

		dbPort = ConnectionInfo.getDbPort();
		webPort = ConnectionInfo.getWebPort();
		vebPort = ConnectionInfo.getVebPort();
		verifPort = ConnectionInfo.getVerifPort();

		ServerSocket serverSocket = null;
		try {
			serverSocket = new ServerSocket(verifPort);
			while (true) {
				Thread t = new Thread(new VerificationServer(
						serverSocket.accept()));
				t.start();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				serverSocket.close();
				timer.stop();

			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}// main

	@Override
	public void run() {
		if(clientAddr.equals(UNKNOWN)){
			System.out.println("unknown host access!! access is blocked.");
			return;
		}
		msg = (Message) objComm.recvObject(clientSocket);
		int state = msg.getState();

		if (isMaintenance) {
			switch (state) {
			case StateInfo.ST_WEB_COMPLETE:
				System.out.println("web all complete");
				isWebComplete = true;
				break;
			case StateInfo.ST_DB_COMPLETE:
				System.out.println("db all complete");
				isDbComplete = true;
				break;
			case StateInfo.ST_VEB_COMPLETE:
				System.out.println("veb all complete");
				isVebComplete = true;
				break;
			case StateInfo.ST_WEB_FAIL:
				System.out.println("web all fail");
				isWebComplete = false;
				break;
			case StateInfo.ST_DB_FAIL:
				System.out.println("db all fail");
				isDbComplete = false;
				break;
			case StateInfo.ST_VEB_FAIL:
				System.out.println("veb all fail");
				isVebComplete = false;
				break;
			default:
				System.out.println("Server is now maintenance in progress");
				Message sendMsg = new Message(StateInfo.ST_MAINTENANCE, null);
				objComm.sendObject(sendMsg, clientSocket);
				return;
			}

			if (isWebComplete && isDbComplete && isVebComplete) {
				System.out.println("Maintenance in progress is over");
				isMaintenance = false;
				updateTimeOver();
			}
			System.out.print("* * * * * * * * ");
			System.out.print(clientAddr + " session over");
			System.out.println(" * * * * * * * *\n\n");
		} else {
			new UpdateDB().deleteUniqueId();
			checkTime();
			
			if (checkState(state)) {
				System.out.print("* * * * * * * * ");
				System.out.print(clientAddr + " session over");
				System.out.println(" * * * * * * * *\n\n");
			} else {
				System.out.print("* * * * * * * * ");
				System.out.print(clientAddr + " session fail");
				System.out.println(" * * * * * * * *\n\n");
			}
			try {
				clientSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		objComm.closeAll();
	}

	private boolean checkState(int _state) {
		switch (_state) {
		case StateInfo.ST_SEND_VOTE_INFO:
			return registerElection();
		case StateInfo.ST_FIND_ELECTION:
			return checkElection();
		case StateInfo.ST_CAND_REGISTER_PW:
			return regCandPw();
		case StateInfo.ST_CAND_CHECK:
			return checkCandPw();
		case StateInfo.ST_VOTE_COMPLETE:
			return voteComplete();
		case StateInfo.ST_CHECK_UNIQUE_ID:
			return sendEmail();
		case StateInfo.ST_CHECK_VOTE_FIRST:
			return checkVoteFirst();
		case StateInfo.ST_CHECK_MANAGER:
			return checkManager();
		case StateInfo.ST_UPDATE_ELECTION:
			return updateElection();
		case StateInfo.ST_UPDATE_EXCEL:
			return updateExcel();
		default:
			return false;
		}
	}

	public static void setIsMaintenance(boolean b) {
		isMaintenance = b;
	}

	// unique id도 삭제하기
	// whenever there is an access to this server, check election time
	// and unique id time to delete old data;
	private boolean checkTime() {
		UpdateDB updateDB = new UpdateDB();
		ArrayList<String> electionIdList = updateDB.checkElectionTime();

		if (electionIdList.size() < 1 || electionIdList == null)
			return true;

		Message sendMsg = new Message(StateInfo.ST_TIME_CHECK, electionIdList);

		try {
			Socket socketForDB = new Socket(dbIp, dbPort);
			Message recvMsg = (Message) objComm
					.commObject(sendMsg, socketForDB);

			if (recvMsg.getState() == StateInfo.ST_TIME_CHECK_SUCCESS) {
				return true;
			} else if (recvMsg.getState() == StateInfo.ST_TIME_CHECK_FAIL) {
				System.out.println("time check from db server err");
			} else {
				System.out.println(" wrong Message from db server!!");
			}

			socketForDB.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return false;
	}

	private boolean checkVote(ContentsObject _contObj) {
		Message sendMsg;

		int state = findFromDB.checkVote(_contObj);
		if (state == StateInfo.ST_VOTE_FAIL || state == StateInfo.ST_NOT_VOTER
				|| state == StateInfo.ST_ALREADY_VOTE
				|| state == StateInfo.ST_START_TIME_ERR
				|| state == StateInfo.ST_END_TIME_ERR) {
			sendMsg = new Message(state, null);
			return objComm.sendObject(sendMsg, clientSocket);
		}

		return true;
	}

	// when client first clicks vote button at web page
	private boolean checkVoteFirst() {
		// contents Object from web server which contains userSerial and email
		ContentsObject contObj = (ContentsObject) msg.getObj();
		Message sendMsg;
		if (!checkVote(contObj)) { // objComm err
			sendMsg = new Message(StateInfo.ST_VERIF_ERR, null);
			return objComm.sendObject(sendMsg, clientSocket);
		}

		String electionKey = findFromDB.getElectionKey(contObj.getUserSerial());

		String uniqueId = dataToDB.createUniqueId(contObj, electionKey);// //////////////////////////////
		sendMsg = new Message(StateInfo.ST_CAN_VOTE, uniqueId);
		return objComm.sendObject(sendMsg, clientSocket);

	}

	// when veb server send object to verif server before vote
	private boolean sendEmail() {
		Message sendMsg;

		// receive unique id from veb server
		int uniqueId = (int) Integer.parseInt((String) msg.getObj());

		ContentsObject contObj = findFromDB.getUniqueIdInfo(uniqueId);
		if (contObj == null) {
			sendMsg = new Message(StateInfo.ST_UNIQUE_ID_FAIL, null);
			return objComm.sendObject(sendMsg, clientSocket);
		}

		if (!checkVote(contObj)) {
			sendMsg = new Message(StateInfo.ST_VERIF_ERR, null);
			return objComm.sendObject(sendMsg, clientSocket);
		}

		String userEmail = contObj.getEmail();
		String userSerial = contObj.getUserSerial();
		String acceptNum = new MailCert().sendMail(userEmail);

		VoteInfo sendVoteInfo = findFromDB.findElection(userSerial, userEmail);

		if (sendVoteInfo == null) {
			sendMsg = new Message(StateInfo.ST_VERIF_ERR, null);
		} else {
			sendVoteInfo.setPswd(String.valueOf(acceptNum));
			sendMsg = new Message(StateInfo.ST_UNIQUE_ID_SUCCESS, sendVoteInfo);
		}
		
		System.out.println(sendVoteInfo.getCandidates());

		return objComm.sendObject(sendMsg, clientSocket);

	}

	// when veb send contentsObject to verif server after vote
	private boolean voteComplete() {
		Message sendMsg;
		ContentsObject recvContObj = (ContentsObject) msg.getObj();
		int uniqueId = Integer.parseInt(recvContObj.getEmail());
		
		ContentsObject newContObj = findFromDB.getUniqueIdInfo(uniqueId);
		//newContObj contains "uniqueIdInfo", userEmail, electionKey;
		
		String electionKey = (String)newContObj.getObject();
		String electionId = findFromDB.getElectionId(electionKey);
		int symToVote = (int)recvContObj.getObject(); 
		
		if (!checkVote(newContObj)) {
			sendMsg = new Message(StateInfo.ST_VERIF_ERR, null);
			return objComm.sendObject(sendMsg, clientSocket);
		}

		Socket socketForDB = null;
		try {
			socketForDB = new Socket(dbIp, dbPort);
			if (!socketForDB.isConnected()) {
				System.out.println("DB server is not connected");
				socketForDB.close();
				return false;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		DBMessage dbMessage = new DBMessage();
		dbMessage.setElectionID(electionId);
		dbMessage.setElectionKey(electionKey);
		dbMessage.setObj(symToVote);

		sendMsg = new Message(StateInfo.ST_VOTE_COMPLETE, dbMessage);
		Message recvMsg = (Message) objComm.commObject(sendMsg, socketForDB);

		if (recvMsg.getState() == StateInfo.ST_VOTE_SUCCESS) {
			sendMsg = new Message(StateInfo.ST_VOTE_SUCCESS, null);
			newContObj.setObject(uniqueId);
			if (!setVoteOver(newContObj)) {
				System.out.println("set vote true fail at verif server");
				sendMsg = new Message(StateInfo.ST_VOTE_FAIL,
						(ContentsObject) msg.getObj());
				recvMsg = (Message) objComm.commObject(sendMsg, socketForDB);
				if (recvMsg.getState() == StateInfo.ST_REMOVE_VOTE_SUCCESS) {
					System.out.println("remove vote from db server success");
				} else if (recvMsg.getState() == StateInfo.ST_REMOVE_VOTE_FAIL) {
					System.out.println("remove vote from db server fail");
				} else {
					System.out.println("wrong msg(required ST_REMOVE_VOTE");
				}
				sendMsg = new Message(StateInfo.ST_VOTE_FAIL, null);

			}
		} else if (recvMsg.getState() == StateInfo.ST_VOTE_FAIL) {
			System.out.println("check vote fail at db server");
			sendMsg = new Message(StateInfo.ST_VOTE_FAIL, null);
		} else {
			System.out.println("wrong msg from db server(required vote msg)");
			sendMsg = new Message(StateInfo.ST_VOTE_FAIL, null);
		}

		new UpdateDB().deleteUniqueID(uniqueId);

		try {
			socketForDB.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return objComm.sendObject(sendMsg, clientSocket);
	}

	// when manager update election. object from web server
	private boolean updateElection() {
		UpdateDB updateDB = new UpdateDB();
		VoteInfo recvVoteInfo = (VoteInfo) msg.getObj();
		
		String managerSerial = recvVoteInfo.getEmail();
		String electionKey = findFromDB.getElectionKey(managerSerial);
		System.out.println("manager serial at server:"+managerSerial);
		recvVoteInfo.setElectionKey(electionKey);
		Message sendMsg;
		if (!updateDB.updateElection(recvVoteInfo)){
			sendMsg = new Message(StateInfo.ST_UPDATE_ELECTION_FAIL, null);
			objComm.sendObject(sendMsg, clientSocket);
			return false;
		}

		return updateResult(recvVoteInfo);
	}

	// when manager upload new excel file. from web server
	private boolean updateExcel() {
		UpdateDB updateDB = new UpdateDB();
		FileSendObject fso = (FileSendObject) msg.getObj();
		ArrayList<String[]> excel = fso.getVoter_list();
		VoteInfo recvVoteInfo = fso.getVote_info();

		String managerSerial = recvVoteInfo.getEmail();
		String electionKey = findFromDB.getElectionKey(managerSerial);
		recvVoteInfo.setElectionKey(electionKey);

		Message sendMsg;
		if (dataToDB.excelToDB("update", excel, electionKey,
				recvVoteInfo.getElectionID()) == null) {
			sendMsg = new Message(StateInfo.ST_UPDATE_EXCEL_FAIL, null);
			return objComm.sendObject(sendMsg, clientSocket);
		}

		if (!updateDB.updateElection(recvVoteInfo)) {
			sendMsg = new Message(StateInfo.ST_UPDATE_EXCEL_FAIL, null);
			return objComm.sendObject(sendMsg, clientSocket);
		}

		return updateResult(recvVoteInfo);
	}

	// when manager updates election information,
	// db server also have to update result table
	private boolean updateResult(VoteInfo _voteInfo) {
		Socket socketForDB = null;
		try {
			socketForDB = new Socket(dbIp, dbPort);
			if (!socketForDB.isConnected()) {
				System.out.println("DB server is not connected");
				socketForDB.close();
				return false;
			}
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		ArrayList<String> candidates = _voteInfo.getCandidates();
		candidates.add(String.valueOf(_voteInfo.getTotalNum()));

		String electionId = _voteInfo.getElectionID();
		String electionKey = _voteInfo.getElectionKey();
		DBMessage dbMessage = new DBMessage();
		dbMessage.setElectionID(electionId);
		dbMessage.setElectionKey(electionKey);
		dbMessage.setObj(candidates);

		Message sendMsg = new Message(StateInfo.ST_UPDATE_ELECTION, dbMessage);
		Message recvMsg = (Message) objComm.commObject(sendMsg, socketForDB);

		if (recvMsg.getState() == StateInfo.ST_UPDATE_ELECTION_SUCCESS) {
			System.out.println("update election success");
			sendMsg = new Message(StateInfo.ST_UPDATE_ELECTION_SUCCESS, null);
		} else if (recvMsg.getState() == StateInfo.ST_UPDATE_ELECTION_FAIL) {
			System.out.println("update election fail at db server");
			sendMsg = new Message(StateInfo.ST_UPDATE_ELECTION_FAIL, null);
		} else {
			System.out
					.println("wrong msg from db server(required update election");
			sendMsg = new Message(StateInfo.ST_UPDATE_ELECTION_FAIL, null);
		}

		try {
			socketForDB.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return objComm.sendObject(sendMsg, clientSocket);
	}

	// when manager log in, check manager email and pswd
	private boolean checkManager() {
		Message sendMsg;
		if (findFromDB.checkManager((ContentsObject) msg.getObj()))
			sendMsg = new Message(StateInfo.ST_CHECK_MANAGER_SUCCESS, null);
		else
			sendMsg = new Message(StateInfo.ST_CHECK_MANAGER_FAIL, null);

		return objComm.sendObject(sendMsg, clientSocket);
	}

	// when set vote success from db server, update isvote = true of userSerial
	private boolean setVoteOver(ContentsObject _contObj) {
		UpdateDB updateDB = new UpdateDB();
		return updateDB.updateIsVoteTrue(_contObj);
	}

	// when client log - in at web server. first page (index)
	private boolean checkElection() {
		VoteInfo sendVoteInfo;
		Message sendMsg;
		LoginInfo loginInfo = (LoginInfo) msg.getObj();
		String userSerial = loginInfo.getUserSerial();
		String userEmail = loginInfo.getEmail();

		sendVoteInfo = findFromDB.findElection(userSerial, userEmail);
		if (sendVoteInfo == null) {
			System.out.println("cannot find election");
			sendMsg = new Message(StateInfo.ST_NOT_FOUND_ELECTION, null);
			return objComm.sendObject(sendMsg, clientSocket);
		}

		String voteOver = sendVoteInfo.getPswd();
		if (voteOver != null && voteOver.equals(VOTE_OVER)) {
			String electionKey = findFromDB.getElectionKey(userSerial);

			DBMessage dbMessage = new DBMessage();
			dbMessage.setElectionKey(electionKey);
			dbMessage.setElectionID(sendVoteInfo.getElectionID());

			Key db_sym_key = new ServerKeys()
					.getSymmetricKey(ConnectionInfo.DB_SERVER);

			Cipher c;
			SealedObject dbMsgSo = null;
			try {
				c = Cipher.getInstance("AES");
				c.init(Cipher.ENCRYPT_MODE, db_sym_key);
				dbMsgSo = new SealedObject(dbMessage, c);
			} catch (NoSuchAlgorithmException | NoSuchPaddingException
					| InvalidKeyException | IllegalBlockSizeException
					| IOException e) {
				e.printStackTrace();
				return false;
			}
			sendVoteInfo.setDbMessage(dbMsgSo);
			sendMsg = new Message(StateInfo.ST_END_ELECTION_SUCCESS,
					sendVoteInfo);
		} else {
			sendMsg = new Message(StateInfo.ST_FOUND_ELECTION, sendVoteInfo);
		}

		return objComm.sendObject(sendMsg, clientSocket);
	}

	// when manager register new elections
	private boolean registerElection() {
		Message sendMsg;

		// get voteInfo and voter_list from web server
		FileSendObject recvFso = (FileSendObject) msg.getObj();
		VoteInfo recvVoteInfo = (VoteInfo) recvFso.getVote_info();
		ArrayList<String[]> excelList = recvFso.getVoter_list();

		// insert election information(voteInfo) to table 'elections'
		// recvVoteInfo which contains new electionId and new electionKey
		recvVoteInfo = dataToDB.voteInfoToDB(recvVoteInfo);
		if (recvVoteInfo == null) {
			sendMsg = new Message(StateInfo.ST_RECV_VOTE_INFO_FAIL, null);
			return objComm.sendObject(sendMsg, clientSocket);
		}

		String electionId = recvVoteInfo.getElectionID();
		String electionKey = recvVoteInfo.getElectionKey();

		// insert new user list(excelList) to table 'users'
		ArrayList<ArrayList<String>> userSerialList = dataToDB.excelToDB("new",
				excelList, electionKey, electionId);
		if (userSerialList == null) {
			System.out.println("excel to db err");
			dataToDB.deleteAll(electionId, electionKey);
			sendMsg = new Message(StateInfo.ST_RECV_VOTE_INFO_FAIL, null);
			return objComm.sendObject(sendMsg, clientSocket);
		}

		if (!makeResult(recvVoteInfo)) {
			System.out.println("make result table at db server fail");
			dataToDB.deleteAll(electionId, electionKey);
			System.out.println("삭제하기");
			sendMsg = new Message(StateInfo.ST_RECV_VOTE_INFO_FAIL, null);
			return objComm.sendObject(sendMsg, clientSocket);
		}

		if (!new MailCert().sendSerialToAll(recvVoteInfo, userSerialList)) {
			System.out.println("Send serial to all fail");
			sendMsg = new Message(StateInfo.ST_RECV_VOTE_INFO_FAIL,
					clientSocket);
			return objComm.sendObject(sendMsg, clientSocket);
		}

		sendMsg = new Message(StateInfo.ST_RECV_VOTE_INFO_SUCCESS, recvVoteInfo);
		objComm.sendObject(sendMsg, clientSocket);

		System.out.println("register election success");
		return true;
	}

	// makeResult에는 electionId, electionKey, totalNum, candidates list만 필요함.
	// 그것만 넘겨주게 할 것. voteInfo를 다 넘기지 말고!

	// when to register new election is over
	// send candidates list and etc information to db server
	// to make db server create result table for this election
	private boolean makeResult(VoteInfo _voteInfo) {
		Socket socketForDB = null;
		try {
			socketForDB = new Socket(dbIp, dbPort);
			if (!socketForDB.isConnected()) {
				System.out.println("DB server is not connected");
				socketForDB.close();
				return false;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		ArrayList<String> candidates = _voteInfo.getCandidates();
		candidates.add(String.valueOf(_voteInfo.getTotalNum()));

		DBMessage dbMessage = new DBMessage();
		dbMessage.setElectionID(_voteInfo.getElectionID());
		dbMessage.setElectionKey(_voteInfo.getElectionKey());
		dbMessage.setObj(candidates);

		Message sendMsg = new Message(StateInfo.ST_MAKE_RESULT_TAB, dbMessage);
		Message recvMsg = (Message) objComm.commObject(sendMsg, socketForDB);

		int recvState = recvMsg.getState();
		if (recvState == StateInfo.ST_MAKE_RESULT_TAB_SUCCESS) {
			candidates.remove(candidates.size() - 1);
		} else if (recvState == StateInfo.ST_MAKE_RESULT_TAB_FAIL) {
			try {
				socketForDB.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return false;
		} else {
			System.out.println("wrong msg.(required ST_MAKE_RESULT)");
			try {
				socketForDB.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return false;
		}

		try {
			socketForDB.close();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}

		return true;

	}

	// when candidate want to set pswd ( to modify pledge)
	private boolean regCandPw() {
		Message sendMsg;
		UpdateDB updateDB = new UpdateDB();

		if (updateDB.updateCandPswd((ContentsObject) msg.getObj())) {
			System.out.println("register candidate pswd success");
			sendMsg = new Message(StateInfo.ST_CAND_REGISTER_PW_SUCCESS, null);
		} else {
			System.out.println("register candidates pswd fail");
			sendMsg = new Message(StateInfo.ST_CAND_REGISTER_PW_FAIL, null);
		}
		return objComm.sendObject(sendMsg, clientSocket);
	}

	// when candidates log in (to modify pledge page)
	private boolean checkCandPw() {
		Message sendMsg;

		try {
			if (findFromDB.checkCandPw((ContentsObject) msg.getObj())) {
				System.out.println("check candidate pswd success");
				sendMsg = new Message(StateInfo.ST_CAND_CHECK_SUCCESS, null);
			} else {
				System.out.println("check candidate pswd fail");
				sendMsg = new Message(StateInfo.ST_CAND_CHECK_FAIL, null);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}

		return objComm.sendObject(sendMsg, clientSocket);
	}

	// when key exchance time is over,
	// send all server that all maintenance in progress is over
	private boolean updateTimeOver() {
		Message sendMsg = new Message(StateInfo.ST_KEY_UPDATE_TIME_OVER, null);
		Socket socketForWeb, socketForDB, socketForVeb;
		try {
			socketForDB = new Socket(dbIp, dbPort);
			objComm.sendObject(sendMsg, socketForDB);

			socketForVeb = new Socket(vebIp, vebPort);
			objComm.sendObject(sendMsg, socketForVeb);

			socketForWeb = new Socket(webIp, webPort);
			objComm.sendObject(sendMsg, socketForWeb);

		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}

}