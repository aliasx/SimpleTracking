package org.simpletracking.www;
/* The part of SimpleTracking open source project. Web: http://simpletracking.org */
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnManagerPNames;
import org.apache.http.conn.params.ConnPerRouteBean;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import android.util.Log;

public class SimpleTrackingSender{
	 public SimpleTrackingSender() {
		 Log.e("gps","SimpleTrackingSender create");
		 initSsl();
	 }
	
	 //Настройка SSL
    public void initSsl()
    {
		SchemeRegistry schemeRegistry = new SchemeRegistry();

   		schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
   		schemeRegistry.register(new Scheme("https", new EasySSLSocketFactory(), 443));

   		params = new BasicHttpParams();
   		params.setParameter(ConnManagerPNames.MAX_TOTAL_CONNECTIONS, 1);
   		params.setParameter(ConnManagerPNames.MAX_CONNECTIONS_PER_ROUTE, new ConnPerRouteBean(1));
   		params.setParameter(HttpProtocolParams.USE_EXPECT_CONTINUE, false);
   		HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
   		HttpProtocolParams.setContentCharset(params, "utf8");

      	//Принимать самоподписанные сертификаты
      	CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
      	credentialsProvider.setCredentials(new AuthScope("yourServerHere.com", AuthScope.ANY_PORT),
      			new UsernamePasswordCredentials("YourUserNameHere", "UserPasswordHere"));
      	clientConnectionManager = new ThreadSafeClientConnManager(params, schemeRegistry);

      	context = new BasicHttpContext();
      	context.setAttribute("http.auth.credentials-provider", credentialsProvider);

    }
    
    //Передача позиции на сервер
    public boolean sendPosition(String imei, double lat, double lon, double alt, double speed, long time) {
    	if ((lat != 0) && (lon != 0)) 
    	{
	    	try {	    		
	    		HttpClient client = new DefaultHttpClient(clientConnectionManager, params);
	    		
	    		String protocol = "";	    		
	    		if (SimpleTrackingService.useSsl) { protocol = "https"; }
	    		else { protocol = "http"; }
	    		
	    		Log.e("gps","sendPosition on " + SimpleTrackingService.serverName + " (" + protocol + ")");
	            
	    		String postURL = protocol + "://" + SimpleTrackingService.serverName + "/index.php/parser/sendposition";
	            postSendPosition = new HttpPost(postURL); 
	                List<NameValuePair> params = new ArrayList<NameValuePair>();
	                params.add(new BasicNameValuePair("imei", imei));
	                params.add(new BasicNameValuePair("lat", "" + lat));
	                params.add(new BasicNameValuePair("lon", "" + lon));
	                params.add(new BasicNameValuePair("alt", "" + alt));
	                params.add(new BasicNameValuePair("speed", "" + speed));
	                
	                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");             	
	                params.add(new BasicNameValuePair("time",  dateFormat.format(new Date(time))));
	
	                UrlEncodedFormEntity ent = new UrlEncodedFormEntity(params,HTTP.UTF_8);
	                postSendPosition.setEntity(ent);
	                HttpResponse responsePOST = client.execute(postSendPosition);  
	                HttpEntity resEntity = responsePOST.getEntity();  
	                String recive = EntityUtils.toString(resEntity);
	                if (recive.equals("OK"))
	                {
	                	Log.e("gps","sendPosition OK lat: " + lat + " lon: " + lon);
	                	return true;
	                }
	                else
	                {
	                	Log.e("gps","sendPosition BAD: " + recive);
	                	return false;                	
	                }
	        } catch (Exception e) {
	            e.printStackTrace();
	            Log.e("gps","sendPosition exception");
	            return false;
	        }
    	}
        else
        {
          	Log.e("gps","sendPosition nulled coords");
           	return false;                	
        }    	
    }   
    
    
    //Передача pushID на сервер
    public boolean sendPushId(String imei, String pushId) 
    {
	    	try {	    		
	    		HttpClient client = new DefaultHttpClient(clientConnectionManager, params);
	    		
	    		String protocol = "";	    		
	    		if (SimpleTrackingService.useSsl) { protocol = "https"; }
	    		else { protocol = "http"; }
	    		           
	    		String postURL = protocol + "://" + SimpleTrackingService.serverName + "/index.php/parser/sendpushid";
	            postSendPosition = new HttpPost(postURL); 
	                List<NameValuePair> params = new ArrayList<NameValuePair>();
	                params.add(new BasicNameValuePair("imei", imei));
	                params.add(new BasicNameValuePair("pushid", "" + pushId));
	
	                UrlEncodedFormEntity ent = new UrlEncodedFormEntity(params,HTTP.UTF_8);
	                postSendPosition.setEntity(ent);
	                HttpResponse responsePOST = client.execute(postSendPosition);  
	                HttpEntity resEntity = responsePOST.getEntity();  
	                String recive = EntityUtils.toString(resEntity);
		            Log.e("gps","Recive: " + recive);
		            return true;
	        } catch (Exception e) {
	            e.printStackTrace();
	            Log.e("gps","sendPosition exception");
	            return false;
	        }    	
    }   
    
    
    private HttpPost postSendPosition;
    private ClientConnectionManager clientConnectionManager;
    private HttpContext context;
    private HttpParams params;
}
