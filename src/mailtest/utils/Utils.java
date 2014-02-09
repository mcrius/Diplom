/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mailtest.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.internet.MimeUtility;
import mailtest.MailTest;
import mailtest.jpa.entities.AttachmentName;
import mailtest.jpa.entities.MailMessage;
import mailtest.jpa.entities.SettingsProperty;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.index.IndexWriter;
import org.jsoup.Jsoup;

/**
 *
 * @author bzzzt
 */
public class Utils {

    public static List<SettingsProperty> makeProperties(String incHost, String outHost, String incPort, String outPort, String incSSL, String outSSL, String inProtocol, boolean isGmail) {
        List<SettingsProperty> props = new ArrayList<>();
        if (isGmail) {
            props.add(new SettingsProperty("mail.smtp.auth", "true"));
            props.add(new SettingsProperty("mail.smtp.starttls.enable", "true"));
            props.add(new SettingsProperty("mail.smtp.host", "smtp.gmail.com"));
            props.add(new SettingsProperty("mail.smtp.port", "587"));
            props.add(new SettingsProperty("mail.store.protocol", "imaps"));
            props.add(new SettingsProperty("mail.transport.protocol", "smtp"));
            props.add(new SettingsProperty("mail.imap.host", "imap.gmail.com"));
            props.add(new SettingsProperty("mail.imap.port", "993"));
            props.add(new SettingsProperty("mail.store.host", "imap.gmail.com"));
        } else {
            props.add(new SettingsProperty("mail.smtp.host", outHost));
            props.add(new SettingsProperty("mail.smtp.port", outPort));
            if (outSSL.equalsIgnoreCase("starttls")) {
                props.add(new SettingsProperty("mail.smtp.starttls.enable", "true"));
                props.add(new SettingsProperty("mail.smtp.auth", "true"));
            }
            if (outSSL.equalsIgnoreCase("SSL/TLS")) {
                props.add(new SettingsProperty("mail.transport.protocol", "smtps"));
                props.add(new SettingsProperty("mail.smtp.auth", "true"));
            } else {
                props.add(new SettingsProperty("mail.transport.protocol", "smtp"));
            }

            if (inProtocol.equalsIgnoreCase("POP3")) {
                props.add(new SettingsProperty("mail.pop3.host", incHost));
                props.add(new SettingsProperty("mail.store.host", incHost));
                props.add(new SettingsProperty("mail.pop3.port", incPort));
                if (incSSL.equalsIgnoreCase("starttls")) {
                    props.add(new SettingsProperty("mail.pop3.starttls.enable", "true"));
                    props.add(new SettingsProperty("mail.pop3.auth", "true"));
                } else {
                    if (incSSL.equalsIgnoreCase("SSL/TLS")) {
                        props.add(new SettingsProperty("mail.store.protocol", "pop3s"));
                    } else {
                        props.add(new SettingsProperty("mail.store.protocol", "pop3"));
                    }
                }
            } else {
                props.add(new SettingsProperty("mail.imap.host", incHost));
                props.add(new SettingsProperty("mail.store.host", incHost));
                props.add(new SettingsProperty("mail.imap.port", incPort));
                if (incSSL.equalsIgnoreCase("starttls")) {
                    props.add(new SettingsProperty("mail.imap.starttls.enable", "true"));
                    props.add(new SettingsProperty("mail.imap.auth", "true"));
                } else {
                    if (incSSL.equalsIgnoreCase("SSL/TLS")) {
                        props.add(new SettingsProperty("mail.store.protocol", "imaps"));
                    } else {
                        props.add(new SettingsProperty("mail.store.protocol", "imap"));
                    }
                }
            }
        }
        return props;
    }

    public static Properties getConnectionProperties(List<SettingsProperty> props) {
        System.setProperty("mail.mime.decodetext.strict", "false");
        Properties p = System.getProperties();
//        p.setProperty("mail.mime.decodetext.strict", "false");
        for (SettingsProperty sp : props) {
            p.setProperty(sp.getKey(), sp.getValue());
        }
        return p;
    }

