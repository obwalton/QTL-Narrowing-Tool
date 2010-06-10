package org.jax.qtln.regions;

import java.io.Serializable;

public class SNPDoesNotMeetCriteriaException extends Throwable
        implements Serializable
{
    private static final long serialVersionUID =
            4741175800356479306L;

    String message;

    public SNPDoesNotMeetCriteriaException(Exception e)
    {
        message = e.getMessage();
        
    }

    public SNPDoesNotMeetCriteriaException()
    {
    }

    public SNPDoesNotMeetCriteriaException(String message)
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