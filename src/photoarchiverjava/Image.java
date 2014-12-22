/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package photoarchiverjava;

import java.io.File;

/**
 *
 * @author adam
 */
public class Image {
    private File file;
    private MyMetaData meta;

    public Image(File file, MyMetaData meta) {
        this.file = file;
        this.meta = meta;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public MyMetaData getMeta() {
        return meta;
    }

    public void setMeta(MyMetaData meta) {
        this.meta = meta;
    }
}
