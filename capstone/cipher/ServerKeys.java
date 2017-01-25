package cipher;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SealedObject;

import access.filesystem.ConnectionInfo;

public class ServerKeys {
	private static final String keyPath;
	static {
		if (new File("H:\\").exists()) { keyPath = "H:\\Dropbox\\Capstone\\14-summer\\Voting_WebServer\\Keys"; }
		else if (new File("D:\\secret").exists()) {  keyPath = "D:\\Keys"; }
		else { keyPath = "C:\\Keys"; }
		
		// server information
		new ConnectionInfo(); // test message trigger
		System.out.println("\t*\tYour key path : "+ keyPath);
	}
	
	// the list of servers
	public static final int VERIF_SERVER = 0;
	public static final int DB_SERVER = 1;
	public static final int WEB_SERVER = 2;
	public static final int VEB_SERVER = 3;
	private static final int[] SERVER_LIST = {VERIF_SERVER, DB_SERVER, WEB_SERVER, VEB_SERVER};
	
	// the types of key
	public static final int PUBLIC_KEY = 10;
	public static final int SYMMETRIC_KEY = 11;
	public static final int PRIVATE_KEY = 12;
	
	// private key
	private static final String MY_PRV_KEY_NAME = "privatekey";
	
	// public keys
	private static final String VERIF_PUB_KEY_NAME = "verif_publickey";
	private static final String DB_PUB_KEY_NAME = "db_publickey";
	private static final String WEB_PUB_KEY_NAME = "web_publickey";
	private static final String VEB_PUB_KEY_NAME = "veb_publickey";
	
	// symmetric keys
	private static final String VERIF_SYM_KEY_NAME = "verif_sym_key";
	private static final String DB_SYM_KEY_NAME = "db_sym_key";
	private static final String WEB_SYM_KEY_NAME = "web_sym_key";
	private static final String VEB_SYM_KEY_NAME = "veb_sym_key";
	
	// the list of key-names
	private static final String[][] KEY_NAME_LIST = {
		{VERIF_PUB_KEY_NAME, DB_PUB_KEY_NAME, WEB_PUB_KEY_NAME, VEB_PUB_KEY_NAME},
		{VERIF_SYM_KEY_NAME, DB_SYM_KEY_NAME, WEB_SYM_KEY_NAME, VEB_SYM_KEY_NAME},
		{MY_PRV_KEY_NAME}
	};
	
	// the list of symmetric key objects
	private static final Key[] SYM_KEY_LIST = new Key[4];
	static {
		SYM_KEY_LIST[VERIF_SERVER] = getKey(VERIF_SERVER, SYMMETRIC_KEY, true);
		SYM_KEY_LIST[DB_SERVER] = getKey(DB_SERVER, SYMMETRIC_KEY, true);
		SYM_KEY_LIST[WEB_SERVER] = getKey(WEB_SERVER, SYMMETRIC_KEY, true);
		SYM_KEY_LIST[VEB_SERVER] = getKey(VEB_SERVER, SYMMETRIC_KEY, true);
		int i = 0;
		for (Key key : SYM_KEY_LIST) {
			System.out.println("\t*\t " + i++ + " : " + (key == null ? "-" : key));
		}
	}
	
	// the last created symm. keys to renew but not fixed
	private static final Key[] RESERVED_SYM_KEY_LIST = new Key[4];

	/* ================================================================== */
	
	public Key getSymmetricKey(int serverNumber) {
		if (verifyServerNumber(serverNumber))
			return SYM_KEY_LIST[serverNumber];
		else return null;
	}

	public boolean setSymmetricKey(int serverNumber, Key key) {
		if(setKey(serverNumber, SYMMETRIC_KEY, key)) {
			SYM_KEY_LIST[serverNumber] = key; return true;
		}
		else return false;
	}
	
	public boolean fixRenewedSymmetricKey(int serverNumber) {
		Key reservedKey = RESERVED_SYM_KEY_LIST[serverNumber];
		if (reservedKey == null) return false;
		else
			return setSymmetricKey(serverNumber, reservedKey);
	}

	public Key renewSymmetricKey(int serverNumber) {
		try {
			Key key = KeyGenerator.getInstance("AES").generateKey();
			RESERVED_SYM_KEY_LIST[serverNumber] = key;
			
			System.out.println("--Created key to send : " + key); // test ----------------
			
			return key;
		}
		catch (NoSuchAlgorithmException e) { e.printStackTrace(); return null; }
	}
	
	private boolean setKey(int serverNumber, int keytype, Key key) {
		System.out.println("--Write a new key"); // test--------------------

		if (!verifyServerNumber(serverNumber)) return false;
		else return keyIO(getKeyfile(serverNumber, keytype), key);
	}
	
	static private Key getKey(int serverNumber, int keytype, boolean ignoreNoKey) {
		if (!verifyServerNumber(serverNumber)) return null;
		else {
			File keyfile = getKeyfile(serverNumber, keytype, ignoreNoKey);
			if (keyfile == null && ignoreNoKey) return null;
			else return keyIO(keyfile);
		}
	}
	
	static private Key getKey(int serverNumber, int keytype) {
		return getKey(serverNumber, keytype, false);
	}
	
