package de.hdm.uls.threadbasedserver.config;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.junit.Test;

/**
 * This class defines test cases for a {@link de.hdm.uls.threadbasedserver.config.ServerConfig} class.
 *
 * @author Dennis Grewe [dg060@hdm-stuttgart.de] 02/04/2014
 */
public class ServerConfigTest
{
    // ---------------------------------------
    // PROPERTIES
    // ---------------------------------------

    private final String    host        = "127.0.0.1";
    private final int       port        = 5555;
    private final String    delimiter   = "$::_$";

    // ---------------------------------------
    // TESTS
    // ---------------------------------------

    @Test
    public void testServerHost()
    {
        assertThat(ServerConfig.SERVER_HOST, equalTo(this.host));
    }

    @Test
    public void testServerPort()
    {
        assertThat(ServerConfig.SERVER_PORT, equalTo(this.port));
    }

    @Test
    public void testDelimiter()
    {
        assertThat(ServerConfig.DELIMITER, equalTo(this.delimiter));
    }
}