package de.hdm.uls.threadbasedserver.client;

import de.hdm.uls.threadbasedserver.config.ServerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.Socket;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * This class defines a simple socket client. Each socket clients runs in a own thread to simulate a threaded server
 * architecture where all incoming connections are dispatched into a single thread. Also the client uses the old
 * blocking java socket library.
 *
 * Created by Dennis Grewe [dg060@hdm-stuttgart.de] 01/29/2014
 */
public class SocketClient extends Thread implements Client
{
    // ---------------------------------------
    // PROPERTIES
    // ---------------------------------------

    private static final Logger log                 = LoggerFactory.getLogger(SocketClient.class);

    private long                id;
    private Socket              socket              = null;
    private ClientState         clientState         = null;

    private DataInputStream     inputStream         = null;
    private DataOutputStream    outputStream        = null;

    private boolean             delimiterDetected   = false;
    private final String        delimiter           = ServerConfig.DELIMITER;

    // ---------------------------------------
    // ENUM CLIENTSTATE
    // ---------------------------------------

    enum ClientState
    {
        /**
         * The client thread is active and ready to send or receive data.
         */
        ACTIVE,
        /**
         * The client thread is ready for clean up.
         */
        IN_ACTIVE
    }

    // ---------------------------------------
    // CONSTRUCTOR
    // ---------------------------------------

    public SocketClient(long id, Socket socket)
    {
        try
        {
            this.id = id;
            this.socket = socket;
            this.inputStream = new DataInputStream(socket.getInputStream());
            this.outputStream = new DataOutputStream(socket.getOutputStream());
        }
        catch (IOException ex)
        {
            log.error("Error to get input / output stream of the socket connection for a client!", ex);
        }

        this.clientState = ClientState.ACTIVE;
    }

    @Override
    public void run()
    {
        while(ClientState.ACTIVE.equals(this.clientState))
        {
            int readBytes = this.receive();

            if (this.delimiterDetected)
            {
                // end of input stream reached or delimiter detected -> start to send data back to the client
                this.send();
                this.clientState = ClientState.IN_ACTIVE;
            }
            else if (readBytes == -1)
            {
                // connection was closed by the client and the read method returns -1 from the input stream
                break;
            }
        }

        this.cleanUp();
    }

    // ---------------------------------------
    // METHODS
    // ---------------------------------------

    @Override
    public boolean send()
    {
        boolean successfulSend = false;

        if (this.socket != null && this.socket.isConnected())
        {
            ByteBuffer bufferToWrite = this.collectSendingBytes();
            try
            {
                this.outputStream.write(bufferToWrite.array());
                successfulSend = true;
            }
            catch (IOException ex)
            {
                log.error("An error occurred while sending bytes to the client! SocketId: " + this.id, ex);
            }
        }

        return successfulSend;
    }

    @Override
    public int receive()
    {
        int readBytes = -1;

        if (this.socket != null && this.socket.isConnected())
        {
            try
            {
                readBytes = this.inputStream.read();
                this.delimiterDetected = this.parseInput(new BufferedReader(new InputStreamReader(this.inputStream)));
            }
            catch (IOException ex)
            {
                log.error("Error to read bytes from the input stream!", ex);
            }
        }

        return readBytes;
    }

    @Override
    public void cleanUp()
    {
        if (this.socket != null)
        {
            try
            {
                this.inputStream.close();
                this.outputStream.close();
                this.socket.close();
            }
            catch (IOException ex)
            {
                log.error("Error while closing the socket connection of client: id -" + this.id, ex);
            }
        }
    }

    /**
     * This method parses the input of an input stream and checks the input for a certain kind of delimiter signs.
     * If the delimiter signs are detected the method returns TRUE, otherwise FALSE,
     *
     * @param reader The buffered reader object containing the input characters of the socket input stream.
     * @return TRUE if the delimiter signs were detected, otherwise FALSE.
     */
    private boolean parseInput(BufferedReader reader)
    {
        boolean delimiterDetected = false;
        String line;

        try
        {
            while ((line = reader.readLine()) != null)
            {
                if (line.contains(this.delimiter))
                {
                    delimiterDetected = true;
                    break;
                }
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        return delimiterDetected;
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
            Path file = Paths.get(SocketClient.class.getResource(ServerConfig.FILE_PATH).toURI());
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
}