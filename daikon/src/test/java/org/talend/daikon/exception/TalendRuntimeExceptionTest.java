// ============================================================================
//
// Copyright (C) 2006-2015 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.daikon.exception;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.Arrays;

import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.talend.daikon.exception.error.CommonErrorCodes;

public class TalendRuntimeExceptionTest {

    @Test
    public void createTalendRuntimeExceptionWithOnlyCode() {
        TalendRuntimeException talendRuntimeException = new TalendRuntimeException(CommonErrorCodes.UNABLE_TO_PARSE_JSON, null,
                null);
        assertNotNull(talendRuntimeException);
    }

    @Test
    public void shouldBeWrittenEntirely() throws Exception {

        TalendRuntimeException exception = new TalendRuntimeException(
                org.talend.daikon.exception.error.CommonErrorCodes.UNEXPECTED_EXCEPTION, new NullPointerException("root cause"),
                ExceptionContext.build().put("key 1", "Value 1").put("key 2", 123).put("key 3",
                        Arrays.asList(true, false, true)));

        String expected = read(TalendRuntimeExceptionTest.class.getResourceAsStream("expected-exception.json"));

        StringWriter writer = new StringWriter();
        exception.writeTo(writer);
        JSONAssert.assertEquals(expected, writer.toString(), false);
    }

    @Test
    public void unexpected1() throws Exception {
        try {
            TalendRuntimeException.unexpectedException("messsage");
            fail("No exception");
        } catch (Exception ex) {
            assertTrue(ex instanceof TalendRuntimeException);
            assertEquals(CommonErrorCodes.UNEXPECTED_EXCEPTION, ((TalendRuntimeException) ex).getCode());
            assertEquals("UNEXPECTED_EXCEPTION:{message=messsage}", ex.getMessage());
        }
    }

    @Test
    public void unexpected2() throws Exception {
        try {
            TalendRuntimeException.unexpectedException(new Exception("test exception"));
            fail("No exception");
        } catch (Exception ex) {
            assertTrue(ex instanceof TalendRuntimeException);
            assertEquals("test exception", ((TalendRuntimeException) ex).getCause().getMessage());
            assertEquals(CommonErrorCodes.UNEXPECTED_EXCEPTION, ((TalendRuntimeException) ex).getCode());
        }
    }

    @Test
    public void testExceptionBuilder() {
        TalendRuntimeException talendRuntimeException = TalendRuntimeException.build(CommonErrorCodes.UNEXPECTED_EXCEPTION)
                .create();
        assertEquals(CommonErrorCodes.UNEXPECTED_EXCEPTION, talendRuntimeException.getCode());
    }

    @Test
    public void testExceptionBuilderWithSetParams() {
        TalendRuntimeException talendRuntimeException = TalendRuntimeException.build(CommonErrorCodes.UNEXPECTED_ARGUMENT)
                .set("foo", "bar");
        assertEquals(CommonErrorCodes.UNEXPECTED_ARGUMENT, talendRuntimeException.getCode());
        assertEquals("foo", talendRuntimeException.getContext().get("argument"));
        assertEquals("bar", talendRuntimeException.getContext().get("value"));
    }

    @Test
    public void testExceptionBuilderWithSetWrongParams() {
        // fewer param
        TalendRuntimeException talendRuntimeException = TalendRuntimeException.build(CommonErrorCodes.UNEXPECTED_ARGUMENT)
                .set("foo");
        assertEquals(CommonErrorCodes.UNEXPECTED_ARGUMENT, talendRuntimeException.getCode());
        assertEquals("foo", talendRuntimeException.getContext().get("argument"));
        assertEquals(null, talendRuntimeException.getContext().get("value"));
        // more param
        talendRuntimeException = TalendRuntimeException.build(CommonErrorCodes.UNEXPECTED_ARGUMENT).set("foo", "bar", "xx");
        assertEquals(CommonErrorCodes.UNEXPECTED_ARGUMENT, talendRuntimeException.getCode());
        assertEquals("foo", talendRuntimeException.getContext().get("argument"));
        assertEquals("bar", talendRuntimeException.getContext().get("value"));
    }

    @Test
    public void testExceptionBuilderWithPutParams() {
        TalendRuntimeException talendRuntimeException = TalendRuntimeException.build(CommonErrorCodes.UNEXPECTED_ARGUMENT)
                .put("argument", "foo").put("value", "bar").create();
        assertEquals(CommonErrorCodes.UNEXPECTED_ARGUMENT, talendRuntimeException.getCode());
        assertEquals("foo", talendRuntimeException.getContext().get("argument"));
        assertEquals("bar", talendRuntimeException.getContext().get("value"));
    }

    @Test
    public void testExceptionBuilderWithPutWrongParams() {
        // fewer param
        TalendRuntimeException talendRuntimeException = TalendRuntimeException.build(CommonErrorCodes.UNEXPECTED_ARGUMENT)
                .put("argument", "foo").create();
        assertEquals(CommonErrorCodes.UNEXPECTED_ARGUMENT, talendRuntimeException.getCode());
        assertEquals("foo", talendRuntimeException.getContext().get("argument"));
        assertEquals(null, talendRuntimeException.getContext().get("value"));
        // more params
        talendRuntimeException = TalendRuntimeException.build(CommonErrorCodes.UNEXPECTED_ARGUMENT).put("argument", "foo")
                .put("value", "bar").put("unknow", "xx").create();
        assertEquals(CommonErrorCodes.UNEXPECTED_ARGUMENT, talendRuntimeException.getCode());
        assertEquals("foo", talendRuntimeException.getContext().get("argument"));
        assertEquals("bar", talendRuntimeException.getContext().get("value"));
        assertEquals(null, talendRuntimeException.getContext().get("unknow"));
    }

    @Test
    public void testExceptionBuilderThrow() {
        try {
            TalendRuntimeException.build(CommonErrorCodes.UNEXPECTED_ARGUMENT).put("argument", "foo").throwIt();
            fail("exception should be ");
        } catch (TalendRuntimeException e) {
            // fine we have caught it
            assertEquals(CommonErrorCodes.UNEXPECTED_ARGUMENT, e.getCode());
            assertEquals("foo", e.getContext().get("argument"));
        }
        try {
            TalendRuntimeException.build(CommonErrorCodes.UNEXPECTED_ARGUMENT).setAndThrow("foo", "bar");
            fail("exception should be ");
        } catch (TalendRuntimeException e) {
            // fine we have caught it
            assertEquals(CommonErrorCodes.UNEXPECTED_ARGUMENT, e.getCode());
            assertEquals("foo", e.getContext().get("argument"));
            assertEquals("bar", e.getContext().get("value"));
        }
    }

    /**
     * Return the given inputstream as a String.
     * 
     * @param input the input stream to read.
     * @return the given inputstream content.
     * @throws IOException if an error occurred.
     */
    private String read(InputStream input) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(input));
        StringBuilder content = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            content.append(line);
        }
        return content.toString();
    }
}
