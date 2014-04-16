package de.hdm.uls.loadtests.loadgenerator.client;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.io.IOException;
import java.net.InetSocketAddress;

import de.hdm.uls.loadtests.loadgenerator.config.Config;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * This class is for testing the {@link SingleClient} implementation.
 *
 * @author Dennis Grewe [dg060@hdm-stuttgart.de] 02/04/2014
 */
public class SingleClientTest extends BaseClientTest
{

    // ---------------------------------------
    // PROPERTIES
    // ---------------------------------------

    private SingleClient client = null;

    // ---------------------------------------
    // BEFORE
    // ---------------------------------------

    @Before
    public void setUp() throws Exception
    {
        initServer();
        this.client = new SingleClient(1);
    }

    // ---------------------------------------
    // TESTS
    // ---------------------------------------

    @Test
    public void testConnect() throws IOException
    {
        InetSocketAddress address = new InetSocketAddress(Config.SERVER_HOST, Config.SERVER_PORT);
        this.client.connect(address);

        assertThat(this.client.isConnected(), equalTo(true));
    }

    @Test
    public void testDisconnect() throws IOException
    {
        this.client.disconnect();

        assertThat(this.client.isConnected(), equalTo(false));
    }

    // ---------------------------------------
    // AFTER
    // ---------------------------------------

    @After
    public void tearDown() throws Exception
    {
        stopServer();

        if (this.client != null)
        {
            this.client = null;
        }
    }
}
