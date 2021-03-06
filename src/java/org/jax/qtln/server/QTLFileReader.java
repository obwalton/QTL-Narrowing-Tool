/*
 * Copyright (c) 2010 The Jackson Laboratory
 * 
 * This is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.jax.qtln.server;

import org.jax.qtln.client.SMSException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;


/**
 *
 * @author dow
 */
public class QTLFileReader {

    public static final int EXPECTED_NUM_COLS = 8;

    public QTLFileReader() {

    }

    public List<String[]> readQTLFile(HttpSession session,
            HttpServletRequest hsr)
            throws SMSException
    {
        String qtlFileName = (String) session.getAttribute("QTLFile");
        File qtlFile = new File(qtlFileName);
        List<String[]> fileResults = new ArrayList<String[]>();
        int line_counter = 0;
        //  If there is a sequence file harvest fasta sequences from file
        if (qtlFile.exists()) {
            try {
                FileReader fileReader = new FileReader(qtlFile);
                BufferedReader bufferedReader = new BufferedReader(fileReader);
                String line = "";
                // loop through lines of the file.  Separating each line
                // into a row of the dataset object.
                while ((line = bufferedReader.readLine()) != null) {
                    ++line_counter;
                    String[] cols = line.split("\t");
                    if (cols.length == QTLFileReader.EXPECTED_NUM_COLS)
                        fileResults.add(cols);
                    else
                        throw new SMSException("Line " + line_counter + 
                                " contains wrong number of columns.  Expected "+
                                QTLFileReader.EXPECTED_NUM_COLS + " found " +
                                cols.length);
                }
            } catch (SMSException sms) {
                throw sms;
            } catch (Exception e) {
                e.printStackTrace();
                throw new SMSException(e);
            }
        }
        System.out.println("There are " + fileResults.size()
                + " QTLs uploaded from the file");

        return fileResults;

    }


}
