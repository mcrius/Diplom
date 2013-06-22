/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mailtest.dto;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author bzzzt
 */
public class SettingsDTO implements Serializable {

    private List<Property> properties;
    private String username;
    private String password;

    public static SettingsDTO readDtoFromFile() throws IOException, ClassNotFoundException {
        if (SettingsDTO.checkFile()) {
            File file = new File("settings.blurp");
            SettingsDTO dto;
            try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))) {
                dto = (SettingsDTO) in.readObject();
            }
            return dto;
        } else {
            return null;
        }
    }

    public static boolean checkFile() {
        File file = new File("settings.blurp");
        return file.exists();
    }

    public static void createFile() {
        File file = new File("settings.blurp");
        if (!file.exists()) {
            try {
                file.createNewFile();
                saveDtoToFile(new SettingsDTO());
            } catch (IOException ex) {
                Logger.getLogger(SettingsDTO.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public static void saveDtoToFile(SettingsDTO dto) throws FileNotFoundException, IOException {
        File file = new File("settings.blurp");
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file))) {
            out.writeObject(dto);
            out.flush();
        }
    }

    public SettingsDTO() {
    }

    public SettingsDTO(List<Property> properties, String username, String password) {
        this.properties = properties;
        this.username = username;
        this.password = password;
    }

    public List<Property> getProperties() {
        return properties;
    }

    public void setProperties(List<Property> properties) {
        this.properties = properties;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Properties getConnectionProperties() {
        Properties p = System.getProperties();
        for (int i = 0; i < this.properties.size(); i++) {
            p.setProperty(this.properties.get(i).getKey(), this.properties.get(i).getValue());
        }
        return p;
    }

    public List<Property> makeProperties(String incHost, String outHost, String incPort, String outPort, String incSSL, String outSSL, String inProtocol, boolean isGmail) {
        List<Property> props = new ArrayList<>();
        if (isGmail) {
            props.add(new Property("mail.smtp.auth", "true"));
            props.add(new Property("mail.smtp.starttls.enable", "true"));
            props.add(new Property("mail.smtp.host", "smtp.gmail.com"));
            props.add(new Property("mail.smtp.port", "587"));
            props.add(new Property("mail.store.protocol", "imaps"));
            props.add(new Property("mail.imap.host", "imap.gmail.com"));
            props.add(new Property("mail.imap.port", "993"));
            props.add(new Property("mail.store.host", "imap.gmail.com"));
        } else {
            props.add(new Property("mail.smtp.host", outHost));
            props.add(new Property("mail.smtp.port", outPort));
            if (outSSL.equalsIgnoreCase("starttls")) {
                props.add(new Property("mail.smtp.starttls.enable", "true"));
                props.add(new Property("mail.smtp.auth", "true"));
            }
            if (outSSL.equalsIgnoreCase("SSL/TLS")) {
                props.add(new Property("mail.transport.protocol", "smtps"));
                props.add(new Property("mail.smtp.auth", "true"));
            } else {
                props.add(new Property("mail.transport.protocol", "smtp"));
            }

            if (inProtocol.equalsIgnoreCase("POP3")) {
                props.add(new Property("mail.pop3.host", incHost));
                props.add(new Property("mail.store.host", incHost));
                props.add(new Property("mail.pop3.port", incPort));
                if (incSSL.equalsIgnoreCase("starttls")) {
                    props.add(new Property("mail.pop3.starttls.enable", "true"));
                    props.add(new Property("mail.pop3.auth", "true"));
                } else {
                    if (incSSL.equalsIgnoreCase("SSL/TLS")) {
                        props.add(new Property("mail.store.protocol", "pop3s"));
                    } else {
                        props.add(new Property("mail.store.protocol", "pop3"));
                    }
                }
            } else {
                props.add(new Property("mail.imap.host", incHost));
                props.add(new Property("mail.store.host", incHost));
                props.add(new Property("mail.imap.port", incPort));
                if (incSSL.equalsIgnoreCase("starttls")) {
                    props.add(new Property("mail.imap.starttls.enable", "true"));
                    props.add(new Property("mail.imap.auth", "true"));
                } else {
                    if (incSSL.equalsIgnoreCase("SSL/TLS")) {
                        props.add(new Property("mail.store.protocol", "imaps"));
                    } else {
                        props.add(new Property("mail.store.protocol", "imap"));
                    }
                }
            }
        }
        return props;
    }
}
