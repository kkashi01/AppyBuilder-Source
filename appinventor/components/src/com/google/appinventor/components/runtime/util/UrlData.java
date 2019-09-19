package com.google.appinventor.components.runtime.util;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class UrlData {

    public String getUrlData(String urlString)  {
        String response= "";
        try {
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            String line;
            response = "";
            StringBuilder sb= new StringBuilder();
            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
            while ((line=br.readLine()) != null) {
                sb.append(line);
                response =sb.toString();
            }
            br.close();
        } catch (IOException e) {
            //return ERROR keyword so that caller knows how to respond
            return "ERROR:" + e.getMessage();
        }

        return response;
    }

}
