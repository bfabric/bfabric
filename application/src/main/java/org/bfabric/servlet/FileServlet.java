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

package org.bfabric.servlet;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.ehcache.util.FindBugsSuppressWarnings;

@WebServlet(urlPatterns = "/filedownload/*")
public class FileServlet extends HttpServlet {

    private static final long serialVersionUID = 1;

    @SuppressWarnings("RedundantThrows")
    @Override
    @FindBugsSuppressWarnings("PT_ABSOLUTE_PATH_TRAVERSAL")
    protected void doGet(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException {
        if (httpServletRequest != null && httpServletResponse != null) {
            String fileName = Paths.get(httpServletRequest.getPathInfo()).normalize().toString().substring(1);
            String selectedDownloadValue = Paths.get(httpServletRequest.getParameter("selectedDownloadValue")).normalize().toString().replaceAll("/../", "/");
            String reportPath = "/opt/download/" + selectedDownloadValue + "/" + fileName;
            File file = new File(reportPath);
            if (!file.exists()) {
                httpServletResponse.sendError(HttpServletResponse.SC_NOT_FOUND, "No file " + reportPath);
                return;
            }
            httpServletResponse.setHeader("Content-Length", String.valueOf(file.length()));
            httpServletResponse.setHeader("Content-Type", getServletContext().getMimeType(fileName));
            httpServletResponse.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
            Files.copy(file.toPath(), httpServletResponse.getOutputStream());
            httpServletResponse.sendRedirect(httpServletRequest.getContextPath());
        }
    }
}