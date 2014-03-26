package com.bsbnb.creditregistry.portlets.report.export;

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

import com.bsbnb.creditregistry.portlets.report.ReportApplication;
import com.bsbnb.creditregistry.portlets.report.ReportPortletResource;
import com.bsbnb.creditregistry.portlets.report.dm.ReportLoadFile;
import com.vaadin.terminal.FileResource;
import com.vaadin.ui.Button;

/**
 *
 * @author Aidar.Myrzahanov
 */
public class FileDownloadComponent extends Button {

    
    private final ArrayList<String> entryNames = new ArrayList<String>();
    private final ArrayList<File> files = new ArrayList<File>();
    private String downloadedFilename;
    private File zippedFile;

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
            ReportApplication.log.log(Level.SEVERE, "IO exception ", ioe);
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
            throw new IllegalArgumentException("Collections should be of equal size");
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
