package server;

import java.util.ArrayList;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import vote.VoteInfo;

public class MailCert implements Runnable{
	private static final String sender_id = "EMAIL@EMAILADDR.COM";
	private static final String sender_pw = "PASSWORD";
	private String receiverAddr;
	private String mailText;
	private Properties properties = new Properties();
	
	private MailCert(String _email, String _mailText, Properties _props){
		receiverAddr = _email;
		mailText = _mailText;
		properties = _props;
	}
	
	public MailCert(){
		
	}

	public String sendMail(String _email) {
		Properties localProps = new Properties();
		localProps.put("mail.smtp.host", "smtp.gmail.com"); // gmail server
		localProps.put("mail.smtp.socketFactory.port", "465");
		localProps.put("mail.smtp.socketFactory.class",
				"javax.net.ssl.SSLSocketFactory");
		localProps.put("mail.smtp.auth", "true");
		localProps.put("mail.smtp.port", "465");
		Session session = Session.getInstance(localProps,
				new javax.mail.Authenticator() {
					protected PasswordAuthentication getPasswordAuthentication() {
						return new PasswordAuthentication(sender_id, sender_pw);
					}
				});

		int randNum = (int) (Math.random() * 100000);
		String acceptNum = String.format("%06d", randNum);

		Message message = new MimeMessage(session);
		try {
			message.setFrom(new InternetAddress(sender_id));
			message.setRecipients(Message.RecipientType.TO,
					InternetAddress.parse(_email));
			message.setSubject("투표를 위한 인증번호입니다.");
			message.setText("인증번호는 " + acceptNum + "입니다.\n화면에 인증번호를 입력해주세요.");

			Transport.send(message);
			System.out.println("send email success");

		} catch (MessagingException e) {
			e.printStackTrace();
			return null;
		}

		return acceptNum;

	}

	public boolean sendSerialToAll(VoteInfo _voteInfo,
			ArrayList<ArrayList<String>> _list) {

		Properties props = new Properties();
		props.put("mail.smtp.host", "smtp.gmail.com"); // gmail server
		props.put("mail.smtp.socketFactory.port", "465");
		props.put("mail.smtp.socketFactory.class",
				"javax.net.ssl.SSLSocketFactory");
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.port", "465");
		
		String voteTitle = _voteInfo.getTitle();
		String startTime = _voteInfo.getStartTime();
		String endTime = _voteInfo.getEndTime();
		int totalNum = _voteInfo.getTotalNum();
		
		String sendText;
		for (int i = 0; i < _list.get(0).size(); i++) {
			sendText = "새로운 선거가 등록되었습니다."
					+ "로그인에 사용되는 이메일은 관리자를 통해 등록된 개인별 이메일 주소입니다." 
					+ "\n선거기간은 실제 투표가 가능한 시간을 의미하며 선거에 관련된 정보나 후보자 정보 등은"
					+ "\n선거 시작기간 이전에도 확인하실 수 있습니다."
					+ "\n등록된 선거 정보는 다음과 같습니다."
					+ "\n\n선거제목: " + voteTitle 
					+ "\n선거기간: "+ startTime + " ~ "+ endTime
					+ "\n전체 유권자 수: "+ totalNum
					+ "\n로그인에 필요한 회원님의 고유번호: "	+ _list.get(1).get(i)
					+ "\n\n선거에 관련한 기타 사항은 저희 홈페이지에 로그인하시면 확인하실 수 있습니다."
					+ "\n 사이트주소: https://203.252.121.217:8443/";
			Thread t = new Thread(new MailCert(_list.get(0).get(i), sendText, props));
			t.start();
				
		}

		return true;
	}

	@Override
	public void run() {
		Session session = Session.getInstance(properties,
				new javax.mail.Authenticator() {
					protected PasswordAuthentication getPasswordAuthentication() {
						return new PasswordAuthentication(sender_id, sender_pw);
					}
				});
		Message message = new MimeMessage(session);
			try {
				message.setFrom(new InternetAddress(sender_id));
				message.setRecipients(Message.RecipientType.TO,
						InternetAddress.parse(receiverAddr));
				message.setSubject("새로운 선거가 등록되었습니다.");
				message.setText(mailText);

				Transport.send(message);

			} catch (MessagingException e) {
				e.printStackTrace();
				System.out.println("send message to"+receiverAddr+"fail");
			}
		
	}
}
