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
 * An interface for writing table rows
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public interface TableWriter
{
    /**
     * Write the given row
     * @param row
     *          the row
     * @throws IOException
     *          if the underlying writer throws an exception
     */
    public void writeRow(String[] row) throws IOException;
    
    /**
     * Calls {@link Writer#flush()} on the underlying stream
     * @throws IOException
     *          if we get an exception from the underlying stream
     */
    public void flush() throws IOException;
    
    /**
     * Calls {@link Writer#close()} on the underlying stream
     * @throws IOException
     *          if we get an exception from the underlying stream
     */
    public void close() throws IOException;
}
