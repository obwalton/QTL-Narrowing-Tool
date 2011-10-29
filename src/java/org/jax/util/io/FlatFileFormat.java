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

/**
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public interface FlatFileFormat
{
    /**
     * int value that represents an N/A value. can be returned from
     * {@link #getQuoteChar()} to indicate that quoting is not supported
     */
    public static int NA_CHAR = -2;
    
    /**
     * What field delimiter should be used for this format?
     * @return
     *          the field delimiter that should be used
     */
    public abstract char getFieldDelimiter();
    
    /**
     * Getter for the choice of row delimiters in order of preference
     * @return
     *          the choice of row delimiters
     */
    public abstract CharSequence[] getRowDelimiterChoices();
    
    /**
     * Getter for the quote character. {@link FlatFileFormat#NA_CHAR} indicates
     * that quoting is not supported
     * @return
     *          the quote character
     */
    public abstract int getQuoteChar();
    
    /**
     * Getter for the row comment character. {@link FlatFileFormat#NA_CHAR}
     * indicates that comments are not supported
     * @return
     *          the quote character
     */
    public abstract int getCommentChar();
}
