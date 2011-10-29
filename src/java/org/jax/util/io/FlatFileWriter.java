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

package org.jax.util.io;

import java.io.IOException;
import java.io.Writer;

/**
 * Writes files in a format readable by {@link FlatFileReader}
 * TODO: deal with comment char's in the writer
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class FlatFileWriter implements TableWriter
{
    private final CharSequence rowDelimiter;
    
    private final char fieldDelimiter;
    
    private final int quoteChar;
    
    private final Writer writer;
    
    private final String quoteString;
    
    private final String doubleQuoteString;
    
    /**
     * Constructor
     * @param writer
     *          the writer that this {@link FlatFileWriter} writes to
     * @param format
     *          the format to use
     */
    public FlatFileWriter(
            Writer writer,
            FlatFileFormat format)
    {
        this(writer,
             format.getRowDelimiterChoices()[0],
             format.getFieldDelimiter(),
             format.getQuoteChar());
    }

    /**
     * Constructor
     * @param writer
     *          the writer that this {@link FlatFileWriter} writes to
     * @param rowDelimiter
     *          Character sequence that separates lines
     * @param fieldDelimiter
     *          character that separates fields
     * @param quoteChar
     *          character used to quote fields
     */
    public FlatFileWriter(
            Writer writer,
            CharSequence rowDelimiter,
            char fieldDelimiter,
            int quoteChar)
    {
        this.writer = writer;
        this.rowDelimiter = rowDelimiter;
        this.fieldDelimiter = fieldDelimiter;
        this.quoteChar = quoteChar;
        
        if(quoteChar != FlatFileFormat.NA_CHAR && Character.isDefined(quoteChar))
        {
            this.quoteString = Character.toString((char)quoteChar);
            this.doubleQuoteString = this.quoteString + this.quoteString;
        }
        else
        {
            this.quoteString = null;
            this.doubleQuoteString = null;
        }
    }
    
    /**
     * Getter for the field delimiter
     * @return the fieldDelimiter
     */
    public char getFieldDelimiter()
    {
        return this.fieldDelimiter;
    }
    
    /**
     * Getter for the row delimiter
     * @return the rowDelimiter
     */
    public CharSequence getRowDelimiter()
    {
        return this.rowDelimiter;
    }
    
    /**
     * Getter for the quote character. Could be a valid character or could
     * be {@link FlatFileFormat#NA_CHAR}.
     * @return the quoteChar
     */
    public int getQuoteChar()
    {
        return this.quoteChar;
    }
    
    /**
     * Getter for the underlying writer
     * @return the writer
     */
    public Writer getWriter()
    {
        return this.writer;
    }
    
    /**
     * {@inheritDoc}
     */
    public void writeRow(String[] row) throws IOException
    {
        for(int i = 0; i < row.length; i++)
        {
            if(i >= 1)
            {
                this.writer.write(this.fieldDelimiter);
            }
            
            String field = row[i];
            boolean needsSurroundingQuotes = false;
            if(this.quoteString != null && field.contains(this.quoteString))
            {
                // escape the quotes
                needsSurroundingQuotes = true;
                
                if(this.quoteString != null)
                {
                    field = field.replace(this.quoteString, this.doubleQuoteString);
                }
            }
            else
            {
                // if thier's a field or row delimiter we need surrounding
                // quotes
                needsSurroundingQuotes =
                    (field.indexOf(this.fieldDelimiter) >= 0);
                if(!needsSurroundingQuotes)
                {
                    needsSurroundingQuotes = field.contains(this.rowDelimiter);
                }
            }
            
            if(needsSurroundingQuotes)
            {
                if(this.quoteString != null)
                {
                    field = this.quoteString + field + this.quoteString;
                }
                else
                {
                    throw new IOException(
                            "Found a field delimiter or a row delimiter in " +
                            "a writer that doesn't support quoting");
                }
            }
            
            this.writer.write(field);
        }
        
        this.writer.write(this.rowDelimiter.toString());
    }
    
    /**
     * {@inheritDoc}
     */
    public void flush() throws IOException
    {
        this.writer.flush();
    }
    
    /**
     * {@inheritDoc}
     */
    public void close() throws IOException
    {
        this.writer.close();
    }
}
