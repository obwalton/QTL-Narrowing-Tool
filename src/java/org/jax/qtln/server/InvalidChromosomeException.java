package org.jax.qtln.server;

import com.google.gwt.user.client.rpc.IsSerializable;

public class InvalidChromosomeException extends Throwable
        implements IsSerializable
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