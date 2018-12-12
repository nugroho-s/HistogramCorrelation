package com.nugsky.tugasakhir.utils;

import java.net.URL;

public class ResourcesWrapper {
    public static String prefix = "";
    public static void init(Class cl){
        if(cl.getResource("/MainFX.fxml")==null){
            prefix = "/resources";
        }
    }

    public static URL getResource(String res, Class cl){
        return cl.getResource(prefix+res);
    }
}
