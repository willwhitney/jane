package com.willwhitney.jane;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import android.util.Log;

public class WebClient {

	public WebClient() {
		// TODO Auto-generated constructor stub
	}

    public static String getURLContents(String url) {
		try {
			URL target = new URL(url);
		    URLConnection conn = target.openConnection();
		    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));


		    String inputLine;
		    String output = "";
		    while ((inputLine = in.readLine()) != null) {
		        output += inputLine;

		    }
		    in.close();

		    return output;

		} catch (MalformedURLException e) {
		    Log.v("malformedEx", e.getMessage());
			e.printStackTrace();
			return null;
		} catch (IOException e) {
		    Log.v("IOEx", e.getLocalizedMessage());
			e.printStackTrace();
			return null;
		}
    }

    public static Document loadXMLFromString(String xml) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        InputSource is = new InputSource(new StringReader(xml));
        return builder.parse(is);
    }

}
