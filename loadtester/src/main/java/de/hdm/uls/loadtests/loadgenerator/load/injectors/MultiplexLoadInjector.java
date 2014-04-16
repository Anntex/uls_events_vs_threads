package de.hdm.uls.loadtests.loadgenerator.load.injectors;

import de.hdm.uls.loadtests.loadgenerator.client.MultiClient;
import de.hdm.uls.loadtests.loadgenerator.config.Config;
import de.hdm.uls.loadtests.loadgenerator.load.LoadInjector;
import de.hdm.uls.loadtests.loadgenerator.load.model.InjectorResults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class defines special behavior of some clients so simulate a bidirectional asynchronous communication between
 * a server and at least one client. So this class injects clients so simulate sending and receiving data asynchronously
 * from the server and the client using a AsynchronousSocketChannel.
 *
 * @author Dennis Grewe [dg060@hdm-stuttgart.de] 03/16/2014
 */
public class MultiplexLoadInjector extends LoadInjector
{
    // ---------------------------------------
    // PROPERTIES
    // ---------------------------------------

    private static final Logger log = LoggerFactory.getLogger(LoadInjector.class);

    // ---------------------------------------
    // CONSTRUCTOR
    // ---------------------------------------

    public MultiplexLoadInjector(int clientIdRange, int totalClients, long injectionDurationMillis, InjectorResults results)
    {
        super(clientIdRange, totalClients, injectionDurationMillis, results);
    }

    // ---------------------------------------
    // METHODS
    // ---------------------------------------

    @Override
    protected void inject()
    {
        MultiClient multiplexClients = getMultiClient();

        if (Config.MeasuringScenarios.CONNECTION.equals(Config.MEASURING_TYPE))
        {
            multiplexClients.simulateConnection(this.getInjectionDurationMillis());
        }
        else if (Config.MeasuringScenarios.SEND.equals(Config.MEASURING_TYPE))
        {
            multiplexClients.simulateSendServerCommunication(this.getInjectionDurationMillis());
        }
        else if (Config.MeasuringScenarios.RECEIVE.equals(Config.MEASURING_TYPE))
        {
            multiplexClients.simulateReceiveServerCommunication(this.getInjectionDurationMillis());
        }
        else if (Config.MeasuringScenarios.SEND_RECEIVE.equals(Config.MEASURING_TYPE))
        {
            multiplexClients.simulateSendReceiveServerCommunication(this.getInjectionDurationMillis());
        }
        else
        {
            // shut down process
            log.info("Shut down system because the given measuring type is not supported!");
            System.exit(-1);
        }
    }
}