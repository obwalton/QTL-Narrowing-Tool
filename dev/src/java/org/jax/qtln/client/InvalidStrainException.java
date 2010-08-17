package org.jax.qtln.client;

import com.google.gwt.user.client.rpc.IsSerializable;

public class InvalidStrainException extends Throwable
        implements IsSerializable
{
    private static final long serialVersionUID =
            4741175800356479306L;

    String message;

    public InvalidStrainException(Exception e)
    {
        message = e.getMessage();
        
    }

    public InvalidStrainException()
    {
    }

    public InvalidStrainException(String message)
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