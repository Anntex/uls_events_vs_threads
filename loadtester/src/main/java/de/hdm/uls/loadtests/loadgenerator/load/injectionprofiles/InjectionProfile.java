package de.hdm.uls.loadtests.loadgenerator.load.injectionprofiles;

/**
 * An injection profile represents a function mapping a point in time to a number of clients.
 * Thus a profile describes the process of load generation.
 *
 * @author Dennis Grewe [dg060@hdm-stuttgart.de] 03/16/2014
 */
public interface InjectionProfile
{
    /**
     * 0.0f -> no load
     * 1.0f -> maximum load
     *
     * @param progressInPercentage
     * @return the mapping point in time [0;1]
     */
    public double getValue(double progressInPercentage);
}