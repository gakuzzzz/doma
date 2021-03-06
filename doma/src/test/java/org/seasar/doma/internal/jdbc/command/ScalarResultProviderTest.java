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
package org.seasar.doma.internal.jdbc.command;

import java.lang.reflect.Method;
import java.util.Optional;

import junit.framework.TestCase;

import org.seasar.doma.internal.jdbc.mock.ColumnMetaData;
import org.seasar.doma.internal.jdbc.mock.MockConfig;
import org.seasar.doma.internal.jdbc.mock.MockResultSet;
import org.seasar.doma.internal.jdbc.mock.MockResultSetMetaData;
import org.seasar.doma.internal.jdbc.mock.RowData;
import org.seasar.doma.internal.jdbc.sql.PreparedSql;
import org.seasar.doma.jdbc.Config;
import org.seasar.doma.jdbc.SelectOptions;
import org.seasar.doma.jdbc.domain.DomainType;
import org.seasar.doma.jdbc.query.SelectQuery;

import example.domain.PhoneNumber;
import example.domain._PhoneNumber;

/**
 * @author taedium
 * 
 */
public class ScalarResultProviderTest extends TestCase {

    private final MockConfig runtimeConfig = new MockConfig();

    public void testBasic() throws Exception {
        MockResultSetMetaData metaData = new MockResultSetMetaData();
        metaData.columns.add(new ColumnMetaData("aaa"));
        MockResultSet resultSet = new MockResultSet(metaData);
        resultSet.rows.add(new RowData("hoge"));
        resultSet.next();

        ScalarResultProvider<String, String> provider = new ScalarResultProvider<>(
                () -> new org.seasar.doma.internal.jdbc.scalar.BasicScalar<String>(
                        () -> new org.seasar.doma.wrapper.StringWrapper(),
                        false), new MySelectQuery());
        String result = provider.get(resultSet);

        assertEquals("hoge", result);
    }

    public void testOptionalBasic() throws Exception {
        MockResultSetMetaData metaData = new MockResultSetMetaData();
        metaData.columns.add(new ColumnMetaData("aaa"));
        MockResultSet resultSet = new MockResultSet(metaData);
        resultSet.rows.add(new RowData("hoge"));
        resultSet.next();

        ScalarResultProvider<String, Optional<String>> provider = new ScalarResultProvider<>(
                () -> new org.seasar.doma.internal.jdbc.scalar.OptionalBasicScalar<String>(
                        () -> new org.seasar.doma.wrapper.StringWrapper()),
                new MySelectQuery());
        Optional<String> result = provider.get(resultSet);

        assertEquals("hoge", result.get());
    }

    public void testDomain() throws Exception {
        MockResultSetMetaData metaData = new MockResultSetMetaData();
        metaData.columns.add(new ColumnMetaData("aaa"));
        MockResultSet resultSet = new MockResultSet(metaData);
        resultSet.rows.add(new RowData("hoge"));
        resultSet.next();

        DomainType<String, PhoneNumber> domainType = _PhoneNumber
                .getSingletonInternal();

        ScalarResultProvider<String, PhoneNumber> provider = new ScalarResultProvider<>(
                () -> domainType.createScalar(), new MySelectQuery());
        PhoneNumber result = provider.get(resultSet);

        assertEquals("hoge", result.getValue());
    }

    public void testOptionalDomain() throws Exception {
        MockResultSetMetaData metaData = new MockResultSetMetaData();
        metaData.columns.add(new ColumnMetaData("aaa"));
        MockResultSet resultSet = new MockResultSet(metaData);
        resultSet.rows.add(new RowData("hoge"));
        resultSet.next();

        DomainType<String, PhoneNumber> domainType = _PhoneNumber
                .getSingletonInternal();

        ScalarResultProvider<String, Optional<PhoneNumber>> provider = new ScalarResultProvider<>(
                () -> domainType.createOptionalScalar(), new MySelectQuery());
        Optional<PhoneNumber> result = provider.get(resultSet);

        assertEquals("hoge", result.get().getValue());
    }

    protected class MySelectQuery implements SelectQuery {

        @Override
        public SelectOptions getOptions() {
            return SelectOptions.get();
        }

        @Override
        public Config getConfig() {
            return runtimeConfig;
        }

        @Override
        public String getClassName() {
            return null;
        }

        @Override
        public String getMethodName() {
            return null;
        }

        @Override
        public PreparedSql getSql() {
            return null;
        }

        @Override
        public boolean isResultEnsured() {
            return false;
        }

        @Override
        public boolean isResultMappingEnsured() {
            return false;
        }

        @Override
        public int getFetchSize() {
            return 0;
        }

        @Override
        public int getMaxRows() {
            return 0;
        }

        @Override
        public int getQueryTimeout() {
            return 0;
        }

        @Override
        public void prepare() {
        }

        @Override
        public void complete() {
        }

        @Override
        public Method getMethod() {
            return null;
        }

    }
}
