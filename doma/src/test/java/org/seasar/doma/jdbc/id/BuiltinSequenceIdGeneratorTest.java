/*
 * Copyright 2004-2010 the Seasar Foundation and the Others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package org.seasar.doma.jdbc.id;

import junit.framework.TestCase;

import org.seasar.doma.internal.jdbc.mock.MockConfig;
import org.seasar.doma.internal.jdbc.mock.MockResultSet;
import org.seasar.doma.internal.jdbc.mock.RowData;
import org.seasar.doma.jdbc.dialect.PostgresDialect;

import example.entity._Emp;

/**
 * @author taedium
 * 
 */
public class BuiltinSequenceIdGeneratorTest extends TestCase {

    public void test() throws Exception {
        MockConfig config = new MockConfig();
        config.setDialect(new PostgresDialect());
        MockResultSet resultSet = config.dataSource.connection.preparedStatement.resultSet;
        resultSet.rows.add(new RowData(11L));

        BuiltinSequenceIdGenerator idGenerator = new BuiltinSequenceIdGenerator();
        idGenerator.setQualifiedSequenceName("aaa");
        idGenerator.setInitialValue(1);
        idGenerator.setAllocationSize(1);
        IdGenerationConfig idGenerationConfig = new IdGenerationConfig(config,
                _Emp.getSingletonInternal(), "EMP", "ID");
        Long value = idGenerator.generatePreInsert(idGenerationConfig);
        assertEquals(new Long(11), value);
        assertEquals("select nextval('aaa')",
                config.dataSource.connection.preparedStatement.sql);
    }
}
