package com.chenpp.common.util;

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author April.Chen
 * @date 2024/6/18 14:51
 */
public class FileTest {

    public void traversalDirectory(String dir) {
        // 指定要遍历的目录路径
        Path startDirectory = Paths.get(dir);

        try {
            // 使用Files.walk()遍历目录
            Files.walk(startDirectory)
                    // 对于遍历到的每个文件或目录应用Lambda表达式
                    .forEach(path -> {
                        // 打印当前路径
                        System.out.print(path);
                        System.out.print("\t");

                        // 这里可以根据需要添加更多操作，如文件过滤、操作等
                        try {
                            long sizeInBytes = Files.size(path);
                            // 如果需要，可以转换为KB、MB等单位
                            double sizeInKB = sizeInBytes / 1024.0;
                            double sizeInMB = sizeInBytes / (1024.0 * 1024.0);
                            System.out.printf("Size in KB: %.2f KB%n", sizeInKB);
                            System.out.printf("Size in MB: %.2f MB%n", sizeInMB);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        System.out.println();
                    });
        } catch (IOException e) {
            // 处理可能发生的I/O异常
            e.printStackTrace();
        }
    }

    @Test
    public void testListFiles() throws IOException {
        String dir = "/Users/chenpp/Downloads";
        try (Stream<Path> stream = Files.list(Paths.get(dir))) {
            stream.filter(path -> path.toFile().isFile()).forEach(path -> {
                if (path.toFile().isFile()) {
                    String filename = path.getFileName().toString();
                    String size = FileUtil.formatSize(path.toFile().length());
                    System.out.println(filename + "\t" + size);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testWriteFile() {
        String filePath = "/Users/chenpp/Downloads/test.txt";
        List<String> lines = Arrays.asList("Line 1", "Line 2", "Line 3");
        FileUtil.writeFile(filePath, lines);
    }
}
