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
package org.seasar.doma.jdbc;

import java.lang.reflect.Method;

import org.seasar.doma.jdbc.command.BatchDeleteCommand;
import org.seasar.doma.jdbc.command.BatchInsertCommand;
import org.seasar.doma.jdbc.command.BatchUpdateCommand;
import org.seasar.doma.jdbc.command.Command;
import org.seasar.doma.jdbc.command.CreateCommand;
import org.seasar.doma.jdbc.command.DeleteCommand;
import org.seasar.doma.jdbc.command.FunctionCommand;
import org.seasar.doma.jdbc.command.InsertCommand;
import org.seasar.doma.jdbc.command.ProcedureCommand;
import org.seasar.doma.jdbc.command.ResultSetHandler;
import org.seasar.doma.jdbc.command.ScriptCommand;
import org.seasar.doma.jdbc.command.SelectCommand;
import org.seasar.doma.jdbc.command.UpdateCommand;
import org.seasar.doma.jdbc.query.BatchDeleteQuery;
import org.seasar.doma.jdbc.query.BatchInsertQuery;
import org.seasar.doma.jdbc.query.BatchUpdateQuery;
import org.seasar.doma.jdbc.query.CreateQuery;
import org.seasar.doma.jdbc.query.DeleteQuery;
import org.seasar.doma.jdbc.query.FunctionQuery;
import org.seasar.doma.jdbc.query.InsertQuery;
import org.seasar.doma.jdbc.query.ProcedureQuery;
import org.seasar.doma.jdbc.query.ScriptQuery;
import org.seasar.doma.jdbc.query.SelectQuery;
import org.seasar.doma.jdbc.query.UpdateQuery;

/**
 * {@link Command} の実装クラスのファクトリです。
 * 
 * @author nakamura-to
 * @since 2.0.0
 */
public interface CommandImplementors {

    default <RESULT> SelectCommand<RESULT> createSelectCommand(Method method,
            SelectQuery query, ResultSetHandler<RESULT> resultSetHandler) {
        return new SelectCommand<>(query, resultSetHandler);
    }

    default DeleteCommand createDeleteCommand(Method method, DeleteQuery query) {
        return new DeleteCommand(query);
    }

    default InsertCommand createInsertCommand(Method method, InsertQuery query) {
        return new InsertCommand(query);
    }

    default UpdateCommand createUpdateCommand(Method method, UpdateQuery query) {
        return new UpdateCommand(query);
    }

    default BatchDeleteCommand createBatchDeleteCommand(Method method,
            BatchDeleteQuery query) {
        return new BatchDeleteCommand(query);
    }

    default BatchInsertCommand createBatchInsertCommand(Method method,
            BatchInsertQuery query) {
        return new BatchInsertCommand(query);
    }

    default BatchUpdateCommand createBatchUpdateCommand(Method method,
            BatchUpdateQuery query) {
        return new BatchUpdateCommand(query);
    }

    default <RESULT> FunctionCommand<RESULT> createFunctionCommand(
            Method method, FunctionQuery<RESULT> query) {
        return new FunctionCommand<>(query);
    }

    default ProcedureCommand createProcedureCommand(Method method,
            ProcedureQuery query) {
        return new ProcedureCommand(query);
    }

    default <RESULT> CreateCommand<RESULT> createCreateCommand(Method method,
            CreateQuery<RESULT> query) {
        return new CreateCommand<>(query);
    }

    default ScriptCommand createScriptCommand(Method method, ScriptQuery query) {
        return new ScriptCommand(query);
    }
}
