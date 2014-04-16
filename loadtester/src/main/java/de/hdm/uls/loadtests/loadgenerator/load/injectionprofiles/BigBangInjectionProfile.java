package de.hdm.uls.loadtests.loadgenerator.load.injectionprofiles;

/**
 * The BigBang profile describes a maximum increasing rate of clients in a short time.
 *
 * @author Dennis Grewe [dg060@hdm-stuttgart.de] 03/16/2014
 */
public class BigBangInjectionProfile implements InjectionProfile
{
    @Override
    public double getValue(double progressInPercentage)
    {
        return 1.0;     // Maximum load
    }
}