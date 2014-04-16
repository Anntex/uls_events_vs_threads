package de.hdm.uls.loadtests.loadgenerator.load.model;

import de.hdm.uls.loadtests.loadgenerator.load.LoadGenerator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This class defines a model to store and handle the overall measurements of the
 * load injectors. Each injector is running in a single thread so the model gathers
 * all injector results.
 *
 * @author Dennis Grewe [dg060@hdm-stuttgart.de] 03/16/2014
 */
public class GeneratorResults
{
    // ---------------------------------------
    // PROPERTIES
    // ---------------------------------------

    private long startTimeInNanos;
    private long stopTimeInNanos;
    private List<InjectorResults> injectorResults = new ArrayList<>();

    // ---------------------------------------
    // METHODS
    // ---------------------------------------

    public void measureStartTime()
    {
        this.startTimeInNanos = System.nanoTime();
    }

    public void measureStopTime()
    {
        this.stopTimeInNanos = System.nanoTime();
    }

    public double getTestDurationInSec()
    {
        return ((double) (this.stopTimeInNanos - startTimeInNanos)) / 1000000000;
    }

    public InjectorResults newInjectorResults()
    {
        InjectorResults results = new InjectorResults();
        this.injectorResults.add(results);
        return results;
    }

    public List<InjectorResults> getInjectorResults()
    {
        return this.injectorResults;
    }

    public int getTotalClients()
    {
        int totalClients = 0;
        for (int i = 0; i < this.injectorResults.size(); i++)
        {
            totalClients += this.injectorResults.get(i).getTotalClients();
        }

        return totalClients;
    }

    public List<Throughput> getThroughputHistory()
    {
        List<Throughput> history = new ArrayList<>();
        fillThroughputHistory(history);
        sortThroughputHistory(history);
        return history;
    }

    private void fillThroughputHistory(List<Throughput> history)
    {
        for (int i = 0; i < this.injectorResults.size(); i++)
        {
            history.add(this.newThroughput(this.injectorResults.get(i).getStartTimeInNanos()));
            history.add(this.newThroughput(this.injectorResults.get(i).getStopTimeInNanos()));
        }
    }

    private Throughput newThroughput(long timeInNanos)
    {
        int clientsCount = getThroughputForTime(timeInNanos);
        Throughput throughput = new Throughput(timeInNanos, clientsCount);
        return throughput;
    }

    private int getThroughputForTime(long timeInNanos)
    {
        int clientsCount = 0;

        for (int i = 0; i < this.injectorResults.size(); i++)
        {
            if(this.injectorResults.get(i).getStartTimeInNanos() <= timeInNanos && this.injectorResults.get(i).getStopTimeInNanos() > timeInNanos)
            {
                clientsCount += this.injectorResults.get(i).getTotalClients();
            }
        }

        return clientsCount;
    }

    /**
     * The method sort a collection of Throughput model objects depending on the time of the creation.
     *
     * @param history The collection to sort
     */
    private void sortThroughputHistory(List<Throughput> history)
    {
        Collections.sort(history);
    }

    // ---------------------------------------
    // TROUGHPUT CLASS
    // ---------------------------------------

    /**
     * @author Dennis Grewe [dg060@hdm-stuttgart.de] 03/16/2014
     */
    public class Throughput implements Comparable<Throughput>
    {
        // ---------------------------------------
        // PROPERTIES
        // ---------------------------------------

        private long timeInNanos = 0;
        private int clientsCount = 0;

        // ---------------------------------------
        // CONSTRUCTOR
        // ---------------------------------------

        public Throughput(long timeInNanos, int clientsCount)
        {
            this.timeInNanos = timeInNanos;
            this.clientsCount = clientsCount;
        }

        // ---------------------------------------
        // COMPARE TO
        // ---------------------------------------

        @Override
        public int compareTo(Throughput compareObject)
        {
            int retVal = 0;

            if(this.timeInNanos < compareObject.timeInNanos)
            {
                retVal = -1;
            }
            else if (this.timeInNanos > compareObject.timeInNanos)
            {
                retVal = 1;
            }

            return retVal;
        }

        // ---------------------------------------
        // METHODS
        // ---------------------------------------

        public long getTimeInMillis()
        {
            return this.timeInNanos / 1000000;
        }

        public double getTimeInSec()
        {
            return this.timeInNanos / 1000000000;
        }

        public double getTimeProgress()
        {
            return ((double)(this.timeInNanos - GeneratorResults.this.startTimeInNanos) /
                    (GeneratorResults.this.stopTimeInNanos - GeneratorResults.this.startTimeInNanos)) * 100;
        }

        public long getTimeInNanos() { return this.timeInNanos; }

        public int getClientsCount() { return this.clientsCount; }
    }
}