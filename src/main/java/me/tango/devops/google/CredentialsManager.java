package me.tango.devops.google;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.java6.auth.oauth2.VerificationCodeReceiver;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.extensions.java6.auth.oauth2.GooglePromptReceiver;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.storage.StorageScopes;
import com.google.common.io.Files;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.HashSet;
import java.util.Set;


public class CredentialsManager {
    /**
     * Global instance of the JSON factory.
     */
    static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    /**
     * If you are running the sample on a machine where you have access to a browser, set
     * AUTH_LOCAL_WEBSERVER to true.
     */
    private static final boolean AUTH_LOCAL_WEBSERVER = false;
    /**
     * Directory to store user credentials.
     */
    private static final java.io.File DATA_STORE_DIR =
        new java.io.File(System.getProperty("user.home"), ".google_cloud/");
    /**
     * Global instance of the HTTP transport.
     */
    static HttpTransport httpTransport;
    /**
     * Global instance of the {@link com.google.api.client.util.store.DataStoreFactory}.
     * The best practice is to make it a single globally shared instance across your application.
     */
    private static FileDataStoreFactory dataStoreFactory;

    private static String clientSecretFile;

    public static void setup(String file) throws IOException, GeneralSecurityException {
        clientSecretFile = file;

        // Initialize the transport.
        httpTransport = GoogleNetHttpTransport.newTrustedTransport();

        // Initialize the data store factory.
        dataStoreFactory = new FileDataStoreFactory(DATA_STORE_DIR);
    }

    /**
     * Authorizes the installed application to access user's protected data.
     */
    public static Credential authorize() throws Exception {
        // Load client secrets.
        GoogleClientSecrets clientSecrets = null;
        try {
            final byte[] bytes = Files.toByteArray(new File(clientSecretFile));
            clientSecrets = GoogleClientSecrets
                .load(JSON_FACTORY, new InputStreamReader(new ByteArrayInputStream(bytes)));
            if (clientSecrets.getDetails().getClientId() == null
                || clientSecrets.getDetails().getClientSecret() == null) {
                throw new Exception("client_secrets not well formed.");
            }
        } catch (Exception e) {
            System.out
                .println("Problem loading client_secrets.json file. Make sure it exists, you are " +
                    "loading it with the right path, and a client ID and client secret are " +
                    "defined in it.\n" + e.getMessage());
            System.exit(1);
        }

        // Set up authorization code flow.
        // Ask for only the permissions you need. Asking for more permissions will
        // reduce the number of users who finish the process for giving you access
        // to their accounts. It will also increase the amount of effort you will
        // have to spend explaining to users what you are doing with their data.
        // Here we are listing all of the available scopes. You should remove scopes
        // that you are not actually using.
        Set<String> scopes = new HashSet<String>();
        scopes.add(StorageScopes.DEVSTORAGE_FULL_CONTROL);
        scopes.add(StorageScopes.DEVSTORAGE_READ_ONLY);
        scopes.add(StorageScopes.DEVSTORAGE_READ_WRITE);

        GoogleAuthorizationCodeFlow flow =
            new GoogleAuthorizationCodeFlow.Builder(httpTransport, JSON_FACTORY, clientSecrets,
                scopes).setDataStoreFactory(dataStoreFactory).build();
        // Authorize.
        VerificationCodeReceiver receiver =
            AUTH_LOCAL_WEBSERVER ? new LocalServerReceiver() : new GooglePromptReceiver();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }
}
