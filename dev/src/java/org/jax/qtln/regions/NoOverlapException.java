package org.jax.qtln.regions;

import java.io.Serializable;

public class NoOverlapException extends Throwable
        implements Serializable
{
    private static final long serialVersionUID =
            4741175800356479306L;

    String message;

    public NoOverlapException(Exception e)
    {
        message = e.getMessage();
        
    }

    public NoOverlapException()
    {
    }

    public NoOverlapException(String message)
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