    /**
     * Return the primary text content of the message.
     *
     * @param p
     * @return
     * @throws javax.mail.MessagingException
     * @throws java.io.IOException
     */
    public static String getText(Part p) throws MessagingException, IOException {
        boolean textIsHtml = false;
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

    public static List<AttachmentName> createAttachmentNames(Part m, MailMessage mm) {
        ArrayList<AttachmentName> files = new ArrayList<>();
        try {
            if (m.isMimeType("multipart/*")) {
                try {
                    Multipart mime = (Multipart) m.getContent();
                    for (int i = 0; i < mime.getCount(); i++) {
                        BodyPart bp = mime.getBodyPart(i);
                        if (bp.isMimeType("multipart/*")) {
                            Multipart inner = (Multipart) bp.getContent();
                            for (int j = 0; j < inner.getCount(); j++) {
                                if (inner.getBodyPart(j).getDisposition() != null && inner.getBodyPart(j).getDisposition().equalsIgnoreCase(Part.ATTACHMENT)) {
                                    files.add(new AttachmentName(MimeUtility.decodeText(inner.getBodyPart(j).getFileName()), mm));
                                }
                            }
                        } else {
                            if (bp.getDisposition() != null && bp.getDisposition().equalsIgnoreCase(Part.ATTACHMENT)) {
                                files.add(new AttachmentName(MimeUtility.decodeText(bp.getFileName()), mm));
                            }
                        }
                    }
                } catch (MessagingException | IOException ex) {
                    Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        } catch (MessagingException ex) {
            Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, ex);
        }
        return files;
    }

    public static InputStream[] getAttachmentStreams(String[] fileNames, Integer id) {
        try {
            ArrayList<InputStream> streams = new ArrayList<>();
            Part m = MailTest.getInbox().getMessage(id);
            try {
                if (m.isMimeType("multipart/*")) {
                    try {
                        Multipart mime = (Multipart) m.getContent();
                        for (int i = 0; i < mime.getCount(); i++) {
                            BodyPart bp = mime.getBodyPart(i);
                            if (bp.isMimeType("multipart/*")) {
                                Multipart inner = (Multipart) bp.getContent();
                                for (int j = 0; j < inner.getCount(); j++) {
                                    if (inner.getBodyPart(j).getDisposition() != null && inner.getBodyPart(j).getDisposition().equalsIgnoreCase(Part.ATTACHMENT)) {
                                        for (int k = 0; k < fileNames.length; k++) {
                                            if (inner.getBodyPart(j).getFileName().equals(fileNames[k])) {
                                                streams.add(inner.getBodyPart(j).getInputStream());
                                            }
                                        }
                                    }
                                }
                            } else {
                                if (bp.getDisposition() != null && bp.getDisposition().equalsIgnoreCase(Part.ATTACHMENT)) {
                                    for (int k = 0; k < fileNames.length; k++) {
                                        if (bp.getFileName().equals(fileNames[k])) {
                                            streams.add(bp.getInputStream());
                                        }
                                    }
                                }
                            }
                        }
                    } catch (MessagingException | IOException ex) {
                        Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            } catch (MessagingException ex) {
                Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, ex);
            }
            return streams.toArray(new InputStream[streams.size()]);
        } catch (MessagingException ex) {
            Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public static InputStream[] getAttachmentStreams(List<AttachmentName> fileNames, Integer id) {
        ArrayList<InputStream> streams = new ArrayList<>();
        try {

            Part m = MailTest.getInbox().getMessage(id);
            try {
                if (m.isMimeType("multipart/*")) {
                    try {
                        Multipart mime = (Multipart) m.getContent();
                        for (int i = 0; i < mime.getCount(); i++) {
                            BodyPart bp = mime.getBodyPart(i);
                            if (bp.isMimeType("multipart/*")) {
                                Multipart inner = (Multipart) bp.getContent();
                                for (int j = 0; j < inner.getCount(); j++) {
                                    if (inner.getBodyPart(j).getDisposition() != null && inner.getBodyPart(j).getDisposition().equalsIgnoreCase(Part.ATTACHMENT)) {
                                        for (AttachmentName name : fileNames) {
                                            if (inner.getBodyPart(j).getFileName().equals(name.getAttachmentName())) {
                                                streams.add(inner.getBodyPart(j).getInputStream());
                                            }
                                        }
                                    }
                                }
                            } else {
                                if (bp.getDisposition() != null && bp.getDisposition().equalsIgnoreCase(Part.ATTACHMENT)) {
                                    for (AttachmentName name : fileNames) {
                                        if (bp.getFileName().equals(name.getAttachmentName())) {
                                            streams.add(bp.getInputStream());
                                        }
                                    }
                                }
                            }
                        }
                    } catch (MessagingException | IOException ex) {
                        Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            } catch (MessagingException ex) {
                Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, ex);
            }

        } catch (MessagingException ex) {
            Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, ex);
        }
        return streams.toArray(new InputStream[streams.size()]);
    }
    
    
    public static void addMessageToIndex(IndexWriter w, MailMessage mm) throws IOException{
        Document document = new Document();
        FieldType type = new FieldType();
        type.setIndexed(true);
        type.setTokenized(true);
        type.setStored(true);
        type.setStoreTermVectors(true);
        type.setStoreTermVectorPositions(true);
        type.freeze();
        
        document.add(new StoredField("messageId", mm.getMessageId()));
        document.add(new Field("from", mm.getFrom(), type));
        document.add(new Field("to", mm.getTo(), type));
        document.add(new Field("cc", mm.getCc(), type));
        document.add(new Field("subject", mm.getSubject(), type));
        document.add(new Field("body", parseHTML(mm.getBody()), type));
        w.addDocument(document);
    }
    
    
    public static String parseHTML(String html){
        return Jsoup.parse(html).text();
    }
    
}

