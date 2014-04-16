package de.hdm.uls.threadbasedserver.client;

/**
 * This interface describes the functions of a server side client.
 *
 * Created by Dennis Grewe [dg060@hdm-stuttgart.de] 01/29/2014
 */
public interface Client
{
    /**
     * This method is for sending data to the client read from disk.
     * @return TRUE if the sending operation was successful, otherwise FALSE.
     */
    public boolean send();

    /**
     * This method is for receiving data from the client.
     * @return returns the number of received bytes of the buffer
     */
    public int receive();

    /**
     * This method cleans up all used streams and close the connection to the client.
     */
    public void cleanUp();
}