/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mailtest.runnables;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.MimeUtility;
import javax.mail.search.AddressStringTerm;
import javax.mail.search.AndTerm;
import javax.mail.search.BodyTerm;
import javax.mail.search.SearchTerm;
import javax.mail.search.SubjectTerm;
import mailtest.MailTest;
import mailtest.SearchScene.SearchSceneController;
import mailtest.jpa.entities.MailMessage;

/**
 *
 * @author v.georgiev
 */
public class SearchInMailThread implements Runnable {

    private String from;
    private String to;
    private String cc;
    private String subject;
    private String body;

    public SearchInMailThread() {
    }

    public SearchInMailThread(String from, String to, String cc, String subject, String body) {
        this.from = from.trim().toLowerCase();
        this.to = to.trim().toLowerCase();
        this.cc = cc.trim().toLowerCase();
        this.subject = subject.trim();
        this.body = body.trim();
    }
    
    

    @Override
    public void run() {
        Platform.runLater(new Runnable() {

            @Override
            public void run() {
                SearchSceneController.getProgress().setVisible(true);
            }
        });
        List<SearchTerm> terms = new ArrayList<>();
        if (!from.equals("")) {
            SearchTerm toTerm = new AddressStringTerm(to) {
                @Override
                public boolean match(Message msg) {
                    try {
                        Address[] recipients = msg.getRecipients(Message.RecipientType.TO);
                        for (Address address : recipients) {
                            if (address != null) {
                                if (MimeUtility.decodeText(address.toString().toLowerCase()).contains(to)) {
                                    return true;
                                }
                            }
                        }
                    } catch (MessagingException | UnsupportedEncodingException ex) {
                        Logger.getLogger(SearchInMailThread.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    return false;
                }
            };
            terms.add(toTerm);
        }
        if (!to.equals("")) {
            SearchTerm fromTerm = new AddressStringTerm(from) {
                @Override
                public boolean match(Message msg) {
                    try {
                        Address[] recipients = msg.getFrom();
                        for (Address address : recipients) {
                            if (address != null) {
                                if (MimeUtility.decodeText(address.toString().toLowerCase()).contains(from)) {
                                    return true;
                                }
                            }
                        }
                    } catch (MessagingException | UnsupportedEncodingException ex) {
                        Logger.getLogger(SearchInMailThread.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    return false;
                }
            };
            terms.add(fromTerm);
        }
        if (!cc.equals("")) {
            SearchTerm ccTerm = new AddressStringTerm(cc) {
                @Override
                public boolean match(Message msg) {
                    try {
                        Address[] recipients = msg.getRecipients(Message.RecipientType.CC);
                        for (Address address : recipients) {
                            if (address != null) {
                                if (MimeUtility.decodeText(address.toString().toLowerCase()).contains(cc)) {
                                    return true;
                                }
                            }
                        }
                    } catch (MessagingException | UnsupportedEncodingException ex) {
                        Logger.getLogger(SearchInMailThread.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    return false;
                }
            };
            terms.add(ccTerm);
        }
        if (!subject.equals("")) {
            SearchTerm subjectTerm = new SubjectTerm(subject);
            terms.add(subjectTerm);
        }
//        if (!body.equals("")) {
//            SearchTerm bodyterm = new BodyTerm(body);
//            terms.add(bodyterm);
//        }
        if (!terms.isEmpty()) {
            
            SearchTerm st = new AndTerm(terms.toArray(new SearchTerm[terms.size()]));
            try {
                System.out.println("Searching...");
                Message[] msgs = MailTest.getInbox().search(st);
                System.out.println("Found " + msgs.length + " messages!");
                if (msgs.length > 0) {
                    System.out.println("Parsing...");
                    for (Message message : msgs) {
                        final MailMessage mm = new MailMessage(message);
                        String body = mm.getBody();
                        if (!from.equals("")) {
                            body = body.replaceAll(from, String.format("<span style=\"background-color:yellow\"> %s </span>", from));
                        }
                        if (!to.equals("")) {
                            body = body.replaceAll(to, String.format("<span style=\"background-color:yellow\"> %s </span>", to));
                        }
                        if (!cc.equals("")) {
                            body = body.replaceAll(cc, String.format("<span style=\"background-color:yellow\"> %s </span>", cc));
                        }
                        if (!subject.equals("")) {
                            body = body.replaceAll(subject, String.format("<span style=\"background-color:yellow\"> %s </span>", subject));
                        }
                        if (!body.equals("")) {
                            body = body.replaceAll(body, String.format("<span style=\"background-color:yellow\"> %s </span>", body));
                        }
                        mm.setBody(body);
                        System.out.println("Add to table");
                        Platform.runLater(new Runnable() {

                            @Override
                            public void run() {
                                SearchSceneController.getTable().getItems().add(mm);
                            }
                        });

                    }
                }
            } catch (MessagingException ex) {
                Logger.getLogger(SearchInMailThread.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        Platform.runLater(new Runnable() {

            @Override
            public void run() {
                SearchSceneController.getProgress().setVisible(false);
            }
        });
    }
}
