package com.cisco.axlsamples;

import java.security.cert.X509Certificate;
import java.util.Map;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import javax.net.ssl.SSLContext;
import javax.net.ssl.X509TrustManager;

// Import only the AXL package modules needed for this sample
import com.cisco.axlsamples.api.AXLAPIService;
import com.cisco.axlsamples.api.AXLPort;
import com.cisco.axlsamples.api.GetPhoneReq;
import com.cisco.axlsamples.api.GetPhoneRes;

// Dotenv for Java
import io.github.cdimascio.dotenv.Dotenv;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.xml.ws.BindingProvider;

// To import the entire AXL package contents:
//
// import com.cisco.axlsamples.api.*;

public class getPhone {
    
    static {
    disableSslVerification();
}

private static void disableSslVerification() {
    try
    {
        // Create a trust manager that does not validate certificate chains
        TrustManager[] trustAllCerts = new TrustManager[] {new X509TrustManager() {
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return null;
            }
            public void checkClientTrusted(X509Certificate[] certs, String authType) {
            }
            public void checkServerTrusted(X509Certificate[] certs, String authType) {
            }
        }
        };

        // Install the all-trusting trust manager
        SSLContext sc = SSLContext.getInstance("SSL");
        sc.init(null, trustAllCerts, new java.security.SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

        // Create all-trusting host name verifier
        HostnameVerifier allHostsValid = new HostnameVerifier() {
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        };

        // Install the all-trusting host verifier
        HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
    } catch (NoSuchAlgorithmException e) {
        e.printStackTrace();
    } catch (KeyManagementException e) {
        e.printStackTrace();
    }
}
    
    public static void main(String[] args) throws KeyManagementException, NoSuchAlgorithmException {

        // Retrieve environment variables from .env, if present
        Dotenv dotenv = Dotenv.load();

        Boolean debug = dotenv.get( "DEBUG" ).equals( "True" );

        if ( debug ) { 
            System.setProperty("com.sun.xml.ws.transport.http.client.HttpTransportPipe.dump", "true");
            // Increase the dump output permitted size
            System.setProperty("com.sun.xml.ws.transport.http.HttpAdapter.dumpTreshold", "999999");
        }

        // Verify the JVM has a console for user input
//        if (System.console() == null) {
//            System.err.println("Error: This sample app requires a console");
//            System.exit(1);
//        }

        // Instantiate the generated AXL API Service client
        AXLAPIService axlService = new AXLAPIService();

        // Get access to the request context so we can set custom params
        AXLPort axlPort = axlService.getAXLPort();
        Map< String, Object > requestContext = ( ( BindingProvider ) axlPort ).getRequestContext();

        // Set the AXL API endpoint address, user, and password
        //   for our particular environment in the JAX-WS client.
        //   Configure these values in .env
        requestContext.put( BindingProvider.ENDPOINT_ADDRESS_PROPERTY, "https://" + dotenv.get( "CUCM" ) + ":8443/axl/");
        requestContext.put( BindingProvider.USERNAME_PROPERTY, dotenv.get( "AXL_USER" ) );
        requestContext.put( BindingProvider.PASSWORD_PROPERTY, dotenv.get( "AXL_PASSWORD" ) );
        // Enable cookies for AXL authentication session reuse
        requestContext.put( BindingProvider.SESSION_MAINTAIN_PROPERTY, true );

        // Create a new <getPhone> request object
        GetPhoneReq req = new GetPhoneReq();

        // Get the device name to retrieve from the user via the console
//        String phoneName = System.console().readLine("%nPhone device name to retrieve: ");
        String phoneName = "SEP010203040506";
        req.setName(phoneName);
        
        // Prepare a GetPhoneRes object to receive the response from AXL
        GetPhoneRes getPhoneResponse;
        
        // Execute the request, wrapped in try/catch in case an exception is thrown
        try {
            getPhoneResponse = axlPort.getPhone(req);

            // Dive into the response object's hierarchy to retrieve the <product> value
            System.console().format("%nPhone product type is: " + 
                getPhoneResponse.getReturn().getPhone().getProduct() + "%n%n");
            
        } catch (Exception err) {

            // If an exception occurs, dump the stacktrace to the console
            err.printStackTrace();
        }
    }
}