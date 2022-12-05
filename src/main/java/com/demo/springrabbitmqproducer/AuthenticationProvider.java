package com.demo.springrabbitmqproducer;

import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.microsoft.graph.auth.enums.NationalCloud;
import com.microsoft.graph.auth.publicClient.UsernamePasswordProvider;
import com.microsoft.graph.authentication.TokenCredentialAuthProvider;
import com.microsoft.graph.requests.GraphServiceClient;


import java.io.IOException;
import java.util.Arrays;

public class AuthenticationProvider {

    private static AuthenticationProvider authenticationProvider = null;


    public AuthenticationProvider(){ }


    public static AuthenticationProvider getInstance(){
        if (authenticationProvider == null) {
            authenticationProvider = new AuthenticationProvider();
        }
        return authenticationProvider;
    }


    public GraphServiceClient getClientAuthProvider() throws IOException {

        final ClientSecretCredential clientSecretCredential = new ClientSecretCredentialBuilder()
                .clientId(ApplicationProperties.getClientId())
                .clientSecret(ApplicationProperties.getClientSecret())
                .tenantId(ApplicationProperties.getTenantId())
                .build();

        final TokenCredentialAuthProvider tokenCredentialAuthProvider =
                new TokenCredentialAuthProvider(ApplicationProperties.getScopeList(),
                        clientSecretCredential);

        final GraphServiceClient graphClient =
                GraphServiceClient
                        .builder()
                        .authenticationProvider(tokenCredentialAuthProvider)
                        .buildClient();

        return graphClient;

        // Use AZURE KEY_VAULT for security key rotation - https://learn.microsoft.com/en-us/graph/tutorial-applications-basics?tabs=http

    }

    public GraphServiceClient getUserAuthProvider() throws IOException {
        UsernamePasswordProvider authProvider = new UsernamePasswordProvider(
                ApplicationProperties.getClientId(),
                ApplicationProperties.getScopeList(),
                ApplicationProperties.getUsername(),
                ApplicationProperties.getPassword(),
                NationalCloud.Global,
                ApplicationProperties.getTenantId(),
                ApplicationProperties.getClientSecret());

        GraphServiceClient graphClient =
                GraphServiceClient
                        .builder()
                        .authenticationProvider(authProvider)
                        .buildClient();

        return graphClient;
    }


}