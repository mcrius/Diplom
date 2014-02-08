/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package mailtest.jpa.entities;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

/**
 *
 * @author bzzzt
 */
@Entity
public class SettingsProperty implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(nullable = false, name = "PROP_KEY", length = 120)
    private String key;
    @Column(nullable = false, name = "PROP_VALUE", length = 120)
    private String value;
    
    @ManyToOne
    private Setting setting;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Setting getSetting() {
        return setting;
    }

    public void setSetting(Setting setting) {
        this.setting = setting;
    }

    public SettingsProperty(String key, String value, Setting setting) {
        this.key = key;
        this.value = value;
        this.setting = setting;
    }
    
    public SettingsProperty(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public SettingsProperty() {
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
        if (!(object instanceof SettingsProperty)) {
            return false;
        }
        SettingsProperty other = (SettingsProperty) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "mailtest.entities.SettingsProperty[ id=" + id + " ]";
    }
    
}
