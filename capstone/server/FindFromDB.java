package server;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

import vote.ContentsObject;
import vote.VoteInfo;
import access.filesystem.StateInfo;

import communication.DBComm;

public class FindFromDB {
	private static final String CANDIDATES = "candidates",
			ELECTIONS = "elections", UNIQUE_ID = "uniqueid", USERS = "users";
	private static final String VOTE_OVER = "vote over";
	private DBComm dbComm;
	private Connection electionsConn;
	private Connection candidatesConn;
	private Connection usersConn;
	private Connection uniqueidConn;

	// connection을 메소드 부를때마다 call하는게 이득인가???
	// findFromDB를 서버에서 한번만 불러놓으면 되지 않을까?

	// dbConnection close하기
	// dbConnection을 열어둔채 계속 서버 돌리면 부하가 큰가?

	public FindFromDB() {
		dbComm = new DBComm();
		electionsConn = dbComm.dbConnect(ELECTIONS);
		candidatesConn = dbComm.dbConnect(CANDIDATES);
		usersConn = dbComm.dbConnect(USERS);
		uniqueidConn = dbComm.dbConnect(UNIQUE_ID);
	}

	// 수정 완료
	public int checkVote(ContentsObject _contObj) {
		String userSerial = _contObj.getUserSerial();
		String userEmail = _contObj.getEmail();
		String electionKey;

		if (!isMatchSerialAndEmail(userSerial, userEmail))
			return StateInfo.ST_NOT_VOTER;

		if (getIsVote(userSerial)) {
			System.out.println("user already vote!");
			return StateInfo.ST_ALREADY_VOTE;
		}

		electionKey = getElectionKey(userSerial);

		Calendar startTime = getElectionTime(electionKey, "start");
		int compare = compareWithCurrentTime(startTime);
		if (compare < 0) {
			System.out.println("election not start yet");
			return StateInfo.ST_START_TIME_ERR;
		}

		Calendar endTime = getElectionTime(electionKey, "end");
		compare = compareWithCurrentTime(endTime);
		if (compare > 0) {
			System.out.println("election is over");
			return StateInfo.ST_END_TIME_ERR;
		}

		System.out.println("can vote");
		return 1;

	}

