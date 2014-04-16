package de.hdm.uls.loadtests.environment.model;

/**
 * The abstract class TestCase describes the functionality of a certain test scenario.
 *
 * @author Dennis Grewe [dg060@hdm-stuttgart.de] 03/15/2014
 */
public abstract class TestCase
{
    // ---------------------------------------
    // PROPERTIES
    // ---------------------------------------

    private String name;

    // ---------------------------------------
    // CONSTRUCTOR
    // ---------------------------------------

    public TestCase(String name)
    {
        this.name = name;
    }

    // ---------------------------------------
    // METHODS
    // ---------------------------------------

    public String getName()
    {
        return this.name;
    }

    public abstract void start();
}
