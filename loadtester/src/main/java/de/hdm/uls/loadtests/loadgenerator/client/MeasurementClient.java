package de.hdm.uls.loadtests.loadgenerator.client;

import de.hdm.uls.loadtests.loadgenerator.config.Config;
import de.hdm.uls.loadtests.loadgenerator.load.model.InjectorResults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

/**
 * This class describes an extended measuring client to measure some response times. So the MeasurementClient
 * extends the functionality of a normal SingleClient to a measurement thread to track the response time
 * of a server.
 *
 * @author Dennis Grewe [dg060@hdm-stuttgart.de] 03/31/2014
 */
public class MeasurementClient extends SingleClient
{
    // ---------------------------------------
    // PROPERTIES
    // ---------------------------------------

    private static final Logger log             = LoggerFactory.getLogger(MeasuringThread.class);

    private MeasuringThread     measuringThread = new MeasuringThread();
    private InjectorResults     results         = null;

    // ---------------------------------------
    // CONSTRUCTOR
    // ---------------------------------------

    public MeasurementClient(long id, InjectorResults results)
    {
        super(id);
        this.results = results;
    }

    // ---------------------------------------
    // METHODS
    // ---------------------------------------

    public void startMeasuring()
    {
        measuringThread.setRunning(true);
        measuringThread.start();
    }

    public void stopMeasuring()
    {
        if (this.measuringThread.istRunning())
        {
            this.measuringThread.setRunning(false);
        }
    }

    @Override
    public void connect(InetSocketAddress serverAddress) throws IOException
    {
        if (this.socket == null)
        {
            long startTimeNs = System.nanoTime();
            this.socket = new Socket(Config.SERVER_HOST, Config.SERVER_PORT);
            this.socket.setSoTimeout(Config.SOCKET_TIME_OUT);
            this.measuringThread.measureResponseTime(startTimeNs);
        }
    }

    @Override
    public boolean sendData(ByteBuffer buffer)
    {
        long startTime = System.nanoTime();
        boolean successfulSend = super.sendData(buffer);
        this.measuringThread.measureResponseTime(startTime);

        return successfulSend;
    }

    @Override
    public boolean receiveData()
    {
        long startTime = System.nanoTime();
        boolean successfulReceive = super.receiveData();
        this.measuringThread.measureResponseTime(startTime);

        return successfulReceive;
    }


    // ---------------------------------------
    // INNER CLASS
    // ---------------------------------------

    private class MeasuringThread extends Thread
    {
        // ---------------------------------------
        // PROPERTIES
        // ---------------------------------------

        private boolean isRunning = false;
        private boolean isMeasuring = false;

        private long startTimeNs = -1l;
        private long stopTimeNs = -1;


        // ---------------------------------------
        // RUN
        // ---------------------------------------

        @Override
        public void run()
        {
            while (this.isRunning)
            {
                if(this.isMeasuring)
                {
                    this.waitForResponse();
                    this.setMeasuring(false);
                }
                this.sleepMs(1);
            }
        }
        // ---------------------------------------
        // METHODS
        // ---------------------------------------

        /**
         * This method sets the thread into sleep mode for certain time of ms.
         * @param timeInMs the sleep time in ms
         */
        private void sleepMs(long timeInMs)
        {
            try
            {
                Thread.sleep(timeInMs);
            }
            catch (InterruptedException ex)
            {
                MeasurementClient.log.error("An error occurred while trying to set thread to sleep!");
            }
        }

        /**
         * The method is called after a collected server response
         */
        private void waitForResponse()
        {
            if (Config.MeasuringScenarios.SEND_RECEIVE.equals(Config.MEASURING_TYPE)
                    || Config.MeasuringScenarios.RECEIVE.equals(Config.MEASURING_TYPE))
            {
                MeasurementClient.this.receiveData();
            }

            this.stopTimeNs = System.nanoTime();
            MeasurementClient.this.results.addResponseTime(this.startTimeNs, this.stopTimeNs);
        }

        public void measureResponseTime(long startTimeNs)
        {
            // measure only when thread is running
            if (this.isMeasuring == false) {
                setMeasuring(true);
                this.startTimeNs = startTimeNs;
            }
        }

        public void setRunning(boolean isRunning)
        {
            this.isRunning = isRunning;
        }

        public boolean istRunning() { return this.isRunning; }

        public void setMeasuring(boolean isMeasuring)
        {
            this.isMeasuring = isMeasuring;
        }
    }
}