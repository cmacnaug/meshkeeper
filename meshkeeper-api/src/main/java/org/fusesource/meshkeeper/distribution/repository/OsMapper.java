package org.fusesource.meshkeeper.distribution.repository;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.StringTokenizer;

public class OsMapper {

    private static String osName = System.getProperty("os.name").toLowerCase();
    private static String osVersion = System.getProperty("os.version").toLowerCase();
    private static String osArch = System.getProperty("os.arch").toLowerCase();
    private static String WILDCARD = "*";

    public static String map(String path, File osMap) throws Exception {
        BufferedReader br = new BufferedReader(new FileReader(osMap));

        String line = null;
        while ((line = br.readLine()) != null) {
            if (line.startsWith("#") || line.startsWith(";")) {
                continue;
            }

            line = line.trim();
            if (line.length() == 0) {
                continue;
            }

            if (line.lastIndexOf("=") == -1) {
                continue;
            }

            String rules = line.substring(0, line.lastIndexOf("=")).trim();
            String mapping = line.substring(line.lastIndexOf("=") + 1).trim();

            OsRule rule = new OsRule(rules, mapping);
            if (rule.match()) {
                br.close();
                return rule.mapping;
            }
        }

        return null;
    }

    private static class OsRule {

        String nameRule = WILDCARD;
        String versionRule = WILDCARD;
        String archRule = WILDCARD;
        String mapping;

        OsRule(String rule, String mapping) {
            StringTokenizer tok = new StringTokenizer(rule, ",");
            if (tok.hasMoreTokens()) {
                nameRule = tok.nextToken().trim().toLowerCase();
            }

            if (tok.hasMoreTokens()) {
                versionRule = tok.nextToken().trim().toLowerCase();
            }

            if (tok.hasMoreTokens()) {
                archRule = tok.nextToken().trim().toLowerCase();
            }
            
            this.mapping = mapping;
        }

        private boolean match() {
            if (!nameRule.equals("*") && osName.indexOf(nameRule) == -1) {
                return false;
            }
            
            if (!versionRule.equals("*") && osVersion.indexOf(versionRule) == -1) {
                return false;
            }
            
            if (!archRule.equals("*") && osArch.indexOf(archRule) == -1) {
                return false;
            }
            
            return true;

        }
    }

}
