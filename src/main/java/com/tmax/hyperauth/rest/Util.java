package com.tmax.hyperauth.rest;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.*;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.util.Properties;
import java.util.Random;

public class Util {
    public static Response setCors( Status status, Object out ) {
		return Response.status(status).entity(out)
    			.header("Access-Control-Allow-Origin", "*")
				.header("Access-Control-Allow-Credentials", "true")
    			.header("Access-Control-Max-Age", "3628800")
    			.header("Access-Control-Allow-Methods", "GET, POST, OPTIONS, PUT, DELETE, HEAD")
        		.header("Access-Control-Allow-Headers", "*" ).build();
    }

	public static String numberGen(int len, int dupCd ) {

		Random rand = new Random();
		String numStr = ""; //난수가 저장될 변수

		for(int i=0;i<len;i++) {
			String ran = null;
			//0~9 까지 난수 생성 ( 첫자리에 0 인 경우는 제외 )
			if (i == 0) {
				ran = Integer.toString(rand.nextInt(9)+1);
			}else {
				ran = Integer.toString(rand.nextInt(10));
			}
			if(dupCd==1) {
				//중복 허용시 numStr에 append
				numStr += ran;
			}else if(dupCd==2) {
				//중복을 허용하지 않을시 중복된 값이 있는지 검사한다
				if(!numStr.contains(ran)) {
					//중복된 값이 없으면 numStr에 append
					numStr += ran;
				}else {
					//생성된 난수가 중복되면 루틴을 다시 실행한다
					i-=1;
				}
			}
		}
		return numStr;
	}

	public static void sendMail( String recipient, String subject, String body, String imgPath, String imgCid ) throws Throwable {
		System.out.println( " Send Mail to User [ " + recipient + "] Start");
		String host = "mail.tmax.co.kr";
		int port = 25;
		String sender = "no-reply-tc@tmax.co.kr";

		String charSetUtf = "UTF-8" ;
		Properties props = System.getProperties();
		props.put( "mail.transport.protocol", "smtp" );
		props.put( "mail.smtp.host", host );
		props.put( "mail.smtp.port", port );
		props.put( "mail.smtp.ssl.trust", host );
		props.put( "mail.smtp.auth", "true" );
		props.put( "mail.smtp.starttls.enable", "true" );
		props.put("mail.smtp.ssl.protocols", "TLSv1.2");

		Session session = Session.getDefaultInstance( props, new javax.mail.Authenticator() {
			String un = "no-reply-tc@tmax.co.kr";
			String pw = "!@tcdnsdudxla11";
			//			String pw = K8sApiCaller.readSecret(Constants.TEMPLATE_NAMESPACE, Constants.SECRET_MAIL_PASSWORD).getStringData().get("password");
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication( un, pw );
			}
		});

		session.setDebug( true );
		MimeMessage mimeMessage = new MimeMessage(session);

		// Sender
		mimeMessage.setFrom( new InternetAddress(sender, sender, charSetUtf));

		// Receiver
		mimeMessage.setRecipient( Message.RecipientType.TO, new InternetAddress( recipient ) );

		// Make Subject
		mimeMessage.setSubject( MimeUtility.encodeText(subject,  charSetUtf, "B") );

		// Make Body ( text/html + img )
		MimeMultipart multiPart = new MimeMultipart();

		System.out.println( " Mail Body : "  + body );
		BodyPart messageBodyPart = new MimeBodyPart();
		messageBodyPart.setContent(body, "text/html; charset="+charSetUtf);
		multiPart.addBodyPart(messageBodyPart);

		BodyPart messageImgPart = new MimeBodyPart();
		if (imgPath != null){
			DataSource ds = new FileDataSource(imgPath);
			messageImgPart.setDataHandler(new DataHandler(ds));
			messageImgPart.setHeader("Content-Type", "image/svg");
			messageImgPart.setHeader("Content-ID", "<"+imgCid+">");
			multiPart.addBodyPart(messageImgPart);
		}
		mimeMessage.setContent(multiPart);

		System.out.println( " Ready to Send Mail to " + recipient);
		try {
			//Send Mail
			Transport.send( mimeMessage );
			System.out.println( " Sent E-Mail to " + recipient);
		}catch (MessagingException e) {
			e.printStackTrace();
			System.out.println( e.getMessage() + e.getStackTrace());
			throw e;
		}
	}
}