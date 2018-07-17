package monarch.ontology.phenoworkbench.browser.basic;

import com.vaadin.server.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class IOUtils {

    public static StreamResource getStreamResource(byte[] toDownload, String filename) {
        StreamResource.StreamSource source = new StreamResource.StreamSource() {
            private static final long serialVersionUID = 21828054412044862L;

            @Override
            public InputStream getStream() {
                return new ByteArrayInputStream(toDownload);
            }
        };

        StreamResource resource = new StreamResource(source, filename) {
            private static final long serialVersionUID = -552993349680185987L;
            DownloadStream downloadStream;

            @Override
            public DownloadStream getStream() {
                if (downloadStream == null)
                    downloadStream = super.getStream();
                return downloadStream;
            }
        };
        resource.getStream().setParameter("Content-Disposition", "attachment;filename=\""+filename+"\"");
        resource.getStream().setParameter("Content-Type", "application/octet-stream");
        resource.getStream().setCacheTime(0);
        return resource;
    }
}
