/*
    MIT License

    Copyright (c) 2005-2026 Functional Genomics Center Zurich, UZH/ETH Zurich

    Permission is hereby granted, free of charge, to any person obtaining a copy
    of this software and associated documentation files (the "Software"), to deal
    in the Software without restriction, including without limitation the rights
    to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
    copies of the Software, and to permit persons to whom the Software is
    furnished to do so, subject to the following conditions:

    The above copyright notice and this permission notice shall be included in all
    copies or substantial portions of the Software.

    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
    IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
    AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
    LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
    OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
    SOFTWARE.
 */

package org.bfabric.enums;

import java.util.Arrays;

import org.bfabric.exception.InvalidEnumValueException;
import org.bfabric.util.CollectionHelper;

public enum LogActionEnum {

    ACCESS,
    ACKNOWLEDGE,
    APPROVE,
    APPROVE_REPORT,
    APPROVE_FINAL,
    ARCHIVE,
    COPY,
    CREATE,
    CLOSE,
    DELETE,
    DISABLE,
    DOWNLOAD,
    DOWNLOAD_HTTP,
    DOWNLOAD_FOLDER,
    EMPLOYEE_ENTRY,
    EMPLOYEE_LEAVE,
    ENABLE,
    EXECUTE,
    FINISH,
    FINISH_ANNOUNCE,
    LOGIN,
    LOGOUT,
    LOGOUT_TIMEOUT,
    LOGOUT_RESTART,
    MOVE,
    PASSWORD_CHANGE,
    PRIVATE,
    PRIVATE_ANNOUNCE,
    PUBLISH,
    PUBLISH_GRANT,
    REJECT,
    REJECT_FINAL,
    REVIEW,
    RUNNING,
    SEND_MAIL,
    SUBMIT,
    SYSTEM_RELEASE,
    SYSTEM_START,
    SYSTEM_STOP,
    TRANSFER,
    UNARCHIVE,
    UNKNOWN,
    UPDATE;

    public static LogActionEnum value(String name) throws InvalidEnumValueException {
        try {
            return name != null ? valueOf(name.toUpperCase()) : null;
        } catch (IllegalArgumentException iae) {
            throw new InvalidEnumValueException("action", name, CollectionHelper.print(Arrays.asList(values())));
        }
    }

    public String getLabel() {
        return name().toLowerCase();
    }
}
