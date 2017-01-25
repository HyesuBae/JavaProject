package server;

import java.io.IOException;
import java.net.Socket;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import java.security.Key;

import access.filesystem.ConnectionInfo;
import access.filesystem.StateInfo;
import vote.Message;
import communication.ObjectComm;
import cipher.ServerKeys;

public class KeyTimer {
	//private static final int timeDelay = 1000 * 60 * 60 * 168; // 7 days
	private static final int timeDelay = 1000*60*60*60;	// 1 minute
	private static Timer timer;
	public KeyTimer() {
		timer = new Timer(false); // daemon true??
		Calendar cal = Calendar.getInstance();
		cal.set(2014, 6, 30, 22, 05, 0); // month = month + 1
		timer.scheduleAtFixedRate(new KeyTimerTask(), new Date(cal.getTimeInMillis()),
				timeDelay);
		
		try {
	         Thread.sleep(20000);
	      } catch(InterruptedException ex) {
	         //
	      }
	}
	
	public void stop(){
		timer.cancel();
	}
}

class KeyTimerTask extends TimerTask {
	private ObjectComm objComm;
	
	public void run() {
		System.out.println("Key Timer task start");
		
		String webIp = ConnectionInfo.getWebIP();
		String vebIp = ConnectionInfo.getVebIP();
		String dbIp = ConnectionInfo.getDbIP();
		
		int webPort = ConnectionInfo.getWebPort();
		int vebPort = ConnectionInfo.getVebPort();
		int dbPort = ConnectionInfo.getDbPort();
		
		objComm = new ObjectComm();
		
		if(!sendTimer(webIp, webPort)){
			System.out.println("send timer to web err");
			return;
		}
		if(!sendTimer(vebIp, vebPort)){
			System.out.println("send timer to veb err");
			return;
		}

		if(!sendTimer(dbIp, dbPort)){
			System.out.println("send timer to db err");
			return;
		}

		VerificationServer.setIsMaintenance(true);
		
		if(!updateKey(webIp, webPort, StateInfo.ST_KEY_FOR_WEB_N_VERIF, ConnectionInfo.WEB_SERVER)){
			System.out.println("update key for web n verif fail");
			return;
		}
		
		if(!updateKey(vebIp, vebPort, StateInfo.ST_KEY_FOR_VEB_N_VERIF, ConnectionInfo.VEB_SERVER)){
			System.out.println("update key for veb n verif fail");
			return;
		}
		
		if(!updateKey(dbIp, dbPort, StateInfo.ST_KEY_FOR_VERIF_N_DB, ConnectionInfo.DB_SERVER)){
			System.out.println("update key for verif n db fail");
			return;
		}
		

		System.out.println("update time and set key of verif server success");
		
		
	}
	
	private boolean sendTimer(String _ip, int _port) {
		Socket socket;
		try {
			socket = new Socket(_ip, _port);
			if (!socket.isConnected()) {
				System.out.println("Web server is not connected");
				socket.close();
				return false;
			}

			Message sendMsg = new Message(StateInfo.ST_KEY_UPDATE_TIME, null);
			Message recvMsg = (Message) objComm.commObject(sendMsg,
					socket);

			if(recvMsg == null){
				System.out.println("recv msg == null");
				return false;
			}

			if (recvMsg.getState() != StateInfo.ST_KEY_UPDATE_TIME_SUCCESS) {
				VerificationServer.setIsMaintenance(false);
				return false;
			}
			
			socket.close();

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	private boolean updateKey(String _ip, int _port, int _state, int _serverNum){
		Socket socket = null;
		Message sendMsg, recvMsg;
		
		try {
			socket = new Socket(_ip, _port);
			sendMsg = new Message(_state, null);
			if(!objComm.sendObject(sendMsg, socket)){
				return false;
			}
			
			recvMsg = objComm.recvKeyMessage(socket);

			if(recvMsg == null){
				System.out.println("recvMsg = null");
				return false;
			}
			
			if(recvMsg.getState() != _state + 100){
				System.out.println("key update for web n verif fail");
				return false;
			}
			
			Key sKey = (Key)recvMsg.getObj();
			ServerKeys servKeys = new ServerKeys();

			if(sKey != null && sKey instanceof Key){
				sendMsg = new Message(StateInfo.ST_UPDATE_KEY_COMPLETE, null);
				objComm.sendObject(sendMsg, socket);
			}else{
				sendMsg = new Message(StateInfo.ST_UPDATE_KEY_FAIL, null);
				objComm.sendObject(sendMsg, socket);
				return false;
			}
			
			servKeys.setSymmetricKey(_serverNum, sKey);
			socket.close();			
			
		} catch (IOException e) {
			e.printStackTrace();
			sendMsg = new Message(StateInfo.ST_UPDATE_KEY_FAIL, null);
			objComm.sendObject(sendMsg, socket);
			return false;
		}
		
		return true;
	}

}