/**
 *  Copyright 2015 TangoMe Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
 
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
import java.io.InputStreamReader;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.HashSet;
import java.util.Set;

/** Manage google credentials. */
public final class CredentialsManager {
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
    private static final File DATA_STORE_DIR = new File(System.getProperty("user.home"),
        ".google_cloud/");
    /**
     * Global instance of the HTTP transport.
     */
    static HttpTransport httpTransport;
    /**
     * Global instance of the {@link com.google.api.client.util.store.DataStoreFactory}.
     * The best practice is to make it a single globally shared instance across your application.
     */
    private static FileDataStoreFactory dataStoreFactory;

    /** client secret file from google. */
    private static String clientSecretFile;

    // Make it a utility class
    private CredentialsManager() {}

    /** Configure. */
    public static void setup(final String file) throws IOException, GeneralSecurityException {
        clientSecretFile = file;

        // Initialize the transport.
        httpTransport = GoogleNetHttpTransport.newTrustedTransport();

        // Initialize the data store factory.
        dataStoreFactory = new FileDataStoreFactory(DATA_STORE_DIR);
    }

    /**
     * Authorizes the installed application to access user's protected data.
     */
    public static Credential authorize() throws IOException {
        // Load client secrets.
        final byte[] bytes = Files.toByteArray(new File(clientSecretFile));
        final GoogleClientSecrets clientSecrets = GoogleClientSecrets
            .load(JSON_FACTORY, new InputStreamReader(new ByteArrayInputStream(bytes)));
        if (clientSecrets.getDetails().getClientId() == null
            || clientSecrets.getDetails().getClientSecret() == null) {
            throw new IllegalStateException("client_secrets not well formed.");
        }


        // Set up authorization code flow.
        // Ask for only the permissions you need. Asking for more permissions will
        // reduce the number of users who finish the process for giving you access
        // to their accounts. It will also increase the amount of effort you will
        // have to spend explaining to users what you are doing with their data.
        // Here we are listing all of the available scopes. You should remove scopes
        // that you are not actually using.
        final Set<String> scopes = new HashSet<String>();
        scopes.add(StorageScopes.DEVSTORAGE_FULL_CONTROL);
        scopes.add(StorageScopes.DEVSTORAGE_READ_ONLY);
        scopes.add(StorageScopes.DEVSTORAGE_READ_WRITE);

        final GoogleAuthorizationCodeFlow flow =
            new GoogleAuthorizationCodeFlow.Builder(httpTransport, JSON_FACTORY, clientSecrets,
                scopes).setDataStoreFactory(dataStoreFactory).build();
        // Authorize.
        final VerificationCodeReceiver receiver =
            AUTH_LOCAL_WEBSERVER ? new LocalServerReceiver() : new GooglePromptReceiver();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }
}
