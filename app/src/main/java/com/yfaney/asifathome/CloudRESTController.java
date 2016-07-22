package com.yfaney.asifathome;

import com.yfaney.internet.HttpClient;
import com.yfaney.internet.HttpResponse;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by Younghwan on 10/26/2015.
 */
public class CloudRESTController {
    public static Boolean registerGcmToken(String param) throws IOException, JSONException {
        HttpClient client = new HttpClient();
        String urlPostGcm = "http://yfaney.duckdns.org:57018/api/iot_66213/gcm";
        String body = "{\"gcm_key\":\"" + param + "\"}";
        HttpResponse resp = client.post(urlPostGcm, body);
        if(resp.getResponseCode() == HttpResponse.CODE_OK){
            String result = resp.getResultString();
            JSONObject jsonResult = new JSONObject(result);
            if(jsonResult.has("result")){
                return true;
            }else{
                return false;
            }
        }else{
            return true;
        }
    }
}
