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
import mailtest.jpa.entities.SettingsProperty;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import mailtest.jpa.entities.Setting;
import mailtest.jpa.controllers.exceptions.NonexistentEntityException;

/**
 *
 * @author bzzzt
 */
public class SettingController implements Serializable {

    public SettingController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Setting setting) {
        if (setting.getProperties() == null) {
            setting.setProperties(new ArrayList<SettingsProperty>());
        }
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            List<SettingsProperty> attachedProperties = new ArrayList<SettingsProperty>();
            for (SettingsProperty propertiesSettingsPropertyToAttach : setting.getProperties()) {
                propertiesSettingsPropertyToAttach = em.getReference(propertiesSettingsPropertyToAttach.getClass(), propertiesSettingsPropertyToAttach.getId());
                attachedProperties.add(propertiesSettingsPropertyToAttach);
            }
            setting.setProperties(attachedProperties);
            em.persist(setting);
            for (SettingsProperty propertiesSettingsProperty : setting.getProperties()) {
                Setting oldSettingOfPropertiesSettingsProperty = propertiesSettingsProperty.getSetting();
                propertiesSettingsProperty.setSetting(setting);
                propertiesSettingsProperty = em.merge(propertiesSettingsProperty);
                if (oldSettingOfPropertiesSettingsProperty != null) {
                    oldSettingOfPropertiesSettingsProperty.getProperties().remove(propertiesSettingsProperty);
                    oldSettingOfPropertiesSettingsProperty = em.merge(oldSettingOfPropertiesSettingsProperty);
                }
            }
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(Setting setting) throws NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Setting persistentSetting = em.find(Setting.class, setting.getId());
            List<SettingsProperty> propertiesOld = persistentSetting.getProperties();
            List<SettingsProperty> propertiesNew = setting.getProperties();
            List<SettingsProperty> attachedPropertiesNew = new ArrayList<SettingsProperty>();
            for (SettingsProperty propertiesNewSettingsPropertyToAttach : propertiesNew) {
                propertiesNewSettingsPropertyToAttach = em.getReference(propertiesNewSettingsPropertyToAttach.getClass(), propertiesNewSettingsPropertyToAttach.getId());
                attachedPropertiesNew.add(propertiesNewSettingsPropertyToAttach);
            }
            propertiesNew = attachedPropertiesNew;
            setting.setProperties(propertiesNew);
            setting = em.merge(setting);
            for (SettingsProperty propertiesOldSettingsProperty : propertiesOld) {
                if (!propertiesNew.contains(propertiesOldSettingsProperty)) {
                    propertiesOldSettingsProperty.setSetting(null);
                    propertiesOldSettingsProperty = em.merge(propertiesOldSettingsProperty);
                }
            }
            for (SettingsProperty propertiesNewSettingsProperty : propertiesNew) {
                if (!propertiesOld.contains(propertiesNewSettingsProperty)) {
                    Setting oldSettingOfPropertiesNewSettingsProperty = propertiesNewSettingsProperty.getSetting();
                    propertiesNewSettingsProperty.setSetting(setting);
                    propertiesNewSettingsProperty = em.merge(propertiesNewSettingsProperty);
                    if (oldSettingOfPropertiesNewSettingsProperty != null && !oldSettingOfPropertiesNewSettingsProperty.equals(setting)) {
                        oldSettingOfPropertiesNewSettingsProperty.getProperties().remove(propertiesNewSettingsProperty);
                        oldSettingOfPropertiesNewSettingsProperty = em.merge(oldSettingOfPropertiesNewSettingsProperty);
                    }
                }
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Integer id = setting.getId();
                if (findSetting(id) == null) {
                    throw new NonexistentEntityException("The setting with id " + id + " no longer exists.");
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
            Setting setting;
            try {
                setting = em.getReference(Setting.class, id);
                setting.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The setting with id " + id + " no longer exists.", enfe);
            }
            List<SettingsProperty> properties = setting.getProperties();
            for (SettingsProperty propertiesSettingsProperty : properties) {
                propertiesSettingsProperty.setSetting(null);
                propertiesSettingsProperty = em.merge(propertiesSettingsProperty);
            }
            em.remove(setting);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<Setting> findSettingEntities() {
        return findSettingEntities(true, -1, -1);
    }

    public List<Setting> findSettingEntities(int maxResults, int firstResult) {
        return findSettingEntities(false, maxResults, firstResult);
    }

    private List<Setting> findSettingEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Setting.class));
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

    public Setting findSetting(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Setting.class, id);
        } finally {
            em.close();
        }
    }

    public int getSettingCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Setting> rt = cq.from(Setting.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
    public void removeAll(){
        EntityManager em = getEntityManager();
        em.createNativeQuery("TRUNCATE TABLE SETTINGSPROPERTY").executeUpdate();
        em.createNativeQuery("TRUNCATE TABLE SETTING").executeUpdate();
    }
    
}
