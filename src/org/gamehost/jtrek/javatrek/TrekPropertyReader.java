/**
 *  Copyright (C) 2003-2007  Joe Hopkinson, Jay Ashworth
 *
 *  JavaTrek is based on Chuck L. Peterson's MTrek.
 *
 *  This file is part of JavaTrek.
 *
 *  JavaTrek is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  JavaTrek is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with JavaTrek; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package org.gamehost.jtrek.javatrek;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

/**
 * Created by IntelliJ IDEA.
 * User: jay
 * Date: Aug 2, 2004
 * Time: 10:31:30 PM
 * To change this template use File | Settings | File Templates.
 */
public class TrekPropertyReader {
    // for reading in any properties we want to define in a file
    // i.e. jdbc connection parameters
    private static String fileName = "./jtrek.properties";
    private static Properties propertyPairs = null;
    private static TrekPropertyReader tpr = null;

    private TrekPropertyReader() {
        File propertyFile = new File(fileName);
        try {
            FileInputStream pfInputStream = new FileInputStream(propertyFile);
            // do the process of populating the Hashtable of properties pairs here
            propertyPairs = new Properties();
            propertyPairs.load(pfInputStream);

            // test code
            // propertyPairs.list(System.out);

        } catch (FileNotFoundException fnfe) {
            fnfe.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

    }

    protected static TrekPropertyReader getInstance() {
        if (tpr == null) {
            tpr = new TrekPropertyReader();
        }

        return tpr;
    }

    protected Properties getProperties() {
        return propertyPairs;
    }

    protected String getValue(String s) {
        String returnValue = "";
        // do a look up in the Hashtable for the property defined by 's', and return the value

        try {
            returnValue = propertyPairs.getProperty(s);
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return returnValue;
    }
}