	// 수정 완료
	public boolean checkManager(ContentsObject _contObj) {
		if (electionsConn == null)
			return false;

		String managerSerial = _contObj.getUserSerial();
		String managerEmail = _contObj.getEmail();

		if (!isMatchSerialAndEmail(managerSerial, managerEmail))
			return false;

		String managerPswd = (String) _contObj.getObject();
		String electionKey = getElectionKey(managerSerial);

		String query = "select AES_DECRYPT(manager,'" + electionKey
				+ "') from " + ELECTIONS + " where electionKey=SHA('"
				+ electionKey + "')" + " and pswd=SHA('" + managerPswd + "')";

		ResultSet rs = dbComm.stmtExecuteQuery(query, electionsConn);
		if (rs == null)
			return false;

		try {
			if (rs.next()) {
				String email = rs.getString("AES_DECRYPT(manager,'"
						+ electionKey + "')");
				if (email.equals(managerEmail)) {
					System.out.println("welcome manager!");
				} else {
					System.out.println("manager email fail");
					return false;
				}
			} else {
				System.out.println("manager pswd or electionId fail");
				return false;
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}

	// 수정 완료
	public VoteInfo findElection(String _userSerial, String _userEmail) {
		if (electionsConn == null)
			return null;

		String electionKey;

		if (!isMatchSerialAndEmail(_userSerial, _userEmail))
			return null;

		electionKey = getElectionKey(_userSerial);

		String electionId = getElectionId(electionKey);

		VoteInfo voteInfo = new VoteInfo();
		voteInfo.setElectionID(electionId);

		String query = "select title, start, end from " + ELECTIONS
				+ " where electionKey=SHA('" + electionKey + "')";

		ResultSet rs = dbComm.stmtExecuteQuery(query, electionsConn);
		if (rs == null)
			return null;
		try {
			if (!rs.next()) {
				System.out.println("there is no election " + electionKey);
				return null;
			}

			Calendar endTime = new GregorianCalendar();
			endTime.setTime(rs.getTimestamp("end"));
			int compare = compareWithCurrentTime(endTime);
			if (compare >= 0) {
				voteInfo.setPswd(VOTE_OVER);
			}

			voteInfo.setTitle(rs.getString("title"));
			voteInfo.setStartTime(rs.getString("start"));
			voteInfo.setEndTime(rs.getString("end"));
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}

		ArrayList<String> columns = new ArrayList<String>();
		columns.add("manager");
		columns.add("total");

		query = dbComm.decryptSelectQuery(ELECTIONS, electionKey, columns);
		query += " where electionKey=SHA('" + electionKey + "')";

		rs = dbComm.stmtExecuteQuery(query, electionsConn);
		if (rs == null)
			return null;

		try {
			if (!rs.next()) {
				System.out.println("there is no election " + electionKey);
				return null;
			}

			String manager = rs.getString("AES_DECRYPT(manager,'" + electionKey
					+ "')");
			int total = rs.getInt("AES_DECRYPT(total,'" + electionKey + "')");

			voteInfo.setEmail(manager);
			voteInfo.setTotalNum(total);

			ArrayList<String> candList = getCandidates(electionId);
			ArrayList<String> candEmailList = getCandEmail(electionId, electionKey);
			ArrayList<ArrayList<String>> reprList = getRepresentatives(electionId);
			
			voteInfo.setCandEmailList(candEmailList);
			voteInfo.setCandList(candList);
			voteInfo.setReprList(reprList);

		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}

		/*
		 * ArrayList<String> columns = new ArrayList<String>();
		 * columns.add("title"); columns.add("start"); columns.add("end");
		 * columns.add("manager"); columns.add("total");
		 * 
		 * String query = dbComm.decryptSelectQuery(ELECTIONS, electionKey,
		 * columns); query = query + " where electionKey=SHA('" + electionKey +
		 * "')";
		 * 
		 * ResultSet rs = dbComm.stmtExecuteQuery(query, electionsConn);
		 * 
		 * try { if (!rs.next()) {
		 * System.out.println("there is no election key " + electionKey); return
		 * null; }
		 * 
		 * String title = rs.getString("AES_DECRYPT(title,'"+electionKey+"')");
		 * String start = rs.getString("AES_DECRYPT(start,'"+electionKey+"')");
		 * String end = rs.getString("AES_DECRYPT(end,'"+electionKey+"')");
		 * String managerEmail = rs.getString("AES_DECRYPT(manager,'" +
		 * electionKey + "')"); int total = rs.getInt("AES_DECRYPT(total,'" +
		 * electionKey + "')");
		 * 
		 * voteInfo.setTitle(title); voteInfo.setStartTime(start);
		 * voteInfo.setEndTime(end); voteInfo.setEmail(managerEmail);
		 * voteInfo.setTotalNum(total);
		 * 
		 * String tableName = dbComm.createTableName(electionId, CANDIDATES);
		 * 
		 * if (!dbComm.isTableUsed(tableName, candidatesConn)) {
		 * System.out.println("no candidates table err"); return null; }
		 * 
		 * Calendar endTime = new GregorianCalendar();
		 * endTime.setTime(rs.getTimestamp("end")); int compare =
		 * compareWithCurrentTime(endTime); if (compare >= 0) {
		 * voteInfo.setPswd(VOTE_OVER); }
		 * 
		 * } catch (SQLException e) { e.printStackTrace(); return null; }
		 */

		return voteInfo;
	}

	// 수정 완료
	public boolean checkCandPw(ContentsObject _contObj) throws SQLException {
		if (candidatesConn == null)
			return false;

		String userSerial = _contObj.getUserSerial();
		String candEmail = _contObj.getEmail();

		if (!isMatchSerialAndEmail(userSerial, candEmail))
			return false;

		String electionKey = getElectionKey(userSerial);
		String electionId = getElectionId(electionKey);
		String candPswd = (String) _contObj.getObject();
		String tableName = dbComm.createTableName(electionId, CANDIDATES);

		String query = "select AES_DECRYPT(candEmail,'" + electionKey + "')"
				+ " from " + tableName + " where candPswd=SHA('" + candPswd
				+ "')";

		ResultSet rs = dbComm.stmtExecuteQuery(query, candidatesConn);
		if (rs == null)
			return false;

		if (rs.next()) {
			if (candEmail.equals(rs.getString("AES_DECRYPT(candEmail,'"
					+ electionKey + "')"))) {
				System.out.println("welcome candidates");
			} else {
				System.out.println("candidates email err");
				return false;
			}
		} else {
			System.out.println("cand pswd err");
			return false;
		}

		return true;
	}

	// 수정완료
	public ContentsObject getUniqueIdInfo(int _uniqueId) {
		String query = "select AES_DECRYPT(email,'" + _uniqueId + "'),"
				+ "AES_DECRYPT(userSerial,'" + _uniqueId + "'),"
				+ "AES_DECRYPT(electionKey,'" + _uniqueId + "') from "
				+ UNIQUE_ID + " where id=SHA('" + _uniqueId + "')";

		ResultSet rs = dbComm.stmtExecuteQuery(query, uniqueidConn);
		if (rs == null) {
			return null;
		}

		String email, electionKey, userSerial;

		try {
			if (!rs.next()) {
				System.out.println("There is no unique id =" + _uniqueId);
				return null;
			}

			userSerial = rs.getString("AES_DECRYPT(userSerial,'" + _uniqueId
					+ "')");
			electionKey = rs.getString("AES_DECRYPT(electionKey,'" + _uniqueId
					+ "')");
			email = rs.getString("AES_DECRYPT(email,'" + _uniqueId + "')");

		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}

		ContentsObject contObj = new ContentsObject(userSerial, email,
				electionKey);
		return contObj;
	}

	// 수정완료
	public boolean isMatchSerialAndEmail(String _userSerial, String _userEmail) {
		if (usersConn == null)
			return false;

		String query = "select AES_DECRYPT(email,'" + _userSerial + "') from "
				+ USERS + " where userSerial=SHA('" + _userSerial + "')";

		ResultSet rs = dbComm.stmtExecuteQuery(query, usersConn);
		if (rs == null)
			return false;

		try {
			if (!rs.next()) {
				System.out.println("there is no userSerial " + _userSerial);
				return false;
			}

			String email = rs.getString("AES_DECRYPT(email,'" + _userSerial
					+ "')");
			if (email.equals(_userEmail)) {
				return true;
			} else {
				System.out.println("userSerial and email doesn't match.");
				return false;
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}

	}

	// 수정 완료
	public String getElectionKey(String _userSerial) {
		if (usersConn == null)
			return null;

		String query = "select AES_DECRYPT(electionKey,'" + _userSerial
				+ "') from " + USERS + " where userSerial=SHA('" + _userSerial
				+ "')";

		ResultSet rs = dbComm.stmtExecuteQuery(query, usersConn);
		if (rs == null) {
			return null;
		}
		try {
			if (!rs.next()) {
				System.out.println("there is no such user serial");
				return null;
			}

			return rs.getString("AES_DECRYPT(electionKey,'" + _userSerial
					+ "')");

		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}

	// 수정완료
	public String getElectionId(String _electionKey) {
		if (electionsConn == null)
			return null;

		String query = "select electionId from " + ELECTIONS
				+ " where electionKey=" + "SHA('" + _electionKey + "')";

		ResultSet rs = dbComm.stmtExecuteQuery(query, electionsConn);
		if (rs == null)
			return null;

		try {
			if (!rs.next()) {
				System.out.println("there is no election which key="
						+ _electionKey);
				return null;
			}

			return rs.getString("electionId");
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}

	}

	// 수정 완료
	private boolean getIsVote(String _userSerial) {
		if (usersConn == null)
			return false;

		String query = "select AES_DECRYPT(isvote,'" + _userSerial + "') from "
				+ USERS + " where userSerial = SHA('" + _userSerial + "')";

		ResultSet rs = dbComm.stmtExecuteQuery(query, usersConn);
		if (rs == null)
			return false;
		try {
			if (!rs.next()) {
				System.out.println("there is no user serial " + _userSerial);
			}

			return rs.getBoolean("AES_DECRYPT(isvote,'" + _userSerial + "')");
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}

	}

	// 수정 완료
	public Calendar getElectionTime(String _electionKey, String _time) {
		if (electionsConn == null)
			return null;

		String query = "select " + _time + " from " + ELECTIONS
				+ " where electionKey=SHA('" + _electionKey + "')";

		ResultSet rs = dbComm.stmtExecuteQuery(query, electionsConn);
		if (rs == null)
			return null;
		try {
			if (!rs.next()) {
				System.out.println("there is no election " + _electionKey);
				return null;
			}

			Calendar time = new GregorianCalendar();
			time.setTime(rs.getTimestamp(_time));

			return time;

		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}

	}

	// 수정 완료
	public int compareWithCurrentTime(Calendar _time) {
		return Calendar.getInstance().compareTo(_time);
	}

	private ArrayList<String> getCandidates(String _electionId) {
		String tableName = dbComm.createTableName(_electionId, CANDIDATES);
		if (!dbComm.isTableUsed(tableName, candidatesConn)) {
			System.out.println("no candidates table err");
			return null;
		}

		String query = "select cand from " + tableName;
		ResultSet rs = dbComm.stmtExecuteQuery(query, candidatesConn);
		ArrayList<String> candList = new ArrayList<String>();
		try {
			while (rs.next()) {
				candList.add(rs.getString("cand"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}

		return candList;
	}

	private ArrayList<ArrayList<String>> getRepresentatives(String _electionId) {
		String tableName = dbComm.createTableName(_electionId, CANDIDATES);
		if (!dbComm.isTableUsed(tableName, candidatesConn)) {
			System.out.println("no candidates table err");
			return null;
		}

		String query = "select * from " + tableName;
		ResultSet rs = dbComm.stmtExecuteQuery(query, candidatesConn);
		ArrayList<ArrayList<String>> reprList = new ArrayList<ArrayList<String>>();
		try {
			ResultSetMetaData metadata = rs.getMetaData();
			int columnCount = metadata.getColumnCount();
			int idx = 0;
			String repr;
			while (rs.next()) {
				reprList.add(new ArrayList<String>());
				for (int i = 0; i < columnCount - 4; i++) {
					repr = rs.getString("rep" + String.valueOf(i + 1));
					if (repr == null) {
						break;
					}
					reprList.get(idx).add(repr);
				}
				idx++;
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}

		return reprList;
	}
	
	private ArrayList<String> getCandEmail(String _electionId, String _electionKey){
		String query = "select AES_DECRYPT(candEmail,'"+_electionKey+"') from "
						+_electionId+"_candidates";
		ArrayList<String> candEmailList = new ArrayList<String>();
		ResultSet rs = dbComm.stmtExecuteQuery(query, candidatesConn);
		try{
			while(rs.next()){
				candEmailList.add(rs.getString("AES_DECRYPT(candEmail,'"+_electionKey+"')"));
			}
		}catch(SQLException e){
			e.printStackTrace();
			return null;
		}
		
		return candEmailList;
	}
}