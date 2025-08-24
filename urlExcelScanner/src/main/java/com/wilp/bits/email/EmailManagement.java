package com.wilp.bits.email;

import java.io.File;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
//Email Management
public class EmailManagement {

public static void main(String[] args){
	EmailManagement email= new EmailManagement();
	String fileName = args[0];
	String to = args[1];
    email.emailConfigurations(fileName,to);
}
public void emailConfigurations(String fileName, String to)
{
	String message= "Hi User, "
			+ "Herby enclosing the attachment for your reference."
			+ "  "
			+ "Thanks & Regards,"
			+ "Gajendran R";
	String subject="Email Confirmation";
	//String to="rgaje02@gmail.com";
	String from="202117bh009@wilp.bits-pilani.ac.in";
	String host="smtp.gmail.com";
	
	//sendEmail(message, subject, to, from, host);
	sendEmailAttachment(fileName, message, subject, to, from, host);

}

//Send the message with attachement
private void sendEmailAttachment(String fileName, String message, String subject, String to, String from, String host) {
	try
	{
	//	String fileName="D:\\Gajendran\\Python Virtual Environments\\CyberSentinelX\\classified_MAH_20240121_153659_1.xlsx";
	//get the system properties
	Properties properties= System.getProperties();
	System.out.println("PROPERTIES: "+properties);

	//Setting important information to properties object
	properties.put("mail.smtp.host", host);
	properties.put("mail.smtp.port", "465");
	properties.put("mail.smtp.ssl.enable", "true");
	properties.put("mail.smtp.auth", "true");
	
	
	//Step 1: Get the Session object
	
	Session session= Session.getInstance(properties, new Authenticator() 
	{
		protected PasswordAuthentication getPasswordAuthentication() {
			return new PasswordAuthentication("202117bh009@wilp.bits-pilani.ac.in", "hyxg njuc zwhd rwtg");
			
		}
		
	});
	
	session.setDebug(true);
	
	//Step 2: Compose the message[text, multimedia]
	MimeMessage mimemess= new MimeMessage(session);
	
	//from email
	mimemess.setFrom(from);
	
	//adding recipient to message
	mimemess.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
	
	//adding subject to message
	mimemess.setSubject(subject);
	
	//adding attachment to message
	MimeMultipart mimepart= new MimeMultipart();
	MimeBodyPart textmime =new MimeBodyPart();
	MimeBodyPart filemime =new MimeBodyPart();
	textmime.setText(message);
	File file= new File(fileName);
	filemime.attachFile(file);
	mimepart.addBodyPart(textmime);
	mimepart.addBodyPart(filemime);
	mimemess.setContent(mimepart);
	
	
	//send mail
	//Step 3: Send the message using Transport class
	 Transport.send(mimemess);
	 
	 System.out.println("Mail sent with Attachment!!");
	
	}
	catch(Exception e)
	{
		e.printStackTrace();
	}
	
}

private void sendEmail(String message, String subject, String to, String from, String host) {
	
	try
	{
	//get the system properties
	Properties properties= System.getProperties();
	System.out.println("PROPERTIES: "+properties);

	//Setting important information to properties object
	properties.put("mail.smtp.host", host);
	properties.put("mail.smtp.port", "465");
	properties.put("mail.smtp.ssl.enable", "true");
	properties.put("mail.smtp.auth", "true");
	
	
	//Step 1: Get the Session object
	
	Session session= Session.getInstance(properties, new Authenticator() 
	{
		protected PasswordAuthentication getPasswordAuthentication() {
			return new PasswordAuthentication("202117bh009@wilp.bits-pilani.ac.in", "hyxg njuc zwhd rwtg");
			
		}
		
	});
	
	session.setDebug(true);
	
	//Step 2: Compose the message[text, multimedia]
	MimeMessage mimemess= new MimeMessage(session);
	
	//from email
	mimemess.setFrom(from);
	
	//adding recipient to message
	mimemess.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
	
	//adding subject to message
	mimemess.setSubject(subject);
	
	//adding text to message
	mimemess.setText(message);
	
	//send mail
	//Step 3: Send the message using Transport class
	 Transport.send(mimemess);
	 
	 System.out.println("Mail sent!!");
	
	}
	catch(Exception e)
	{
		e.printStackTrace();
	}
}

public void sendFailedEmail(String message, String subject, String to, String from, String host) {
	
	try
	{
	//get the system properties
	Properties properties= System.getProperties();
	System.out.println("PROPERTIES: "+properties);

	//Setting important information to properties object
	properties.put("mail.smtp.host", host);
	properties.put("mail.smtp.port", "465");
	properties.put("mail.smtp.ssl.enable", "true");
	properties.put("mail.smtp.auth", "true");
	
	
	//Step 1: Get the Session object
	
	Session session= Session.getInstance(properties, new Authenticator() 
	{
		protected PasswordAuthentication getPasswordAuthentication() {
			return new PasswordAuthentication("202117bh009@wilp.bits-pilani.ac.in", "hyxg njuc zwhd rwtg");
			
		}
		
	});
	
	session.setDebug(true);
	
	//Step 2: Compose the message[text, multimedia]
	MimeMessage mimemess= new MimeMessage(session);
	
	//from email
	mimemess.setFrom(from);
	
	//adding recipient to message
	mimemess.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
	
	//adding subject to message
	mimemess.setSubject(subject);
	
	//adding text to message
	mimemess.setText(message);
	
	//send mail
	//Step 3: Send the message using Transport class
	 Transport.send(mimemess);
	 
	 System.out.println("Mail sent!!");
	
	}
	catch(Exception e)
	{
		e.printStackTrace();
	}
	
}
}


