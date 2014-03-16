/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package photoarchiverjava;

import java.util.Date;
import java.util.LinkedList;

/**
 *
 * @author adam_fejes_dell
 */
public class MyMetaData {
    private Date date;
    private LinkedList<String> keywords;
    private String name;
    private String desc;
    private String cameraSettings;

    public MyMetaData(Date date, LinkedList<String> keywords, String name, String desc, String cameraSettings) {
        this.date = date;
        this.keywords = keywords;
        this.name = name;
        this.desc = desc;
        this.cameraSettings = cameraSettings;
    }

    
    
    public String getCameraSettings() {
        return cameraSettings;
    }

    public void setCameraSettings(String cameraSettings) {
        this.cameraSettings = cameraSettings;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public LinkedList<String> getKeywords() {
        return keywords;
    }

    public void setKeywords(LinkedList<String> keywords) {
        this.keywords = keywords;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    @Override
    public String toString() {
        return "MyMetaData{" + "date=" + date + ", keywords=" + keywords + ", name=" + name + ", desc=" + desc + ", cameraSettings=" + cameraSettings + '}';
    }
    
    
}
