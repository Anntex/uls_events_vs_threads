package de.hdm.uls.loadtests.loadgenerator.client;

import java.io.IOException;
import java.net.ServerSocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.hdm.uls.loadtests.loadgenerator.config.Config;

/**
 * This class defines an abstract class for testing sockets between a server and
 * a client.
 *
 * @author Dennis Grewe [dg060@hdm-stuttgart.de] 02/04/2014
 *
 */
public abstract class BaseClientTest
{
    // ---------------------------------------
    // PROPERTIES
    // ---------------------------------------

    private static final Logger log = LoggerFactory
            .getLogger(BaseClientTest.class);

    private Thread serverThread = null;

    // ---------------------------------------
    // METHODS
    // ---------------------------------------

    /**
     * This method starts a new server instance in a new thread. The server
     * accepts connections on the port specified by the {@link Config} class.
     */
    protected void initServer()
    {
        this.serverThread = new Thread() {

            @Override
            public void run()
            {
                ServerSocket server;
                try
                {
                    server = new ServerSocket(Config.SERVER_PORT);
                    server.accept();
                }
                catch (IOException ex)
                {
                    log.error("Error starting a new test server instance!", ex);
                }
            }
        };
        this.serverThread.start();
    }

    /**
     * This method stops a running server thread.
     */
    protected void stopServer()
    {
        if (this.serverThread != null)
        {
            this.serverThread.interrupt();
            this.serverThread = null;
        }
    }
}