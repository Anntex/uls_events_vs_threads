package de.hdm.uls.loadtests.loadgenerator.exceptions;

/**
 * This class defines an exception type for load measurements.
 *
 * @author Dennis Grewe [dg060@hdm-stuttgart.de] 02/09/2014
 */
public class MeasurementException extends Exception
{
    // ---------------------------------------
    // CONSTUROCTURS
    // ---------------------------------------

    /**
     * Default constructor method of a measurement exception.
     */
    public MeasurementException()
    {
        super();
    }

    /**
     * Constructor for throwing a measurement exception containing a certain message.
     *
     * @param message The message of this exception.
     */
    public MeasurementException(String message)
    {
        super(message);
    }

    /**
     * Constructor for throwing a measurement exception containing a certain message caused by a certain throwable object.
     *
     * @param message The message of this exception.
     * @param cause The throwable cause of this exception.
     */
    public MeasurementException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
