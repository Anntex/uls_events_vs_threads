package de.hdm.uls.threadbasedserver.config;

/**
 * This class defines static parameters to permit a quick access and changes for
 * the load testing lifecycle.
 *
 * @author Dennis Grewe [dg060@hdm-stuttgart.de] 02/04/2014
 */
public final class ServerConfig
{
    // ---------------------------------------
    // PROPERTIES
    // ---------------------------------------

    public static final String    SERVER_HOST   = "127.0.0.1";
    public static final String    DELIMITER     = "$::_$";
    public static final int       SERVER_PORT   = 5555;

    /**
     * This file was downloaded from:
     * @see "http://www.galileocomputing.de/download/dateien/3023/galileocomputing_node.js.pdf"
     */
    public static final String    FILE_PATH     = "/assets/galileocomputing_node.js.pdf";

    /**
     * The maximum acceptable time to wait for a server response
     */
    //public static final int maxAcceptableResponseTimeInMs = 100;

    // ---------------------------------------
    // CONSTRUCTOR
    // ---------------------------------------

    private ServerConfig()
    {}
}