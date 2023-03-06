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

import org.finos.legend.engine.persistence.components.logicalplan.LogicalPlan;
import org.finos.legend.engine.persistence.components.logicalplan.LogicalPlanFactory;
import org.finos.legend.engine.persistence.components.logicalplan.datasets.DatasetDefinition;
import org.finos.legend.engine.persistence.components.relational.SqlPlan;
import org.finos.legend.engine.persistence.components.relational.ansi.AnsiSqlSink;
import org.finos.legend.engine.persistence.components.relational.transformer.RelationalTransformer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.finos.legend.engine.persistence.components.logicalplan.operations.BaseTestUtils.schemaWithAllColumns;

public class LogicalPlanFactoryTest
{

    @Test
    public void testSelectTable()
    {
        DatasetDefinition dataset = DatasetDefinition.builder()
                .database("my_db")
                .group("my_schema")
                .name("my_table")
                .alias("my_alias")
                .schema(schemaWithAllColumns)
                .build();

        RelationalTransformer transformer = new RelationalTransformer(AnsiSqlSink.get());
        LogicalPlan logicalPlan = LogicalPlanFactory.getLogicalPlanForIsDatasetEmpty(dataset);
        SqlPlan physicalPlan = transformer.generatePhysicalPlan(logicalPlan);
        List<String> list = physicalPlan.getSqlList();

        String expectedQuery = "SELECT COUNT(*) as \"isTableNonEmpty\" FROM (SELECT * FROM \"my_db\".\"my_schema\".\"my_table\" as my_alias LIMIT 1) as X";
        Assertions.assertEquals(expectedQuery, list.get(0));
    }

}