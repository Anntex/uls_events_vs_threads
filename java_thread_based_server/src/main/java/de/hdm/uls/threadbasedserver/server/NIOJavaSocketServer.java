package de.hdm.uls.threadbasedserver.server;

import de.hdm.uls.threadbasedserver.config.ServerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;

/**
 * This class is an implementation of a thread driven socket server based on the java.nio library. Every connection will be
 * dispatched to a new thread to demonstrate a classic thread based socket server architecture. <br/>
 * The server uses the NIO library of Java 7 release where non-blocking I/O is implemented.
 *
 * Created by Dennis Grewe [dg060@hdm-stuttgart.de] 03/15/2014
 */
public class NIOJavaSocketServer implements Server
{
    // ---------------------------------------
    // PROPERTIES
    // ---------------------------------------

    private static final Logger              log                 = LoggerFactory.getLogger(NIOJavaSocketServer.class);

    private final        String              delimiter           = ServerConfig.DELIMITER;

    private              ServerSocketChannel serverSocketChannel = null;
    private              Selector            selector            = null;

    // ---------------------------------------
    // CONSTRUCTOR
    // ---------------------------------------

    public NIOJavaSocketServer()
    {
        try
        {
            this.setUp();
        }
        catch (IOException ex)
        {
            log.error("An error occurred while setting up the server environment!", ex);
            System.exit(-1);
        }
    }

    // ---------------------------------------
    // METHODS
    // ---------------------------------------

    @Override
    public void start()
    {
        log.info("server running on: " + ServerConfig.SERVER_HOST + ":" + ServerConfig.SERVER_PORT);

        /* the select method is a blocking method. the method returns a result if a client connects to
         * one of the registered channels. It is also possible to perform a non-blocking call
         * using the selectNow() function or specify a blocking timeout using select(long timeout). */
        while(!Thread.currentThread().isInterrupted())
        {
            try
            {
                /* the select method is a blocking method. the method returns a result if a client connects to
                 * one of the registered channels. It is also possible to perform a non-blocking call
                 * using the selectNow() function or specify a blocking timeout using select(long timeout). */

                if (this.selector.select() == 0)
                {
                    continue;
                }

                // iterate over all clients connections in the selector list
                Iterator<SelectionKey> keys = this.selector.selectedKeys().iterator();

                while(keys.hasNext())
                {
                    /* get the next operation by selector key and remove it from the list so that we do not process
                     * this operation again */
                    SelectionKey key = keys.next();
                    keys.remove();

                    /* check if the key is already valid; an example for an invalid key: a client closed the connection
                     * before the server processed the operation */
                    if (!key.isValid())
                    {
                        // start a new iteration in while loop
                        continue;
                    }

                    // ---  CHECK OPERATION TYPES --- //

                    /* start listening to the OP_ACCEPT when we register with the Selector.
                     * If the key from the key set is Acceptable, then we must get ready to accept the client
                     * connection and do something with it. */
                    if (key.isAcceptable())
                    {
                        // process accept operation
                        this.acceptOperation(key);
                    }

                    /* if an client is already connected to the server and the key is CONNECT so this happened
                     * intentional to signal the server to tear down the connection to the client. */
                    if (key.isConnectable())
                    {
                        // process connect operation
                        this.connectOperation(key);
                    }

                    /* if a connection is already processed and the OP_WRITE flag is set the server is ready to write
                     * data to the client using a write channel. */
                    if (key.isWritable())
                    {
                        // process write operation
                        this.writeOperation(key);
                    }
                    /* if a connection is already processed and the OP_READ flag is set the server is ready to write
                     * data to the client using a write channel. */
                    if (key.isReadable())
                    {
                        // process read operation
                        this.readOperation(key);
                    }
                }
            }
            catch (IOException ex)
            {
                log.error("An error occurred while selecting a connection!", ex);
            }
        }

        this.stop();
    }

    @Override
    public void stop()
    {
        if (this.serverSocketChannel != null && this.selector != null)
        {
            try
            {
                this.serverSocketChannel.close();
                this.selector.close();

                this.selector = null;
                this.serverSocketChannel = null;
            }
            catch (IOException ex)
            {
                log.error("An error occurred while closing the server socket channel and selector instance.", ex);
            }
        }
    }

    /**
     * This method set up a new server instance before the server can start to work.
     * The method throws an exception if setting up the server environment failed.
     */
    private void setUp() throws IOException
    {
        log.info("initialize server");
        /* open a new server socket channel and bind a listener to a certain ip address and port configured in
         * ServerConfig. Define the behavior of the channel as non-blocking.*/
        this.serverSocketChannel = ServerSocketChannel.open();
        this.serverSocketChannel.bind(new InetSocketAddress(ServerConfig.SERVER_HOST, ServerConfig.SERVER_PORT));
        this.serverSocketChannel.configureBlocking(false);

        /* open a new selector instance and register the serverSocketChannel. The selector will be used to multiplex
         * incoming connections. The OP_ACCEPT option marks the selection key as ready when the channel accepts a
         * new connection. A selector must be understood as a buffer instance for incoming requests. The server instance
         * can iterate over all new client requests and can perform them. */
        this.selector = Selector.open();
        this.serverSocketChannel.register(this.selector, SelectionKey.OP_ACCEPT);
    }

