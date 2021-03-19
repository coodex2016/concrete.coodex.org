/*
 * Copyright (c) 2018 coodex.org (jujus.shen@126.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.csource.common;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;

/**
 * ini file reader / parser
 *
 * @author Happy Fish / YuQing
 * @version Version 1.0
 */
@SuppressWarnings("unchecked")
public class IniFileReader {
    private Hashtable paramTable;
    private String conf_filename;

    /**
     * @param conf_filename config filename
     */
    public IniFileReader(String conf_filename) throws FileNotFoundException, IOException {
        this.conf_filename = conf_filename;
        loadFromFile(conf_filename);
    }

    /**
     * get the config filename
     *
     * @return config filename
     */
    public String getConfFilename() {
        return this.conf_filename;
    }

    /**
     * get string value from config file
     *
     * @param name item getName in config file
     * @return string value
     */
    public String getStrValue(String name) {
        Object obj;
        obj = this.paramTable.get(name);
        if (obj == null) {
            return null;
        }

        if (obj instanceof String) {
            return (String) obj;
        }

        return (String) ((ArrayList) obj).get(0);
    }

    /**
     * get int value from config file
     *
     * @param name          item getName in config file
     * @param default_value the default value
     * @return int value
     */
    public int getIntValue(String name, int default_value) {
        String szValue = this.getStrValue(name);
        if (szValue == null) {
            return default_value;
        }

        return Integer.parseInt(szValue);
    }

    /**
     * get boolean value from config file
     *
     * @param name          item getName in config file
     * @param default_value the default value
     * @return boolean value
     */
    public boolean getBoolValue(String name, boolean default_value) {
        String szValue = this.getStrValue(name);
        if (szValue == null) {
            return default_value;
        }

        return szValue.equalsIgnoreCase("yes") || szValue.equalsIgnoreCase("on") ||
                szValue.equalsIgnoreCase("true") || szValue.equals("1");
    }

    /**
     * get all values from config file
     *
     * @param name item getName in config file
     * @return string values (array)
     */
    public String[] getValues(String name) {
        Object obj;
        String[] values;

        obj = this.paramTable.get(name);
        if (obj == null) {
            return null;
        }

        if (obj instanceof String) {
            values = new String[1];
            values[0] = (String) obj;
            return values;
        }

        Object[] objs = ((ArrayList) obj).toArray();
        values = new String[objs.length];
        System.arraycopy(objs, 0, values, 0, objs.length);
        return values;
    }

    private void loadFromFile(String conf_filename) throws FileNotFoundException, IOException {
        FileReader fReader;
        BufferedReader buffReader;
        String line;
        String[] parts;
        String name;
        String value;
        Object obj;
        ArrayList valueList;

        fReader = new FileReader(conf_filename);
        buffReader = new BufferedReader(fReader);
        this.paramTable = new Hashtable();

        try {
            while ((line = buffReader.readLine()) != null) {
                line = line.trim();
                if (line.length() == 0 || line.charAt(0) == '#') {
                    continue;
                }

                parts = line.split("=", 2);
                if (parts.length != 2) {
                    continue;
                }

                name = parts[0].trim();
                value = parts[1].trim();

                obj = this.paramTable.get(name);
                if (obj == null) {
                    this.paramTable.put(name, value);
                } else if (obj instanceof String) {
                    valueList = new ArrayList();
                    valueList.add(obj);
                    valueList.add(value);
                    this.paramTable.put(name, valueList);
                } else {
                    valueList = (ArrayList) obj;
                    valueList.add(value);
                }
            }
        } finally {
            fReader.close();
        }
    }
}
