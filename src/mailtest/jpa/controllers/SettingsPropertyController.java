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
import mailtest.jpa.entities.Setting;
import mailtest.jpa.entities.SettingsProperty;
import mailtest.jpa.controllers.exceptions.NonexistentEntityException;

/**
 *
 * @author bzzzt
 */
public class SettingsPropertyController implements Serializable {

    public SettingsPropertyController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(SettingsProperty settingsProperty) {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Setting setting = settingsProperty.getSetting();
            if (setting != null) {
                setting = em.getReference(setting.getClass(), setting.getId());
                settingsProperty.setSetting(setting);
            }
            em.persist(settingsProperty);
            if (setting != null) {
                setting.getProperties().add(settingsProperty);
                setting = em.merge(setting);
            }
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(SettingsProperty settingsProperty) throws NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            SettingsProperty persistentSettingsProperty = em.find(SettingsProperty.class, settingsProperty.getId());
            Setting settingOld = persistentSettingsProperty.getSetting();
            Setting settingNew = settingsProperty.getSetting();
            if (settingNew != null) {
                settingNew = em.getReference(settingNew.getClass(), settingNew.getId());
                settingsProperty.setSetting(settingNew);
            }
            settingsProperty = em.merge(settingsProperty);
            if (settingOld != null && !settingOld.equals(settingNew)) {
                settingOld.getProperties().remove(settingsProperty);
                settingOld = em.merge(settingOld);
            }
            if (settingNew != null && !settingNew.equals(settingOld)) {
                settingNew.getProperties().add(settingsProperty);
                settingNew = em.merge(settingNew);
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Integer id = settingsProperty.getId();
                if (findSettingsProperty(id) == null) {
                    throw new NonexistentEntityException("The settingsProperty with id " + id + " no longer exists.");
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
            SettingsProperty settingsProperty;
            try {
                settingsProperty = em.getReference(SettingsProperty.class, id);
                settingsProperty.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The settingsProperty with id " + id + " no longer exists.", enfe);
            }
            Setting setting = settingsProperty.getSetting();
            if (setting != null) {
                setting.getProperties().remove(settingsProperty);
                setting = em.merge(setting);
            }
            em.remove(settingsProperty);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<SettingsProperty> findSettingsPropertyEntities() {
        return findSettingsPropertyEntities(true, -1, -1);
    }

    public List<SettingsProperty> findSettingsPropertyEntities(int maxResults, int firstResult) {
        return findSettingsPropertyEntities(false, maxResults, firstResult);
    }

    private List<SettingsProperty> findSettingsPropertyEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(SettingsProperty.class));
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

    public SettingsProperty findSettingsProperty(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(SettingsProperty.class, id);
        } finally {
            em.close();
        }
    }

    public int getSettingsPropertyCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<SettingsProperty> rt = cq.from(SettingsProperty.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
