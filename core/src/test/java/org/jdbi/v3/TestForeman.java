/*
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
package org.jdbi.v3;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

import org.jdbi.v3.tweak.Argument;
import org.jdbi.v3.tweak.ArgumentFactory;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

public class TestForeman
{
    @Rule
    public MockitoRule mockito = MockitoJUnit.rule();

    private static final String I_AM_A_STRING = "I am a String";

    private final Foreman foreman = new Foreman();

    @Mock
    public PreparedStatement stmt;

    @Test
    public void testWaffleLong() throws Exception
    {
        foreman.waffle(Object.class, new Long(3L), null).apply(1, stmt, null);
        verify(stmt).setLong(1, 3);
    }

    @Test
    public void testWaffleShort() throws Exception
    {
        foreman.waffle(Object.class, (short) 2000, null).apply(2, stmt, null);
        verify(stmt).setShort(2, (short) 2000);
    }

    @Test
    public void testWaffleString() throws Exception {
        foreman.waffle(Object.class, I_AM_A_STRING, null).apply(3, stmt, null);
        verify(stmt).setString(3, I_AM_A_STRING);
    }

    @Test
    public void testExplicitWaffleLong() throws Exception {
        foreman.waffle(Long.class, new Long(3L), null).apply(1, stmt, null);
        verify(stmt).setLong(1, 3);
    }

    @Test
    public void testExplicitWaffleShort() throws Exception {
        foreman.waffle(short.class, (short) 2000, null).apply(2, stmt, null);
        verify(stmt).setShort(2, (short) 2000);
    }

    @Test
    public void testExplicitWaffleString() throws Exception {
        foreman.waffle(String.class, I_AM_A_STRING, null).apply(3, stmt, null);
        verify(stmt).setString(3, I_AM_A_STRING);
    }

    @Test
    public void testPull88WeirdClassArgumentFactory() throws Exception
    {
        final Foreman foreman = new Foreman();
        foreman.register(new WeirdClassArgumentFactory());

        // Pull Request #88 changes the outcome of this waffle call from ObjectArgument to WeirdArgument
        // when using SqlStatement#bind(..., Object) and the Object is != null
        final Weird weird = new Weird();
        assertEquals(WeirdArgument.class, foreman.waffle(Weird.class, weird, null).getClass());

        foreman.waffle(Object.class, weird, null).apply(2, stmt, null);
        verify(stmt).setObject(2, weird);
    }

    @Test
    public void testPull88NullClassArgumentFactory() throws Exception
    {
        final Foreman foreman = new Foreman();
        foreman.register(new WeirdClassArgumentFactory());

        assertEquals(WeirdArgument.class, foreman.waffle(Weird.class, null, null).getClass());

        foreman.waffle(Object.class, null, null).apply(3, stmt, null);
        verify(stmt).setNull(3, Types.NULL);
    }

    @Test
    public void testPull88WeirdValueArgumentFactory()
    {
        final Foreman foreman = new Foreman();
        foreman.register(new WeirdValueArgumentFactory());

        // Pull Request #88 changes the outcome of this waffle call from ObjectArgument to WeirdArgument
        // when using SqlStatement#bind(..., Object) and the Object is != null
        assertEquals(WeirdArgument.class, foreman.waffle(Weird.class, new Weird(), null).getClass());
        assertEquals(WeirdArgument.class, foreman.waffle(Object.class, new Weird(), null).getClass());
    }

    @Test
    public void testPull88NullValueArgumentFactory() throws Exception
    {
        final Foreman foreman = new Foreman();
        foreman.register(new WeirdValueArgumentFactory());

        foreman.waffle(Weird.class, null, null).apply(3, stmt, null);
        verify(stmt).setNull(3, Types.NULL);

        foreman.waffle(Object.class, null, null).apply(5, stmt, null);
        verify(stmt).setNull(5, Types.NULL);
    }

    private static class Weird
    {
    }

    private static class WeirdClassArgumentFactory implements ArgumentFactory<Weird>
    {
        @Override
        public boolean accepts(Class<?> expectedType, Object value, StatementContext ctx)
        {
            return expectedType == Weird.class;
        }

        @Override
        public Argument build(Class<?> expectedType, Weird value, StatementContext ctx)
        {
            return new WeirdArgument();
        }
    }

    private static class WeirdValueArgumentFactory implements ArgumentFactory<Weird>
    {
        @Override
        public boolean accepts(Class<?> expectedType, Object value, StatementContext ctx)
        {
            return value instanceof Weird;
        }

        @Override
        public Argument build(Class<?> expectedType, Weird value, StatementContext ctx)
        {
            return new WeirdArgument();
        }
    }

    private static class WeirdArgument implements Argument
    {

        @Override
        public void apply(int position, PreparedStatement statement, StatementContext ctx) throws SQLException
        {
        }
    }
}