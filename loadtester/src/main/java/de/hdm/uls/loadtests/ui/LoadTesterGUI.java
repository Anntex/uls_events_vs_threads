package de.hdm.uls.loadtests.ui;

import de.hdm.uls.loadtests.LoadTester;
import de.hdm.uls.loadtests.environment.AbstractEnvironment;
import de.hdm.uls.loadtests.environment.Environment;
import de.hdm.uls.loadtests.environment.model.TestCase;
import de.hdm.uls.loadtests.loadgenerator.config.Config;
import de.hdm.uls.loadtests.loadgenerator.load.LoadGenerator;
import de.hdm.uls.loadtests.loadgenerator.load.generators.RealisticLoadGenerator;
import de.hdm.uls.loadtests.loadgenerator.load.injectionprofiles.BigBangInjectionProfile;
import de.hdm.uls.loadtests.loadgenerator.load.injectionprofiles.IncreasingClientsInjectionProfile;
import de.hdm.uls.loadtests.loadgenerator.load.injectionprofiles.IncreasingClientsWithStepInjectionProfile;
import de.hdm.uls.loadtests.loadgenerator.load.injectionprofiles.InjectionProfile;
import de.hdm.uls.loadtests.loadgenerator.load.model.GeneratorResults;
import de.hdm.uls.loadtests.loadgenerator.load.model.InjectorResults;
import de.hdm.uls.loadtests.loadgenerator.load.model.ResponseTime;
import de.hdm.uls.loadtests.loadtests.GenericLoadCase;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisLabelLocation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.RangeType;
import org.jfree.data.time.DynamicTimeSeriesCollection;
import org.jfree.data.time.Second;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.ApplicationFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.util.*;
import java.util.List;

/**
 * This class defines a GUI for the load testing framework, so it is possible to disply the load test results
 * in graphs and figures.
 *
 * @author Dennis Grewe [dg060@hdm-stuttgart.de] 03/20/2014
 */
public class LoadTesterGUI extends ApplicationFrame implements ActionListener
{
    // ---------------------------------------
    // PROPERTIES
    // ---------------------------------------

    private static final Logger log = LoggerFactory.getLogger(LoadTesterGUI.class);

    // ---------------------------------------
    // WINDOW PROPERTIES
    private static final int CHART_WIDTH            = 600;
    private static final int CHART_HEIGHT           = 250;

    private ChartPanel                  throughputChartPanel;
    private DynamicTimeSeriesCollection throughputTimeSeriesCollection;
    private ChartPanel                  responseTimeChartPanel;
    private DynamicTimeSeriesCollection responseTimeTimeSeriesCollection;
    // ---------------------------------------

    // ---------------------------------------
    // TIMER PROPERTIES
    private static final int kUpdateIntervallInMs   = 1000;
    private long lastUpdateInNanos                  = 0;
    private long lastAvgResponseTime                = 0;
    private long lastMaxResponseTime                = 0;
    // ---------------------------------------

    // ---------------------------------------
    // STATE PROPERTIES
    private int                     totalClients        = 1000;             // CHANGE THIS VALUE TO INCREASE CLIENT NUMBERS
    private int                     durationInMs        = 10 * 1000;
    private InjectionProfile        injectionProfile    = new IncreasingClientsInjectionProfile();
    private LoadGenerator           loadGenerator       = new RealisticLoadGenerator(this.totalClients, this.durationInMs, this.injectionProfile);
    private GenericLoadCase         loadCase            = new GenericLoadCase(this.loadGenerator, this.injectionProfile);
    private AbstractEnvironment     env                 = null;
    private int                     loopVariable        = 0;
    // ---------------------------------------

    // ---------------------------------------
    // APPEARANCE PROPERTIES
    private Font    titleFont       = new Font("SansSerif", Font.BOLD, 14);
    private Font    tickLabelFont   = new Font("SansSerif", Font.PLAIN, 10);
    private Font    labelFont       = new Font("SansSerif", Font.PLAIN, 12);
    private int     kSecondsRange   = (int)((((float)durationInMs) / 1000) * 2.7f);
    // ---------------------------------------

    // ---------------------------------------
    // STRING PROPERTIES
    private static final String STR_TESTER          = " " + Config.MEASURING_TYPE.toString() + " Tester (1k clients)";
    // ---------------------------------------

    // ---------------------------------------
    // CONSTRUCTOR
    // ---------------------------------------

