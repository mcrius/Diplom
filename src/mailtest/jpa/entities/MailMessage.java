package mailtest.jpa.entities;

import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.MimeUtility;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Temporal;
import mailtest.utils.Utils;

/**
 *
 * @author bzzzt
 */
@Entity
@NamedQueries({
@NamedQuery(name = "MailMessage.findByMessageId", query = "SELECT m from MailMessage m where m.messageId = :messageId")})
public class MailMessage implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(name = "MESSAGE_ID", nullable = false)
    private Integer messageId;
    @Column(name = "SENDER", nullable = false)
    private String from;
    @Column(name = "RECIPIENT", nullable = false)
    private String to;
    @Column(name = "COPY_TO")
    private String cc;
    @Column(name = "SUBJECT")
    private String subject;
    @Column(name = "BODY")
    private String body;
    @OneToMany(mappedBy = "mailMessage", cascade = CascadeType.ALL)
    private List<AttachmentName> attachmentNames;
    @Column(name = "RECEIVED")
    @Temporal(javax.persistence.TemporalType.DATE)
    private Date receivedDate;
    @Column(name = "SENT")
    @Temporal(javax.persistence.TemporalType.DATE)
    private Date sentDate;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getMessageId() {
        return messageId;
    }

    public void setMessageId(Integer messageId) {
        this.messageId = messageId;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
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

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public List<AttachmentName> getAttachmentNames() {
        return attachmentNames;
    }

    public void setAttachmentNames(List<AttachmentName> attachmentNames) {
        this.attachmentNames = attachmentNames;
    }

    public Date getReceivedDate() {
        return receivedDate;
    }

    public void setReceivedDate(Date receivedDate) {
        this.receivedDate = receivedDate;
    }

    public Date getSentDate() {
        return sentDate;
    }

    public void setSentDate(Date sentDate) {
        this.sentDate = sentDate;
    }

    public MailMessage() {
    }
    
    public MailMessage(Message m) {
        try {
            this.messageId = m.getMessageNumber();
            this.from = MimeUtility.decodeText(m.getFrom()[0].toString());
            this.to = MimeUtility.decodeText(Arrays.toString(m.getRecipients(Message.RecipientType.TO)).replaceAll("\\[", "").replaceAll("\\]", ""));
            this.cc = MimeUtility.decodeText(Arrays.toString(m.getRecipients(Message.RecipientType.CC)).replaceAll("\\[", "").replaceAll("\\]", ""));
            this.receivedDate = m.getReceivedDate();
            this.sentDate = m.getSentDate();
            if (m.getSubject() != null) {
                this.subject = m.getSubject();
            }else{
                this.subject = "";
            }
            this.body = MimeUtility.decodeText(Utils.getText(m));
        } catch (MessagingException | IOException ex) {
            Logger.getLogger(MailMessage.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public MailMessage(Integer messageId, String from, String to, String cc, String subject, String body, Date receivedDate, Date sentDate) {
        this.messageId = messageId;
        this.from = from;
        this.to = to;
        this.cc = cc;
        this.subject = subject;
        this.body = body;
        this.receivedDate = receivedDate;
        this.sentDate = sentDate;
    }
    
    
    
    
    
    
    

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof MailMessage)) {
            return false;
        }
        MailMessage other = (MailMessage) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "mailtest.entities.MailMessage[ id=" + id + " ]";
    }
    
}
