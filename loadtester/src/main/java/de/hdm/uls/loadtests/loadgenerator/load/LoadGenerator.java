package de.hdm.uls.loadtests.loadgenerator.load;

import de.hdm.uls.loadtests.loadgenerator.load.injectionprofiles.InjectionProfile;
import de.hdm.uls.loadtests.loadgenerator.load.model.GeneratorResults;
import de.hdm.uls.loadtests.loadgenerator.load.model.InjectorResults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class describes a load generator. The load generator generates the overall load. Each method represents a
 * different load testing strategy. This class uses a set of load injectors to simulate different situations.
 * Each injector is executed in a different thread. The injector profile describes how much load is generated at the
 * specific time.
 *
 * @author Dennis Grewe [dg060@hdm-stuttgart.de] 03/16/2014
 */
public abstract class LoadGenerator
{
    // ---------------------------------------
    // PROPERTIES
    // ---------------------------------------

    private static final Logger log                         = LoggerFactory.getLogger(LoadGenerator.class);
    private static final int    INJECTION_FREQUENCY_IN_K    = 100;

    private GeneratorResults    testResults                 = new GeneratorResults();
    private boolean             isRunning                   = false;

    private int                 totalClients                = 0;
    private int                 durationInMillis            = 0;
    private InjectionProfile    injectionProfile            = null;

    // ---------------------------------------
    // CONSTRUCTOR
    // ---------------------------------------

    public LoadGenerator(int totalClients, int durationInMillis, InjectionProfile injectionProfile)
    {
        this.totalClients = totalClients;
        this.durationInMillis = durationInMillis;
        this.injectionProfile = injectionProfile;
    }

    // ---------------------------------------
    // RUN
    // ---------------------------------------

    public void run()
    {
        log.info("-- LoadGenerator#run");
        isRunning = true;
        testResults.measureStartTime();
        generateLoad();
        finishInjection();
        testResults.measureStopTime();
        isRunning = false;
    }

    // ---------------------------------------
    // METHODS
    // ---------------------------------------

    /**
     * This method runs the methods and functions to generate load.
     */
    private void generateLoad()
    {
        long startTimeMs = System.currentTimeMillis();
        while (true)
        {
            long currentTimeMs = System.currentTimeMillis();
            long timePastMs = currentTimeMs - startTimeMs;
            double progressInPercent = ((double)timePastMs / durationInMillis) * 100;
            if (progressInPercent >= 100)
            {
                break;
            }
            double targetLoadInPercent = this.injectionProfile.getValue(progressInPercent);
            int targetLoad = (int) (targetLoadInPercent * totalClients);
            long timeLeftMs = durationInMillis - timePastMs;
            injectLoad(targetLoad, timeLeftMs, testResults);
            sleep((long) (1.0 / LoadGenerator.INJECTION_FREQUENCY_IN_K) * 1000);
        }
    }

    private void sleep(long durationMs)
    {
        try
        {
            Thread.sleep(durationMs);
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
    }

    protected abstract void injectLoad(int targetLoad, long timeLeftMs, GeneratorResults testResults);

    protected abstract void finishInjection();

    public abstract int getCurrentClientsCount();

    /**
     * @return the results of the load generator.
     */
    public GeneratorResults getGeneratorResults()
    {
        return this.testResults;
    }

    /**
     * @return if the generator is running.
     */
    public boolean isRunning()
    {
        return this.isRunning;
    }
}