/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mailtest.dto;

import java.io.Serializable;

/**
 *
 * @author bzzzt
 */
public class Property implements Serializable{
    private String key;
    private String value;

    public Property() {
    }

    public Property(String key, String value) {
        this.key = key;
        this.value = value;
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
    
    
}
