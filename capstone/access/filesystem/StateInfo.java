package access.filesystem;

public class StateInfo {
	
	public static final int ST_SEND_VOTE_INFO = 1;		//VOTE INFO 전송(클라이언트에서 서버로!)
	public static final int ST_FIND_ELECTION = 3;		// index.jsp에서 선거를 찾아달라는 요청 (클라이언트에서 서버로)
	public static final int	ST_VOTE_COMPLETE = 4 ;		// vote.jsp에서 투표한 결과를 보내는 것(클라이언트에서 DB서버)
	public static final int	ST_CAND_REGISTER_PW = 5	;//후보자 비밀번호 등록
	public static final int	ST_CAND_CHECK = 6;		//후보자가 공약수정 페이지로 들어가기 위한 비밀번호 입력 인증 요청
	public static final int	ST_MAKE_RESULT_TAB = 7;		// 인증 서버가 db 서버로 후보자 정보를 전송
	public static final int	ST_CHECK_MANAGER = 9; 		//manager 비밀번호를 웹서버가 인증서버로 전송
	public static final int	ST_UPDATE_ELECTION = 10; 	// 선거 정보 수정해서 웹서버가 서버로
	public static final int	ST_UPDATE_EXCEL = 11;		// 선거 정보 수정에 엑셀 파일도 포함
	public static final int	ST_TIME_CHECK = 12;		//선거 시간 체크. 인증서버에서 디비서버로
	public static final int	ST_CHECK_VOTE_FIRST = 13;	//투표하기 버튼 눌렀을 때 투표 여부 체크(인증서버로)
	public static final int	ST_KEY_UPDATE_TIME = 14;	//Key 업데이트 시간
	public static final int	ST_KEY_FOR_VERIF_N_DB = 15;	// 인증서버가 디비서버에게 키를 요구
	public static final int	ST_KEY_FOR_WEB_N_DB = 16;	// 디비서버가 웹서버에게 키를 요구
	public static final int	ST_KEY_FOR_WEB_N_VERIF = 17;	//
	public static final int	ST_CHECK_UNIQUE_ID = 18;	//투표 서버가 인증서버로 고유번호를 보내고 인증번호 및 후보자목록, 					대표자 목록, 인증번호 돌려받음
	public static final int	ST_KEY_FOR_VEB_N_VERIF = 20;	//인증서버가 Veb버에게 키를 요구
	public static final int	ST_KEY_FOR_DB_N_VEB = 21;	//DB 서버가 veb 서버에게 키를 요구
	public static final int ST_KEY_FOR_WEB_N_VEB = 22;
	public static final int ST_SESSION_LOGOUT = 23;		// 로그아웃 되었다고 WEB->VEB 또는 VEB->WEB으로 알릴 때

	public static final int	ST_RECV_VOTE_INFO_SUCCESS = 101;	// 선거 정보를 성공적으로 수신 (서버가 클라이언트에게 전송)
	public static final int	ST_END_ELECTION_SUCCESS = 102;		//
	public static final int	ST_FOUND_ELECTION = 103;		// 이메일과 선거를 찾았을 때
	public static final int	ST_VOTE_SUCCESS =104;			//투표 성공
	public static final int	ST_CAND_REGISTER_PW_SUCCESS = 105;	//후보자 비밀번호 등록 성공
	public static final int	ST_CAND_CHECK_SUCCESS = 106;		//후보자 비밀번호 인증 성공
	public static final int	ST_MAKE_RESULT_TAB_SUCCESS = 107;	// DB 서버에 후보자 정보 저장 성공
	public static final int	ST_CHECK_MANAGER_SUCCESS = 109;		// 관리자 비밀번호가 올바른 비밀번호임을 웹서버로 전송
	public static final int	ST_UPDATE_ELECTION_SUCCESS = 110;	// 선거 정보 수정 완료 정보를 서버가 웹서버로
	public static final int	ST_UPDATE_EXCEL_SUCCESS = 111;		//선거 정보 수정 및 엑셀 수정 완료
	public static final int	ST_TIME_CHECK_SUCCESS = 112;		//시간 체크 및 선거 삭제 완료
	public static final int	ST_CAN_VOTE = 113;			//투표 가능
	public static final int	ST_KEY_UPDATE_TIME_SUCCESS = 114;	//웹서버가 점검중 페이지 띄우기 완료 (웹서버가 인증서버로)
	public static final int	ST_KEY_FOR_VERIF_N_DB_SUCCESS= 115;
	public static final int	ST_KEY_FOR_WEB_N_DB_SUCCESS = 116;
	public static final int	ST_KEY_FOR_WEB_N_VERIF_SUCCESS = 117; 	// 인증서버의 Symm 키를 잘 받은 후 (웹서버가)
	public static final int	ST_UNIQUE_ID_SUCCESS = 118;		// unique id check 완료
	public static final int	ST_REMOVE_VOTE_SUCCESS = 119;	// db server가 verif server로
	public static final int	ST_KEY_FOR_VEB_N_VERIF_SUCCESS = 120;	//veb server가 인증서버로
	public static final int	ST_KEY_FOR_DB_N_VEB_SUCCESS = 121;
	public static final int ST_KEY_FOR_WEB_N_VEB_SUCCESS = 122;

