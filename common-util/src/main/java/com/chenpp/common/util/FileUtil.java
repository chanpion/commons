package com.chenpp.common.util;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.stylesheets.LinkStyle;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author April.Chen
 * @date 2024/6/18 09:30
 */
public class FileUtil {
    private static final Logger log = LoggerFactory.getLogger(FileUtil.class);
    private static final double KB = 1024.0;

    public static void downloadFile(String url, String targetPath, String name) {
        File file = new File(targetPath + File.separator + name);
        URL netUrl;
        try {
            netUrl = new URL(url);
            DataInputStream dataInputStream = new DataInputStream(netUrl.openStream());
            FileOutputStream fileOutputStream = new FileOutputStream(file);

            IOUtils.copy(dataInputStream, fileOutputStream);
            dataInputStream.close();
            fileOutputStream.close();
        } catch (Exception e) {
            log.info("下载文件失败：" + url, e);
        }
    }

    public static String readFile(String filePath) {
        try {
            return FileUtils.readFileToString(new File(filePath), StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("read file error", e);
            return null;
        }
    }

    public static String formatSize(long sizeInBytes) {
        double sizeInKB = sizeInBytes / KB;
        double sizeInMB = sizeInKB / KB;
        if (sizeInMB < 1) {
            return String.format("%.2fKB", sizeInKB);
        }
        double sizeInGB = sizeInMB / KB;
        if (sizeInGB < 1) {
            return String.format("%.2fMB", sizeInMB);
        }

        return String.format("%.2fGB", sizeInGB);
    }

    public static List<String> listFilenames(String dirPath) {
        try (Stream<Path> stream = Files.list(Paths.get(dirPath))) {
            return stream.filter(path -> path.toFile().isFile()).map(path -> path.getFileName().toString()).collect(Collectors.toList());
        } catch (IOException e) {
            log.error("list files", e);
            return Collections.emptyList();
        }
    }

    public static void traversalDirectory(String dirPath, Consumer<Path> action) {
        Path dir = Paths.get(dirPath);
        try (Stream<Path> stream = Files.walk(dir)) {
            stream.forEach(action::accept);
        } catch (IOException e) {
            log.error("traversal directory error", e);
        }
    }

    public static void writeFile(String filePath, String content) {
        try {
            FileUtils.writeStringToFile(new File(filePath), content, StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("write file error", e);
        }
    }

    public static void writeFile(String filePath, List<String> lines) {
        try {
            FileUtils.writeLines(new File(filePath), lines);
        } catch (IOException e) {
            log.error("write file error", e);
        }
    }

    public static String getCharset(String filePath) {
        String charset = null;
        try {
            charset = getCharset(new FileInputStream(filePath));
        } catch (FileNotFoundException e) {
            log.error(e.getMessage(), e);
        }
        log.info("文件[" + filePath + "] 采用的字符集为: [" + charset + "]");
        return charset;
    }

    public static String getCharset(InputStream inputStream) {
        String charset = "GBK";
        byte[] first3Bytes = new byte[3];
        try {
            boolean checked = false;
            BufferedInputStream bis = new BufferedInputStream(inputStream);
            bis.mark(0);
            int read = bis.read(first3Bytes, 0, 3);
            if (read == -1) {
                bis.close();
                // 文件编码为 ANSI
                return charset;
            } else if (first3Bytes[0] == (byte) 0xFF && first3Bytes[1] == (byte) 0xFE) {
                // 文件编码为 Unicode
                charset = "UTF-16LE";
                checked = true;
            } else if (first3Bytes[0] == (byte) 0xFE && first3Bytes[1] == (byte) 0xFF) {
                // 文件编码为 Unicode big endian
                charset = "UTF-16BE";
                checked = true;
            } else if (first3Bytes[0] == (byte) 0xEF && first3Bytes[1] == (byte) 0xBB
                    && first3Bytes[2] == (byte) 0xBF) {
                // 文件编码为 UTF-8
                charset = "UTF-8";
                checked = true;
            }
            bis.reset();
            if (!checked) {
                while ((read = bis.read()) != -1) {
                    if (read >= 0xF0) {
                        break;
                    }
                    // 单独出现BF以下的，也算是GBK
                    if (0x80 <= read && read <= 0xBF) {
                        break;
                    }
                    if (0xC0 <= read && read <= 0xDF) {
                        read = bis.read();
                        // 双字节 (0xC0 - 0xDF)
                        if (0x80 <= read && read <= 0xBF)
                        // (0x80 - 0xBF),也可能在GB编码内
                        {
                            continue;
                        } else {
                            break;
                        }
                    } else if (0xE0 <= read && read <= 0xEF) {
                        // 也有可能出错，但是几率较小
                        read = bis.read();
                        if (0x80 <= read && read <= 0xBF) {
                            read = bis.read();
                            if (0x80 <= read && read <= 0xBF) {
                                charset = "UTF-8";
                                break;
                            } else {
                                break;
                            }
                        } else {
                            break;
                        }
                    }
                }
            }
            bis.close();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            try {
                if (null != inputStream) {
                    inputStream.close();
                }
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        }
        log.info("文件采用的字符集为: [" + charset + "]");
        return charset;
    }

    public static String getCodeString(String fileName) throws Exception {
        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(fileName));
        int i = (bis.read() << 8) + bis.read();
        bis.close();
        String code = null;
        switch (i) {
            case 0xefbb:
                code = "UTF-8";
                break;
            case 0xfffe:
                code = "Unicode";
                break;
            case 0xfeff:
                code = "UTF-16BE";
                break;
            default:
                code = "GBK";
        }
        return code;
    }

}
