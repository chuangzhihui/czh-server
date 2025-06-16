package com.czh.common.utils;

import java.io.*;
import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public class FileUtil {
    //删除文件夹
    public static void deleteFolder(File folder) {
        if (folder.isDirectory()) {
            File[] files = folder.listFiles();
            if (files != null) {
                for (File file : files) {
                    deleteFolder(file);
                }
            }
        }
        folder.delete();
    }
    //将文件夹压缩为zip
    public static void addFolderToZip(String folderPath, String zipPath) throws IOException {
        try (ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(zipPath))) {
            addFolderToZip("", new File(folderPath), zipOut);
        }
    }

    private static void addFolderToZip(String parentFolderName, File folder, ZipOutputStream zipOut) throws IOException {
        for (File file : folder.listFiles()) {
            if (file.isFile()) {
                ZipEntry zipEntry = new ZipEntry(parentFolderName + "/" + file.getName());
                zipOut.putNextEntry(zipEntry);
                try (FileInputStream fileInputStream = new FileInputStream(file)) {
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = fileInputStream.read(buffer)) > 0) {
                        zipOut.write(buffer, 0, length);
                    }
                }
            } else {
                addFolderToZip(parentFolderName + "/" + file.getName(), file, zipOut);
            }
        }
    }



    /**
     * 解压到指定目录
     *
     * @param zipPath 待解压的zip文件
     * @param descDir 指定目录
     */
    public static void unZipFiles(String zipPath, String descDir) {
        unZipFiles(new File(zipPath), descDir);
    }

    /**
     * 解压文件到指定目录
     * 解压后的文件名，和之前一致
     *
     * @param zipFile 待解压的zip文件
     * @param descDir 指定目录
     */
    private static void unZipFiles(File zipFile, String descDir) {
        try (ZipFile zip = new ZipFile(zipFile, Charset.forName("GBK"));) {
//            String name = zip.getName().substring(zip.getName().lastIndexOf('/') + 1, zip.getName().lastIndexOf('.'));
            File pathFile = new File(descDir);
            if (!pathFile.exists()) {
                pathFile.mkdirs();
            }
            for (Enumeration<? extends ZipEntry> entries = zip.entries(); entries.hasMoreElements(); ) {
                ZipEntry entry = entries.nextElement();
                String zipEntryName = entry.getName();

                try (InputStream in = zip.getInputStream(entry);) {
                    String outPath = (descDir + "/" + zipEntryName).replaceAll("\\*", "/");
                    // 判断路径是否存在,不存在则创建文件路径
                    File file = new File(outPath.substring(0, outPath.lastIndexOf('/')));
                    if (!file.exists()) {
                        file.mkdirs();
                    }
                    // 判断文件全路径是否为文件夹,如果是上面已经上传,不需要解压
                    if (new File(outPath).isDirectory()) {
                        continue;
                    }

                    try (FileOutputStream out = new FileOutputStream(outPath);) {
                        byte[] buf1 = new byte[1024];
                        int len;
                        while ((len = in.read(buf1)) > 0) {
                            out.write(buf1, 0, len);
                        }
                    } catch (FileNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

}
