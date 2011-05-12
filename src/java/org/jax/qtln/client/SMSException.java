package org.jax.qtln.client;

import com.google.gwt.user.client.rpc.IsSerializable;

public class SMSException extends Throwable
        implements IsSerializable
{
    private static final long serialVersionUID =
            4741175800356479306L;

    String message;

    public SMSException(Exception e)
    {
        message = e.getMessage();
        
    }

    public SMSException()
    {
    }

    public SMSException(String message)
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