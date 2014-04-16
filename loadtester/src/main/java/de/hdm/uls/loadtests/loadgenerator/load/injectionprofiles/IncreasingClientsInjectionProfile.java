package de.hdm.uls.loadtests.loadgenerator.load.injectionprofiles;

/**
 * The increasing profile describes a slow increasing rate of clients in a period of time.
 *
 * @author Dennis Grewe [dg060@hdm-stuttgart.de] 03/16/2014
 */
public class IncreasingClientsInjectionProfile implements InjectionProfile
{
    @Override
    public double getValue(double progressInPercentage)
    {
        return progressInPercentage / 100;
    }
}