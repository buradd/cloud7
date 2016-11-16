package com.buradd.cloud7.net;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;


import com.buradd.cloud7.MainActivity;

import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 * Created by bradley.miller on 8/8/2016.
 */
public class SendRegisteredEmail extends AsyncTask<String, Void, String>{


    private String rec;
    private String subject;
    private String textMessage;
    private Session session;
    MainActivity mainActivity = MainActivity.getInstance();

    @Override
    protected void onPreExecute(){
        super.onPreExecute();
        rec = "buradd@gmail.com";
        subject = "New Cloud7 User";
        textMessage = "There has been a new registration. Please check Firebase to create FTP user account";

        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.socketFactory.port", "465");
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.port", "465");

        session = Session.getDefaultInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication("cloud7.buradd@gmail.com", "tickle9pants1");
            }
        });


    }

    @Override
    protected String doInBackground(String... params){
        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress("cloud7.buradd@gmail.com"));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(rec));
            message.setSubject(subject);
            message.setContent(textMessage, "text/html; charset=utf-8");
            Transport.send(message);
        } catch (MessagingException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(final String result){
        super.onPostExecute(result);

    }


}
