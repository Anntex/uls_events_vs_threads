package de.hdm.uls.loadtests.loadgenerator.client;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is for testing the {@link MultiClient} implementation.
 *
 * @author Dennis Grewe [dg060@hdm-stuttgart.de] 02/04/2014
 */
public class MultiClientTest extends BaseClientTest
{
    // ---------------------------------------
    // PROPERTIES
    // ---------------------------------------
    private final int MAX_NUMBER_OF_CLIENTS = 10;
    private MultiClient multiClient = null;

    // ---------------------------------------
    // BEFORE
    // ---------------------------------------

    @Before
    public void setUp()
    {
        initServer();
        this.multiClient = new MultiClient(10);
    }

    // ---------------------------------------
    // TESTS
    // ---------------------------------------

    @Test
    public void testInitMultiClientByNumberOfClients()
    {
        assertThat(this.multiClient.getClients(), hasSize(MAX_NUMBER_OF_CLIENTS));
    }

    @Test
    public void testInitMultiClientByListOfClients()
    {
        List<SingleClient> clients = new ArrayList<>();
        clients.add(new SingleClient(1));
        clients.add(new SingleClient(2));
        clients.add(new SingleClient(3));

        this.multiClient = null;
        this.multiClient = new MultiClient(clients);

        assertThat(this.multiClient.getClients(), hasSize(3));
    }

    // ---------------------------------------
    // AFTER
    // ---------------------------------------

    @After
    public void tearDown() throws Exception
    {
        stopServer();

        if (this.multiClient != null)
        {
            this.multiClient = null;
        }
    }
}