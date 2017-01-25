package communication;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import javax.crypto.SealedObject;

import vote.Message;
import access.filesystem.ConnectionInfo;
import cipher.ServerKeys;
public class ObjectComm {

	private Message receiveMsg = null;
	private SealedObject receive_so = null;
	private ObjectOutputStream out = null;
	private ObjectInputStream in = null;
	private ServerKeys sk= null;
	
	public ObjectComm(){
		sk = new ServerKeys();
	}
	
	public Message commObject(Message _sendMsg, Socket _clntSocket) {
		int server_num = -1;
		
		if(!_clntSocket.isConnected()){
			System.out.println("clnt Socket not connected");
			return null;
		}else{
			
			server_num = find_server(_clntSocket);
		}
		try {
									
			SealedObject msg_so = sk.encrypt(server_num, _sendMsg);
			out = new ObjectOutputStream(_clntSocket.getOutputStream());
			out.writeObject(msg_so);
			out.flush();

			in = new ObjectInputStream(_clntSocket.getInputStream());
			receive_so = (SealedObject) in.readObject();
			receiveMsg = (Message)sk.decrypt(server_num, receive_so);
			return receiveMsg;
			
		} catch (Exception e) {
			System.out.println("commObject err");
			e.printStackTrace();
			return null;
		}

	}

	public boolean sendObject(Message _msg, Socket _clntSocket){
		int server_num = -1;
		
		if(!_clntSocket.isConnected()){
			System.out.println("clnt Socket not connected");
			return false;
		}else{
			
			server_num = find_server(_clntSocket);
		}

		try {
			
			SealedObject msg_so = sk.encrypt(server_num, _msg);
			out = new ObjectOutputStream(_clntSocket.getOutputStream());
			out.writeObject(msg_so);
			out.flush();

			return true;
		} catch (IOException e) {
			System.out.println("Object comm send err");
			e.printStackTrace();
			return false;
		}
	}

	public Message recvObject(Socket _clntSocket){
		int server_num = -1;
		
		if(!_clntSocket.isConnected()){
			System.out.println("clnt Socket not connected");
			return null;
		}else{
			
			server_num = find_server(_clntSocket);
		}

		try {
			
			in = new ObjectInputStream(_clntSocket.getInputStream());
			receive_so = (SealedObject) in.readObject();
//			System.out.println(server_num);
			receiveMsg = (Message)sk.decrypt(server_num, receive_so);
			
			return receiveMsg;
			
		} catch (Exception e) {
			System.out.println("Object comm recvObject err");
			e.printStackTrace();
			return null;
/*			if(server_num ==ConnectionInfo.VERIF_SERVER ){
					server_num=ConnectionInfo.DB_SERVER;
					receiveMsg = (Message)sk.decrypt(server_num, receive_so);
				
			}else if( server_num ==ConnectionInfo.DB_SERVER){
					server_num=ConnectionInfo.VERIF_SERVER ;
					receiveMsg = (Message)sk.decrypt(server_num, receive_so);
			}
			return receiveMsg;*/
			
		}
	}
	public boolean sendKeyMessage(Message _msg, Socket _clntSocket){
		int server_num = -1;
		
		if(!_clntSocket.isConnected()){
			System.out.println("clnt Socket not connected");
			return false;
		}else{
			
			server_num = find_server(_clntSocket);
		}

		try {
			SealedObject msg_so = sk.encrypt(server_num,ServerKeys.PUBLIC_KEY,_msg);
			out = new ObjectOutputStream(_clntSocket.getOutputStream());
			out.writeObject(msg_so);
			out.flush();

			return true;
		} catch (IOException e) {
			System.out.println("Object comm send err");
			e.printStackTrace();
			return false;
		}
	}
	public Message recvKeyMessage(Socket _clntSocket){
		int server_num = -1;
		
		if(!_clntSocket.isConnected()){
			System.out.println("clnt Socket not connected");
			return null;
		}else{
			
			server_num = find_server(_clntSocket);
		}

		try {
			
			in = new ObjectInputStream(_clntSocket.getInputStream());
			receive_so = (SealedObject) in.readObject();
			receiveMsg = (Message)sk.decrypt(server_num,ServerKeys.PRIVATE_KEY,receive_so);
			
			return receiveMsg;
			
		} catch (Exception e) {
			System.out.println("Object comm recvObject err");
			e.printStackTrace();
			return null;
		}
	}
	public boolean closeAll(){
		try{
			if( out != null)
				out.close();
			if( in != null)
				in.close();
		}catch(IOException e){
			e.printStackTrace();
			return false;
		}
		return true;
	}
	private int find_server(Socket s){
		String sender_ip = s.getInetAddress().getHostAddress();
		int server_num = -1;
		if(sender_ip.equals(ConnectionInfo.getVerifIP())){
			server_num = ConnectionInfo.VERIF_SERVER;
		}else if(sender_ip.equals(ConnectionInfo.getDbIP())){
			server_num = ConnectionInfo.DB_SERVER;
		}else if(sender_ip.equals(ConnectionInfo.getWebIP())){
			server_num = ConnectionInfo.WEB_SERVER;
		}else if(sender_ip.equals(ConnectionInfo.getVebIP())){
			server_num = ConnectionInfo.VEB_SERVER;
		}else{
			System.out.println("IP match Error");
			server_num = -1;
		}
		return server_num;
	}

}
