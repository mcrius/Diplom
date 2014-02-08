/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package mailtest.jpa.entities;

import java.io.Serializable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

/**
 *
 * @author bzzzt
 */
@Entity
public class AttachmentName implements Serializable {
    private static final long serialVersionUID = 1L;
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;
    
    @Column(name = "ATTACHMENT_NAME")
    private String attachmentName;
    
    @ManyToOne
    @JoinColumn(foreignKey = @ForeignKey(name = "FK_ATTACHMENTNAME_MAILMESSAGE_ID"), name = "MAILMESSAGE_ID", referencedColumnName = "ID")
    private MailMessage mailMessage;
    
    
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getAttachmentName() {
        return attachmentName;
    }

    public void setAttachmentName(String attachmentName) {
        this.attachmentName = attachmentName;
    }

    public MailMessage getMailMessage() {
        return mailMessage;
    }

    public void setMailMessage(MailMessage mailMessage) {
        this.mailMessage = mailMessage;
    }

    public AttachmentName() {
    }

    public AttachmentName(String attachmentName, MailMessage mailMessage) {
        this.attachmentName = attachmentName;
        this.mailMessage = mailMessage;
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
        if (!(object instanceof AttachmentName)) {
            return false;
        }
        AttachmentName other = (AttachmentName) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "mailtest.entities.AttachmentName[ id=" + id + " ]";
    }
    
}
