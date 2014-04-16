package de.hdm.uls.loadtests.loadgenerator.load.injectionprofiles;

/**
 * The increasing profile describes a slow increasing rate of clients in a period of time.
 *
 * @author Dennis Grewe [dg060@hdm-stuttgart.de] 03/16/2014
 */
public class IncreasingClientsWithStepInjectionProfile implements InjectionProfile
{
    // ---------------------------------------
    // PROPERTIES
    // ---------------------------------------

    private int numberOfSteps = 0;

    // ---------------------------------------
    // CONSTRUCTOR
    // ---------------------------------------

    public IncreasingClientsWithStepInjectionProfile(int numberOfSteps)
    {
        this.numberOfSteps = numberOfSteps;
    }

    // ---------------------------------------
    // METHODS
    // ---------------------------------------

    @Override
    public double getValue(double progressInPercentage)
    {
        int step = (int)((progressInPercentage / 100) * (this.numberOfSteps + 1));
        double value = ((double) step ) / (this.numberOfSteps + 1);
        return value;
    }
}