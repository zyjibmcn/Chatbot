package com.ibm.cto;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.fluent.Executor;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

public class Utility {

	public static HttpResponse invokeRequest(Request request, String username, String password, boolean useSSL) throws ClientProtocolException, IOException, KeyManagementException, NoSuchAlgorithmException {
		Executor executor = null;
		if (useSSL){
			executor = Executor.newInstance(getTrustedHttpClient());
		}
		else{
			executor = Executor.newInstance();
		}
		executor = executor.auth(username, password);

		HttpResponse httpResponse = invokeRequest(request, executor);
		return httpResponse;
	}

	public static HttpResponse invokeRequest(Request request, Executor executor) throws ClientProtocolException, IOException {
		Response response = executor.execute(request);
		HttpResponse httpResponse = response.returnResponse();
		return httpResponse;
	}

	public static CloseableHttpClient getTrustedHttpClient() throws KeyManagementException, NoSuchAlgorithmException{
		CloseableHttpClient httpClient = null;

		SSLContext sslContext = SSLContext.getInstance("SSL");

		// set up a TrustManager that trusts everything
		sslContext.init(null, new TrustManager[] { new X509TrustManager() {
			@Override
			public X509Certificate[] getAcceptedIssuers() {
				return null;
			}

			@Override
			public void checkClientTrusted(X509Certificate[] certs, String authType) { }

			@Override
			public void checkServerTrusted(X509Certificate[] certs, String authType) { }
		} }, new SecureRandom());
		
		SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslContext, NoopHostnameVerifier.INSTANCE);
		PlainConnectionSocketFactory plainsf = new PlainConnectionSocketFactory();

		Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create()
	            .register("http", plainsf)
	            .register("https", sslsf)
	            .build();

		PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager(registry);
		httpClient = HttpClients.custom().setConnectionManager(connectionManager).build();

		return httpClient;
	}
	

	/**
	 * JSONArray to List of Object
	 * @param array
	 * @return List<Object>
	 */
	public static List<Object> toList(JSONArray array) {
	    List<Object> list = new ArrayList<Object>();
	    for(int i = 0; i < array.size(); i++) {
	        Object value = array.get(i);
	        if(value instanceof JSONArray) {
	            value = toList((JSONArray) value);
	        }

	        else if(value instanceof JSONObject) {
	            value = toMap((JSONObject) value);
	        }
	        list.add(value);
	    }
	    return list;
	}

	/**
	 * JSONObject to Map<String, Object>
	 * @param object
	 * @return Map<String, Object>
	 */
	public static Map<String, Object> toMap(JSONObject object) {
	    Map<String, Object> map = new HashMap<String, Object>();

	    Iterator<?> keyIterator = object.keySet().iterator();
	    while(keyIterator.hasNext()) {
	        String key = keyIterator.next().toString();
	        Object value = object.get(key);

	        if(value instanceof JSONArray) {
	            value = toList((JSONArray) value);
	        }

	        else if(value instanceof JSONObject) {
	            value = toMap((JSONObject) value);
	        }
	        map.put(key, value);
	    }
	    return map;
	}
	
	public static HttpEntity invokeRequest(String serviceUrl) throws Exception
	{
	     if(serviceUrl.length() > 0) {
           URI converseURI = new URI(serviceUrl).normalize();
           Request request = Request.Get(converseURI);
           HttpResponse httpResponse = Utility.invokeRequest(request, "", "", true);
           if(httpResponse.getStatusLine().getStatusCode() == 200){
               HttpEntity entity = httpResponse.getEntity();
               return entity;
           }
	     }
         throw new Exception("Invalid request serviceUrl");
	}
	
	public static String convertHttpEntityToString(HttpEntity entity)
	{
	  String result = "";
	  
	  InputStream inputStream;
      try {
          inputStream = entity.getContent();
          if(inputStream != null) {
              if(inputStream.available() > 0) {
                  byte[] buffer = new byte[4096];
                  int length = 0;
                  StringBuilder sb = new StringBuilder();
                  while((length = inputStream.read(buffer)) > 0) {
                      sb.append(new String(buffer, 0, length));
                  }
                  result = sb.toString();
              }
              inputStream.close();
          }
      } catch (IllegalStateException | IOException e) {
          e.printStackTrace();
      }
      
	  return result;
	}
}
