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
package org.seasar.doma.jdbc.query;

import java.lang.reflect.Method;

import org.seasar.doma.jdbc.Config;
import org.seasar.doma.jdbc.Sql;

/**
 * @author taedium
 * @param <RESULT>
 *            結果
 */
public abstract class AbstractCreateQuery<RESULT> implements
        CreateQuery<RESULT> {

    protected Config config;

    protected String callerClassName;

    protected String callerMethodName;

    protected Method method;

    public void setConfig(Config config) {
        this.config = config;
    }

    public void setCallerClassName(String callerClassName) {
        this.callerClassName = callerClassName;
    }

    public void setCallerMethodName(String callerMethodName) {
        this.callerMethodName = callerMethodName;
    }

    @Override
    public Config getConfig() {
        return config;
    }

    @Override
    public String getClassName() {
        return callerClassName;
    }

    @Override
    public String getMethodName() {
        return callerMethodName;
    }

    @Override
    public int getQueryTimeout() {
        return -1;
    }

    @Override
    public Sql<?> getSql() {
        return null;
    }

    @Override
    public void complete() {
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

}
