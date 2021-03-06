/*
 * Copyright 2004-2009 the Seasar Foundation and the Others.
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
package org.seasar.doma.it.auto;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.seasar.doma.it.dao.CompKeyEmployeeDao;
import org.seasar.doma.it.dao.EmployeeDao;
import org.seasar.doma.it.dao.NoIdDao;
import org.seasar.doma.it.dao.PersonDao;
import org.seasar.doma.it.entity.CompKeyEmployee;
import org.seasar.doma.it.entity.Employee;
import org.seasar.doma.it.entity.NoId;
import org.seasar.doma.it.entity.Person;
import org.seasar.doma.jdbc.JdbcException;
import org.seasar.doma.jdbc.OptimisticLockException;
import org.seasar.doma.jdbc.Result;
import org.seasar.doma.message.Message;
import org.seasar.framework.unit.Seasar2;

@RunWith(Seasar2.class)
public class AutoDeleteTest {

    @Test
    public void test() throws Exception {
        EmployeeDao dao = EmployeeDao.get();
        Employee employee = new Employee();
        employee.setEmployeeId(1);
        employee.setVersion(1);
        int result = dao.delete(employee);
        assertEquals(1, result);

        employee = dao.selectById(new Integer(1));
        assertNull(employee);
    }

    @Test
    public void testImmutable() throws Exception {
        PersonDao dao = PersonDao.get();
        Person person = new Person(1, null, null, null, null, null, null, null,
                1);
        Result<Person> result = dao.delete(person);
        assertEquals(1, result.getCount());
        person = result.getEntity();
        assertEquals("null_preD_postD", person.getEmployeeName());

        person = dao.selectById(new Integer(1));
        assertNull(person);
    }

    @Test
    public void testIgnoreVersion() throws Exception {
        EmployeeDao dao = EmployeeDao.get();
        Employee employee = new Employee();
        employee.setEmployeeId(1);
        employee.setVersion(99);
        int result = dao.delete_ignoreVersion(employee);
        assertEquals(1, result);

        employee = dao.selectById(new Integer(1));
        assertNull(employee);
    }

    @Test
    public void testCompositeKey() throws Exception {
        CompKeyEmployeeDao dao = CompKeyEmployeeDao.get();
        CompKeyEmployee employee = new CompKeyEmployee();
        employee.setEmployeeId1(1);
        employee.setEmployeeId2(1);
        employee.setVersion(1);
        int result = dao.delete(employee);
        assertEquals(1, result);

        employee = dao.selectById(new Integer(1), new Integer(1));
        assertNull(employee);
    }

    @Test
    public void testOptimisticLockException() throws Exception {
        EmployeeDao dao = EmployeeDao.get();
        Employee employee1 = dao.selectById(new Integer(1));
        employee1.setEmployeeName("hoge");
        Employee employee2 = dao.selectById(new Integer(1));
        employee2.setEmployeeName("foo");
        dao.delete(employee1);
        try {
            dao.delete(employee2);
            fail();
        } catch (OptimisticLockException expected) {
        }
    }

    @Test
    public void testSuppressOptimisticLockException() throws Exception {
        EmployeeDao dao = EmployeeDao.get();
        Employee employee1 = dao.selectById(1);
        employee1.setEmployeeName("hoge");
        Employee employee2 = dao.selectById(1);
        employee2.setEmployeeName("foo");
        dao.delete(employee1);
        dao.delete_suppressOptimisticLockException(employee2);
    }

    @Test
    public void testNoId() throws Exception {
        NoIdDao dao = NoIdDao.get();
        NoId entity = new NoId();
        entity.setValue1(1);
        entity.setValue2(2);
        try {
            dao.delete(entity);
            fail();
        } catch (JdbcException expected) {
            assertEquals(Message.DOMA2022, expected.getMessageResource());
        }
    }
}
