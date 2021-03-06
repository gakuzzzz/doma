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
package org.seasar.doma.internal.apt.meta;

import static org.seasar.doma.internal.util.AssertionUtil.*;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;

import org.seasar.doma.ResultHandlerType;
import org.seasar.doma.internal.apt.AptException;
import org.seasar.doma.internal.apt.cttype.AnyCtType;
import org.seasar.doma.internal.apt.cttype.BasicCtType;
import org.seasar.doma.internal.apt.cttype.CtType;
import org.seasar.doma.internal.apt.cttype.DomainCtType;
import org.seasar.doma.internal.apt.cttype.EntityCtType;
import org.seasar.doma.internal.apt.cttype.FunctionCtType;
import org.seasar.doma.internal.apt.cttype.IterableCtType;
import org.seasar.doma.internal.apt.cttype.IterationCallbackCtType;
import org.seasar.doma.internal.apt.cttype.MapCtType;
import org.seasar.doma.internal.apt.cttype.OptionalCtType;
import org.seasar.doma.internal.apt.cttype.SelectOptionsCtType;
import org.seasar.doma.internal.apt.cttype.SimpleCtTypeVisitor;
import org.seasar.doma.internal.apt.cttype.StreamCtType;
import org.seasar.doma.internal.apt.mirror.SelectMirror;
import org.seasar.doma.message.Message;

/**
 * @author taedium
 * 
 */
