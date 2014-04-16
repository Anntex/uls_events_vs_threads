package de.hdm.uls.loadtests.environment;

import com.sun.corba.se.spi.activation.Server;
import de.hdm.uls.loadtests.LoadTester;
import de.hdm.uls.loadtests.environment.exceptions.EnvironmentException;
import de.hdm.uls.loadtests.environment.model.TestCase;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * The abstract environment class defines functionality for every load test environment.
 *
 * @author Dennis Grewe [dg060@hdm-stuttgart.de] 03/15/2014
 */
public abstract class AbstractEnvironment implements Runnable
{
    // ---------------------------------------
    // PROPERTIES
    // ---------------------------------------

    private static final Logger     log                             = LoggerFactory.getLogger(AbstractEnvironment.class);

    private static final int        TIME_BETWEEN_TESTS_IN_MILLIS    = 50;
    private static final boolean    AUTO_START_SERVER               = true;

    private List<TestCase>          tests                           = null;
    private Path                    logFile                         = null;
    private CountDownLatch          lock                            = new CountDownLatch(1);

    // ---------------------------------------
    // CONSTRUCTOR
    // ---------------------------------------

    public AbstractEnvironment(List<TestCase> tests)
    {
        this.tests = tests;
        this.logFile = Paths.get("./server.log");
    }

    // ---------------------------------------
    // RUN
    // ---------------------------------------

    @Override
    public void run()
    {
        this.createEnvironment();
        this.runTests();
        this.destroyEnvironment();
    }

    // ---------------------------------------
    // METHODS
    // ---------------------------------------

    /**
     * The method calls the create method of the child class to initialize the load testing environment.
     */
    private void createEnvironment()
    {
        if(AbstractEnvironment.AUTO_START_SERVER)
        {
            try
            {
                this.create();
            }
            catch (EnvironmentException ex)
            {
                log.error("Error to create the test environment!", ex);
            }
        }
    }

    /**
     * The method runs all passed test cases after each other.
     */
    private void runTests()
    {
            try
            {
                for(TestCase test : this.tests)
                {
                    log.info("------------------------------");
                    runTestCase(test);
                    lock.await(AbstractEnvironment.TIME_BETWEEN_TESTS_IN_MILLIS, TimeUnit.MILLISECONDS);
                }
                log.info("------------------------------");
                log.info("All tests were successful! :)");
            }
            catch (InterruptedException ex)
            {
                log.error("Error while locking the test environment for certain millis!", ex);
                log.info("------------------------------");
                log.info("Some tests failed! :(");
            }
    }

    /**
     * The method executes a certain test case in a separated server environment. For this case the method starts new
     * server instances for every single test case.
     *
     * @param testCase The test case to run.
     */
    private void runTestCase(TestCase testCase)
    {
        startNewServerInstance();
        log.info(testCase.getName() + " started!");
        testCase.start();
        log.info(testCase.getName() + " successful!");
        stopServerInstance();
    }

    /**
     * The method starts a new instance of a server environment.
     */
    private void startNewServerInstance()
    {
        if (AbstractEnvironment.AUTO_START_SERVER)
        {
            if (Environment.ServerType.NODEJS.equals(LoadTester.SERVER_TYPE))
            {
                try
                {
                    InputStream serverOutput = this.startServer();
                    this.writeServerOutputToLogFile(serverOutput);
                    log.info("Server started!");
                }
                catch (EnvironmentException ex)
                {
                    log.error("An error occurred while starting a new server instance!", ex);
                    System.exit(-1);
                }
            }
            else
            {
                log.info("Server started!");
            }
        }
    }

    /**
     * The method stops a running server instance.
     */
    private void stopServerInstance()
    {
        if (AbstractEnvironment.AUTO_START_SERVER)
        {
            try
            {
                this.stopServer();
            }
            catch (EnvironmentException ex)
            {
                log.error("An error occurred while stopping the running server instance!", ex);
            }
        }
    }

    /**
     * The method writes content to a log file using a new thread instance.
     *
     * @param content The content to write to the log file.
     */
    private void writeServerOutputToLogFile(InputStream content)
    {
        new Thread(() -> {
            try
            {
                StringWriter writer = new StringWriter();
                IOUtils.copy(content, writer);

                if (!Files.exists(AbstractEnvironment.this.logFile))
                {
                    Files.createFile(AbstractEnvironment.this.logFile);
                }

                Files.write(AbstractEnvironment.this.logFile, writer.toString().getBytes());
            }
            catch (IOException ex)
            {
                log.error("Error while opening the logFile!", ex);
            }
        }).start();
    }

    /**
     * The method calls the destroy method of the child class to destroy the load testing environment.
     */
    private void destroyEnvironment()
    {
        if(AbstractEnvironment.AUTO_START_SERVER)
        {
            try
            {
                destroy();
            }
            catch (EnvironmentException ex)
            {
                log.error("An error occured while destroying the test environment!", ex);
            }
        }
    }

    /**
     * The method initialize a certain test environment.
     *
     * @throws EnvironmentException if anything goes wrong.
     */
    protected abstract void create() throws EnvironmentException;

    protected abstract InputStream startServer() throws EnvironmentException;

    protected abstract void stopServer() throws EnvironmentException;

    /**
     * The method sets all allocated objects etc. free.
     *
     * @throws EnvironmentException
     */
    protected abstract void destroy() throws EnvironmentException;
}