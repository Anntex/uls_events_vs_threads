package de.hdm.uls.loadtests.loadgenerator.load.model;

/**
 * This class represents a model for a certain time interval of a client request and the
 * server response, given in millis
 *
 * @author Dennis Grewe [dg060@hdm-stuttgart.de] 02/09/2014
 */
public class ResponseTime
{
    // ---------------------------------------
    // PROPERTIES
    // ---------------------------------------

    public long startTimeNs;
    public long stopTimeNs;

    // ---------------------------------------
    // CONSTRUCTOR
    // ---------------------------------------

    public ResponseTime(long startTimeNs, long stopTimeNs)
    {
        this.startTimeNs = startTimeNs;
        this.stopTimeNs = stopTimeNs;
    }

    // ---------------------------------------
    // METHODS
    // ---------------------------------------

    public double getStartTimeInMs()
    {
        return ((double) (startTimeNs)) / 1000000;
    }

    public double getResponseTimeInSec()
    {
        return ((double) (stopTimeNs - startTimeNs)) / 1000000000;
    }

    public double getResponseTimeInMs()
    {
        return ((double) (stopTimeNs - startTimeNs)) / 1000000;
    }
}