public class SqlFileSelectQueryMetaFactory extends
        AbstractSqlFileQueryMetaFactory<SqlFileSelectQueryMeta> {

    public SqlFileSelectQueryMetaFactory(ProcessingEnvironment env) {
        super(env);
    }

    @Override
    public QueryMeta createQueryMeta(ExecutableElement method, DaoMeta daoMeta) {
        assertNotNull(method, daoMeta);
        SqlFileSelectQueryMeta queryMeta = createSqlFileSelectQueryMeta(method,
                daoMeta);
        if (queryMeta == null) {
            return null;
        }
        doTypeParameters(queryMeta, method, daoMeta);
        doParameters(queryMeta, method, daoMeta);
        doReturnType(queryMeta, method, daoMeta);
        doThrowTypes(queryMeta, method, daoMeta);
        doSqlFiles(queryMeta, method, daoMeta);
        return queryMeta;
    }

    protected SqlFileSelectQueryMeta createSqlFileSelectQueryMeta(
            ExecutableElement method, DaoMeta daoMeta) {
        SelectMirror selectMirror = SelectMirror.newInstance(method, env);
        if (selectMirror == null) {
            return null;
        }
        SqlFileSelectQueryMeta queryMeta = new SqlFileSelectQueryMeta(method);
        queryMeta.setSelectMirror(selectMirror);
        queryMeta.setQueryKind(QueryKind.SQLFILE_SELECT);
        return queryMeta;
    }

    @Override
    protected void doParameters(final SqlFileSelectQueryMeta queryMeta,
            ExecutableElement method, DaoMeta daoMeta) {
        for (VariableElement parameter : method.getParameters()) {
            final QueryParameterMeta parameterMeta = createParameterMeta(parameter);
            parameterMeta.getCtType().accept(
                    new ParamCtTypeVisitor(queryMeta, parameterMeta), null);
            queryMeta.addParameterMeta(parameterMeta);
            if (parameterMeta.isBindable()) {
                queryMeta.addBindableParameterCtType(parameterMeta.getName(),
                        parameterMeta.getCtType());
            }
        }

        if (queryMeta.getResultHandlerType() == ResultHandlerType.ITERATION) {
            if (queryMeta.getIterationCallbackCtType() == null) {
                throw new AptException(Message.DOMA4056, env, method);
            }
        } else {
            if (queryMeta.getIterationCallbackCtType() != null) {
                SelectMirror selectMirror = queryMeta.getSelectMirror();
                throw new AptException(Message.DOMA4057, env, method,
                        selectMirror.getAnnotationMirror(),
                        selectMirror.getResultHandler());
            }
        }

        if (queryMeta.getResultHandlerType() == ResultHandlerType.STREAM) {
            if (queryMeta.getFunctionCtType() == null) {
                throw new AptException(Message.DOMA4247, env, method);
            }
        } else {
            if (queryMeta.getFunctionCtType() != null) {
                SelectMirror selectMirror = queryMeta.getSelectMirror();
                throw new AptException(Message.DOMA4248, env, method,
                        selectMirror.getAnnotationMirror(),
                        selectMirror.getResultHandler());
            }
        }
    }

    @Override
    protected void doReturnType(final SqlFileSelectQueryMeta queryMeta,
            ExecutableElement method, DaoMeta daoMeta) {
        final QueryReturnMeta returnMeta = createReturnMeta(method);
        queryMeta.setReturnMeta(returnMeta);

        if (queryMeta.getResultHandlerType() == ResultHandlerType.ITERATION) {
            IterationCallbackCtType iterationCallbackCtType = queryMeta
                    .getIterationCallbackCtType();
            AnyCtType returnCtType = iterationCallbackCtType.getReturnCtType();
            if (returnCtType == null
                    || !env.getTypeUtils().isSameType(returnMeta.getType(),
                            returnCtType.getTypeMirror())) {
                throw new AptException(Message.DOMA4055, env, method,
                        returnMeta.getType(), returnCtType.getBoxedTypeName());
            }
        } else if (queryMeta.getResultHandlerType() == ResultHandlerType.STREAM) {
            FunctionCtType functionCtType = queryMeta.getFunctionCtType();
            AnyCtType returnCtType = functionCtType.getReturnCtType();
            if (returnCtType == null
                    || !env.getTypeUtils().isSameType(returnMeta.getType(),
                            returnCtType.getTypeMirror())) {
                throw new AptException(Message.DOMA4246, env, method,
                        returnMeta.getType(), returnCtType.getBoxedTypeName());
            }
        } else {
            returnMeta.getCtType().accept(new ReturnCtTypeVisitor(returnMeta),
                    null);
        }
    }

    /**
     * 
     * @author nakamura-to
     * 
     */
    protected class ParamCtTypeVisitor extends
            SimpleCtTypeVisitor<Void, Void, RuntimeException> {

        protected SqlFileSelectQueryMeta queryMeta;

        protected QueryParameterMeta parameterMeta;

        protected ParamCtTypeVisitor(SqlFileSelectQueryMeta queryMeta,
                QueryParameterMeta parameterMeta) {
            this.queryMeta = queryMeta;
            this.parameterMeta = parameterMeta;
        }

        @Override
        public Void visitIterationCallbackCtType(
                IterationCallbackCtType ctType, Void p) throws RuntimeException {
            if (queryMeta.getIterationCallbackCtType() != null) {
                throw new AptException(Message.DOMA4054, env,
                        parameterMeta.getElement());
            }
            ctType.getTargetCtType().accept(
                    new ParamIterationCallbackTargetCtTypeVisitor(queryMeta,
                            parameterMeta), null);
            queryMeta.setIterationCallbackCtType(ctType);
            queryMeta
                    .setIterationCallbackParameterName(parameterMeta.getName());
            return null;
        }

        @Override
        public Void visitFunctionCtType(FunctionCtType ctType, Void p)
                throws RuntimeException {
            if (queryMeta.getFunctionCtType() != null) {
                throw new AptException(Message.DOMA4249, env,
                        parameterMeta.getElement());
            }
            ctType.getTargetCtType().accept(
                    new ParamFunctionTargetCtTypeVisitor(queryMeta,
                            parameterMeta), null);
            queryMeta.setFunctionCtType(ctType);
            queryMeta.setFunctionParameterName(parameterMeta.getName());
            return null;
        }

        @Override
        public Void visitSelectOptionsCtType(SelectOptionsCtType ctType, Void p)
                throws RuntimeException {
            if (queryMeta.getSelectOptionsCtType() != null) {
                throw new AptException(Message.DOMA4053, env,
                        parameterMeta.getElement());
            }
            queryMeta.setSelectOptionsCtType(ctType);
            queryMeta.setSelectOptionsParameterName(parameterMeta.getName());
            return null;
        }
    }

    /**
     * 
     * @author nakamura-to
     * 
     */
    protected class ParamIterationCallbackTargetCtTypeVisitor extends
            SimpleCtTypeVisitor<Void, Void, RuntimeException> {

        protected SqlFileSelectQueryMeta queryMeta;

        protected QueryParameterMeta parameterMeta;

        protected ParamIterationCallbackTargetCtTypeVisitor(
                SqlFileSelectQueryMeta queryMeta,
                QueryParameterMeta parameterMeta) {
            this.queryMeta = queryMeta;
            this.parameterMeta = parameterMeta;
        }

        @Override
        protected Void defaultAction(CtType type, Void p)
                throws RuntimeException {
            throw new AptException(Message.DOMA4058, env,
                    queryMeta.getExecutableElement());
        }

        @Override
        public Void visitBasicCtType(BasicCtType ctType, Void p)
                throws RuntimeException {
            return null;
        }

        @Override
        public Void visitDomainCtType(DomainCtType ctType, Void p)
                throws RuntimeException {
            return null;
        }

        @Override
        public Void visitMapCtType(MapCtType ctType, Void p)
                throws RuntimeException {
            return null;
        }

        @Override
        public Void visitEntityCtType(EntityCtType ctType, Void p)
                throws RuntimeException {
            if (ctType.isAbstract()) {
                throw new AptException(Message.DOMA4158, env,
                        parameterMeta.getElement(), ctType.getTypeName());
            }
            return null;
        }

        @Override
        public Void visitOptionalCtType(OptionalCtType ctType, Void p)
                throws RuntimeException {
            Boolean valid = ctType.getElementCtType().accept(
                    new SimpleCtTypeVisitor<Boolean, Void, RuntimeException>() {

                        @Override
                        protected Boolean defaultAction(CtType ctType, Void p)
                                throws RuntimeException {
                            return false;
                        }

                        @Override
                        public Boolean visitBasicCtType(BasicCtType ctType,
                                Void p) throws RuntimeException {
                            return true;
                        }

                        @Override
                        public Boolean visitDomainCtType(DomainCtType ctType,
                                Void p) throws RuntimeException {
                            return true;
                        }

                    }, null);
            if (Boolean.FALSE == valid) {
                defaultAction(ctType, null);
            }
            return null;
        }
    }

    /**
     * 
     * @author nakamura-to
     * 
     */
    protected class ParamFunctionTargetCtTypeVisitor extends
            SimpleCtTypeVisitor<Void, Void, RuntimeException> {

        protected SqlFileSelectQueryMeta queryMeta;

        protected QueryParameterMeta parameterMeta;

        protected ParamFunctionTargetCtTypeVisitor(
                SqlFileSelectQueryMeta queryMeta,
                QueryParameterMeta parameterMeta) {
            this.queryMeta = queryMeta;
            this.parameterMeta = parameterMeta;
        }

        @Override
        protected Void defaultAction(CtType type, Void p)
                throws RuntimeException {
            throw new AptException(Message.DOMA4244, env,
                    queryMeta.getExecutableElement());
        }

        @Override
        public Void visitStreamCtType(StreamCtType ctType, Void p)
                throws RuntimeException {
            return ctType.getElementCtType().accept(
                    new StreamElementCtTypeVisitor(), null);
        }

        protected class StreamElementCtTypeVisitor extends
                SimpleCtTypeVisitor<Void, Void, RuntimeException> {

            @Override
            protected Void defaultAction(CtType ctType, Void p)
                    throws RuntimeException {
                throw new AptException(Message.DOMA4245, env,
                        queryMeta.getExecutableElement());
            }

            @Override
            public Void visitBasicCtType(BasicCtType ctType, Void p)
                    throws RuntimeException {
                return null;
            }

            @Override
            public Void visitDomainCtType(DomainCtType ctType, Void p)
                    throws RuntimeException {
                return null;
            }

            @Override
            public Void visitMapCtType(MapCtType ctType, Void p)
                    throws RuntimeException {
                return null;
            }

            @Override
            public Void visitEntityCtType(EntityCtType ctType, Void p)
                    throws RuntimeException {
                if (ctType.isAbstract()) {
                    throw new AptException(Message.DOMA4250, env,
                            parameterMeta.getElement(), ctType.getTypeName());
                }
                return null;
            }

            @Override
            public Void visitOptionalCtType(OptionalCtType ctType, Void p)
                    throws RuntimeException {
                Boolean valid = ctType
                        .getElementCtType()
                        .accept(new SimpleCtTypeVisitor<Boolean, Void, RuntimeException>() {

                            @Override
                            protected Boolean defaultAction(CtType ctType,
                                    Void p) throws RuntimeException {
                                return false;
                            }

                            @Override
                            public Boolean visitBasicCtType(BasicCtType ctType,
                                    Void p) throws RuntimeException {
                                return true;
                            }

                            @Override
                            public Boolean visitDomainCtType(
                                    DomainCtType ctType, Void p)
                                    throws RuntimeException {
                                return true;
                            }

                        }, null);
                if (Boolean.FALSE == valid) {
                    defaultAction(ctType, null);
                }
                return null;
            }
        }
    }

    /**
     * 
     * @author nakamura-to
     * 
     */
    protected class ReturnCtTypeVisitor extends
            SimpleCtTypeVisitor<Void, Void, RuntimeException> {

        protected QueryReturnMeta returnMeta;

        protected ReturnCtTypeVisitor(QueryReturnMeta returnMeta) {
            this.returnMeta = returnMeta;
        }

        @Override
        protected Void defaultAction(CtType type, Void p)
                throws RuntimeException {
            throw new AptException(Message.DOMA4008, env,
                    returnMeta.getElement(), returnMeta.getType());
        }

        @Override
        public Void visitBasicCtType(BasicCtType ctType, Void p)
                throws RuntimeException {
            return null;
        }

        @Override
        public Void visitDomainCtType(DomainCtType ctType, Void p)
                throws RuntimeException {
            return null;
        }

        @Override
        public Void visitEntityCtType(EntityCtType ctType, Void p)
                throws RuntimeException {
            if (ctType.isAbstract()) {
                throw new AptException(Message.DOMA4154, env,
                        returnMeta.getElement(), ctType.getQualifiedName());
            }
            return null;
        }

        @Override
        public Void visitMapCtType(MapCtType ctType, Void p)
                throws RuntimeException {
            return null;
        }

        @Override
        public Void visitIterableCtType(IterableCtType ctType, Void p)
                throws RuntimeException {
            if (!ctType.isList()) {
                defaultAction(ctType, p);
            }
            ctType.getElementCtType().accept(
                    new ReturnIterableElementCtTypeVisitor(returnMeta), p);
            return null;
        }

        @Override
        public Void visitOptionalCtType(OptionalCtType ctType, Void p)
                throws RuntimeException {
            ctType.getElementCtType().accept(
                    new ReturnOptionalElementCtTypeVisitor(returnMeta), p);
            return null;
        }
    }

    /**
     * 
     * @author nakamura-to
     * 
     */
    protected class ReturnIterableElementCtTypeVisitor extends
            SimpleCtTypeVisitor<Void, Void, RuntimeException> {

        protected QueryReturnMeta returnMeta;

        protected ReturnIterableElementCtTypeVisitor(QueryReturnMeta returnMeta) {
            this.returnMeta = returnMeta;
        }

        @Override
        protected Void defaultAction(CtType type, Void p)
                throws RuntimeException {
            throw new AptException(Message.DOMA4007, env,
                    returnMeta.getElement(), type.getTypeName());
        }

        @Override
        public Void visitBasicCtType(BasicCtType ctType, Void p)
                throws RuntimeException {
            return null;
        }

        @Override
        public Void visitDomainCtType(DomainCtType ctType, Void p)
                throws RuntimeException {
            return null;
        }

        @Override
        public Void visitMapCtType(MapCtType ctType, Void p)
                throws RuntimeException {
            return null;
        }

        @Override
        public Void visitEntityCtType(EntityCtType ctType, Void p)
                throws RuntimeException {
            if (ctType.isAbstract()) {
                throw new AptException(Message.DOMA4155, env,
                        returnMeta.getElement(), ctType.getTypeMirror());
            }
            return null;
        }

        @Override
        public Void visitOptionalCtType(OptionalCtType ctType, Void p)
                throws RuntimeException {
            ctType.getElementCtType().accept(
                    new ReturnOptionalElementCtTypeVisitor(returnMeta), p);
            return null;
        }

    }

    /**
     * 
     * @author nakamura-to
     * 
     */
    protected class ReturnOptionalElementCtTypeVisitor extends
            SimpleCtTypeVisitor<Void, Void, RuntimeException> {

        protected QueryReturnMeta returnMeta;

        protected ReturnOptionalElementCtTypeVisitor(QueryReturnMeta returnMeta) {
            this.returnMeta = returnMeta;
        }

        @Override
        protected Void defaultAction(CtType type, Void p)
                throws RuntimeException {
            throw new AptException(Message.DOMA4235, env,
                    returnMeta.getElement(), type.getTypeName());
        }

        @Override
        public Void visitBasicCtType(BasicCtType ctType, Void p)
                throws RuntimeException {
            return null;
        }

        @Override
        public Void visitDomainCtType(DomainCtType ctType, Void p)
                throws RuntimeException {
            return null;
        }

        @Override
        public Void visitMapCtType(MapCtType ctType, Void p)
                throws RuntimeException {
            return null;
        }

        @Override
        public Void visitEntityCtType(EntityCtType ctType, Void p)
                throws RuntimeException {
            if (ctType.isAbstract()) {
                throw new AptException(Message.DOMA4234, env,
                        returnMeta.getElement(), ctType.getTypeMirror());
            }
            return null;
        }
    }

}
