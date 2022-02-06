/*
 * Copyright 2020 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package pt.tml.plannedoffer.validators;

import com.google.common.collect.Multimaps;
import pt.powerqubit.validator.core.annotation.GtfsValidator;
import pt.powerqubit.validator.core.notice.NoticeContainer;
import pt.powerqubit.validator.core.notice.SeverityLevel;
import pt.powerqubit.validator.core.notice.ValidationNotice;
import pt.powerqubit.validator.core.table.GtfsStopTime;
import pt.powerqubit.validator.core.table.GtfsStopTimeTableContainer;
import pt.powerqubit.validator.core.table.GtfsStopTimeTableLoader;
import pt.powerqubit.validator.core.type.GtfsTime;
import pt.powerqubit.validator.core.validator.FileValidator;

import javax.inject.Inject;
import java.util.List;

/**
 * Validates departure_time and arrival_time fields in "stop_times.txt".
 *
 * <p>Generated notices:
 *
 * <ul>
 *   <li>{@link StopTimeWithOnlyArrivalOrDepartureTimeNotice} - a single departure_time or
 *       arrival_time is defined for a row (both or none are expected)
 *   <li>{@link StopTimeWithArrivalBeforePreviousDepartureTimeNotice} - prev(arrival_time) &lt;
 *       curr(departure_time)
 * </ul>
 */
@GtfsValidator
public class StopTimeArrivalAndDepartureTimeValidator extends FileValidator
{
    private final GtfsStopTimeTableContainer table;

    @Inject
    StopTimeArrivalAndDepartureTimeValidator(GtfsStopTimeTableContainer table)
    {
        this.table = table;
    }

    @Override
    public void validate(NoticeContainer noticeContainer)
    {
        for (List<GtfsStopTime> stopTimeList : Multimaps.asMap(table.byTripIdMap()).values())
        {
            int previousDepartureRow = -1;
            for (int i = 0; i < stopTimeList.size(); ++i)
            {
                GtfsStopTime stopTime = stopTimeList.get(i);
                final boolean hasDeparture = stopTime.hasDepartureTime();
                final boolean hasArrival = stopTime.hasArrivalTime();
                if (hasArrival != hasDeparture)
                {
                    noticeContainer.addValidationNotice(
                            new StopTimeWithOnlyArrivalOrDepartureTimeNotice(
                                    stopTime.csvRowNumber(),
                                    stopTime.tripId(),
                                    stopTime.stopSequence(),
                                    hasArrival
                                            ? GtfsStopTimeTableLoader.ARRIVAL_TIME_FIELD_NAME
                                            : GtfsStopTimeTableLoader.DEPARTURE_TIME_FIELD_NAME));
                }
                if (hasArrival
                        && previousDepartureRow != -1
                        && stopTime
                        .arrivalTime()
                        .isBefore(stopTimeList.get(previousDepartureRow).departureTime()))
                {
                    noticeContainer.addValidationNotice(
                            new StopTimeWithArrivalBeforePreviousDepartureTimeNotice(
                                    stopTime.csvRowNumber(),
                                    stopTimeList.get(previousDepartureRow).csvRowNumber(),
                                    stopTime.tripId(),
                                    stopTime.arrivalTime(),
                                    stopTimeList.get(previousDepartureRow).departureTime()));
                }
                if (hasDeparture)
                {
                    previousDepartureRow = i;
                }
            }
        }
    }

    /**
     * Two {@code GtfsTime} are out of order
     *
     * <p>Severity: {@code SeverityLevel.ERROR}
     */
    static class StopTimeWithArrivalBeforePreviousDepartureTimeNotice extends ValidationNotice
    {
        private final long csvRowNumber;
        private final long prevCsvRowNumber;
        private final String tripId;
        private final GtfsTime arrivalTime;
        private final GtfsTime departureTime;

        StopTimeWithArrivalBeforePreviousDepartureTimeNotice(
                long csvRowNumber,
                long prevCsvRowNumber,
                String tripId,
                GtfsTime arrivalTime,
                GtfsTime departureTime)
        {
            super(SeverityLevel.ERROR);
            this.csvRowNumber = csvRowNumber;
            this.prevCsvRowNumber = prevCsvRowNumber;
            this.tripId = tripId;
            this.departureTime = departureTime;
            this.arrivalTime = arrivalTime;
        }
    }

    /**
     * Missing `stop_times.arrival_time` or `stop_times.departure_time`
     *
     * <p>Severity: {@code SeverityLevel.ERROR}
     */
    static class StopTimeWithOnlyArrivalOrDepartureTimeNotice extends ValidationNotice
    {
        private final long csvRowNumber;
        private final String tripId;
        private final int stopSequence;
        private final String specifiedField;

        StopTimeWithOnlyArrivalOrDepartureTimeNotice(
                long csvRowNumber, String tripId, int stopSequence, String specifiedField)
        {
            super(SeverityLevel.ERROR);
            this.csvRowNumber = csvRowNumber;
            this.tripId = tripId;
            this.stopSequence = stopSequence;
            this.specifiedField = specifiedField;
        }
    }
}
