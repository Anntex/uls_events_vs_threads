package de.hdm.uls.loadtests.loadgenerator.config;

/**
 * This class defines static parameters to permit a quick access and changes for
 * the load testing lifecycle.
 *
 * @author Dennis Grewe [dg060@hdm-stuttgart.de] 02/04/2014
 */
public final class Config
{
    // ---------------------------------------
    // PROPERTIES
    // ---------------------------------------

    public static String                    SERVER_HOST     = "127.0.0.1";
    public static int                       SERVER_PORT     = 5555;

    public static String                    DELIMITER       = "$::_$";

    /**
     * This file was downloaded from:
     * @see "http://file.ithome.com.tw/20130806/KA-0945%20-%201045.pdf"
     */
    public static final String              FILE_PATH       = "/assets/55_new_features_in_java8_stephen_chin.pdf";

    /**
     * Defines the steps of client ids between the different threads
     */
    public static final int                 ID_RANGE_STEPS  = 1000;

    public static final MeasuringScenarios  MEASURING_TYPE  = MeasuringScenarios.RECEIVE;

    public static int                       SOCKET_TIME_OUT = 30000;

    // ---------------------------------------
    // SCENARIO TYPES
    // ---------------------------------------

    /**
     * This enumeration contains keys for different scenarios of the load tester.
     */
    public static enum MeasuringScenarios
    {
        /**
         * Measure only the connection time
         */
        CONNECTION,
        /**
         * Measure connection and sending time
         */
        SEND,
        /**
         * Measure connection and receiving time
         */
        RECEIVE,
        /**
         * Measure connection, sending and receiving time
         */
        SEND_RECEIVE
    }

    // ---------------------------------------
    // CONSTRUCTOR
    // ---------------------------------------

    private Config()
    {}
}