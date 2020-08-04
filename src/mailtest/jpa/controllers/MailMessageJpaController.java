/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package mailtest.jpa.controllers;

import java.io.Serializable;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import mailtest.jpa.entities.AttachmentName;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import mailtest.jpa.controllers.exceptions.NonexistentEntityException;
import mailtest.jpa.entities.MailMessage;

/**
 *
 * @author bzzzt
 */
public class MailMessageJpaController implements Serializable {

    public MailMessageJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(MailMessage mailMessage) {
        if (mailMessage.getAttachmentNames() == null) {
            mailMessage.setAttachmentNames(new ArrayList<AttachmentName>());
        }
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            List<AttachmentName> attachedAttachmentNames = new ArrayList<AttachmentName>();
            for (AttachmentName attachmentNamesAttachmentNameToAttach : mailMessage.getAttachmentNames()) {
                attachmentNamesAttachmentNameToAttach = em.getReference(attachmentNamesAttachmentNameToAttach.getClass(), attachmentNamesAttachmentNameToAttach.getId());
                attachedAttachmentNames.add(attachmentNamesAttachmentNameToAttach);
            }
            mailMessage.setAttachmentNames(attachedAttachmentNames);
            em.persist(mailMessage);
            for (AttachmentName attachmentNamesAttachmentName : mailMessage.getAttachmentNames()) {
                MailMessage oldMailMessageOfAttachmentNamesAttachmentName = attachmentNamesAttachmentName.getMailMessage();
                attachmentNamesAttachmentName.setMailMessage(mailMessage);
                attachmentNamesAttachmentName = em.merge(attachmentNamesAttachmentName);
                if (oldMailMessageOfAttachmentNamesAttachmentName != null) {
                    oldMailMessageOfAttachmentNamesAttachmentName.getAttachmentNames().remove(attachmentNamesAttachmentName);
                    oldMailMessageOfAttachmentNamesAttachmentName = em.merge(oldMailMessageOfAttachmentNamesAttachmentName);
                }
            }
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(MailMessage mailMessage) throws NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            MailMessage persistentMailMessage = em.find(MailMessage.class, mailMessage.getId());
            List<AttachmentName> attachmentNamesOld = persistentMailMessage.getAttachmentNames();
            List<AttachmentName> attachmentNamesNew = mailMessage.getAttachmentNames();
            List<AttachmentName> attachedAttachmentNamesNew = new ArrayList<AttachmentName>();
            for (AttachmentName attachmentNamesNewAttachmentNameToAttach : attachmentNamesNew) {
                attachmentNamesNewAttachmentNameToAttach = em.getReference(attachmentNamesNewAttachmentNameToAttach.getClass(), attachmentNamesNewAttachmentNameToAttach.getId());
                attachedAttachmentNamesNew.add(attachmentNamesNewAttachmentNameToAttach);
            }
            attachmentNamesNew = attachedAttachmentNamesNew;
            mailMessage.setAttachmentNames(attachmentNamesNew);
            mailMessage = em.merge(mailMessage);
            for (AttachmentName attachmentNamesOldAttachmentName : attachmentNamesOld) {
                if (!attachmentNamesNew.contains(attachmentNamesOldAttachmentName)) {
                    attachmentNamesOldAttachmentName.setMailMessage(null);
                    attachmentNamesOldAttachmentName = em.merge(attachmentNamesOldAttachmentName);
                }
            }
            for (AttachmentName attachmentNamesNewAttachmentName : attachmentNamesNew) {
                if (!attachmentNamesOld.contains(attachmentNamesNewAttachmentName)) {
                    MailMessage oldMailMessageOfAttachmentNamesNewAttachmentName = attachmentNamesNewAttachmentName.getMailMessage();
                    attachmentNamesNewAttachmentName.setMailMessage(mailMessage);
                    attachmentNamesNewAttachmentName = em.merge(attachmentNamesNewAttachmentName);
                    if (oldMailMessageOfAttachmentNamesNewAttachmentName != null && !oldMailMessageOfAttachmentNamesNewAttachmentName.equals(mailMessage)) {
                        oldMailMessageOfAttachmentNamesNewAttachmentName.getAttachmentNames().remove(attachmentNamesNewAttachmentName);
                        oldMailMessageOfAttachmentNamesNewAttachmentName = em.merge(oldMailMessageOfAttachmentNamesNewAttachmentName);
                    }
                }
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Integer id = mailMessage.getId();
                if (findMailMessage(id) == null) {
                    throw new NonexistentEntityException("The mailMessage with id " + id + " no longer exists.");
                }
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void destroy(Integer id) throws NonexistentEntityException {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            MailMessage mailMessage;
            try {
                mailMessage = em.getReference(MailMessage.class, id);
                mailMessage.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The mailMessage with id " + id + " no longer exists.", enfe);
            }
            List<AttachmentName> attachmentNames = mailMessage.getAttachmentNames();
            for (AttachmentName attachmentNamesAttachmentName : attachmentNames) {
                attachmentNamesAttachmentName.setMailMessage(null);
                attachmentNamesAttachmentName = em.merge(attachmentNamesAttachmentName);
            }
            em.remove(mailMessage);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<MailMessage> findMailMessageEntities() {
        return findMailMessageEntities(true, -1, -1);
    }

    public List<MailMessage> findMailMessageEntities(int maxResults, int firstResult) {
        return findMailMessageEntities(false, maxResults, firstResult);
    }

    private List<MailMessage> findMailMessageEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(MailMessage.class));
            Query q = em.createQuery(cq);
            if (!all) {
                q.setMaxResults(maxResults);
                q.setFirstResult(firstResult);
            }
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    public MailMessage findMailMessage(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(MailMessage.class, id);
        } finally {
            em.close();
        }
    }

    public MailMessage findMailMessageByMessageId(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return (MailMessage) em.createNamedQuery("MailMessage.findByMessageId").setParameter("messageId", id).getSingleResult();
//            return em.find(MailMessage.class, id);
        } finally {
            em.close();
        }
    }

    public int getMailMessageCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<MailMessage> rt = cq.from(MailMessage.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
