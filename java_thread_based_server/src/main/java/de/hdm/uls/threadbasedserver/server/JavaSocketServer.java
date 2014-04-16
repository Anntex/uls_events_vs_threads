package de.hdm.uls.threadbasedserver.server;

import de.hdm.uls.threadbasedserver.client.SocketClient;
import de.hdm.uls.threadbasedserver.config.ServerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * This class defines a threading socket server implementation based on the java.net socket library.
 * Every incoming socket connection will be dispatched into a client service thread.
 * So this server is a simple and classic java socket server implementation.
 *
 * Created by Dennis Grewe [dg060@hdm-stuttgart.de] 03/15/2014
 */
public class JavaSocketServer implements Server
{
    // ---------------------------------------
    // PROPERTIES
    // ---------------------------------------

    private static final Logger log = LoggerFactory.getLogger(Server.class);

    private ServerSocket server     = null;
    private int          clientId  = 0;

    // ---------------------------------------
    // METHODS
    // ---------------------------------------

    @Override
    public void start()
    {
        if (this.server == null)
        {
            try
            {
                this.server = new ServerSocket();
                this.server.bind(new InetSocketAddress(ServerConfig.SERVER_HOST, ServerConfig.SERVER_PORT));
                log.info(this.getClass().getSimpleName() + " running on " + ServerConfig.SERVER_HOST + ":" + ServerConfig.SERVER_PORT);

                while (true)
                {
                    Socket socket = this.server.accept();

                    SocketClient client = new SocketClient(clientId, socket);
                    client.start();
                    this.clientId++;
                }
            }
            catch (IOException ex)
            {
                log.error("Error to create a new server instance!", ex);
            }
        }
        else
        {
            log.info("Server already started!");
        }
    }

    @Override
    public void stop()
    {
        if (this.server != null)
        {
            try
            {
                this.server.close();
                this.server = null;
            }
            catch (IOException e)
            {
                log.error("An error occurred while stopping the server!", e);
            }
        }
    }


    // ---------------------------------------
    // MAIN
    // ---------------------------------------

    /**
     * main method of the server.
     *
     * @param args runtime arguments
     */
    public static void main(String[] args) throws Exception
    {
        JavaSocketServer server = new JavaSocketServer();
        server.start();
    }
}