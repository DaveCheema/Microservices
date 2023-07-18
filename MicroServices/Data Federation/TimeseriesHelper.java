package com.gejenbacher.myplant.dataitem;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by 105052659 on 18.05.2015.
 */
public class TimeseriesHelper {

    public static List<CompressedDataItemValue<Double>> downSampleSeries(List<DataItemValue<Double>> original, long fromTs, long toTs, long timeCycle) {

        if (original.size() == 0)
            return new ArrayList<>();

        int originalIndex = 0;
        List<DataItemValue<Double>> originalVector = new ArrayList<>(original);
        long startCycleTs = fromTs;

        if (original.get(0).getTimestamp() > startCycleTs)
            startCycleTs = (original.get(0).getTimestamp() / timeCycle + 1) * timeCycle;

        List<CompressedDataItemValue<Double>> downSampledResult = new ArrayList<>();
        while (startCycleTs < toTs) {
            long endCycleTs = startCycleTs + timeCycle;
            LinkedList<DataItemValue<Double>> timeCycleValues = new LinkedList<>();
            originalIndex = prepareTimeCycleValues(originalIndex, originalVector, startCycleTs, endCycleTs, timeCycleValues);

            // now we have all the values for the time cycle run the average
            CompressedDataItemValue timeCycleAvgMinMax = calculateTimeCycle(timeCycleValues);
            downSampledResult.add(timeCycleAvgMinMax);

            startCycleTs += timeCycle;
        }

        return downSampledResult;
    }

    private static int prepareTimeCycleValues(int startingIndex, List<DataItemValue<Double>> originalVector, long startCycleTs, long endCycleTs, LinkedList<DataItemValue<Double>> result) {
        int index = startingIndex;
        // add one artifical entry to start of cycle
        DataItemValue<Double> cycleStartEntry = new DataItemValue<>(startCycleTs, originalVector.get(index).getValue());
        result.add(cycleStartEntry);
        index++;
        while (index < originalVector.size() && originalVector.get(index).getTimestamp() < endCycleTs) {
            result.add(originalVector.get(index));
            index++;
        }
        // decrement because this one has not been considered in this timecycle
        index--;

        // at one artifical entry to end of cycle
        result.add(new DataItemValue<>(endCycleTs, result.getLast().getValue()));
        return index;
    }

    private static CompressedDataItemValue<Double> calculateTimeCycle(LinkedList<DataItemValue<Double>> timeCycleValues) {
        double totalSum = 0;
        long startTs = timeCycleValues.getFirst().getTimestamp();
        long endTs = timeCycleValues.getLast().getTimestamp();
        long timeCycle = endTs - startTs;
        double min = Double.MAX_VALUE;
        double max = -Double.MAX_VALUE;
        DataItemValue<Double> lastValue = null;
        for (DataItemValue<Double> dv : timeCycleValues) {
            if (lastValue != null)
                totalSum += lastValue.getValue() * (dv.getTimestamp() - lastValue.getTimestamp());

            min = Math.min(min, dv.getValue());
            max = Math.max(max, dv.getValue());

            lastValue = dv;
        }

        double average = totalSum/timeCycle;
        return new CompressedDataItemValue<>(startTs, average, min, max);
    }
}
