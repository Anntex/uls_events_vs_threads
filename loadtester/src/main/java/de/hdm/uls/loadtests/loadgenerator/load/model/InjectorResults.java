package de.hdm.uls.loadtests.loadgenerator.load.model;

import de.hdm.uls.loadtests.loadgenerator.exceptions.MeasurementException;

import java.util.ArrayList;
import java.util.List;

/**
 * This class defines a load injector result model to store and handle
 * results of load measurements.
 *
 * @author Dennis Grewe [dg060@hdm-stuttgart.de] 02/09/2014
 */
public class InjectorResults
{
    // ---------------------------------------
    // PROPERTIES
    // ---------------------------------------

    private String injectionMethod = null;
    private long startTimeInNanos = -1;
    private long stopTimeInNanos = -1;
    private int totalClients = 0;
    private List<ResponseTime> responseTimes = new ArrayList<>();

    // ---------------------------------------
    // METHODS
    // ---------------------------------------

    public void measureStartTime()
    {
        this.startTimeInNanos = System.nanoTime();
    }

    public void measureStopTime()
    {
        this.stopTimeInNanos = System.nanoTime();
    }

    /**
     * @return the duration of the response time in seconds, otherwise -1
     */
    public double getDurationInSec() throws MeasurementException
    {
        double result = -1d;

        if(this.stopTimeInNanos > -1d && this.startTimeInNanos > -1d)
        {
            result = ((double) (this.stopTimeInNanos - this.startTimeInNanos)) / 1000000000;
        }
        else
        {
            throw new MeasurementException("One of the required measurement parameters are empty!");
        }

        return result;
    }

    public void addResponseTime(long startInNs, long stopInNs)
    {
        this.responseTimes.add(new ResponseTime(startInNs, stopInNs));
    }

    public double getAverageResponseTimeInMs()
    {
        double average = 0;

        for (ResponseTime responseTime : this.responseTimes)
        {
            average += responseTime.getResponseTimeInMs();
        }

        if (average != 0)
        {
            average /= responseTimes.size();
        }

        return average;
    }

    // ---------------------------------------
    // GETTERS / SETTERS
    // ---------------------------------------

    public void setTotalClients(int clients) { this.totalClients = clients; }

    public void setInjectionMethod(String method) { this.injectionMethod = method; }

    public String getInjectionMethod() { return this.injectionMethod; }

    public int getTotalClients()
    {
        return this.totalClients;
    }

    public long getStartTimeInNanos() { return this.startTimeInNanos; }

    public long getStopTimeInNanos() { return this.stopTimeInNanos; }

    public List<ResponseTime> getResponseTimes() { return this.responseTimes; }
}