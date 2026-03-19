package io.papermc.paperclip;

import java.io.*;
import java.util.Properties;

public class PatchData {

    private final String originalUrl;
    private final String originalHash;
    private final String patchedHash;

    private PatchData(String originalUrl, String originalHash, String patchedHash) {
        this.originalUrl = originalUrl;
        this.originalHash = originalHash;
        this.patchedHash = patchedHash;
    }

    public static PatchData parse(InputStream is) {
        if (is == null) return null;
        try {
            Properties props = new Properties();
            props.load(is);
            is.close();

            String url = props.getProperty("originalUrl");
            String origHash = props.getProperty("originalHash");
            String patchHash = props.getProperty("patchedHash");

            if (url == null) return null;

            return new PatchData(url, origHash, patchHash);
        } catch (Exception e) {
            return null;
        }
    }

    public String getOriginalUrl() { return originalUrl; }
    public String getOriginalHash() { return originalHash; }
    public String getPatchedHash() { return patchedHash; }
}
