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

package pt.tml.plannedoffer.tables;

import pt.powerqubit.validator.core.annotation.GtfsEnumValue;

@GtfsEnumValue(name = "ALLOWED", value = 0)
@GtfsEnumValue(name = "NOT_AVAILABLE", value = 1)
@GtfsEnumValue(name = "MUST_PHONE", value = 2)
@GtfsEnumValue(name = "ON_REQUEST_TO_DRIVER", value = 3)
public interface GtfsPickupDropOffEnum
{
}