    /**
     * This method accepting an incoming connection and instantiate a server socket channel. After accepting a connection
     * the method sets the selector flag to listen to read operations.
     *
     * @param key The SelectionKey of the selector. Like an ID to identify the client who wants to connect.
     */
    private void acceptOperation(SelectionKey key)
    {
        try
        {
            ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
            SocketChannel socketChannel = serverSocketChannel.accept();
            socketChannel.configureBlocking(false);

            socketChannel.register(this.selector, SelectionKey.OP_READ);
        }
        catch (IOException ex)
        {
            log.error("An error occurred while accepting an incoming client connection request!", ex);
        }
    }

    /**
     * This method finish an existing connection to the client if the connection is still pending.
     *
     * @param key The SelectionKey of the selector. Like an ID to identify the client who wants to connect.
     */
    private void connectOperation(SelectionKey key)
    {
        SocketChannel channel = (SocketChannel) key.channel();

        if (channel.isConnectionPending())
        {
            try
            {
                channel.finishConnect();
            }
            catch (IOException ex)
            {
                log.error("An error occurred while finishing the connection to the client!", ex);
            }
        }
    }

    /**
     * This method writes data to a client if a connection already exists and the channel is registered in the selector.
     *
     * @param key The SelectionKey of the selector. Like an ID to identify the client to send data.
     * @throws java.net.SocketException if no data is available to send
     */
    private void writeOperation(SelectionKey key) throws SocketException
    {
        // get the channel, collect all data from disk
        SocketChannel channel = (SocketChannel) key.channel();
        byte[] data = this.collectSendingBytes();

        if (data != null)
        {
            try
            {
                // write the data to the client
                channel.write(ByteBuffer.wrap(data));

                /* In case of the testing scenario set the key to CONNECT to close the connection after writing data
                 * to the client. You can also assume the next operation as a read or write operation, so you can set
                 * key.interestOps(SelectionKey.OP_READ) */
                key.interestOps(SelectionKey.OP_CONNECT);
             }
            catch (IOException ex)
            {
                log.error("An error occurred while sending data to the client!", ex);
            }
        }
        else
        {
            throw new SocketException("No data to send! An error occurred while reading the data from disk!");
        }
    }

    /**
     * This method read data from a client if a connection already exists and the channel is registered in the selector.
     *
     * @param key he SelectionKey of the selector. Like an ID to identify the client to read data from.
     * @throws java.io.IOException if closing the channel failed during an error
     */
    private void readOperation(SelectionKey key) throws IOException
    {
        SocketChannel channel = (SocketChannel) key.channel();

        try
        {
            // allocate byte buffer to read bytes from channel
            int byteLength = (int) Math.pow(2,20);
            ByteBuffer readBuffer = ByteBuffer.allocate(byteLength);
            readBuffer.clear();

            int readBytes = channel.read(readBuffer);

            if (this.parseInput(readBuffer))
            {
                // next reasonable operation will be a write operation
                key.interestOps(SelectionKey.OP_WRITE);
            }
            else if (readBytes == - 1)
            {
                log.info("Nothing to read from socket channel!");
                // close connection and remove key
                channel.close();
                key.cancel();
            }

            readBuffer.flip();
        }
        catch (IOException ex)
        {
            log.error("An error occurred while reading data from socket channel! Close connection!", ex);
            key.cancel();
            channel.close();
        }
    }

    /**
     * This method reads the sending data from disk and returns that as a byte[] object.
     *
     * @return A byte[] object which contains the binary data to send to the server.
     */
    private byte[] collectSendingBytes()
    {
        byte[] data = null;
        try
        {
            Path file = Paths.get(NIOJavaSocketServer.class.getResource(ServerConfig.FILE_PATH).toURI());
            data = Files.readAllBytes(file);
        }
        catch (IOException e)
        {
            log.error("Error to read all bytes of the file to send!", e);
        }
        catch (URISyntaxException e)
        {
            log.error("Error to access the file to send!", e);
        }

        return data;
    }

    /**
     * This method parses the input of an input stream and checks the input for a certain kind of delimiter signs.
     * If the delimiter signs are detected the method returns TRUE, otherwise FALSE,
     *
     * @param bufferedData The ByteBuffer object containing the binary data of the input channel.
     * @return TRUE if the delimiter signs were detected, otherwise FALSE.
     */
    private boolean parseInput(ByteBuffer bufferedData)
    {
        boolean delimiterDetected = false;

        if (bufferedData.hasArray())
        {
            String dataToCompare = new String(bufferedData.array(), StandardCharsets.UTF_8);

            if (dataToCompare.contains(this.delimiter))
            {
                delimiterDetected = true;
            }
        }

        return delimiterDetected;
    }

    // ---------------------------------------
    // MAIN
    // ---------------------------------------

    /**
     * Main method of the server implementation.
     * @param args
     */
    public static void main(String[] args)
    {
        Server server = new NIOJavaSocketServer();
        server.start();
    }
}