package de.hdm.uls.loadtests.environment;

import de.hdm.uls.loadtests.environment.exceptions.EnvironmentException;
import de.hdm.uls.loadtests.environment.model.TestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * This class defines the conditions of a local testing environment. Depending on the environment parameters the load tests
 * can be configured.
 *
 * @author Dennis Grewe [dg060@hdm-stuttgart.de] 03/15/2014
 */
public class Environment extends AbstractEnvironment
{
    // ---------------------------------------
    // PROPERTIES
    // ---------------------------------------

    private static final    Logger      log                   = LoggerFactory.getLogger(Environment.class);
    private static final    int         START_UP_WAIT_SECONDS = 10000;

    private                 Process     serverProcess         = null;
    private                 ServerType  serverType            = null;

    // ---------------------------------------
    // ENUM
    // ---------------------------------------

    /**
     * This enumeration defines the different server types that can be tested.
     */
    public enum ServerType
    {
        /**
         * A classic thread based java server to test
         */
        JAVA_CLASSIC,
        /**
         * An improved thread based java server to test
         */
        JAVA_IMPROVED,
        /**
         * A node.js server to test
         */
        NODEJS
    }

    // ---------------------------------------
    // CONSTRUCTOR
    // ---------------------------------------

    public Environment(ServerType type, List<TestCase> tests)
    {
        super(tests);
        this.serverType = type;
    }

    // ---------------------------------------
    // METHODS
    // ---------------------------------------
    @Override
    protected void create() throws EnvironmentException
    {
        log.info("-- Environment#create");
        ProcessBuilder compileProcess = this.collectCompileServerProcessBuilder(this.serverType);
        if (compileProcess != null)
        {
            try
            {
                this.serverProcess = compileProcess.start();
                Thread.sleep(10000);
            }
            catch (IOException | InterruptedException ex)
            {
                log.error("Error while creating a new server environment and compile the sources!", ex);
            }
        }
    }

    @Override
    protected InputStream startServer() throws EnvironmentException
    {
        ProcessBuilder processBuilder = this.collectStartServerProcessBuilder(this.serverType);
        if(processBuilder != null)
        {
            try
            {
                this.serverProcess = processBuilder.start();
                log.info("-- Start server process successfully -- wait " + (Environment.START_UP_WAIT_SECONDS / 1000) + " seconds until the process has stabilized!");
                Thread.sleep(Environment.START_UP_WAIT_SECONDS);
            }
            catch (IOException | InterruptedException ex)
            {
                log.error("Error while starting a new server instance!", ex);
                throw new EnvironmentException("Error while starting a new server instance!", ex);
            }
        }

        return serverProcess.getInputStream();
    }

    @Override
    protected void stopServer() throws EnvironmentException
    {
        if (this.serverProcess != null)
        {
            this.serverProcess.destroy();
            try
            {
                this.serverProcess.waitFor();
                log.info("-- Stop server process successfully!");
            }
            catch (InterruptedException ex)
            {
                log.error("Error while waiting for parent process!", ex);
                throw new EnvironmentException("Error while trying to stop the server process!", ex);
            }
        }
    }

    @Override
    protected void destroy() throws EnvironmentException
    {
        this.stopAllServerProcesses();
    }

    /**
     * This method returns the compile process. This process is only required to compile java resources before running
     * a java project.
     *
     * @param type The ServerType of the server to be compiled.
     * @return JAVA: the ProcessBuilder containing the javac command and the parameters, otherwise NULL
     */
    private ProcessBuilder collectCompileServerProcessBuilder(ServerType type)
    {
        ProcessBuilder compileProcess = null;
        String projectDir = System.getProperty("user.dir");

        // compile the resources is only necessary for java servers
        if (ServerType.JAVA_CLASSIC.equals(type) || ServerType.JAVA_IMPROVED.equals(type))
        {
            String compileParameters = "-d " + projectDir + "\\java_thread_based_server\\bin " +
                    projectDir + "\\java_thread_based_server\\src\\main\\java\\de\\hdm\\uls\\threadbasedserver\\config\\*.java " +
                    projectDir + "\\java_thread_based_server\\src\\main\\java\\de\\hdm\\uls\\threadbasedserver\\client\\*.java " +
                    projectDir + "\\java_thread_based_server\\src\\main\\java\\de\\hdm\\uls\\threadbasedserver\\server\\*.java";
            compileProcess = new ProcessBuilder("javac", compileParameters);
        }

        return compileProcess;
    }

    /**
     * The method returns the execution process to run the given server project.
     *
     * @param type The The ServerType of the server to be started.
     * @return The terminal command to execute the server process.
     */
    private ProcessBuilder collectStartServerProcessBuilder(ServerType type) throws EnvironmentException
    {
        ProcessBuilder builder = null;
        String projectDir = System.getProperty("user.dir");

        if (ServerType.JAVA_CLASSIC.equals(type))
        {
            // please start the server process manually -> JavaSocketServer.class

            /*String processStatements = "-classpath " + projectDir + "\\java_thread_based_server\\bin\\";
            builder = new ProcessBuilder("java", processStatements);
            returnStatement = "java -classpath ../java_thread_based_server/bin/java_thread_based_server.JavaServer";*/
        }
        else if (ServerType.JAVA_IMPROVED.equals(type))
        {
            // please start the server process manually -> NIOJavaSocketServer.class
        }
        else if (ServerType.NODEJS.equals(type))
        {
            String processStatement = projectDir + "\\loadtester\\tools\\node.exe";
            String nodeScriptPath = projectDir + "\\node_event_based_server\\src\\main\\server.js";
            builder = new ProcessBuilder(processStatement, nodeScriptPath);
        }
        else
        {
            throw new EnvironmentException("No statements found for the server type: " + type);
        }

        return builder;
    }

    /**
     * This method tries to stop running server processes to keep the new environment clean from possible side effects.
     */
    private void stopAllServerProcesses()
    {
        try
        {
            if(ServerType.JAVA_CLASSIC.equals(this.serverType) || ServerType.JAVA_IMPROVED.equals(this.serverType))
            {
                Process javaProcess = Runtime.getRuntime().exec("taskkill /F /IM java.exe /T");
                javaProcess.waitFor();
            }
            else if (ServerType.NODEJS.equals(this.serverType))
            {
                Process nodeProcess = Runtime.getRuntime().exec("taskkill /F /IM node.exe /T");
                nodeProcess.waitFor();
            }
        }
        catch (IOException | InterruptedException ex)
        {
            log.error("Error while stopping running server processes!", ex);
        }
    }
}