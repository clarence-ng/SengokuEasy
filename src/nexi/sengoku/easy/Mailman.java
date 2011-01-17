package nexi.sengoku.easy;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.URLName;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.log4j.Logger;

import com.sun.mail.smtp.SMTPTransport;

public class Mailman {

	private static final Logger logger = Logger.getLogger(Mailman.class);
	private static final String STMP_HOST = "smtp.gmail.com";
	
	private final Properties properties;

	public Mailman(Properties properties) {
		this.properties = properties;
	}

	public void tryDeliver(String content) {
		try {
			deliver("SengokuEasy", content);
		} catch (Exception e) {
			logger.error("failed to send mail.", e);
		}
	}
	
	public void tryDeliver(String subject, String content) {
		try {
			deliver(subject, content);
		} catch (Exception e) {
			logger.error("failed to send mail.", e);
		}
	}
	
	public void deliver(String subject, String content) throws Exception {

		String from = "sengokueasy@gmail.com";
		
		//Set the host smtp address
		properties.put("mail.smtp.auth", "true");
		String login = properties.getProperty("gmailLogin");
		String password = properties.getProperty("gmailPassword");
		String targetMail = properties.getProperty("mailTarget");
		Session session = Session.getDefaultInstance(properties);
		
		Message msg = new MimeMessage(session);
		
		InternetAddress addressFrom = new InternetAddress(from);
		InternetAddress[] addressTo = new InternetAddress[] {new InternetAddress(targetMail)};
		msg.setFrom(addressFrom);
		msg.setRecipients(Message.RecipientType.TO, addressTo);

		msg.setSubject(subject);
		msg.setContent(content, "text/plain; charset=UTF-8");

		SMTPTransport t = new SMTPTransport(session, new URLName(STMP_HOST));
		t.setStartTLS(true);
		t.connect(STMP_HOST, login, password);
		t.sendMessage(msg, addressTo);
	}

}
