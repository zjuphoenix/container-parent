package com.study.container;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.study.container.util.ConfigFileUtil;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * @author cangxing
 * @date 2017-04-17 10:14
 */
public class FileToJsonTest {
    public static void main(String[] args) throws IOException {
        File jsonFile = new File("/Users/cangxing/Documents/Study/hsf/container-parent/test/src/main/resources/container/bundles/bundle-first/config.json");
        String json = ConfigFileUtil.readStringFromFile(jsonFile);
        JSONObject obj = JSON.parseObject(json);
        //System.out.println(JSON.parseObject((String)obj.get("importPackages"), new TypeReference<List<String>>(){}));
        //System.out.println(JSON.parseObject((String)obj.get("exportJars"), new TypeReference<List<String>>(){}));
    }
}
