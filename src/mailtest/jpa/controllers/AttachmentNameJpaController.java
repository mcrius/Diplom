/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package mailtest.jpa.controllers;

import java.io.Serializable;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import mailtest.jpa.controllers.exceptions.NonexistentEntityException;
import mailtest.jpa.entities.AttachmentName;
import mailtest.jpa.entities.MailMessage;

/**
 *
 * @author bzzzt
 */
public class AttachmentNameJpaController implements Serializable {

    public AttachmentNameJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(AttachmentName attachmentName) {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            MailMessage mailMessage = attachmentName.getMailMessage();
            if (mailMessage != null) {
                mailMessage = em.getReference(mailMessage.getClass(), mailMessage.getId());
                attachmentName.setMailMessage(mailMessage);
            }
            em.persist(attachmentName);
            if (mailMessage != null) {
                mailMessage.getAttachmentNames().add(attachmentName);
                mailMessage = em.merge(mailMessage);
            }
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(AttachmentName attachmentName) throws NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            AttachmentName persistentAttachmentName = em.find(AttachmentName.class, attachmentName.getId());
            MailMessage mailMessageOld = persistentAttachmentName.getMailMessage();
            MailMessage mailMessageNew = attachmentName.getMailMessage();
            if (mailMessageNew != null) {
                mailMessageNew = em.getReference(mailMessageNew.getClass(), mailMessageNew.getId());
                attachmentName.setMailMessage(mailMessageNew);
            }
            attachmentName = em.merge(attachmentName);
            if (mailMessageOld != null && !mailMessageOld.equals(mailMessageNew)) {
                mailMessageOld.getAttachmentNames().remove(attachmentName);
                mailMessageOld = em.merge(mailMessageOld);
            }
            if (mailMessageNew != null && !mailMessageNew.equals(mailMessageOld)) {
                mailMessageNew.getAttachmentNames().add(attachmentName);
                mailMessageNew = em.merge(mailMessageNew);
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Integer id = attachmentName.getId();
                if (findAttachmentName(id) == null) {
                    throw new NonexistentEntityException("The attachmentName with id " + id + " no longer exists.");
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
            AttachmentName attachmentName;
            try {
                attachmentName = em.getReference(AttachmentName.class, id);
                attachmentName.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The attachmentName with id " + id + " no longer exists.", enfe);
            }
            MailMessage mailMessage = attachmentName.getMailMessage();
            if (mailMessage != null) {
                mailMessage.getAttachmentNames().remove(attachmentName);
                mailMessage = em.merge(mailMessage);
            }
            em.remove(attachmentName);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<AttachmentName> findAttachmentNameEntities() {
        return findAttachmentNameEntities(true, -1, -1);
    }

    public List<AttachmentName> findAttachmentNameEntities(int maxResults, int firstResult) {
        return findAttachmentNameEntities(false, maxResults, firstResult);
    }

    private List<AttachmentName> findAttachmentNameEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(AttachmentName.class));
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

    public AttachmentName findAttachmentName(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(AttachmentName.class, id);
        } finally {
            em.close();
        }
    }

    public int getAttachmentNameCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<AttachmentName> rt = cq.from(AttachmentName.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
