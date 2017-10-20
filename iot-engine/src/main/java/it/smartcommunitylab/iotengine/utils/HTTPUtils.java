package it.smartcommunitylab.iotengine.utils;

import it.smartcommunitylab.iotengine.common.Utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class HTTPUtils {
	
	public static Map<String, Object> send(String address, String method, 
			Map<String, String[]> params, Object content, 
			String basicAuthUser, String basicAuthPassowrd) throws Exception {
		StringBuffer response = new StringBuffer();

		if(params != null) {
			address += "?";
			for(String paramName : params.keySet()) {
				String[] paramValueList = params.get(paramName);
				for(String paramValue : paramValueList) {
					address += paramName + "=" + URLEncoder.encode(paramValue, "UTF-8") + "&"; 
				}
			}
			address = address.substring(0, address.length()-1);
		}
		URL url = new URL(address);

		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod(method);
		conn.setDoOutput(true);
		conn.setDoInput(true);

		conn.setRequestProperty("Accept", "application/json");
		conn.setRequestProperty("Content-Type", "application/json");
		
		if(Utils.isNotEmpty(basicAuthUser) && Utils.isNotEmpty(basicAuthPassowrd)) {
			String authString = basicAuthUser + ":" + basicAuthPassowrd;
			byte[] authEncBytes = Base64.encodeBase64(authString.getBytes());
			String authStringEnc = new String(authEncBytes);
			conn.setRequestProperty("Authorization", "Basic " + authStringEnc);
		}
		
		ObjectMapper mapper = new ObjectMapper();
		
		if(content != null) {
			String contentString = mapper.writeValueAsString(content);
			OutputStream out = conn.getOutputStream();
			Writer writer = new OutputStreamWriter(out, "UTF-8");
			writer.write(contentString);
			writer.close();
			out.close();		
		}
		
		if (conn.getResponseCode() >= 300) {
			throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
		}

		BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream()), Charset.defaultCharset()));

		String output = null;
		while ((output = br.readLine()) != null) {
			response.append(output);
		}

		conn.disconnect();

		String json = new String(response.toString().getBytes(), Charset.forName("UTF-8"));
		
		Map<String, Object> result = mapper.readValue(json, new TypeReference<Map<String, Object>>(){});
		
		return result;
	}	
	
}
