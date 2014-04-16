package de.hdm.uls.threadbasedserver.server;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

/**
 * This interface describes the functionality of a server implementation in java.
 *
 * Created by Dennis Grewe [dg060@hdm-stuttgart.de] 03/15/2014
 */
public interface Server
{
    /**
     * starts a new server instance. the port and the host information are declared in the ServerConfig class.
     *
     * @throws IOException, InterruptedException, ExecutionException if no socket can be bind to the port or be accepted.
     */
    public void start();

    /**
     * stops a running server instance.
     * @throws IOException
     */
    public void stop();
}