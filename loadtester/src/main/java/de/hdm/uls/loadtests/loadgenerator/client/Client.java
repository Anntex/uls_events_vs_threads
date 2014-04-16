package de.hdm.uls.loadtests.loadgenerator.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;

/**
 * This interface describes the functionality of a simple socket client for load
 * testing purposes.
 *
 * @author Dennis Grewe [dg060@hdm-stuttgart.de] 02/04/2014
 */
public interface Client
{
    /**
     * This method connects a client to the server.
     * @param address The socket address object to connect to the server.
     * @throws IOException
     */
    public void connect(InetSocketAddress address) throws IOException;

    /**
     * The method disconnects a client from the server.
     *
     * @throws IOException
     */
    public void disconnect() throws IOException;

    /**
     * The method checks if a client is still connected to the server.
     *
     * @return TRUE if the client is still connected, otherwise FALSE
     */
    public boolean isConnected();

    /**
     * The method sends certain binary data to the server.
     *
     * @param buffer ByteBuffer containing the bytes to send to the server.
     * @return TRUE if the transmitting of data was successful, otherwise FALSE
     */
    public boolean sendData(ByteBuffer buffer);

    /**
     * The method sends a certain delimiter binary data to the server to indicate to be ready for receiving data.
     *
     * @return TRUE if the transmitting of data was successful, otherwise FALSE
     */
    public boolean sendDelimiter();

    /**
     * The method receives data from the server.
     *
     * @return TRUE if the client receives data from the server in a certain period of time, otherwise FALSE
     */
    public boolean receiveData();

    /**
     * The method sends and receives data from and to a server simultaneously using asynchronous operations.
     * The method blocks the client thread until both operations are terminated.
     *
     * @param address The address to connect the client to.
     * @param writeBuffer The byte buffer contains the data which will be send to the server.
     */
    public void biDirAsyncSendAndReceiveData(InetSocketAddress address, ByteBuffer writeBuffer);
}