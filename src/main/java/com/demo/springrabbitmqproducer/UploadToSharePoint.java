package com.demo.springrabbitmqproducer;

import com.google.gson.JsonPrimitive;
import com.microsoft.graph.models.*;
import com.microsoft.graph.options.Option;
import com.microsoft.graph.options.QueryOption;
import com.microsoft.graph.requests.GraphServiceClient;
import com.microsoft.graph.requests.SiteCollectionPage;
import com.microsoft.graph.tasks.IProgressCallback;
import com.microsoft.graph.tasks.LargeFileUploadTask;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.LinkedList;


public class UploadToSharePoint {

    private String getSiteID() throws IOException {

        try {
            GraphServiceClient graphClient = new AuthenticationProvider().getClientAuthProvider();
            LinkedList<Option> requestOptions = new LinkedList<Option>();
            requestOptions.add(new QueryOption("search", ApplicationProperties.getSharepointSiteName()));
            SiteCollectionPage site = graphClient.sites()
                    .buildRequest(requestOptions)
                    .get();

            // site.getNextPage().buildRequest().get();

            for (Site sp : site.getCurrentPage()) {
                //System.out.println(sp.id);
                //System.out.println(sp.displayName);
                //System.out.println(sp.name);

                if (sp.displayName.equalsIgnoreCase(ApplicationProperties.getSharepointSiteName()))
                    return sp.id;
            }
        } catch (Exception ex) {
            System.out.println("Exception occured");
            ex.printStackTrace();
        }
        System.out.println("Site ID not found\nUsing site id from config");
        return ApplicationProperties.getSharepointSiteName();
    }

    public void uploadSmallFile(String localFilePath) throws Exception {

        byte[] stream = Base64.getEncoder().encode(localFilePath.getBytes(StandardCharsets.UTF_8));
        GraphServiceClient graphClient = new AuthenticationProvider().getClientAuthProvider();

        try {
            DriveItem uploadedFile = graphClient
                    .sites(getSiteID())
                    .drive()
                    .root()
                    .itemWithPath("/test_create_directory/alvaro2_.jpg") // This is directory on Sharepoint. Name of file as per custom logic
                    .content()
                    .buildRequest()
                    .put(stream);

            System.out.println("File uploaded to: " + uploadedFile.webUrl);


        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    public void uploadLargeFile(String localFilePath) throws Exception {

        GraphServiceClient graphClient = new AuthenticationProvider().getClientAuthProvider();

        // Get an input stream for the file
        File file = new File(localFilePath);
        InputStream fileStream = new FileInputStream(file);
        long streamSize = file.length();

        // Create a callback used by the upload provider
        IProgressCallback callback = new IProgressCallback() {
            @Override
            // Called after each slice of the file is uploaded
            public void progress(final long current, final long max) {
                System.out.println(
                        String.format("Uploaded %d bytes of %d total bytes", current, max)
                );
            }
        };

        DriveItemCreateUploadSessionParameterSet uploadParams =
                DriveItemCreateUploadSessionParameterSet.newBuilder()
                        .withItem(new DriveItemUploadableProperties()).build();

        // Create an upload session
        UploadSession uploadSession = graphClient
                .sites(getSiteID())
                .drive()
                .root()
                .itemWithPath("/test_create_directory/details.zip")
                .createUploadSession(uploadParams)
                .buildRequest()
                .post();

        LargeFileUploadTask<DriveItem> largeFileUploadTask =
                new LargeFileUploadTask<DriveItem>
                        (uploadSession, graphClient, fileStream, streamSize, DriveItem.class);

        // Do the bulk upload
        largeFileUploadTask.upload(0, null, callback);
        //Resume UPLOAD INCASE of interruption - currently unsupported in JAVA GRAPH SDK!
        // https://learn.microsoft.com/en-us/graph/sdks/large-file-upload?tabs=java

     }

    void uploadFile(String filePath) throws Exception {

         File file = new File(filePath);
        double fileSize = getFileSizeMegaBytes(file);

        System.out.println("File name - "+filePath +"\nFile size - "+fileSize +" MB");

         if (fileSize < 4.0)   //Default ONEDRIVE file size categorization
         {
             System.out.println("SMALL FILE UPLOAD <><><><><><>");
             uploadSmallFile( filePath);
         }else
         {
             System.out.println("LARGE FILE UPLOAD <><><><><><>");
             uploadLargeFile(filePath);
         }

    }

    public void createFolder (String folderName) throws IOException {

        GraphServiceClient graphClient = new AuthenticationProvider().getClientAuthProvider();

        DriveItem driveItem = new DriveItem();
        driveItem.name = folderName;
        Folder folder = new Folder();
        driveItem.folder = folder;
        driveItem.additionalDataManager().put("@microsoft.graph.conflictBehavior", new JsonPrimitive("rename"));  //FIXME: replace and rename

        graphClient
                .sites(getSiteID())
                .drive()
                .root()
                .children()
                .buildRequest()
                .post(driveItem);
    }

    private double getFileSizeMegaBytes(File file) {
        return (double) file.length() / (1024 * 1024) ;
    }

    private double getFileSizeKiloBytes(File file) {
        return (double) file.length() / 1024 ;
    }

    private double getFileSizeBytes(File file) {
        return file.length() ;
    }


}