    public LoadTesterGUI(Environment.ServerType type)
    {
        super(type.name().toLowerCase() + LoadTesterGUI.STR_TESTER);

        List<TestCase> tests = Arrays.asList(LoadTesterGUI.this.loadCase);
        this.env = new Environment(type, tests);
        this.createWindow();
        this.runTest();
        this.updateCharts();
    }

    // ---------------------------------------
    // METHODS
    // ---------------------------------------

    @Override
    public void actionPerformed(ActionEvent e)
    {
        if (this.loadGenerator.isRunning())
        {
            this.updateThroughputChart();
            this.updateResponseTimeChart();
        }
    }

    /**
     * The method runs the declared environment in a new thread to get test results.
     */
    private void runTest() {
        Thread thread = new Thread(env);
        thread.start();
    }


    /**
     * This method creates the main window and their components of the GUI test application
     */
    private void createWindow()
    {
        this.throughputChartPanel = this.createThroughputChartPanel();
        this.responseTimeChartPanel = this.createResponseTimeChartPanel();

        JPanel contentPanel = new JPanel();
        GridLayout layout = new GridLayout(2, 1);
        contentPanel.setLayout(layout);

        JPanel responsePanel = new JPanel();
        GridLayout responseLayout = new GridLayout(1, 2);
        responsePanel.setLayout(responseLayout);
        responsePanel.add(this.throughputChartPanel);

        contentPanel.add(this.responseTimeChartPanel);
        contentPanel.add(responsePanel);

        setContentPane(contentPanel);

        this.pack();
    }

    private ChartPanel createThroughputChartPanel()
    {
        this.throughputTimeSeriesCollection = this.createThroughputDataSet();
        ChartPanel chartPanel = createThroughputChartPanel(this.throughputTimeSeriesCollection);
        chartPanel.setPreferredSize(new java.awt.Dimension(LoadTesterGUI.CHART_WIDTH,
                LoadTesterGUI.CHART_HEIGHT));

        return chartPanel;
    }

    private ChartPanel createResponseTimeChartPanel()
    {
        responseTimeTimeSeriesCollection = this.createResponseTimeDataSet();
        ChartPanel chartPanel = createResponseTimeChartPanel(responseTimeTimeSeriesCollection);
        chartPanel.setPreferredSize(new java.awt.Dimension(LoadTesterGUI.CHART_WIDTH,
                LoadTesterGUI.CHART_HEIGHT));
        return chartPanel;
    }

    private DynamicTimeSeriesCollection createThroughputDataSet()
    {
        int numberOfSeries = 1;
        int secondsRange = this.kSecondsRange;
        final DynamicTimeSeriesCollection dataSet = new DynamicTimeSeriesCollection(
                numberOfSeries, secondsRange, new Second());
        dataSet.setTimeBase(new Second(0, 0, 0, 1, 1, 2013));
        dataSet.addSeries(new float[] {}, 0, "Throughput");

        return dataSet;
    }

    private DynamicTimeSeriesCollection createResponseTimeDataSet()
    {
        int numberOfSeries = 2;
        int secondsRange = this.kSecondsRange;
        final DynamicTimeSeriesCollection dataset = new DynamicTimeSeriesCollection(
                numberOfSeries, secondsRange, new Second());
        dataset.setTimeBase(new Second(0, 0, 0, 1, 1, 2011));
        dataset.addSeries(new float[] {}, 0, "Response Time");
        dataset.addSeries(new float[] {}, 0, "Max Response Time");
        return dataset;
    }

    private DynamicTimeSeriesCollection createClientsOverTimeDataset() {
        int numberOfSeries = 1;
        int secondsRange = this.kSecondsRange;
        final DynamicTimeSeriesCollection dataset = new DynamicTimeSeriesCollection(
                numberOfSeries, secondsRange, new Second());
        dataset.setTimeBase(new Second(0, 0, 0, 1, 1, 2011));
        dataset.addSeries(new float[] {}, 0, "Clients over time");
        return dataset;
    }

    private ChartPanel createThroughputChartPanel(XYDataset dataSet)
    {
        JFreeChart chart = createThroughputChart(dataSet);
        ChartPanel panel = new ChartPanel(chart);
        panel.setFillZoomRectangle(false);
        panel.setMouseWheelEnabled(false);

        return panel;
    }

