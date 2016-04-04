package kz.bsbnb.usci.portlet.report.export;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import kz.bsbnb.usci.eav.util.Errors;
import kz.bsbnb.usci.portlet.report.ReportApplication;
import kz.bsbnb.usci.portlet.report.ReportPortletResource;
import kz.bsbnb.usci.portlet.report.dm.ReportLoadFile;
import com.vaadin.terminal.FileResource;
import com.vaadin.ui.Button;
import org.apache.log4j.Logger;

/**
 *
 * @author Aidar.Myrzahanov
 */
public class FileDownloadComponent extends Button {

    
    private final ArrayList<String> entryNames = new ArrayList<String>();
    private final ArrayList<File> files = new ArrayList<File>();
    private String downloadedFilename;
    private File zippedFile;
    private static final Logger logger = Logger.getLogger(FileDownloadComponent.class);

    public FileDownloadComponent(String downloadedFilename) {
        this.downloadedFilename = downloadedFilename;
        setIcon(ReportPortletResource.ARCHIVE_ICON);
        setImmediate(true);
    }

    public FileDownloadComponent(String downloadedFilename, String entryName, File file) {
        this(downloadedFilename);
        entryNames.add(entryName);
        files.add(file);
    }

    @Override
    public void attach() {
        this.addListener(new Button.ClickListener() {

            public void buttonClick(ClickEvent event) {
                zipFile();
                FileResource fileResource = new FileResource(zippedFile, getApplication()) {

                    @Override
                    public String getFilename() {
                        return getDownloadedFileName();
                    }
                };
                (getWindow()).open(fileResource);
            }
        });
    }

    public void zipFile() {
        FileOutputStream fos = null;
        try {
            if (zippedFile == null) {
                final File zipFile = File.createTempFile("Records", ".zip", AbstractReportExporter.REPORT_FILES_FOLDER);
                fos = new FileOutputStream(zipFile);
                ZipOutputStream zos = new ZipOutputStream(fos);
                for (int i = 0; i < files.size(); i++) {
                    zos.putNextEntry(new ZipEntry(entryNames.get(i)));
                    FileInputStream fis = null;
                    try {
                        fis = new FileInputStream(files.get(i));
                        byte[] buf = new byte[10000];
                        int read = 1;
                        while (true) {
                            read = fis.read(buf);
                            if (read > 0) {
                                zos.write(buf, 0, read);
                            } else {
                                break;
                            }
                        }
                    } finally {
                        if (fis != null) {
                            fis.close();
                        }
                    }
                    zos.closeEntry();
                }
                zos.close();

                zippedFile = zipFile;
            }

        } catch (IOException ioe) {
            logger.error("IO exception ", ioe);
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (Exception e) {
                }
            }
        }
    }

    public List<String> getFilenames() {
        return new ArrayList<String>(entryNames);
    }

    public List<File> getFiles() {
        return new ArrayList<File>(files);
    }

    public void addFile(String filename, File file) {
        entryNames.add(filename);
        files.add(file);
    }

    public void addAllFiles(Collection<String> filenames, Collection<File> files) {
        if (filenames.size() != files.size()) {
            logger.error(Errors.getError(String.valueOf(Errors.E254)));
            throw new IllegalArgumentException(Errors.getMessage(Errors.E254));
        }
        this.entryNames.addAll(filenames);
        this.files.addAll(files);
    }

    /**
     * @return the downloadedFileName
     */
    public String getDownloadedFileName() {
        return downloadedFilename;
    }

    /**
     * @param downloadedFileName the downloadedFileName to set
     */
    public void setDownloadedFileName(String downloadedFileName) {
        this.downloadedFilename = downloadedFileName;
    }

    public ReportLoadFile getLoadedFile() {
        ReportLoadFile result = new ReportLoadFile();
        result.setFilename(getDownloadedFileName());
        result.setMimeType("application/zip");
        result.setPath(zippedFile.getAbsolutePath());
        return result;
    }
}
