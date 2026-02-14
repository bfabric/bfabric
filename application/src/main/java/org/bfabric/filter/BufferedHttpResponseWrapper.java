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

package org.bfabric.filter;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import org.bfabric.util.ConfigurationHelper;

public class BufferedHttpResponseWrapper extends HttpServletResponseWrapper {

    private final BufferedServletOutputStream bufferedServletOutputStream = new BufferedServletOutputStream();

    private ServletOutputStream outputStream = null;

    private PrintWriter printWriter = null;

    public BufferedHttpResponseWrapper(HttpServletResponse response) {
        super(response);
    }

    @Override
    public void flushBuffer() throws IOException {
        if (outputStream != null) {
            outputStream.flush();
        } else if (printWriter != null) {
            printWriter.flush();
        }
    }

    public byte[] getBuffer() {
        return bufferedServletOutputStream.getBuffer();
    }

    public String getBufferContent() {
        if (printWriter == null) {
            return "Print writer not yet initialized";
        }
        // printWriter.flush();
        try {
            return new String(bufferedServletOutputStream.getBuffer(), ConfigurationHelper.getConfiguration().getDefaultCharset());
        } catch (Exception e) {
            return "could not build String, cause: " + e.getMessage();
        }
    }

    @Override
    public int getBufferSize() {
        return bufferedServletOutputStream.getBuffer().length;
    }

    @Override
    public ServletOutputStream getOutputStream() {
        if (printWriter != null) {
            throw new IllegalStateException("The Servlet API forbids calling getOutputStream( ) after" + " getWriter( ) has been called");
        }
        if (outputStream == null) {
            outputStream = bufferedServletOutputStream;
        }
        return outputStream;
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        if (outputStream != null) {
            throw new IllegalStateException("The Servlet API forbids calling getWriter( ) after" + " getOutputStream( ) has been called");
        }
        if (printWriter == null) {
            printWriter = new PrintWriter(new OutputStreamWriter(bufferedServletOutputStream, ConfigurationHelper.getConfiguration().getDefaultCharset()));
        }
        return printWriter;
    }

    @Override
    public void reset() {
        bufferedServletOutputStream.reset();
    }

    @Override
    public void resetBuffer() {
        bufferedServletOutputStream.reset();
    }

    @Override
    public void setBufferSize(int size) {
        bufferedServletOutputStream.setBufferSize(size);
    }
}