    private ChartPanel createResponseTimeChartPanel(XYDataset dataSet)
    {
        JFreeChart chart = this.createResponseTimeChart(dataSet);
        ChartPanel panel = new ChartPanel(chart);
        panel.setFillZoomRectangle(false);
        panel.setMouseWheelEnabled(false);

        return panel;
    }

    private JFreeChart createResponseTimeChart(XYDataset dataSet)
    {
        JFreeChart chart = ChartFactory.createTimeSeriesChart(
                "Average Response Time (in ms)", // title
                "Time", // x-axis label
                "Response Time in ms", // y-axis label
                dataSet // data
        );

        // Title font
        chart.getTitle().setFont(titleFont);

        XYPlot xyPlot = chart.getXYPlot();
        xyPlot.setBackgroundPaint(Color.WHITE);
        xyPlot.setDomainGridlinePaint(Color.BLACK);
        xyPlot.setRangeGridlinePaint(Color.BLACK);
        xyPlot.setDomainCrosshairVisible(true);
        xyPlot.setRangeCrosshairVisible(true);

        // DomainAxis
        ValueAxis domainAxis = xyPlot.getDomainAxis();
        domainAxis.setTickLabelFont(tickLabelFont);
        domainAxis.setLabelFont(labelFont);
        domainAxis.setLowerMargin(0.05);
        domainAxis.setUpperMargin(0.0);
        domainAxis.setLabelLocation(AxisLabelLocation.HIGH_END);
        domainAxis.setAutoRange(true);

        // Range Axis
        NumberAxis rangeAxis = (NumberAxis) xyPlot.getRangeAxis();
        rangeAxis.setTickLabelFont(tickLabelFont);
        rangeAxis.setLabelFont(labelFont);
        rangeAxis.setRangeType(RangeType.POSITIVE);
        rangeAxis.setAutoRange(true);
        rangeAxis.setAutoRangeMinimumSize(20);

        // Transparency
        xyPlot.setForegroundAlpha(0.5f);

        // Renderer
        XYLineAndAreaRenderer renderer = new XYLineAndAreaRenderer();
        renderer.setSeriesFillPaint(0, new GradientPaint(0f, 0f, new Color(255,
                165, 0), 0f, 0f, new Color(255, 165, 0)));
        renderer.setOutline(true);
        renderer.setSeriesOutlinePaint(0, Color.black);
        xyPlot.setRenderer(0, renderer);

        chart.removeLegend();

        return chart;
    }

    private JFreeChart createThroughputChart(XYDataset dataSet)
    {
        JFreeChart chart = ChartFactory.createTimeSeriesChart(
                "Throughput (Total clients)", // title
                "Time", // x-axis label
                "Total clients", // y-axis label
                dataSet // data
        );

        // Title font
        chart.getTitle().setFont(titleFont);

        XYPlot plot = chart.getXYPlot();

        // Plot appearance
        plot.setBackgroundPaint(Color.WHITE);
        plot.setDomainGridlinePaint(Color.BLACK);
        plot.setRangeGridlinePaint(Color.BLACK);
        plot.setDomainCrosshairVisible(true);
        plot.setRangeCrosshairVisible(true);

        // Domain Axis
        ValueAxis domainAxis = plot.getDomainAxis();
        domainAxis.setTickLabelFont(tickLabelFont);
        domainAxis.setLabelFont(labelFont);
        domainAxis.setLowerMargin(0.05);
        domainAxis.setUpperMargin(0.0);
        domainAxis.setLabelLocation(AxisLabelLocation.HIGH_END);
        domainAxis.setAutoRange(true);
        domainAxis.setAutoRangeMinimumSize(10);

        // Range Axis
        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setTickLabelFont(tickLabelFont);
        rangeAxis.setLabelFont(labelFont);
        rangeAxis.setRangeType(RangeType.POSITIVE);
        rangeAxis.setAutoRange(true);
        rangeAxis.setAutoRangeMinimumSize(100);

        // Transparency
        plot.setForegroundAlpha(0.5f);

        // Renderer
        XYLineAndAreaRenderer renderer = new XYLineAndAreaRenderer();
        renderer.setSeriesFillPaint(0, new GradientPaint(0f, 0f, new Color(173,
                216, 230), 0f, 0f, new Color(173, 216, 230)));
        renderer.setOutline(true);
        renderer.setSeriesOutlinePaint(0, Color.black);
        plot.setRenderer(0, renderer);

        chart.removeLegend();

        return chart;
    }

