package server;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

import communication.DBComm;
import vote.ContentsObject;
import vote.VoteInfo;

public class UpdateDB {
	private static final String USERS = "users", CANDIDATES = "candidates",
			ELECTIONS = "elections", UNIQUE_ID = "uniqueid";
	DBComm dbComm = new DBComm();
	FindFromDB findFromDB = new FindFromDB();

	public boolean updateElection(VoteInfo _voteInfo) {
		Connection conn;
		if ((conn = dbComm.dbConnect(ELECTIONS)) == null)
			return false;

		String managerSerial = _voteInfo.getEmail();
		String electionKey = findFromDB.getElectionKey(managerSerial);
		System.out.println("manager serial at election updateDB:"+managerSerial);
		String pswd = _voteInfo.getPswd();

		StringBuilder query = new StringBuilder("select * from " + ELECTIONS);
		query.append(" where electionKey=SHA('" + electionKey + "')");

		ResultSet originData = dbComm.stmtExecuteQuery(query.toString(), conn);

		query = new StringBuilder("");
		query.append("update ");
		query.append(ELECTIONS);
		query.append(" set title='");
		query.append(_voteInfo.getTitle());
		query.append("',start ='");
		query.append(_voteInfo.getStartTime());
		query.append("',end ='");
		query.append(_voteInfo.getEndTime());
		query.append("'");
		if (pswd != null) {
			query.append(",pswd=SHA('");
			query.append(pswd);
			query.append("')");
		}
		query.append(",total=AES_ENCRYPT(");
		query.append(_voteInfo.getTotalNum());
		query.append(",'" + electionKey + "')");
		query.append(" where electionKey=SHA('" + electionKey + "')");

		if (!dbComm.stmtExecute(query.toString(), conn)) {
			System.out.println("update election fail");
			restoreData(originData, ELECTIONS, conn);
			return false;
		}

		if (!updateCand(_voteInfo)){
			restoreData(originData,ELECTIONS,conn);
			return false;
		}
		
		System.out.println("update election success");

		return true;
	}

	private boolean updateCand(VoteInfo _voteInfo) {
		Connection conn = dbComm.dbConnect(CANDIDATES);
		if (conn == null)
			return false;

		ArrayList<String> candidates = _voteInfo.getCandidates();
		ArrayList<String> candEmail = _voteInfo.getCandEmails();
		ArrayList<ArrayList<String>> representatives = _voteInfo
				.getRepresentatives();

		String electionId = _voteInfo.getElectionID();
		String electionKey = findFromDB.getElectionKey(_voteInfo.getEmail());
		System.out.println("manager serial at cand in updateDB:"+_voteInfo.getEmail());
		String tableName = dbComm.createTableName(electionId, CANDIDATES);
		if (!dbComm.isTableUsed(tableName, conn)) {
			System.out.println("There is no table " + tableName);
			return false;
		}

		StringBuilder query = new StringBuilder("select * from ");
		query.append(tableName);

		// original data which is not updated
		ResultSet originData = dbComm.stmtExecuteQuery(query.toString(), conn);

		query = new StringBuilder("delete from ");
		query.append(tableName);

		if (!dbComm.stmtExecute(query.toString(), conn))
			return false;

		String strMail = null;

		for (int i = 0; i < candidates.size(); i++) {
			if (candEmail.size() != 0)
				strMail = "AES_ENCRYPT('" + candEmail.get(i) + "','"
						+ electionKey + "')";

			int symbol = i + 1;
			String candName = candidates.get(i);
			String[] repr = new String[5];

			int numOfRepr = representatives.get(i).size();
			if (numOfRepr != 0) {
				for (int j = 0; j < numOfRepr; j++) {
					repr[j] = (representatives.get(i).get(j));
				}
			}

			query = new StringBuilder("insert into ");
			query.append(tableName);
			query.append(" values(");
			query.append(symbol);
			query.append(",'");
			query.append(candName);
			query.append("',");
			for (int j = 0; j < 5; j++) {
				if (repr[j] == null || repr[j].equals("null"))
					query.append(repr[j]);
				else
					query.append("'" + repr[j] + "'");
				query.append(",");
			}
			query.append(strMail+",null)");
			System.out.println(query);
			if (!dbComm.stmtExecute(query.toString(), conn)) {
				System.out.println("update candidate err");
				restoreData(originData, tableName, conn);
				return false;
			}
		}

		return true;
	}

	public boolean updateCandPswd(ContentsObject _contObj) {
		Connection conn;
		if ((conn = dbComm.dbConnect(CANDIDATES)) == null)
			return false;

		String candSerial = _contObj.getUserSerial();
		String candEmail = _contObj.getEmail();
		String pswd = (String) _contObj.getObject();

		if (!findFromDB.isMatchSerialAndEmail(candSerial, candEmail)) {
			return false;
		}

		String electionKey = findFromDB.getElectionKey(candSerial);
		String electionId = findFromDB.getElectionId(electionKey);
		String tableName = dbComm.createTableName(electionId, CANDIDATES);
		if (!dbComm.isTableUsed(tableName, conn)) {
			System.out.println("There is no table " + tableName);
			return false;
		}

		StringBuilder query = new StringBuilder("select candPswd from ");
		query.append(tableName);
		query.append(" where candEmail='");
		query.append(candEmail);
		query.append("'");

		ResultSet originalData = dbComm
				.stmtExecuteQuery(query.toString(), conn);

		query = new StringBuilder("update ");
		query.append(tableName);
		query.append(" set candPswd=SHA('");
		query.append(pswd);
		query.append("') where candEmail=AES_ENCRYPT('" + candEmail + "','"
				+ electionKey + "')");

		if (!dbComm.stmtExecute(query.toString(), conn)) {
			System.out.println("update candidate pswd fail");
			restoreData(originalData, tableName, conn);
			return false;
		}

		return true;
	}

