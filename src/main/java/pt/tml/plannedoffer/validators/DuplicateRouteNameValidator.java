/*
 * Copyright 2020 Google LLC, MobilityData IO
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

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import pt.powerqubit.validator.core.annotation.GtfsValidator;
import pt.powerqubit.validator.core.notice.NoticeContainer;
import pt.powerqubit.validator.core.notice.SeverityLevel;
import pt.powerqubit.validator.core.notice.ValidationNotice;
import pt.powerqubit.validator.core.table.GtfsRoute;
import pt.powerqubit.validator.core.table.GtfsRouteTableContainer;
import pt.powerqubit.validator.core.validator.FileValidator;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

/**
 * Validates that combinations of route type, short and long name are unique within an agency.
 */
@GtfsValidator
public class DuplicateRouteNameValidator extends FileValidator
{
    private static final HashFunction HASH_FUNCTION = Hashing.farmHashFingerprint64();
    private final GtfsRouteTableContainer routeTable;

    @Inject
    DuplicateRouteNameValidator(GtfsRouteTableContainer routeTable)
    {
        this.routeTable = routeTable;
    }

    /**
     * Generates a hash based on route names, type and agency.
     */
    private static long getRouteKey(GtfsRoute route)
    {
        return HASH_FUNCTION
                .newHasher()
                .putUnencodedChars(route.routeLongName())
                .putChar('\0')
                .putUnencodedChars(route.routeShortName())
                .putChar('\0')
                .putInt(route.routeType().getNumber())
                .putUnencodedChars(route.agencyId())
                .hash()
                .asLong();
    }

    @Override
    public void validate(NoticeContainer noticeContainer)
    {
        final Map<Long, GtfsRoute> routeByKey = new HashMap<>(routeTable.entityCount());
        for (GtfsRoute route : routeTable.getEntities())
        {
            GtfsRoute prevRoute = routeByKey.putIfAbsent(getRouteKey(route), route);
            if (prevRoute != null)
            {
                noticeContainer.addValidationNotice(new DuplicateRouteNameNotice(prevRoute, route));
            }
        }
    }

    /**
     * Describes two routes that have the same long and short names, route type and belong to the same
     * agency.
     *
     * <p>Severity: {@code SeverityLevel.WARNING}
     */
    static class DuplicateRouteNameNotice extends ValidationNotice
    {
        private final long csvRowNumber1;
        private final String routeId1;
        private final long csvRowNumber2;
        private final String routeId2;
        private final String routeShortName;
        private final String routeLongName;
        private final int routeTypeValue;
        private final String agencyId;

        DuplicateRouteNameNotice(GtfsRoute route1, GtfsRoute route2)
        {
            super(SeverityLevel.WARNING);
            this.csvRowNumber1 = route1.csvRowNumber();
            this.routeId1 = route1.routeId();
            this.csvRowNumber2 = route2.csvRowNumber();
            this.routeId2 = route2.routeId();
            this.routeShortName = route1.routeShortName();
            this.routeLongName = route1.routeLongName();
            this.routeTypeValue = route1.routeTypeValue();
            this.agencyId = route1.agencyId();
        }
    }
}
