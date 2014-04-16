package de.hdm.uls.loadtests.loadgenerator.load.generators;

import de.hdm.uls.loadtests.loadgenerator.client.SingleClient;
import de.hdm.uls.loadtests.loadgenerator.config.Config;
import de.hdm.uls.loadtests.loadgenerator.load.LoadGenerator;
import de.hdm.uls.loadtests.loadgenerator.load.LoadInjector;
import de.hdm.uls.loadtests.loadgenerator.load.injectionprofiles.InjectionProfile;
import de.hdm.uls.loadtests.loadgenerator.load.injectors.MultiplexLoadInjector;
import de.hdm.uls.loadtests.loadgenerator.load.model.GeneratorResults;
import de.hdm.uls.loadtests.loadgenerator.load.model.InjectorResults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * This class defines most realistic simulation flow. Some clients only requests data, some clients send data back to
 * the server, other accept parallel processing.
 *
 * @author Dennis Grewe [dg060@hdm-stuttgart.de] 03/16/2014
 */
public class RealisticLoadGenerator extends LoadGenerator
{
    // ---------------------------------------
    // PROPERTIES
    // ---------------------------------------

    private static final Logger log           = LoggerFactory.getLogger(RealisticLoadGenerator.class);

    private List<LoadInjector>  loadInjectors = new ArrayList<>();

    // ---------------------------------------
    // CONSTRUCTOR
    // ---------------------------------------

    public RealisticLoadGenerator(int totalClients, int durationInMillis, InjectionProfile profile)
    {
        super(totalClients, durationInMillis, profile);
        log.info("-- Generator: RealisticLoadGenerator - clients: " + totalClients + " durationInMillis: "
                + durationInMillis + " profile: " + profile.getClass().getSimpleName());
    }

    // ---------------------------------------
    // METHODS
    // ---------------------------------------

    @Override
    protected void injectLoad(int targetLoad, long timeLeftMs, GeneratorResults testResults)
    {
        int currentLoad = getCurrentLoad();
        int loadToInject = targetLoad - currentLoad;
        if (loadToInject > 0)
        {
            this.injectPayload(loadToInject, timeLeftMs, testResults);
        }
        else
        {
            // Do nothing and just wait for terminating injectors
        }
    }

    @Override
    protected void finishInjection()
    {
        for (LoadInjector injector : this.loadInjectors)
        {
            try
            {
                injector.join();
            }
            catch (InterruptedException ex)
            {
                log.error("Error to join the load injector thread!", ex);
            }
        }
    }

    @Override
    public int getCurrentClientsCount()
    {
        int currentLoad = 0;

        for (int i = 0; i < this.loadInjectors.size(); i++)
        {
            LoadInjector injector = this.loadInjectors.get(i);

            if (injector.isAlive())
            {
                for (int j = 0; j < injector.getMultiClient().getClients().size(); j++)
                {
                    if (injector.getMultiClient().getClients().get(j).isConnected())
                    {
                        currentLoad++;
                    }
                }
            }
        }

        return currentLoad;
    }

    private int getCurrentLoad()
    {
        int loadResult = 0;
        for (int i = 0; i < this.loadInjectors.size(); i++)
        {
            LoadInjector injector = this.loadInjectors.get(i);

            if (injector.isAlive())
            {
                loadResult += injector.getMultiClient().getClients().size();
            }
        }

        return loadResult;
    }

    private void injectPayload(int clientsToInject, long timeLeftMs, GeneratorResults testResults)
    {
        int clientIdRangeSteps = 0;

        for (int i = 0; i < clientsToInject; i++)
        {
            int numberOfMultiplexClients = ((clientsToInject % 10) < 1) ? 1 : (clientsToInject % 10);
            InjectorResults injectorResults = testResults.newInjectorResults();
            LoadInjector injector = new MultiplexLoadInjector(clientIdRangeSteps, numberOfMultiplexClients, timeLeftMs, injectorResults);
            this.loadInjectors.add(injector);
            injector.start();
            clientIdRangeSteps += Config.ID_RANGE_STEPS;
        }
    }
}