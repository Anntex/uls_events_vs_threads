package de.hdm.uls.loadtests.loadtests;

import de.hdm.uls.loadtests.environment.model.TestCase;
import de.hdm.uls.loadtests.loadgenerator.exceptions.MeasurementException;
import de.hdm.uls.loadtests.loadgenerator.load.LoadGenerator;
import de.hdm.uls.loadtests.loadgenerator.load.generators.RealisticLoadGenerator;
import de.hdm.uls.loadtests.loadgenerator.load.injectionprofiles.BigBangInjectionProfile;
import de.hdm.uls.loadtests.loadgenerator.load.injectionprofiles.IncreasingClientsInjectionProfile;
import de.hdm.uls.loadtests.loadgenerator.load.injectionprofiles.IncreasingClientsWithStepInjectionProfile;
import de.hdm.uls.loadtests.loadgenerator.load.model.GeneratorResults;
import de.hdm.uls.loadtests.loadgenerator.load.model.InjectorResults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DecimalFormat;
import java.util.List;

/**
 * This class defines different load test scenario to test the different server technologies (java, nodejs) under terms
 * of load.
 *
 * @author Dennis Grewe [dg060@hdm-stuttgart.de] 03/16/2014
 */
public class RealisticLoadCase extends TestCase
{
    // ---------------------------------------
    // PROPERTIES
    // ---------------------------------------

    private static final Logger log                     = LoggerFactory.getLogger(RealisticLoadGenerator.class);

    private static final String NAME                    = "RealisticLoadTests";
    private static final int    TOTAL_CLIENTS           = 300;
    private static final int    TEST_DURATION_IN_MILLIS = 10 * 1000;

    private List<TestCase>      tests                   = null;
    private LoadGenerator       realisticLoadGenerator  = null;
    private DecimalFormat       df                      = new DecimalFormat("#.##");

    // ---------------------------------------
    // CONSTRUCTOR
    // ---------------------------------------

    public RealisticLoadCase()
    {
        super(RealisticLoadCase.NAME);
        log.info("-- TestCase: " + RealisticLoadCase.NAME);
        this.realisticLoadGenerator = new RealisticLoadGenerator(RealisticLoadCase.TOTAL_CLIENTS,
                RealisticLoadCase.TEST_DURATION_IN_MILLIS, new IncreasingClientsInjectionProfile());
    }

    // ---------------------------------------
    // START
    // ---------------------------------------

    @Override
    public void start()
    {
        this.realisticLoadGenerator.run();
        this.showResponseTimes();
        this.showThroughputHistory();
    }

    // ---------------------------------------
    // METHODS
    // ---------------------------------------

    public List<TestCase> getTests()
    {
        return this.tests;
    }

    /**
     * The method gathers all information about the response times and delegates the values to printing methods
     */
    private void showResponseTimes()
    {
        GeneratorResults results = this.realisticLoadGenerator.getGeneratorResults();
        double durationInSec = results.getTestDurationInSec();
        int totalNumberOfClients = results.getTotalClients();

        this.printResponseTime(totalNumberOfClients, durationInSec);

        for(InjectorResults result : results.getInjectorResults())
        {
            try
            {
                this.showInjectorResults(result);
            }
            catch (MeasurementException ex)
            {
                log.error("Error to read information of an injector result!", ex);
            }
        }
    }

    /**
     * The method prints the response times of all test results to stdout.
     */
    private void printResponseTime(int totalNumberOfClients, double durationInSec)
    {
        log.info("Load test overall execution time = " + df.format(durationInSec)
                + " sec (total amount of clients: " + totalNumberOfClients + ")");

        log.info("---------------------------");
        log.info("Response times");
        log.info("---------------------------");
    }

    /**
     * The method gathers all information about the injector results and delegates the values to printing methods.
     *
     * @param result The InjectorResult to get information about avg response time, etc.
     */
    private void showInjectorResults(InjectorResults result) throws MeasurementException
    {
        String injectionMethod = result.getInjectionMethod();
        int totalClients = result.getTotalClients();
        double injectorDurationInSec = result.getDurationInSec();
        double avgResponseTimeInMillis = result.getAverageResponseTimeInMs();

        log.info(injectionMethod + " - average response time: "
                + df.format(avgResponseTimeInMillis) + " ms (duration "
                + df.format(injectorDurationInSec) + " sec - "
                + totalClients + " clients)");
    }

    private void showThroughputHistory()
    {
        log.info("---------------------------");
        log.info("Throughput history");
        log.info("---------------------------");

        GeneratorResults results = this.realisticLoadGenerator.getGeneratorResults();
        List<GeneratorResults.Throughput> throughputHistory = results.getThroughputHistory();
        for(int i = 0;i < throughputHistory.size();i++)
        {
            GeneratorResults.Throughput throughput = throughputHistory.get(i);
            double timeProgress = throughput.getTimeProgress();
            int totalClients = throughput.getClientsCount();
            DecimalFormat ddf = new DecimalFormat("#.###");
            log.info(ddf.format(timeProgress) + " % -> " + totalClients + " clients");
        }
    }
}