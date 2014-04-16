package de.hdm.uls.loadtests.loadgenerator.client;

import de.hdm.uls.loadtests.loadgenerator.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.channels.ConnectionPendingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * A MultiClient represents a set of SingleClients (maximum number of clients configured in Config.MAX_SINGLE_CLIENTS).
 * All clients perform the same actions when a specific method is performed. Each method represents a sequence
 * of actions. You can think of it as a traversal through a communication path in the server's protocol state machine.
 *
 * @author Dennis Grewe [dg060@hdm-stuttgart.de] 02/09/2014
 */
public class MultiClient
{
    // ---------------------------------------
    // PROPERTIES
    // ---------------------------------------

    private static final Logger             log             = LoggerFactory.getLogger(MultiClient.class);

    private              List<SingleClient> clients         = new ArrayList<>();
    private              InetSocketAddress  serverAddress   = null;

    // ---------------------------------------
    // CONSTRUCTOR
    // ---------------------------------------

    public MultiClient(int numberOfClient)
    {
        // create a new address for connection
        this.serverAddress = new InetSocketAddress(
                Config.SERVER_HOST, Config.SERVER_PORT);
        this.createClients(numberOfClient);
    }

    public MultiClient(List<SingleClient> clients)
    {
        // create a new address for connection
        this.serverAddress = new InetSocketAddress(
                Config.SERVER_HOST, Config.SERVER_PORT);
        this.clients.addAll(clients);
    }

    // ---------------------------------------
    // METHODS
    // ---------------------------------------

    public void simulateReceiveServerCommunication(long injectionDurationMillis)
    {
        for (SingleClient client : this.clients)
        {
            try
            {
                client.connect(this.serverAddress);
                client.receiveData();
                Thread.sleep(injectionDurationMillis);
                client.disconnect();
            }
            catch (InterruptedException ex)
            {
                log.error("Error to wait injection duration millis after connecting a single client to the server!", ex);
            }
            catch (IOException ex)
            {
                // comment this line out if to much console messages slow down the server --> If this exceptions was thrown, the client couldn´t
                // connected to the busy server
                log.error("Error to connect a client to the server!", ex);
            }
        }
    }

    public void simulateSendReceiveServerCommunication(long injectionDurationMillis)
    {
        ByteBuffer buffer = this.collectSendingBytes();

        if (buffer != null)
        {
            for (SingleClient client : this.clients)
            {
                try
                {
                    client.connect(this.serverAddress);
                    client.sendData(buffer);
                    client.sendDelimiter();
                    client.receiveData();
                    Thread.sleep(injectionDurationMillis);
                    client.disconnect();
                }
                catch (InterruptedException ex)
                {
                    log.error("Error to wait injection duration millis after connecting a single client to the server!", ex);
                }
                catch (IOException ex)
                {
                    // comment this line out if to much console messages slow down the server --> If this exceptions was thrown, the client couldn´t
                    // connected to the busy server
                    log.error("Error to connect a client to the server!", ex);
                }
            }
        }
        else
        {
            log.error("Error while reading the file from file system. The buffer is NULL!");
            throw new ConnectionPendingException();
        }
    }

    public void simulateSendServerCommunication(long injectionDurationMillis) throws ConnectionPendingException
    {
        ByteBuffer buffer = this.collectSendingBytes();

        if (buffer != null)
        {
            for (SingleClient client : this.clients)
            {
                try
                {
                    client.connect(this.serverAddress);
                    client.sendData(buffer);
                    Thread.sleep(injectionDurationMillis);
                    client.disconnect();
                }
                catch (InterruptedException ex)
                {
                    log.error("Error to wait injection duration millis after connecting a single client to the server!", ex);
                }
                catch (IOException ex)
                {
                    // comment this line out if to much console messages slow down the server --> If this exceptions was thrown, the client couldn´t
                    // connected to the busy server
                    log.error("Error to connect a client to the server!", ex);
                }
            }
        }
        else
        {
            log.error("Error while reading the file from file system. The buffer is NULL!");
            throw new ConnectionPendingException();
        }
    }

    public void simulateConnection(long injectionDurationMillis)
    {
        for (SingleClient client : this.clients)
        {
            try
            {
                client.connect(this.serverAddress);
                Thread.sleep(injectionDurationMillis);
                client.disconnect();
            }
            catch (IOException ex)
            {
                // comment this line out if to much console messages slow down the server --> If this exceptions was thrown, the client couldn´t
                // connected to the busy server
                log.error("Error to connect a client to the server!", ex);
            }
            catch (InterruptedException ex)
            {
                log.error("Error to wait injection duration millis after connecting a single client to the server!", ex);
            }
        }
    }

    /**
     * The method creates a number of clients
     *
     * @param numberOfClients to create
     */
    private void createClients(int numberOfClients)
    {
        for (int i = 0; i < numberOfClients; i++)
        {
            this.clients.add(new SingleClient(i+1));
        }
    }

    /**
     * This method reads the sending data from disk and returns that as a byteBuffer object.
     *
     * @return A ByteBuffer object which contains the binary data to send to the server.
     */
    private ByteBuffer collectSendingBytes()
    {
        ByteBuffer buffer = null;
        try
        {
            Path file = Paths.get(MultiClient.class.getResource(Config.FILE_PATH).toURI());
            byte[] fileBytes = Files.readAllBytes(file);
            buffer = ByteBuffer.wrap(fileBytes);
        }
        catch (IOException e)
        {
            log.error("Error to read all bytes of the file to send!", e);
        }
        catch (URISyntaxException e)
        {
            log.error("Error to access the file to send!", e);
        }

        return buffer;
    }

    /**
     * @return set of all SingleClients
     */
    public List<SingleClient> getClients()
    {
        return this.clients;
    }
}