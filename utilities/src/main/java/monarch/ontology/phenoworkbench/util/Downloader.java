package monarch.ontology.phenoworkbench.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.LaxRedirectStrategy;

public class Downloader {

    Files files = Files.getInstance();

    protected Downloader() {
        // Exists only to defeat instantiation.
    }

    private static Downloader instance = null;

    public static Downloader getInstance() {
        if(instance == null) {
            instance = new Downloader();
        }
        return instance;
    }

    public File download(URL url, File dstFile) {
        CloseableHttpClient httpclient = HttpClients.custom()
                .setRedirectStrategy(new LaxRedirectStrategy()) // adds HTTP REDIRECT support to GET and POST methods
                .build();
        try {
            HttpGet get = new HttpGet(url.toURI()); // we're using GET but it could be via POST as well
            File downloaded = httpclient.execute(get, new FileDownloadResponseHandler(dstFile));
            return downloaded;
        } catch (Exception e) {
            throw new IllegalStateException(e);
        } finally {
            IOUtils.closeQuietly(httpclient);
        }
    }

    static class FileDownloadResponseHandler implements ResponseHandler<File> {

        private final File target;

        public FileDownloadResponseHandler(File target) {
            this.target = target;
        }

        @Override
        public File handleResponse(HttpResponse response) throws IOException {
            InputStream source = response.getEntity().getContent();
            FileUtils.copyInputStreamToFile(source, this.target);
            return this.target;
        }

    }

    public void downloadFiles(Set<String> iris, String extension) {
        for (String iri : iris) {
            downloadFile(iri, extension);
        }
    }

    public File downloadFile(String url, String extension) {
        String filename = url.replaceAll("[^A-Za-z0-9]", "") + "." + extension;
        File f = new File(files.getTmpdir(), filename);
        try {
            download(new URL(url), f);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return f;
    }

}
