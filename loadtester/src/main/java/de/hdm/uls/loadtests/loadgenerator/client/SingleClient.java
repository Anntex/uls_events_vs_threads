package de.hdm.uls.loadtests.loadgenerator.client;

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import de.hdm.uls.loadtests.LoadTester;
import de.hdm.uls.loadtests.environment.Environment;
import de.hdm.uls.loadtests.loadgenerator.config.Config;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class describes a simple client instance for testing load on a server. A
 * client is the smallest component the load testing chain and is able to send
 * and receive actions to/from the server.
 *
 * @author Dennis Grewe [dg060@hdm-stuttgart.de] 02/04/2014
 */
public class SingleClient implements Client
{
    // ---------------------------------------
    // PROPERTIES
    // ---------------------------------------

    private static final Logger log         = LoggerFactory.getLogger(SingleClient.class);

    protected            Socket socket      = null;
    private              long   clientID;

    // ---------------------------------------
    // CONSTRUCTOR
    // ---------------------------------------

    public SingleClient(long id)
    {
        this.clientID = id;
    }

    // ---------------------------------------
    // METHODS
    // ---------------------------------------

    @Override
    public void connect(InetSocketAddress serverAddress) throws IOException
    {
        if (this.socket == null)
        {
            socket = new Socket(Config.SERVER_HOST, Config.SERVER_PORT);
            this.socket.setSoTimeout(Config.SOCKET_TIME_OUT);
        }
    }

    @Override
    public void disconnect() throws IOException
    {
        if (this.socket != null)
        {
            this.socket.close();
            this.socket = null;
        }
    }

    @Override
    public boolean isConnected()
    {
        boolean isConnected = false;
        if (this.socket != null)
        {
            isConnected = this.socket.isConnected();
        }

        return isConnected;
    }

    @Override
    public boolean sendData(ByteBuffer buffer)
    {
        boolean successfulWrite = false;
        if (this.isConnected())
        {
            try
            {
                this.socket.getOutputStream().write(buffer.array());
                successfulWrite = true;
            }
            catch (IOException ex)
            {
                log.error("Try to write data to the server but an error occurred!", ex);
            }
        }

        return successfulWrite;
    }

    @Override
    public boolean receiveData()
    {
        boolean successfulRead = false;
        if(this.isConnected())
        {
            try
            {
                // append \n to the delimiter to perform readline method on server side
                String delimiterStatement = Config.DELIMITER + "\n";
                this.socket.getOutputStream().write(delimiterStatement.getBytes());
                this.socket.getInputStream().read();
                DataInputStream inputStream = new DataInputStream(this.socket.getInputStream());
                this.parseInput(new BufferedReader(new InputStreamReader(inputStream)));
            }
            catch (IOException e)
            {
                log.error("Error to read bytes from server!", e);
            }
        }

        return successfulRead;
    }

    @Override
    public boolean sendDelimiter()
    {
        boolean successful = false;

        if (this.isConnected())
        {
            try
            {
                this.socket.getOutputStream().write(Config.DELIMITER.getBytes());
                successful = true;
            }
            catch (IOException ex)
            {
                log.error("Try to write delimiter data to the server but an error occurred!", ex);
            }
        }

        return successful;
    }

    @Override
    public void biDirAsyncSendAndReceiveData(InetSocketAddress address, ByteBuffer writeBuffer)
    {
        try
        {
            AsynchronousSocketChannel socketChannel = AsynchronousSocketChannel.open();
            socketChannel.connect(address, "bidirectional asynchronous client sending and receiving data!", new CompletionHandler<Void, String>()
            {
                @Override
                public void completed(Void result, String attachment)
                {
                    socketChannel.write(writeBuffer, "async write operation", new CompletionHandler<Integer, String>()
                    {
                        @Override
                        public void completed(Integer result, String attachment)
                        {
                            // We do nothing after sending the data, just be happy
                        }

                        @Override
                        public void failed(Throwable exc, String attachment)
                        {
                            log.error("Error to send data to the server!", exc);
                        }
                    });

                    int bufferSize = (int) Math.pow(2, 22);
                    ByteBuffer readBuffer = ByteBuffer.allocate(bufferSize);
                    socketChannel.read(readBuffer, "async read operation", new CompletionHandler<Integer, String>()
                    {
                        @Override
                        public void completed(Integer result, String attachment)
                        {
                            // We do nothing with the read data, just smile to the person next tu us
                            log.info("Successful read!");
                        }

                        @Override
                        public void failed(Throwable exc, String attachment)
                        {
                            log.error("Error to read data from the server!", exc);
                        }
                    });
                }

                @Override
                public void failed(Throwable exc, String attachment)
                {
                    log.error("Error while connecting the client to the server!", exc);
                }
            });
        }
        catch (IOException ex)
        {
            log.error("Error to connect a client to the server!", ex);
        }
    }

    protected void parseInput(BufferedReader reader)
    {
        String line = null;
        int i = 0;
        try
        {
            // skips the half of all reader buffers for faster processing
            reader.skip((reader.lines().toArray().length / 2));
            while ((line = reader.readLine()) != null)
            {
                // Do nothing because the data is not important just read the lines of the buffer
                // after that skip the method. In this case it is important to measure the I/O time of the server
                // not the time the load testing framework need to parse the input send by a server. so keep the connection
                // alive until the server sends all bytes
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public long getClientID()
    {
        return this.clientID;
    }
}