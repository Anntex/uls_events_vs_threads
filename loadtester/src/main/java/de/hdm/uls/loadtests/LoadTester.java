package de.hdm.uls.loadtests;

import de.hdm.uls.loadtests.environment.Environment;
import de.hdm.uls.loadtests.environment.model.TestCase;
import de.hdm.uls.loadtests.loadgenerator.config.Config;
import de.hdm.uls.loadtests.loadtests.RealisticLoadCase;
import de.hdm.uls.loadtests.ui.LoadTesterGUI;
import org.jfree.ui.RefineryUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is for load testing socket server. The LoadTester class defines the main class of the testing framework.
 *
 * @author Dennis Grewe [dg060@hdm-stuttgart.de] 03/15/2014
 */
public class LoadTester
{
    // ---------------------------------------
    // PROPERTIES
    // ---------------------------------------

    private static final Logger             log             = LoggerFactory.getLogger(LoadTester.class);
    private static final boolean            USE_GUI_TESTER  = true;

    public static Environment.ServerType    SERVER_TYPE      = Environment.ServerType.JAVA_IMPROVED;

    // ---------------------------------------
    // MAIN
    // ---------------------------------------

    // ATTENTION: PLEASE START A JAVA SERVER INSTANCE BY HAND
    //      -> THE LOAD TESTING FRAMEWORK WILL NOT START A JAVA SERVER INSTANCE AUTOMATICALLY
    //      -> THE NODEJS INSTANCE WILL BE STARTED AUTOMATICALLY BY THIS LOAD TESTER FRAMEWORK
    public static void main(String[] args) throws InterruptedException
    {
        if (USE_GUI_TESTER)
        {
            log.info("-------- START TESTER USING GUI --- SERVERTYPE: " + LoadTester.SERVER_TYPE
                    + " --- SCENARIO: " + Config.MEASURING_TYPE + " --------");
            LoadTesterGUI testerGUI = new LoadTesterGUI(LoadTester.SERVER_TYPE);
            RefineryUtilities.centerFrameOnScreen(testerGUI);
            testerGUI.setVisible(true);
        }
        else
        {
            log.info("-------- START TESTER USING CONSOLE --- SERVERTYPE: " + LoadTester.SERVER_TYPE
                    + " --- SCENARIO: " + Config.MEASURING_TYPE + " --------");
            List<TestCase>           loadTests       = new ArrayList(){{
                // you can also use the GenericLoadCase scenario
                add(new RealisticLoadCase());
            }};
            Environment environment = new Environment(LoadTester.SERVER_TYPE, loadTests);
            Thread thread = new Thread(environment);
            thread.start();
            thread.join();
        }
    }
}