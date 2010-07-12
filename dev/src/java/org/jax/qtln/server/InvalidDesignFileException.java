package org.jax.qtln.server;

import org.jax.qtln.regions.*;
import java.io.Serializable;

public class InvalidDesignFileException extends Throwable
        implements Serializable
{
    private static final long serialVersionUID =
            4741175800356479306L;

    String message;

    public InvalidDesignFileException(Exception e)
    {
        message = e.getMessage();
        
    }

    public InvalidDesignFileException()
    {
        message = "Not a valid chromosome value";
    }

    public InvalidDesignFileException(String message)
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