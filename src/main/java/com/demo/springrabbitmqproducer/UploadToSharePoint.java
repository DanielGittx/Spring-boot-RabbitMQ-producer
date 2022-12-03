package com.demo.springrabbitmqproducer;

import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.google.gson.JsonPrimitive;
import com.microsoft.graph.authentication.TokenCredentialAuthProvider;
import com.microsoft.graph.models.*;
import com.microsoft.graph.requests.GraphServiceClient;
import com.microsoft.graph.tasks.IProgressCallback;
import com.microsoft.graph.tasks.LargeFileUploadResult;
import com.microsoft.graph.tasks.LargeFileUploadTask;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;


public class UploadToSharePoint {

    private InputStream getInputStream() throws FileNotFoundException {
        // Get an input stream for the file
        InputStream fileStream = new FileInputStream("C:\\Users\\dmgitau\\Downloads\\fail.jpg");
        return fileStream;
    }

    private void getFileName(InputStream inputStream) throws FileNotFoundException {
        InputStream fileStream = new FileInputStream("C:\\Users\\dmgitau\\Downloads\\fail.jpg");
    }

    private long getStreamSize(InputStream fileStream) throws IOException {
        long streamSize = (long) fileStream.available();
        return streamSize;
    }

    public void setUploadSession2() throws Exception {

        String localFilePath = "C:\\Users\\dmgitau\\Downloads\\alvaro2.JPG";
        // Get an input stream for the file
        File fileName = new File(localFilePath);
        InputStream fileStream = null;

        fileStream = new FileInputStream(fileName);
        long streamSize = fileName.length();

        // Create a callback used by the upload provider
        IProgressCallback callback = new IProgressCallback() {
            @Override
            // Called after each slice of the file is uploaded
            public void progress(final long current, final long max) {
                System.out.println(String.format("Uploaded %d bytes of %d total bytes", current, max));
            }
        };

        GraphServiceClient graphClient = new AuthenticationProvider().getAuthClientProvider();

        //byte[] stream = Base64.getEncoder().encode(localFilePath.getBytes(StandardCharsets.UTF_8));
try {
    DriveItem driveItem = new DriveItem();
    driveItem.name = "SpringBoot DriveItem";
    Folder folder = new Folder();
    driveItem.folder = folder;
    driveItem.additionalDataManager().put("@microsoft.graph.conflictBehavior", new JsonPrimitive("replace"));
    graphClient.me().drive().root().children()
            .buildRequest()
            .post(driveItem);
}catch  (Exception ex)
        {
            ex.printStackTrace();
        }
/*

        DriveItemCreateUploadSessionParameterSet uploadParams =
                DriveItemCreateUploadSessionParameterSet.newBuilder()
                        .withItem(new DriveItemUploadableProperties()).build();

        // Create an upload session
        UploadSession uploadSession = graphClient
                .drives("b!Mca1ElNvN0C6oM-T4Tln0L0SJAZimN9Dkdbe****")
                .items("017N4JGSDWBUSNV4CPWZDJMLJZJFQZZS6X")
                .itemWithPath(localFilePath).createUploadSession(null)
                .buildRequest().post();

        LargeFileUploadTask<DriveItem> largeFileUploadTask = new LargeFileUploadTask<DriveItem>(uploadSession,
                graphClient, fileStream, streamSize, DriveItem.class);

        // Do the upload
        LargeFileUploadResult<DriveItem> item = largeFileUploadTask.upload(10 * 320 * 1024, null, callback);
        System.out.println("Uploaded to: " + item.responseBody.webUrl);
*/


    }


/*
    // Create a callback used by the upload provider
    IProgressCallback<DriveItem> callback = new IProgressCallback<DriveItem>() {
        @Override
        // Called after each slice of the file is uploaded
        public void progress(final long current, final long max) {
            System.out.println(
                    String.format("Uploaded %d bytes of %d total bytes", current, max)
            );
        }

        @Override
        public void success(final DriveItem result) {
            System.out.println(
                    String.format("Uploaded file with ID: %s", result.id)
            );
        }

        public void failure(final ClientException ex) {
            System.out.println(
                    String.format("Error uploading file: %s", ex.getMessage())
            );
        }
    };

    public void setUploadSession() throws IOException {
        final IGraphServiceClient graphClient = new AuthenticationProvider().getAppProvider();

        // upload to share point
        UploadSession uploadSession1 = graphClient
                .sites()
                .byId("19a4db07-607d-475f-a518-0e3b699ac7d0")
                .drive()
                .root()
                .itemWithPath("fail.jpg")
                .createUploadSession(new DriveItemUploadableProperties())
                .buildRequest()
                .post();

        ChunkedUploadProvider<DriveItem> chunkedUploadProvider =
                new ChunkedUploadProvider<DriveItem>
                        (uploadSession1, graphClient, getInputStream(), getStreamSize(getInputStream()), DriveItem.class);

        // Config parameter is an array of integers
        // customConfig[0] indicates the max slice size
        // Max slice size must be a multiple of 320 KiB
        int[] customConfig = {320 * 1024};

        // Do the upload
        chunkedUploadProvider.upload(callback, customConfig);
    }


    public void getDrive() throws IOException {

        final UsernamePasswordProvider authProvider = new AuthenticationProvider().getUsernamePasswordProvider();
        OkHttpClient httpclient = HttpClients.createDefault(authProvider);
        Request request = new Request.Builder().url("https://graph.microsoft.com/v1.0/me/drive").build();
        Response response = httpclient.newCall(request).execute();
        System.out.println(response.body().string());

    }
*/

}