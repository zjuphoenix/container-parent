package com.study.container.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * @author cangxing
 * @date 2017-03-09 20:33
 */
public class IOUtil {
    private static final Logger logger = LoggerFactory.getLogger(IOUtil.class);

    public static String readFileContent(String fileName) {
        File file = new File(fileName);
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            String tempString = null;
            // 一次读入一行，直到读入null为文件结束
            while ((tempString = reader.readLine()) != null) {
                // 显示行号
                return tempString;
            }
        } catch (IOException e) {
            logger.error("read file content error!", e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                }
            }
        }
        return null;
    }
}
