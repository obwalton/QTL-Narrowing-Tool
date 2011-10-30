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

package org.jax.qtln.server.restful;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.List;

import javax.ws.rs.core.StreamingOutput;

import org.jax.util.io.CommonFlatFileFormat;
import org.jax.util.io.FlatFileWriter;

class BaseModelToCsvStreamingOutput implements StreamingOutput
{
    private final List<String[]> model;
    private final String delimiter;
    private final boolean include_header;
    
    
    /**
     * Constructor
     * @param model
     *          the model to use
     */
    public BaseModelToCsvStreamingOutput(List<String[]> table, String delim,
            boolean header)
    {
        this.model = table;
        this.delimiter = delim;
        this.include_header = header;
    }

    /**
     * {@inheritDoc}
     */
    public void write(OutputStream os)
    throws IOException
    {
        System.out.println("Writing out CSV");
        FlatFileWriter flatFile;
        if ("TAB".equals(this.delimiter))
             flatFile = new FlatFileWriter(
                new OutputStreamWriter(os),
                CommonFlatFileFormat.UNQUOTED_TAB_DELIMITED_UNIX);
        else
             flatFile = new FlatFileWriter(
                new OutputStreamWriter(os),
                CommonFlatFileFormat.CSV_RFC_4180);


        // write the header

        String[] tableHeader = this.model.get(0);
        if (this.include_header)
            flatFile.writeRow(tableHeader);

        // write the data
        writeResultsToFlatFile(this.model,
                flatFile);

        flatFile.flush();
    }

    private void writeResultsToFlatFile(List<String[]> model,
            FlatFileWriter flatFile)
            throws IOException
    {

        int columnCount = model.get(0).length;
        boolean first = true;
        // skip header row
        for (int i = 1; i < model.size(); i++)
        {
            String[] currRowStrings = model.get(i);
            flatFile.writeRow(currRowStrings);
        }

    }
}
