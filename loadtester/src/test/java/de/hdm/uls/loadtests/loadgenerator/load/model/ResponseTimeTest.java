package de.hdm.uls.loadtests.loadgenerator.load.model;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * This class defines a test case for a ResponseTime class.
 *
 * @author Dennis Grewe [dg060@hdm-stuttgart.de] 02/09/2014
 */
public class ResponseTimeTest
{
    // ---------------------------------------
    // PROPERTIES
    // ---------------------------------------

    private static final long startTimeNs = 1;
    private static final long stopTimeNs = 2;

    private ResponseTime responseTime = null;

    // ---------------------------------------
    // BEFORE
    // ---------------------------------------

    @Before
    public void setUp()
    {
        responseTime = new ResponseTime(startTimeNs, stopTimeNs);
    }

    // ---------------------------------------
    // TESTS
    // ---------------------------------------

    @Test
    public void testGetResponseTimeInMs()
    {
        assertThat(responseTime.getResponseTimeInMs(), equalTo((1d / 1000000)));
    }

    @Test
    public void testGetResponseTimeInSec()
    {
        assertThat(responseTime.getResponseTimeInSec(), equalTo((1d / 1000000000)));
    }

    @Test
    public void testGetStartTimeInMs()
    {
        assertThat(responseTime.getStartTimeInMs(), equalTo((1d / 1000000)));
    }

    // ---------------------------------------
    // AFTER
    // ---------------------------------------

    @After
    public void tearDown()
    {
        if(responseTime != null)
        {
            responseTime = null;
        }
    }
}