	// encrypt a given object with a symmetric key to a given server
	public SealedObject encrypt(int serverNumber, Serializable o) {
		return encrypt(serverNumber, SYMMETRIC_KEY, o);
	}
	
	// encrypt a given object with an indicated key
	public SealedObject encrypt(int serverNumber, int keytype, Serializable o) {
		SealedObject result = null;

		// get a cipher object
		Cipher cipher = null;
		switch (keytype) {
		case SYMMETRIC_KEY: cipher = AEScipher(Cipher.ENCRYPT_MODE, getSymmetricKey(serverNumber)); break;
		case PUBLIC_KEY: cipher = RSAcipher(Cipher.ENCRYPT_MODE, getKey(serverNumber, keytype)); break;
		case PRIVATE_KEY: cipher = RSAcipher(Cipher.ENCRYPT_MODE, getKey(serverNumber, keytype)); break;
		}
		
		try { result = new SealedObject(o, cipher); }
		catch (NullPointerException ne) {
			if (o == null) System.out.println("**encryption error : the target object is null");
			ne.printStackTrace();
		}
		catch (Exception e) {
			System.out.println("**encryption error"); // test -----------------------
			e.printStackTrace();
		}
		
		return result;
	}
	
	// decrypt a sealed object with a symmetric key to a given server
	public Object decrypt(int serverNumber, SealedObject sealed) {
		return decrypt(serverNumber, SYMMETRIC_KEY, sealed);
	}
	
	public Object decrypt(int serverNumber, int keytype, SealedObject sealed) {
		Object result = null;

		// get a cipher object
		Cipher cipher = null;
		switch (keytype) {
		case SYMMETRIC_KEY: cipher = AEScipher(Cipher.DECRYPT_MODE, getSymmetricKey(serverNumber)); break;
		case PUBLIC_KEY: cipher = RSAcipher(Cipher.DECRYPT_MODE, getKey(serverNumber, keytype)); break;
		case PRIVATE_KEY: cipher = RSAcipher(Cipher.DECRYPT_MODE, getKey(serverNumber, keytype)); break;
		}
		
		try { result = sealed.getObject(cipher); }
		catch (ClassNotFoundException | IOException | IllegalBlockSizeException | BadPaddingException e)
			{ System.out.println("**decryption error"); e.printStackTrace(); }
		
		return result;
	}
	
	private boolean keyIO(File keyfile, Key key) {
		System.out.println("--the given key : " + key); // test------------------------------
		
		// if default file does not exist :
		File dir = new File(keyPath);
		if (!dir.exists()) dir.mkdirs();
		try { keyfile.createNewFile(); } // create new file only if the file does not exist
		catch (IOException e) { e.printStackTrace(); }
		
		// write the key file
		boolean check = false; // a fault detector
		try (ObjectOutputStream ooStream =
				new ObjectOutputStream(new FileOutputStream(keyfile))){
			ooStream.writeObject(key);
			
			System.out.println("--the last time to modify this keyfile : " + // test -----------------
					new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format( // ----------------------
							new Date(keyfile.lastModified()) )); // -----------------------------
			
			check = true;
		}
		catch (Exception e){ e.printStackTrace(); }
		
		return check;
	}
	
	// get a secret key from a given file
	private static Key keyIO(File keyfile) {
		Key key = null;
		try (ObjectInputStream oiStream =
				new ObjectInputStream(new FileInputStream(keyfile))){
			key = (Key)oiStream.readObject();
		}
		catch (Exception e) { e.printStackTrace(); }

		return key;
	}
	
	private static File getKeyfile(int serverNumber, int keytype, boolean ignoreNoFile) {
		try {
			String filename = null;
			if (keytype == PRIVATE_KEY) filename = MY_PRV_KEY_NAME;
			else filename = KEY_NAME_LIST[keytype % 10][serverNumber];
			
			if (filename != null) {
				File keyfile = new File(keyPath + "\\" + filename);
				if (keyfile.exists()) return keyfile;
				else if (ignoreNoFile) return null;
			}
			throw new Exception();
		} catch (Exception e) {
			System.out.println("**The key for serverNum to communicate : " +
					serverNumber + " / keytype : " + keytype + " is not accessible");
			return null;
		}
	}
	
	private static File getKeyfile(int serverNumber, int keytype) {
		return getKeyfile(serverNumber, keytype, false);
	}
	  
	// get a cipher instance to encrypt some object with a given key
	private Cipher RSAcipher(int mode, Key key) { return cipher("RSA", mode, key); }
	private Cipher AEScipher(int mode, Key key) { return cipher("AES", mode, key); }
	private Cipher cipher(String algorithm, int mode, Key key) {
		try {
			Cipher cipher = Cipher.getInstance(algorithm);
			cipher.init(mode, key);
			return cipher;
		}
		catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException e)
		{ e.printStackTrace(); return null; }
	}
	
	// check if a given server-number is valid
	private static boolean verifyServerNumber(int serverNumber) {
		boolean validNum = false;
		for (int server : SERVER_LIST)
			if (serverNumber == server) { validNum = true; break; }
		
		if (!validNum) System.out.println("**invalid server number : " + serverNumber); // test-------------
		return validNum;
	}
}
