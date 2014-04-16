package de.hdm.uls.loadtests.loadgenerator.load.model;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

import de.hdm.uls.loadtests.loadgenerator.exceptions.MeasurementException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * This class defines a test case for a injector result model.
 *
 * @author Dennis Grewe [dg060@hdm-stuttgart.de] 02/09/2014
 */
public class InjectorResultsTest
{
    // ---------------------------------------
    // PROPERTIES
    // ---------------------------------------
    private InjectorResults injectorResult = null;

    // ---------------------------------------
    // BEFORE
    // ---------------------------------------

    @Before
    public void setUp()
    {
        this.injectorResult = new InjectorResults();
    }

    // ---------------------------------------
    // TESTS
    // ---------------------------------------

    @Test(expected = MeasurementException.class)
    public void testGetDurationInSecFailure() throws MeasurementException
    {
        double result = this.injectorResult.getDurationInSec();
        System.out.println(result);
    }

    @Test
    public void testGetDurationInSec() throws MeasurementException
    {
        this.injectorResult.measureStartTime();
        this.injectorResult.measureStopTime();

        assertThat(this.injectorResult.getDurationInSec(), is(not(-1d)));
    }

    @Test
    public void testGetAverageResponseTimeInMsForMissingResponseTimes()
    {
        assertThat(this.injectorResult.getAverageResponseTimeInMs(), equalTo(0.0d));
    }

    @Test
    public void testGetAverageResponseTimeInMs()
    {
        this.injectorResult.addResponseTime(1, 2);
        assertThat(this.injectorResult.getAverageResponseTimeInMs(), equalTo(1d / 1000000));
    }

    // ---------------------------------------
    // AFTER
    // ---------------------------------------

    @After
    public void tearDown()
    {
        if(this.injectorResult != null)
        {
            this.injectorResult = null;
        }
    }
}