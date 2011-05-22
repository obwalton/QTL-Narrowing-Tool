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
package org.jax.qtln.regions;

import java.io.Serializable;

public class InvalidChromosomeException extends Throwable
        implements Serializable
{
    private static final long serialVersionUID =
            4741175800356479306L;

    String message;

    public InvalidChromosomeException(Exception e)
    {
        message = e.getMessage();
        
    }

    public InvalidChromosomeException()
    {
        message = "Not a valid chromosome value";
    }

    public InvalidChromosomeException(String message)
    {
        this.message = message;
    }

    public String getMessage()
    {
        return message;
    }

    public void setMessage(String message)
    {
        this.message = message;
    }
}