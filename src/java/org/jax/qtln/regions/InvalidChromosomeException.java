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