    private JFreeChart createClientsOverTimeChart(XYDataset dataset)
    {
        JFreeChart chart = ChartFactory.createTimeSeriesChart(
                "Clients injected over time", // title
                "Time", // x-axis label
                "Clients injected", // y-axis label
                dataset // data
        );

        // Title font
        chart.getTitle().setFont(titleFont);

        XYPlot xyPlot = chart.getXYPlot();
        xyPlot.setBackgroundPaint(Color.WHITE);
        xyPlot.setDomainGridlinePaint(Color.BLACK);
        xyPlot.setRangeGridlinePaint(Color.BLACK);
        xyPlot.setDomainCrosshairVisible(true);
        xyPlot.setRangeCrosshairVisible(true);

        // DomainAxis
        ValueAxis domainAxis = xyPlot.getDomainAxis();
        domainAxis.setTickLabelFont(tickLabelFont);
        domainAxis.setLabelFont(labelFont);
        domainAxis.setLowerMargin(0.05);
        domainAxis.setUpperMargin(0.0);
        domainAxis.setLabelLocation(AxisLabelLocation.HIGH_END);
        domainAxis.setAutoRange(true);

        // Range Axis
        NumberAxis rangeAxis = (NumberAxis) xyPlot.getRangeAxis();
        rangeAxis.setTickLabelFont(tickLabelFont);
        rangeAxis.setLabelFont(labelFont);
        rangeAxis.setRangeType(RangeType.POSITIVE);
        rangeAxis.setAutoRange(true);
        rangeAxis.setAutoRangeMinimumSize(20);

        // Transparency
        xyPlot.setForegroundAlpha(0.5f);

        // Renderer
        XYLineAndAreaRenderer renderer = new XYLineAndAreaRenderer();
        renderer.setSeriesFillPaint(0, new GradientPaint(0f, 0f, new Color(30,
                190, 40), 0f, 0f, new Color(30, 190, 40)));
        renderer.setOutline(true);
        renderer.setSeriesOutlinePaint(0, Color.black);
        xyPlot.setRenderer(0, renderer);

        chart.removeLegend();
        return chart;
    }

    private void updateCharts()
    {
        Timer chartUpdateTimer = new Timer(LoadTesterGUI.kUpdateIntervallInMs, this);
        chartUpdateTimer.start();
    }

    private void updateThroughputChart()
    {
        float[] newData = new float[1];
        newData[0] = this.loadGenerator.getCurrentClientsCount();
        this.throughputTimeSeriesCollection.advanceTime();
        this.throughputTimeSeriesCollection.appendData(newData);
    }

    private void updateResponseTimeChart()
    {
        GeneratorResults results = this.loadCase.getResults();
        List<InjectorResults> injectorResults = results.getInjectorResults();
        responseTimeTimeSeriesCollection.advanceTime();

        long avgResponseTime = 0;
        long maxResponseTime = 0;
        int responseTimesCount = 0;
        for (int i = 0; i < injectorResults.size(); i++)
        {
            InjectorResults injectorResult = injectorResults.get(i);
            for (int j = 0; j < injectorResult.getResponseTimes().size(); j++)
            {
                ResponseTime responseTime = injectorResult.getResponseTimes().get(j);
                long timeInMs = responseTime.startTimeNs;
                if (timeInMs > lastUpdateInNanos)
                {
                    long responseTimeInMs = (long) responseTime
                            .getResponseTimeInMs();
                    if (responseTimeInMs > maxResponseTime)
                    {
                        maxResponseTime = responseTimeInMs;
                    }
                    avgResponseTime += responseTimeInMs;
                    responseTimesCount++;
                }
            }
        }

        if (responseTimesCount > 0)
        {
            avgResponseTime /= responseTimesCount;
            lastAvgResponseTime = avgResponseTime;
            lastMaxResponseTime = maxResponseTime;
        }
        else
        {
            avgResponseTime = lastAvgResponseTime;
            maxResponseTime = lastMaxResponseTime;
        }
        responseTimeTimeSeriesCollection.addValue(0, this.loopVariable, avgResponseTime);
        responseTimeTimeSeriesCollection.addValue(1, this.loopVariable, maxResponseTime);
        if (this.loopVariable < 26)
        {
            this.loopVariable++;
        }
        else
        {
            this.loopVariable = 0;
        }
        lastUpdateInNanos = System.nanoTime();
    }
}