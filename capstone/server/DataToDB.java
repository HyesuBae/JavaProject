package server;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Random;

import vote.ContentsObject;
import vote.VoteInfo;

import communication.DBComm;

public class DataToDB {
	private static final String CANDIDATES = "candidates", USERS = "users",
			ELECTIONS = "elections", UNIQUE_ID = "uniqueid";
	private DBComm dbComm = new DBComm();

	private boolean isRandomStrUsed(String _randStr, String _dbName,
			String _tableName, String _columnName) {
		Connection conn = null;
		if ((conn = dbComm.dbConnect(_dbName)) == null)
			return false;

		StringBuilder query = new StringBuilder("select " + _columnName
				+ " from ");
		query.append(_tableName);
		query.append(" where " + _columnName + "=");
		query.append(_randStr);
		query.append("");

		ResultSet rs = dbComm.stmtExecuteQuery(query.toString(), conn);
		if (rs == null)
			return false;

		try {
			if (rs.next())
				return true; // electionId exists
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return false;
	}

	private String makeRandomString(int _length) {
		String alphabet = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
		int length = alphabet.length();

		Random r = new Random();
		StringBuilder query = new StringBuilder();
		for (int i = 0; i < _length; i++) {
			query.append(alphabet.charAt(r.nextInt(length)));
		}

		return query.toString();
	}

	// 다 바꿨음
	public boolean candToDB(VoteInfo _voteInfo) {
		Connection conn = dbComm.dbConnect(CANDIDATES);
		if (conn == null)
			return false;

		String electionId = _voteInfo.getElectionID();
		String electionKey = _voteInfo.getElectionKey();
		ArrayList<String> candidates = _voteInfo.getCandidates();
		ArrayList<String> candEmail = _voteInfo.getCandEmails();
		ArrayList<ArrayList<String>> representatives = _voteInfo
				.getRepresentatives();

		String tableName = dbComm.createTableName(electionId, CANDIDATES);

		if (dbComm.isTableUsed(tableName, conn)) {
			System.out.println(tableName + " is already used");
			return false;
		}

		StringBuilder createquery = new StringBuilder("create table ");
		createquery.append(tableName);
		createquery.append("(symbol int primary key not null");
		createquery.append(",cand char(40) character set utf8 not null");
		createquery.append(",rep1 char(40) character set utf8");
		createquery.append(",rep2 char(40) character set utf8");
		createquery.append(",rep3 char(40) character set utf8");
		createquery.append(",rep4 char(40) character set utf8");
		createquery.append(",rep5 char(40) character set utf8");
		createquery.append(",candEmail blob");
		createquery.append(",candPswd blob)");

		// create candidate table repr column dynamically?

		if (!dbComm.stmtExecute(createquery.toString(), conn))
			return false;

		StringBuilder insertquery;
		String strMail = null;
		for (int i = 0; i < candidates.size(); i++) {
			if (candEmail.size() != 0) {
				strMail = "AES_ENCRYPT('" + candEmail.get(i) + "','"
						+ electionKey + "')";
			}

			int symbol = i + 1;
			String candName = candidates.get(i);
			String[] repr = { null, null, null, null, null };

			int numOfRepr = representatives.get(i).size();
			if (numOfRepr != 0) {
				for (int j = 0; j < numOfRepr; j++) {
					repr[j] = representatives.get(i).get(j);
				}
			}
			insertquery = new StringBuilder("insert into " + tableName
					+ " values(");
			insertquery.append(symbol + ",");
			insertquery.append("'" + candName + "',");

			for (int j = 0; j < 5; j++) {
				if (repr[j] == null)
					insertquery.append(repr[j]);
				else
					insertquery.append("'" + repr[j] + "'");
				insertquery.append(",");
			}
			insertquery.append(strMail);
			insertquery.append(",null)");

			if (!dbComm.stmtExecute(insertquery.toString(), conn))
				return false;
		}
		return true;
	}

	public VoteInfo voteInfoToDB(VoteInfo _voteInfo) {
		Connection conn = null;
		if ((conn = dbComm.dbConnect(ELECTIONS)) == null)
			return null;

		String title = _voteInfo.getTitle();
		String startTime = _voteInfo.getStartTime();
		String endTime = _voteInfo.getEndTime();
		String manager = _voteInfo.getEmail();
		String pswd = _voteInfo.getPswd();
		int totalNum = _voteInfo.getTotalNum();

		// make electionId of elections
		String electionId;
		do {
			electionId = String.valueOf(new Random().nextInt(100000000));

		} while (isRandomStrUsed("'" + electionId + "'", ELECTIONS, ELECTIONS,
				"electionId"));
		_voteInfo.setElectionID(electionId);

		// make key of elections
		String electionKey;
		String value;
		do {
			electionKey = makeRandomString(15);
			value = "SHA('" + electionKey + "')";
		} while (isRandomStrUsed(value, ELECTIONS, ELECTIONS, "electionKey"));
		_voteInfo.setElectionKey(electionKey);

		StringBuilder query = new StringBuilder("insert into " + ELECTIONS
				+ " values(");
		query.append("'" + electionId + "',");
		query.append("'" + title + "',");
		query.append("'" + startTime + "',");
		query.append("'" + endTime + "',");
		query.append("AES_ENCRYPT('" + manager + "','" + electionKey + "'),");
		query.append("SHA('" + pswd + "'),");
		query.append("AES_ENCRYPT(" + totalNum + ",'" + electionKey + "'),");
		query.append("AES_ENCRYPT(0,'" + electionKey + "'),");
		query.append("SHA('" + electionKey + "'))");

		// 모든 정보를 암호화???
		/*
		 * StringBuilder query = new StringBuilder("insert into " + ELECTIONS +
		 * " values("); query.append("AES_ENCRYPT('" + electionId + "','" +
		 * electionKey + "'),"); query.append("AES_ENCRYPT('" + title + "','" +
		 * electionKey + "'),"); query.append("AES_ENCRYPT('" + startTime +
		 * "','" + electionKey + "'),"); query.append("AES_ENCRYPT('" + endTime
		 * + "','" + electionKey + "'),"); query.append("AES_ENCRYPT('" +
		 * manager + "','" + electionKey + "'),"); query.append("SHA('" + pswd +
		 * "'),"); query.append("AES_ENCRYPT(" + totalNum + ",'" + electionKey +
		 * "'),"); query.append("AES_ENCRYPT(0,'" + electionKey + "'),");
		 * query.append("SHA('" + electionKey + "'))");
		 */

		if (!dbComm.stmtExecute(query.toString(), conn)) {
			dbComm.deleteElection(electionId);
			return null;
		}

		if (!candToDB(_voteInfo)) {
			System.out.println("candToDB error");
			deleteAll(electionId, electionKey);
			return null;
		}
		return _voteInfo;
	}

	// 다 바꿨음
	public ArrayList<ArrayList<String>> excelToDB(String _state,
			ArrayList<String[]> _excelList, String _electionKey,
			String _electionId) {
		Connection conn = dbComm.dbConnect(USERS);
		if (conn == null) {
			return null;
		}

		ArrayList<ArrayList<String>> userSerialList = new ArrayList<ArrayList<String>>();
		userSerialList.add(new ArrayList<String>());
		userSerialList.add(new ArrayList<String>());
		ResultSet originData = null;
		if (_state.equals("update")) {
			StringBuilder query = new StringBuilder("select * from " + USERS);
			query.append(" where electionId='" + _electionId + "'");

			// to restore original data
			originData = dbComm.stmtExecuteQuery(query.toString(), conn);
			System.out.println("delete origin data");

			if (!dbComm.deleteData(USERS, "electionId", _electionId, USERS)) {
				System.out.println("delete data fail");
				return null;
			}
		}

		String userSerial;
		for (int i = 1; i < _excelList.size(); i++) {
			do {
				userSerial = makeRandomString(8);
			} while (isRandomStrUsed("'" + userSerial + "'", USERS, USERS,
					"userSerial"));

			StringBuilder query = new StringBuilder("insert into ");
			query.append(USERS);
			query.append(" values(");

			query.append("SHA('" + userSerial + "'),");
			query.append("AES_ENCRYPT('" + _excelList.get(i)[0] + "','"
					+ userSerial + "'),");
			//userName
			//query.append("AES_ENCRYPT('" + _excelList.get(i)[1] + "','"
			//		+ userSerial + "'),");
			query.append("AES_ENCRYPT('" + _electionKey + "','" + userSerial
					+ "'),");
			query.append("AES_ENCRYPT(false,'" + userSerial + "'),");
			query.append("'" + _electionId + "')");

			userSerialList.get(0).add(_excelList.get(i)[0]);
			userSerialList.get(1).add(userSerial);

			if (!dbComm.stmtExecute(query.toString(), conn)) {
				if (_state.equals("update")) {
					System.out.println("update voter table fail");
					restoreUsers(originData);
					return null;
				}
				return null;
			}

		}

		return userSerialList;
	}

	public boolean deleteAll(String _electionId, String _electionKey) {
		// delete voters
		// delete users method하나 만들기
		if (!dbComm.deleteData(USERS, "electionId", _electionId, USERS))
			return false;
		// delete candidates
		String tableName = dbComm.createTableName(_electionId, CANDIDATES);
		if (!dbComm.dropTable(CANDIDATES, tableName))
			return false;
		// delete election
		if (!dbComm.deleteElection(_electionId))
			return false;

		return true;
	}

	// when receive contobject from veb server before vote
	public String createUniqueId(ContentsObject _contObj, String _electionKey) {
		Connection conn = dbComm.dbConnect(UNIQUE_ID);
		int uniqueId;
		StringBuilder query;
		ResultSet rs;
		String userEmail = _contObj.getEmail();
		String userSerial = _contObj.getUserSerial();

		try {
			do {
				uniqueId = (int) (Math.random() * 10000000);
				query = new StringBuilder("select * from ");
				query.append(UNIQUE_ID);
				query.append(" where id=SHA(" + uniqueId + ")");

				rs = dbComm.stmtExecuteQuery(query.toString(), conn);

			} while (rs.next());
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}

		Calendar cal = Calendar.getInstance();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		String validTime = dateFormat.format(cal.getTime());
		System.out.println(validTime);

		query = new StringBuilder("insert into ");
		query.append(UNIQUE_ID);
		query.append(" values(");
		query.append("SHA('" + uniqueId + "'),");
		query.append("AES_ENCRYPT('" + userSerial + "','" + uniqueId + "'),");
		query.append("AES_ENCRYPT('" + userEmail + "','" + uniqueId + "'),");
		query.append("AES_ENCRYPT('" + _electionKey + "','" + uniqueId + "'),");
		query.append("'" + validTime + "')");

		// encrypt해서 넣기

		if (!dbComm.stmtExecute(query.toString(), conn)) {
			return null;
		}

		return String.valueOf(uniqueId);
	}

	private boolean restoreUsers(ResultSet _rs) {
		Connection conn = dbComm.dbConnect(USERS);
		if (conn == null)
			return false;

		StringBuilder query = new StringBuilder("insert into ");
		query.append(USERS);
		query.append(" values(");
		try {
			while (_rs.next()) {
				query.append("'" + _rs.getString("userSerial") + "'");
				query.append("'" + _rs.getString("email") + "'");
				query.append("'" + _rs.getString("electionKey") + "'");
				query.append("'" + _rs.getString("isvote") + "'");
				query.append("'" + _rs.getString("electionId") + "'");

				if (!dbComm.stmtExecute(query.toString(), conn))
					return false;
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}
}