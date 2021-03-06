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
package org.seasar.doma.jdbc.tx;

import java.sql.SQLException;
import java.sql.Savepoint;

import junit.framework.TestCase;

import org.seasar.doma.internal.jdbc.mock.MockConnection;

/**
 * @author taedium
 * 
 */
public class LocalTransactionContextTest extends TestCase {

    public void testReleaseAndGetSavepoint() throws Exception {
        LocalTransactionalConnection connection = new LocalTransactionalConnection(
                new MockConnection());
        LocalTransactionContext context = new LocalTransactionContext(
                connection);
        context.addSavepoint("1", new MySavepoint("1"));
        context.addSavepoint("2", new MySavepoint("2"));
        context.addSavepoint("3", new MySavepoint("3"));
        context.addSavepoint("4", new MySavepoint("4"));
        context.addSavepoint("5", new MySavepoint("5"));

        Savepoint savepoint = context.releaseAndGetSavepoint("3");
        assertEquals("3", savepoint.getSavepointName());

        assertNull(context.getSavepoint("1"));
        assertNull(context.getSavepoint("2"));
        assertNull(context.getSavepoint("3"));
        assertNotNull(context.getSavepoint("4"));
        assertNotNull(context.getSavepoint("5"));
    }

    class MySavepoint implements Savepoint {

        String name;

        public MySavepoint(String name) {
            this.name = name;
        }

        @Override
        public int getSavepointId() throws SQLException {
            return 0;
        }

        @Override
        public String getSavepointName() throws SQLException {
            return name;
        }

    }
}