	public boolean updateIsVoteTrue(ContentsObject _contObj) {
		Connection conn;
		if ((conn = dbComm.dbConnect(USERS)) == null)
			return false;

		String userSerial = _contObj.getUserSerial(); // user serial to set									// isvote=true
		String userEmail = _contObj.getEmail();
		int uniqueId = (int) _contObj.getObject(); // unique id to delete unique
													// id

		if (!findFromDB.isMatchSerialAndEmail(userSerial, userEmail)) {
			return false;
		}

		String electionKey = findFromDB.getElectionKey(userSerial);

		StringBuilder query = new StringBuilder("update " + USERS);
		query.append(" set isVote=AES_ENCRYPT('" + true + "','" + userSerial
				+ "')");
		query.append(" where userSerial=SHA('" + userSerial + "')");

		if (!dbComm.stmtExecute(query.toString(), conn))
			return false;

		if ((conn = dbComm.dbConnect(ELECTIONS)) == null)
			return false;

		query = new StringBuilder(
				"update elections set numofvote=AES_ENCRYPT(numofvote+1,'"
						+ electionKey + "')");
		query.append(" where electionKey=SHA('" + electionKey + "')");

		if (!dbComm.stmtExecute(query.toString(), conn))
			return false;

		if (!deleteUniqueID(uniqueId)) {
			return false;
		}

		System.out.println(userEmail + " isvote set true success");
		return true;
	}

	public ArrayList<String> checkElectionTime() {
		Connection conn;
		if ((conn = dbComm.dbConnect(ELECTIONS)) == null)
			return null;

		ArrayList<String> electionIdList = new ArrayList<String>();

		String query = "select electionId, end from elections order by end asc";
		ResultSet rs = dbComm.stmtExecuteQuery(query, conn);
		if (rs == null)
			return null;

		String electionId = null;
		String tableName = null;

		Calendar endTime = new GregorianCalendar();
		Calendar currentTime = Calendar.getInstance();
		System.out.println("present time : " + currentTime.getTime());

		int compare;
		try {
			while (rs.next()) {
				endTime.setTime(rs.getTimestamp("end"));
				electionId = rs.getString("electionId");
				currentTime.add(Calendar.WEEK_OF_MONTH, -2);	//-2
				// currentTime.add(Calendar.DAY_OF_MONTH, -1);
				compare = currentTime.compareTo(endTime);
				if (compare >= 0) {
					System.out.println((electionId) + " = end : "
							+ endTime.getTime());
					// delete voters
					dbComm.deleteData(USERS, "electionId", electionId, USERS);
					// delete candidates
					tableName = dbComm.createTableName(electionId, CANDIDATES);
					dbComm.dropTable(CANDIDATES, tableName);
					// delete election
					dbComm.deleteElection(electionId);
					electionIdList.add(electionId);
				} else {
					break;
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
		return electionIdList;
	}

	private boolean restoreData(ResultSet _originRs, String _tableName,
			Connection _conn) {
		try {
			ResultSetMetaData rsmd = _originRs.getMetaData();
			int rsColNum = rsmd.getColumnCount();
			String prefix;
			StringBuilder query;
			while (_originRs.next()) {
				query = new StringBuilder("insert into ");
				query.append(_tableName);
				query.append(" values(");
				prefix = "'";
				for (int i = 1; i <= rsColNum; i++) {
					query.append(prefix);
					prefix = ",'";
					query.append(_originRs.getObject(i));
					query.append("'");
				}
				query.append(")");
				if (!dbComm.stmtExecute(query.toString(), _conn)) {
					System.out.println("restore candidate table fail");
					return false;
				}

			}
			System.out.println("restore candidate table success");
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}

	public boolean deleteUniqueID(int uniqueId) {
		Connection conn = dbComm.dbConnect(UNIQUE_ID);
		if (conn == null) {
			return false;
		}

		StringBuilder query = new StringBuilder("delete from ");
		query.append(UNIQUE_ID);
		query.append(" where id=SHA('" + uniqueId + "')");
		return dbComm.stmtExecute(query.toString(), conn);
	}

	public boolean deleteUniqueId() {
		Connection conn = dbComm.dbConnect(UNIQUE_ID);
		if (conn == null)
			return false;

		String query = "select validTime, id from " + UNIQUE_ID
				+ " order by validTime asc";
		ResultSet rs = dbComm.stmtExecuteQuery(query, conn);

		Calendar validTime = new GregorianCalendar();
		try {
			while (rs.next()) {
				String uniqueId = rs.getString("id");
				validTime.setTime(rs.getTimestamp("validTime"));
				validTime.add(Calendar.MINUTE, +10);
				int compare = findFromDB.compareWithCurrentTime(validTime);
				if (compare >= 0) {
					query = "delete from " + UNIQUE_ID + " where id='"
							+ uniqueId + "'";
				} else {
					break;
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
}