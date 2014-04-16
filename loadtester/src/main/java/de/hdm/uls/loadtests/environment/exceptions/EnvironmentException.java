package de.hdm.uls.loadtests.environment.exceptions;

/**
 * This class defines an exception type for the load test environment processes.
 *
 * @author Dennis Grewe [dg060@hdm-stuttgart.de] 03/15/2014
 */
public class EnvironmentException extends Exception
{
    public EnvironmentException(String message)
    {
        super(message);
    }

    public EnvironmentException(String message, Throwable cause)
    {
        super(message, cause);
    }
}