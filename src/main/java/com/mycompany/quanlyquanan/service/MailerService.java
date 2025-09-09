/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.quanlyquanan.service;
import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.io.UnsupportedEncodingException;
import java.util.Properties;
/**
 *
 * @author Admin
 */
public class MailerService {
    
    private static final String USERNAME = "zweikaze@gmail.com";       
    private static final String PASSWORD = "srncpbuemmckiezc"; 
    private static final String FROM_NAME = "Admin-JavaHotPot";

    private static MailerService instance;

    private Session session;

    private MailerService() {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(USERNAME, PASSWORD);
            }
        });
    }

    public static MailerService getInstance() {
        if (instance == null) {
            instance = new MailerService();
        }
        return instance;
    }

    public boolean sendEmail(String to, String subject, String content, boolean isHTMLContent) throws UnsupportedEncodingException {
        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(USERNAME,FROM_NAME));
            message.setRecipients(
                    Message.RecipientType.TO, InternetAddress.parse(to)
            );
            message.setSubject(subject);
            
            if(isHTMLContent){
             message.setContent(content, "text/html; charset=UTF-8");
            }
            else{
            message.setText(content);
            }
            Transport.send(message);
            return true;

        } catch (MessagingException e) {
            e.printStackTrace();
            return false;
        }
    }
    public boolean sendPasswordResetMail(String to, String subject,String newPassword) throws UnsupportedEncodingException{
        String html = getPasswordResetHtml(newPassword);
        try {
             sendEmail(to,subject,html,true);
             return true;
        } catch (Exception e) {
        return false;
        }
       
        
    }
    
    
    private static String getPasswordResetHtml(String password) {
        return """
        <html>
        <body style="font-family: Arial, sans-serif; background-color: #f5f5f5; padding: 20px;">
          <div style="max-width: 600px; margin: auto; background-color: white; padding: 30px; border-radius: 8px; box-shadow: 0 0 10px rgba(0,0,0,0.1);">
            <h2 style="color: #333;">Xin chào,</h2>
            <p>Bạn đã yêu cầu đặt lại mật khẩu tài khoản của mình.</p>
            <p><strong>Mật khẩu mới của bạn là:</strong></p>
            <p style="font-size: 20px; font-weight: bold; background-color: #f0f0f0; padding: 10px; border-radius: 5px; text-align: center;">
              %s
            </p>
            <p>Vui lòng đăng nhập và thay đổi mật khẩu này ngay sau khi truy cập hệ thống để đảm bảo an toàn tài khoản.</p>
            <hr style="margin: 30px 0;">
            <p style="font-size: 12px; color: #888;">Nếu bạn không yêu cầu đặt lại mật khẩu, vui lòng bỏ qua email này.</p>
            <p style="font-size: 12px; color: #888;">Trân trọng,<br>Đội ngũ hỗ trợ hệ thống</p>
          </div>
        </body>
        </html>
        """.formatted(password);
    }
}
