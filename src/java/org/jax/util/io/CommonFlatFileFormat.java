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
 * Some commonly used data formats
 */
public enum CommonFlatFileFormat implements FlatFileFormat{
    /**
     * for CSV format
     * <a href="http://tools.ietf.org/html/rfc4180">RFC 4180</a>. This
     * allows both CRLF and LF row delimiters even though the
     * specification only allows for CRLF delimiters. Comments are
     * not supported.
     * <pre>
     *    ABNF:
     *    =====
     *    file = [header CRLF] record *(CRLF record) [CRLF]
     *    header = name *(COMMA name)
     *    record = field *(COMMA field)
     *    name = field
     *    field = (escaped / non-escaped)
     *    escaped = DQUOTE *(TEXTDATA / COMMA / CR / LF / 2DQUOTE) DQUOTE
     *    non-escaped = *TEXTDATA
     *    COMMA = %x2C
     *    CR = %x0D ;as per section 6.1 of RFC 2234 [2]
     *    DQUOTE =  %x22 ;as per section 6.1 of RFC 2234 [2]
     *    LF = %x0A ;as per section 6.1 of RFC 2234 [2]
     *    CRLF = CR LF ;as per section 6.1 of RFC 2234 [2]
     *    TEXTDATA =  %x20-21 / %x23-2B / %x2D-7E
     * </pre>
     */
    CSV_RFC_4180
    {
        /**
         * {@inheritDoc}
         */
        public char getFieldDelimiter()
        {
            return ',';
        }
        
        /**
         * {@inheritDoc}
         */
        public CharSequence[] getRowDelimiterChoices()
        {
            return new CharSequence[] {"\r\n", "\n"};
        }
        
        /**
         * {@inheritDoc}
         */
        public int getQuoteChar()
        {
            return '"';
        }
        
        /**
         * {@inheritDoc}
         */
        public int getCommentChar()
        {
            return FlatFileFormat.NA_CHAR;
        }
    },
    
    /**
     * This is like {@link CommonFlatFileFormat#CSV_RFC_4180} except
     * that LF is the default row delimiter instead of CRLF, and rows
     * that start with '#' are considered comments.
     */
    CSV_UNIX
    {
        /**
         * {@inheritDoc}
         */
        public char getFieldDelimiter()
        {
            return ',';
        }
        
        /**
         * {@inheritDoc}
         */
        public CharSequence[] getRowDelimiterChoices()
        {
            return new CharSequence[] {"\n", "\r\n"};
        }
        
        /**
         * {@inheritDoc}
         */
        public int getQuoteChar()
        {
            return '"';
        }
        
        /**
         * {@inheritDoc}
         */
        public int getCommentChar()
        {
            return '#';
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        public String toString()
        {
            return "Comma Separated (CSV)";
        }
    },
    
    /**
     * This is like {@link CommonFlatFileFormat#CSV_UNIX} except
     * that tab separators are used in place of comma separators.
     */
    TAB_DELIMITED_UNIX
    {
        /**
         * {@inheritDoc}
         */
        public char getFieldDelimiter()
        {
            return '\t';
        }
        
        /**
         * {@inheritDoc}
         */
        public CharSequence[] getRowDelimiterChoices()
        {
            return new CharSequence[] {"\n", "\r\n"};
        }
        
        /**
         * {@inheritDoc}
         */
        public int getQuoteChar()
        {
            return '"';
        }
        
        /**
         * {@inheritDoc}
         */
        public int getCommentChar()
        {
            return '#';
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        public String toString()
        {
            return "Tab Delimited";
        }
    },
    
    /**
     * This is like {@link CommonFlatFileFormat#TAB_DELIMITED_UNIX} except
     * that field quoting is not supported ({@link #getQuoteChar()} returns
     * {@link FlatFileFormat#NA_CHAR}).
     */
    UNQUOTED_TAB_DELIMITED_UNIX
    {
        /**
         * {@inheritDoc}
         */
        public char getFieldDelimiter()
        {
            return '\t';
        }
        
        /**
         * {@inheritDoc}
         */
        public CharSequence[] getRowDelimiterChoices()
        {
            return new CharSequence[] {"\n", "\r\n"};
        }
        
        /**
         * {@inheritDoc}
         */
        public int getQuoteChar()
        {
            return FlatFileFormat.NA_CHAR;
        }
        
        /**
         * {@inheritDoc}
         */
        public int getCommentChar()
        {
            return '#';
        }
   }
}