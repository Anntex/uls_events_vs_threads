package de.hdm.uls.loadtests.loadtests;

import de.hdm.uls.loadtests.environment.model.TestCase;
import de.hdm.uls.loadtests.loadgenerator.exceptions.MeasurementException;
import de.hdm.uls.loadtests.loadgenerator.load.LoadGenerator;
import de.hdm.uls.loadtests.loadgenerator.load.generators.RealisticLoadGenerator;
import de.hdm.uls.loadtests.loadgenerator.load.injectionprofiles.InjectionProfile;
import de.hdm.uls.loadtests.loadgenerator.load.model.GeneratorResults;
import de.hdm.uls.loadtests.loadgenerator.load.model.InjectorResults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DecimalFormat;
import java.util.List;

/**
 * This class defines a generic load test case to test different load test scenario. The generic load test case allows
 * to define different parameters like totalClients, testDurationInMs and so on.
 *
 * @author Dennis Grewe [dg060@hdm-stuttgart.de] 03/20/2014
 */
public class GenericLoadCase extends TestCase
{
    // ---------------------------------------
    // PROPERTIES
    // ---------------------------------------

    private static final Logger log                     = LoggerFactory.getLogger(RealisticLoadGenerator.class);

    private static final String NAME                    = "GenericLoadTests";

    private LoadGenerator loadGenerator;
    private InjectionProfile injectionProfile;
    private DecimalFormat decimalFormat = new DecimalFormat("#.##");

    // ---------------------------------------
    // CONSTRUCTOR
    // ---------------------------------------

    public GenericLoadCase(LoadGenerator loadGenerator, InjectionProfile injectionProfile) {
        super(GenericLoadCase.NAME);
        this.loadGenerator = loadGenerator;
        this.injectionProfile = injectionProfile;
    }

    // ---------------------------------------
    // METHODS
    // ---------------------------------------

    @Override
    public void start()
    {
        this.loadGenerator.run();
        showResponseTimes();
        showThroughputHistory();
    }

    public GeneratorResults getResults()
    {
        return this.loadGenerator.getGeneratorResults();
    }

    private void showResponseTimes() {
        GeneratorResults results = this.loadGenerator.getGeneratorResults();
        double durationInSecs = results.getTestDurationInSec();
        int totalNumOfClients = results.getTotalClients();
        double totalResponseAverageTime = 0.0d;
        double validCount = 0.0d;

        log.info("Load test overall execution time = " + this.decimalFormat.format(durationInSecs)
                + " sec (total amount of clients: " + totalNumOfClients + ")");

        log.info("---------------------------");
        log.info("Response times");
        log.info("---------------------------");
        for(int i = 0; i < results.getInjectorResults().size(); i++)
        {
            InjectorResults injectorResults = results.getInjectorResults().get(i);
            try
            {
                String injectionMethod = injectorResults.getInjectionMethod();
                int totalClients = injectorResults.getTotalClients();
                double injectorDurationInSecs = injectorResults.getDurationInSec();
                double avgResponseTimeInMs = injectorResults.getAverageResponseTimeInMs();
                totalResponseAverageTime += avgResponseTimeInMs;
                if (avgResponseTimeInMs > 0)
                {
                    validCount++;
                }
                log.info(injectionMethod + " " + i
                        + " - average response time: " + this.decimalFormat.format(avgResponseTimeInMs) + " ms"
                        + " ->  (duration " + this.decimalFormat.format(injectorDurationInSecs) + " sec - "
                        + totalClients + " clients)");
            }
            catch (MeasurementException ex)
            {
                log.error("Error to get measurement results!", ex);
            }
        }

        log.info("Injector results: " + results.getInjectorResults().size() + " - valid count: " + validCount
                + " - total average response time: " + this.decimalFormat.format(totalResponseAverageTime / validCount) + " ms (total: " + totalResponseAverageTime + ")\n\n");
    }

    private void showThroughputHistory() {
        log.info("---------------------------");
        log.info("Throughput history");
        log.info("---------------------------");
        GeneratorResults results = loadGenerator.getGeneratorResults();
        List<GeneratorResults.Throughput> throughputHistory = results.getThroughputHistory();
        for(int i = 0;i < throughputHistory.size();i++)
        {
            GeneratorResults.Throughput throughput = throughputHistory.get(i);
            double timeProgress = throughput.getTimeProgress();
            int totalClients = throughput.getClientsCount();
            DecimalFormat df = new DecimalFormat("#.###");
            log.info(df.format(timeProgress) + " % -> " + totalClients + " clients");
        }
    }
}