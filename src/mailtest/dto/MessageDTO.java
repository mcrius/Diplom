/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mailtest.dto;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;

/**
 *
 * @author bzzzt
 */
public class MessageDTO implements Serializable{
    private int id;
    private String from;
    private String recipient;
    private String cc;
    private String subject;
    private String body;
    private String[] attachmentNames;
    private Date receivedDate;
    private Date sentDate;
    private boolean read;
    private boolean textIsHtml = false;


    public MessageDTO(int id, String from, String recipient, String cc, String body, String[] attachmentNames, Date receivedDate, Date sentDate, boolean read) {
        this.id = id;
        this.from = from;
        this.recipient = recipient;
        this.cc = cc;
        this.body = body;
        this.attachmentNames = attachmentNames;
        this.receivedDate = receivedDate;
        this.sentDate = sentDate;
        this.read = read;
    }

    
    
    public MessageDTO(Message m){
        try {
            this.id = m.getMessageNumber();
            this.from = m.getFrom()[0].toString();
//            this.from = Arrays.toString(m.getFrom());
//                    .substring(1, Arrays.toString(m.getRecipients(Message.RecipientType.TO)).length() -1);
            this.recipient = Arrays.toString(m.getRecipients(Message.RecipientType.TO)).replaceAll("\\[", "").replaceAll("\\]", "");
            this.cc = Arrays.toString(m.getRecipients(Message.RecipientType.TO));
//                    .substring(1, Arrays.toString(m.getRecipients(Message.RecipientType.CC)).length()-1 );
            this.receivedDate = m.getReceivedDate();
            this.sentDate = m.getSentDate();
            this.subject = m.getSubject();
            this.body = getText(m);
            this.read = m.isExpunged();
            this.attachmentNames = createAttachmentNames(m);
        } catch (MessagingException | IOException ex) {
            Logger.getLogger(MessageDTO.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public MessageDTO() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public String getCc() {
        return cc;
    }

    public void setCc(String cc) {
        this.cc = cc;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public Date getSentDate() {
        return sentDate;
    }

    public void setSentDate(Date sentDate) {
        this.sentDate = sentDate;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String[] getAttachmentNames() {
        return attachmentNames;
    }

    public void setAttachmentNames(String[] attachmentNames) {
        this.attachmentNames = attachmentNames;
    }

    public Date getReceivedDate() {
        return receivedDate;
    }

    public void setReceivedDate(Date receivedDate) {
        this.receivedDate = receivedDate;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }
    
        /**
     * Return the primary text content of the message.
     */
    private String getText(Part p) throws MessagingException, IOException {
        if (p != null) {


            if (p.isMimeType("text/*")) {
                String s = (String) p.getContent();
                textIsHtml = p.isMimeType("text/html");
                return s;
            }

            if (p.isMimeType("multipart/alternative")) {
                // prefer html text over plain text
                Multipart mp = (Multipart) p.getContent();
                String text = null;
                for (int i = 0; i < mp.getCount(); i++) {
                    Part bp = mp.getBodyPart(i);
                    if (bp.isMimeType("text/plain")) {
                        if (text == null) {
                            text = getText(bp);
                        }
                        continue;
                    } else if (bp.isMimeType("text/html")) {
                        String s = getText(bp);
                        if (s != null) {
                            return s;
                        }
                    } else {
                        return getText(bp);
                    }
                }
                return text;
            } else if (p.isMimeType("multipart/*")) {
                Multipart mp = (Multipart) p.getContent();
                for (int i = 0; i < mp.getCount(); i++) {
                    String s = getText(mp.getBodyPart(i));
                    if (s != null) {
                        return s;
                    }
                }
            }
        }
            return null;
        
    }
    
    private String[] createAttachmentNames(Part m){
        ArrayList<String> files = new ArrayList<>();
        try {
            if (m.isMimeType("multipart/*")) {
                try {
                    Multipart mime = (Multipart) m.getContent();
                    for (int i = 0; i < mime.getCount(); i++) {
                        BodyPart bp = mime.getBodyPart(i);
                        if (bp.isMimeType("multipart/*")) {
                            Multipart inner = (Multipart) bp.getContent();
                            for (int j = 0; j < inner.getCount(); j++) {
                                if (inner.getBodyPart(j).getDisposition()!=null && inner.getBodyPart(j).getDisposition().equalsIgnoreCase(Part.ATTACHMENT)) {
                                    files.add(inner.getBodyPart(j).getFileName());
                                    //TODO hashmap to get stream easily with key fileName
                                }
                            }
                        }else{
                            if (bp.getDisposition()!=null && bp.getDisposition().equalsIgnoreCase(Part.ATTACHMENT)) {
                                files.add(bp.getFileName());
                            }
                        }
                    }
                } catch (        MessagingException | IOException ex) {
                    Logger.getLogger(MessageDTO.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        } catch (MessagingException ex) {
            Logger.getLogger(MessageDTO.class.getName()).log(Level.SEVERE, null, ex);
        }
        return files.toArray(new String[files.size()]);
    }

    
}
