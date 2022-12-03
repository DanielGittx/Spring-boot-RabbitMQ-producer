package com.demo.springrabbitmqproducer;

import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
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


    public GraphServiceClient getAuthClientProvider  () throws IOException {

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
        //final User me = graphClient.me().buildRequest().get();

        return graphClient;

    }
/*
    public GraphServiceClient getAuthProvider() throws IOException {

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

    public UsernamePasswordProvider  getUsernamePasswordProvider() throws IOException {

        UsernamePasswordProvider authProvider = new UsernamePasswordProvider(
                ApplicationProperties.getClientId(),
                ApplicationProperties.getScopeList(),
                ApplicationProperties.getUsername(),
                ApplicationProperties.getPassword(),
                NationalCloud.Global,
                ApplicationProperties.getTenantId(),
                ApplicationProperties.getClientSecret());

     return authProvider;
    }
*/
}