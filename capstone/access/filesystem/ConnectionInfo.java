package access.filesystem;

public class ConnectionInfo {
	private static final String HyeSu = "IPaddr1";
	private static final String GiBbeum = "IPaddr2";
	private static final String JeongSeok = "IPaddr3";
	private static final String JiHyun = "IPaddr4";
	
	public static final String verifIP = HyeSu;
	public static final String dbIP = JiHyun;
	public static final String webIP = GiBbeum;
	public static final String vebIP = JeongSeok;
			
	public static final int VERIF_SERVER = 0;
	public static final int DB_SERVER = 1;
	public static final int WEB_SERVER = 2;
	public static final int VEB_SERVER = 3;
		
	// http, https port X    server socket port O
	public static final int verifPort = 9999;
	public static final int dbPort = 9999;
	public static final int webPort = 11436; // allowed port range at individual firewall: 9000-12000
	public static final int vebPort = 10101;
	
	public static String getVerifIP() { return verifIP; }
	public static String getDbIP() { return dbIP; }
	public static String getWebIP() { return webIP;	}
	public static String getVebIP() { return vebIP; }
	public static int getVerifPort() { return verifPort; }
	public static int getDbPort() { return dbPort; }
	public static int getWebPort() { return webPort; }
	public static int getVebPort() { return vebPort; }
}
