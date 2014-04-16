package de.hdm.uls.loadtests.loadgenerator.load;

import de.hdm.uls.loadtests.loadgenerator.client.MeasurementClient;
import de.hdm.uls.loadtests.loadgenerator.client.MultiClient;
import de.hdm.uls.loadtests.loadgenerator.client.SingleClient;
import de.hdm.uls.loadtests.loadgenerator.load.model.InjectorResults;

import java.util.ArrayList;
import java.util.List;

/**
 * This class describes a load injector. A load injector uses a MultiClient instance to
 * inject a server some load for a specific duration. At the same time the
 * injector collects some profiling data for subsequent analysis.
 *
 * @author Dennis Grewe [dg060@hdm-stuttgart.de] 03/16/2014
 */
public abstract class LoadInjector extends Thread
{
    // ---------------------------------------
    // PROPERTIES
    // ---------------------------------------

    private MultiClient         multiClient             = null;
    private long                clientIdRange           = 0;
    private long                injectionDurationMillis = 0;
    private MeasurementClient   measurementClient       = null;
    private InjectorResults     injectorResults         = null;

    // ---------------------------------------
    // CONSTRUCTOR
    // ---------------------------------------

    public LoadInjector(long clientIdRange, int totalClients, long injectionDurationMillis, InjectorResults results)
    {
        this.clientIdRange = clientIdRange;
        this.prepareInjection(totalClients, injectionDurationMillis, results);
    }

    // ---------------------------------------
    // RUN
    // ---------------------------------------

    @Override
    public final void run()
    {
        this.injectorResults.measureStartTime();
        this.measurementClient.startMeasuring();
        this.inject();
        this.measurementClient.stopMeasuring();
        this.injectorResults.measureStopTime();
    }

    // ---------------------------------------
    // METHODS
    // ---------------------------------------

    /**
     * The method set up a certain injector.
     *
     * @param totalClients Number of clients to inject
     * @param injectionDurationMillis millis between the client injection
     * @param results InjectorResults model to store the measurement results
     */
    private void prepareInjection(int totalClients, long injectionDurationMillis, InjectorResults results)
    {
        this.injectorResults = results;
        this.injectorResults.setInjectionMethod(this.getClass().getSimpleName());
        this.injectorResults.setTotalClients(totalClients);
        this.setInjectionDurationMillis(injectionDurationMillis);
        this.createClients(totalClients);
    }

    private void createClients(int totalClients)
    {
        long id = clientIdRange;
        List<SingleClient> clients = new ArrayList<>();
        for (int i = 0; i < totalClients - 1; i++)
        {
            clients.add(new SingleClient(id++));
        }
        this.measurementClient = new MeasurementClient(id++, this.injectorResults);
        clients.add(this.measurementClient);
        this.multiClient = new MultiClient(clients);
    }

    protected void setInjectionDurationMillis(long injectionDurationMillis)
    {
        this.injectionDurationMillis = injectionDurationMillis;
    }

    public MultiClient getMultiClient() { return this.multiClient; }

    protected long getInjectionDurationMillis() { return this.injectionDurationMillis; }

    protected abstract void inject();
}