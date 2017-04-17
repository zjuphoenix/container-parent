package com.study.container.config;

import java.util.List;

/**
 * @author cangxing
 * @date 2017-04-16 17:02
 */
public class Config {
    private List<String> importPackages;
    private List<String> exportJars;

    public List<String> getImportPackages() {
        return importPackages;
    }

    public void setImportPackages(List<String> importPackages) {
        this.importPackages = importPackages;
    }

    public List<String> getExportJars() {
        return exportJars;
    }

    public void setExportJars(List<String> exportJars) {
        this.exportJars = exportJars;
    }
}
