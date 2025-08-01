// Copyright 2023 Goldman Sachs
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.finos.legend.engine.persistence.components.util;

public enum SchemaEvolutionCapability
{
    ADD_COLUMN,
    DATA_TYPE_CONVERSION,
    DATA_TYPE_LENGTH_CHANGE,
    DATA_TYPE_SCALE_CHANGE,
    COLUMN_NULLABILITY_CHANGE,
    ALLOW_MISSING_COLUMNS
}