	public static final int	ST_RECV_VOTE_INFO_FAIL = 201;		//선거 정보 저장 실패
	public static final int	ST_END_ELECTION_FAIL = 202;	//끝난 선거 찾기 실패
	public static final int	ST_NOT_FOUND_ELECTION = 203;	// 선거를 찾지 못했을 때
	public static final int	ST_VOTE_FAIL =204;		//투표 실패
	public static final int	ST_CAND_REGISTER_PW_FAIL = 205;	//후보자 비밀번호 등록 실패
	public static final int	ST_CAND_CHECK_FAIL = 206;	//후보자 비밀번호 인증 실패
	public static final int	ST_MAKE_RESULT_TAB_FAIL = 207;	//DB 서버에 후보자 정보 저장 실패
	public static final int	ST_CHECK_MANAGER_FAIL = 209;	//관리자 비밀번호가 옳지 않을 때 웹서버로 전송
	public static final int	ST_UPDATE_ELECTION_FAIL = 210;	// 선거 정보 수정 실패 정보를 서버가 웹서버로
	public static final int	ST_UPDATE_EXCEL_FAIL = 211;	//선거 정보 및 엑셀 정보 수정 실패
	public static final int	ST_TIME_CHECK_FAIL = 212; 	//시간 체크 및 선거 삭제 실패
	public static final int	ST_KEY_UPDATE_TIME_FAIL = 214;	//점검중입니다 페이지 띄우기 실패(웹서버가 인증서버로)
	public static final int	ST_KEY_FOR_VERIF_N_DB_FAIL = 215;	
	public static final int	ST_KEY_FOR_WEB_N_DB_FAIL = 216;
	public static final int	ST_KEY_FOR_WEB_N_VERIF_FAIL = 217;
	public static final int	ST_UNIQUE_ID_FAIL = 218	;	//고유 번호가 목록에 없을 때 
	public static final int	ST_REMOVE_VOTE_FAIL = 219;
	public static final int	ST_KEY_FOR_VEB_N_VERIF_FAIL = 220; //veb server가 인증 서버로
	public static final int ST_KEY_FOR_DB_N_VEB_FAIL = 221;
	public static final int ST_KEY_FOR_WEB_N_VEB_FAIL= 222;

	public static final int	ST_ALREADY_VOTE = 300;		//이미 투표한 사람일때
	public static final int	ST_NOT_VOTER = 301;		//소속된 선거가 아닌 사람이 투표했을 때
	public static final int	ST_START_TIME_ERR = 302;	//선거가 아직 시작되지 않았을 때
	public static final int	ST_END_TIME_ERR = 303;		//선거가 이미 끝났을 때
	public static final int ST_SEND_SERIAL_TO_ALL_FAIL = 304;

	public static final int	ST_VERIF_ERR = 500;		//인증서버 에러
	
	public static final int	ST_MAINTENANCE = 600;		//점검중일 때 다른 요청이 서버에 들어오면 return
	public static final int ST_UPDATE_KEY_COMPLETE = 601;
	public static final int ST_UPDATE_KEY_FAIL = 602;
	public static final int ST_KEY_UPDATE_TIME_OVER = 603;
	
	public static final int ST_DB_COMPLETE = 1000;		// update all keys of db server success
	public static final int ST_WEB_COMPLETE = 2000;		// update all keys of web server success
	public static final int ST_VEB_COMPLETE = 3000;		// update all keys of veb server success
	
	public static final int ST_DB_FAIL = 1100;
	public static final int ST_WEB_FAIL = 2100;
	public static final int ST_VEB_FAIL = 3100;
}
