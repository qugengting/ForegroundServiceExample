package com.qugengting.foregroundservicedemo;

import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This class is about file Tool class.
 */
public final class FileUtil {
    private FileUtil() {
    }


    /**
     * copy file
     *
     * @param input   FileInputStream
     * @param trgPath The path to the target file to be copied
     */
    public static boolean copyFile(InputStream input, String trgPath) {
        boolean sdCardExist = Environment.getExternalStorageState()
                .equals(Environment.MEDIA_MOUNTED);
        if (!sdCardExist) {
            return false;
        }

        if (null == input || TextUtils.isEmpty(trgPath)) {
            return false;
        }

        BufferedInputStream inBuffStream = null;
        FileOutputStream output = null;
        BufferedOutputStream outBuffStream = null;

        File trgFile = new File(trgPath);
        try {
            if (!trgFile.exists()) {
                boolean isCreateSuccess = trgFile.createNewFile();
                if (!isCreateSuccess) {
                    return false;
                }
            }
            inBuffStream = new BufferedInputStream(input);
            output = new FileOutputStream(trgFile);
            outBuffStream = new BufferedOutputStream(output);
            byte[] buffer = new byte[2 * 1024 * 1024];
            while (true) {
                int inBuflen = inBuffStream.read(buffer);
                if (-1 == inBuflen) {
                    outBuffStream.flush();
                    break;
                } else {
                    outBuffStream.write(buffer, 0, inBuflen);
                }
            }

            return true;
        } catch (FileNotFoundException e) {
            return false;
        } catch (IOException e) {
            return false;
        } finally {
        }
    }

    /**
     * is sdcard exist
     *
     * @return boolean
     */
    public static boolean isSdCardExist() {
        boolean sdCardExist = Environment.getExternalStorageState()
                .equals(Environment.MEDIA_MOUNTED);
        if (sdCardExist) {
            return true;
        }
        return false;
    }

    public static void deleteFile(File file) {
        deleteFile(file, null);
    }

    public static void deleteFile(File file, File[] exceptFiles) {
        if (file == null || !file.exists()) {
            return;
        }

        if (file.isFile() && !file.isHidden()) {
            boolean success = file.delete();

            if (!success) {
            }

            return;
        }

        if (file.isDirectory() && !isContainFile(file, exceptFiles)) {
            File[] files = file.listFiles();
            if (null != files && 0 != files.length) {
                for (File f : files) {
                    deleteFile(f, exceptFiles);
                }
            }

            if (!file.delete()) {
            }
        }
    }

    /**
     * Unit conversion
     *
     * @param size
     * @return String
     */
    public static String makeUpSizeShow(double size) {
        double unit = 1024.0;
        String sizeUnit = "B";
        // to KB
        if (unit < size) {
            sizeUnit = "KB";
            size = size / unit;
        }
        // to M
        if (unit < size) {
            sizeUnit = "M";
            size = size / unit;
        }
        // to .00
        DecimalFormat df = new DecimalFormat(".00");
        return df.format(size) + sizeUnit;
    }

    private static boolean isContainFile(File file, File[] files) {
        if (file == null || files == null || files.length == 0) {
            return false;
        }

        for (File f : files) {
            if (file.equals(f)) {
                return true;
            }
        }

        return false;
    }

    private interface OpenResult {
        int OPEN_SUCCESS = 0;
        int OPEN_BY_THIRDPARTY_FAIL = 1;
    }

    /**
     * 向文件中写入数据
     *
     * @param filePath 文件目录
     * @param content  要写入的内容
     * @param append   如果为 true，则将数据写入文件末尾处，而不是写入文件开始处
     * @return 写入成功返回true， 写入失败返回false
     * @throws IOException
     */
    public static void writeFile(String filePath, String content, boolean append) {
        if (TextUtils.isEmpty(filePath)) {
            return ;
        }
        if (TextUtils.isEmpty(content)) {
            return ;
        }
        FileWriter fileWriter = null;
        try {
            createFile(filePath);
            fileWriter = new FileWriter(filePath, append);
            fileWriter.write(content);
            fileWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fileWriter != null) {
                try {
                    fileWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void createFile(String fileName) {
        File file = new File(fileName);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 获得文件或文件夹的大小
     *
     * @param path 文件或目录的绝对路径
     * @return 返回当前目录的大小 ，注：当文件不存在，为空，或者为空白字符串，返回 -1
     */
    public static long getFileSize(String path) {
        if (TextUtils.isEmpty(path)) {
            return -1;
        }
        File file = new File(path);
        return (file.exists() && file.isFile() ? file.length() : -1);
    }

    /**
     * 获取某个目录下的指定扩展名的文件名称
     *
     * @param dirPath 目录
     * @return 某个目录下的所有文件名
     */
    public static List<String> getFileNameList(String dirPath, final String extension) {
        if (TextUtils.isEmpty(dirPath)) {
            return Collections.emptyList();
        }
        File dir = new File(dirPath);
        File[] files = dir.listFiles(new FilenameFilter() {

            @Override
            public boolean accept(File dir, String filename) {
                if (filename.indexOf("." + extension) > 0)
                    return true;
                return false;
            }
        });
        if (files == null) {
            return Collections.emptyList();
        }
        List<String> conList = new ArrayList<String>();
        for (File file : files) {
            if (file.isFile())
                conList.add(file.getName());
        }
        return conList;
    }

    /**
     * 删除指定目录中特定的文件
     *
     * @param dir
     * @param filter
     */
    public static void delete(String dir, FilenameFilter filter) {
        if (TextUtils.isEmpty(dir))
            return;
        File file = new File(dir);
        if (!file.exists())
            return;
        if (file.isFile())
            file.delete();
        if (!file.isDirectory())
            return;

        File[] lists = null;
        if (filter != null)
            lists = file.listFiles(filter);
        else
            lists = file.listFiles();

        if (lists == null)
            return;
        for (File f : lists) {
            if (f.isFile()) {
                f.delete();
            }
        }
    }

    //*******************************************************************************

    /**
     * 获取要打印日志的文本
     * 未超出指定的文本大小在原日志末尾继续添加，否则重新建个日志文件
     *
     * @return
     */
    public static String getFileName(Context context) {
        String fileName = context.getExternalFilesDir("location") + File.separator + "test.txt";
        File file = new File(fileName);
        if (!file.exists()) {
            FileUtil.createFile(fileName);
        }
        return fileName;
    }
}
