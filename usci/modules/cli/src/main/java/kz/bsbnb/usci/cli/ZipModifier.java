package kz.bsbnb.usci.cli;

import java.io.*;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public class ZipModifier {
    public void run(String[] args) throws Exception{
        File folder = new File(args[0]);

        for(File file: folder.listFiles()) {
            if(file.getName().endsWith(".zip")) {
                ZipFile zipFile = new ZipFile(file.getAbsolutePath());
                final ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(args[1] + "/" + file.getName()));

                for(Enumeration e = zipFile.entries(); e.hasMoreElements(); ) {
                    ZipEntry entryIn = (ZipEntry) e.nextElement();
                    if(!entryIn.getName().equalsIgnoreCase("data.xml")){
                        //zos.putNextEntry(entryIn);
                        zos.putNextEntry(new ZipEntry(entryIn.getName()));
                        InputStream is = zipFile.getInputStream(entryIn);
                        byte [] buf = new byte[4096];
                        int len;
                        while((len = (is.read(buf))) > 0) {
                            zos.write(buf, 0, len);
                        }
                    }
                    else{
                        zos.putNextEntry(new ZipEntry("data.xml"));

                        InputStream is = zipFile.getInputStream(entryIn);
                        BufferedReader in = new BufferedReader(new InputStreamReader(is));

                        String s;
                        while( ( s = in.readLine()) != null) {
                            s += "\n";
                            if(s.contains("data_creditor"))
                                s = s.replaceAll("<data_creditor>\n","").replaceAll("</data_creditor>\n","");
                            zos.write(s.getBytes());
                        }
                    }
                    zos.closeEntry();
                }

                zos.close();
                zipFile.close();
            }
        }
        System.out.println("Done.");
    }
    public static void main(String [] args) throws Exception{
        new ZipModifier().run(args);
    }
}
