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

package org.bfabric.util;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Objects;
import java.util.logging.Logger;

import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import org.bfabric.Constants;
import org.bfabric.entity.Configuration;
import org.bfabric.entity.Mail;

public class MailTemplateEngine implements Serializable {

    private static final long serialVersionUID = 1;

    private static final Logger logger = Logger.getLogger(MailTemplateEngine.class.getName());

    private static final String MailTemplatesRelativePath = "email-templates/";

    private Configuration configuration;

    private transient freemarker.template.Configuration engine;

    public MailTemplateEngine(Configuration configuration) {
        setConfiguration(configuration);
        // Default configuration as mail template engine till there is another usage.
        setMailTemplateEngine();
    }

    public String buildMailHtmlMessage(Mail mail) throws Exception {
        try {
            mail.setInput("mail", mail);
            Template template = getEngine().getTemplate(mail.getType().getMailTemplateFileName());
            StringWriter stringWriter = new StringWriter();
            template.process(mail.getMailHelper().getInput(), stringWriter);
            return stringWriter.toString();
        } catch (IOException | TemplateException e) {
            logger.warning(e.getMessage());
            throw new Exception("Mail template issue: " + mail.getType().getMailTemplateFileName());
        }
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public freemarker.template.Configuration getEngine() {
        return engine;
    }

    public String getFullPath() {
        String ret = Constants.EMPTY_STRING;
        String fullPath;
        try {
            fullPath = URLDecoder.decode(Objects.requireNonNull(this.getClass().getClassLoader().getResource("")).getPath(), "UTF-8");
            if (fullPath != null) {
                String[] pathArr = fullPath.split("WEB-INF/classes/");
                ret = pathArr[0];
            }
        } catch (UnsupportedEncodingException e) {
            logger.warning(e.getMessage());
        }
        return ret;
    }

    public File getTemplateDirectory(String relativePath) {
        return new File(getFullPath() + relativePath);
    }

    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    private void setEngine(File templateDirectory) {
        // Configure template engine and specify up to what FreeMarker version you want to apply the fixes that are not 100% backward-compatible. See the Configuration JavaDoc for details.
        setEngine(new freemarker.template.Configuration(freemarker.template.Configuration.VERSION_2_3_32));
        try {
            // Core settings
            getEngine().setDefaultEncoding(getConfiguration().getDefaultCharset());
            getEngine().setLogTemplateExceptions(false);
            getEngine().setWrapUncheckedExceptions(true);
            getEngine().setDirectoryForTemplateLoading(templateDirectory);
            getEngine().setTemplateExceptionHandler(getConfiguration().isEnvironmentLocal() ? TemplateExceptionHandler.HTML_DEBUG_HANDLER : TemplateExceptionHandler.RETHROW_HANDLER);
            // Set default number formatting
            getEngine().setNumberFormat("computer");
            // Set objects available in all templates
            getEngine().setSharedVariable("configuration", getConfiguration());
            getEngine().setSharedVariable("mail_style", Constants.MAIL_STYLE);
        } catch (Exception e) {
            logger.fine("Set Mail Template Engine Error: " + e);
        }
    }

    public void setEngine(freemarker.template.Configuration engine) {
        this.engine = engine;
    }

    public void setMailTemplateEngine() {
        setEngine(getTemplateDirectory(MailTemplatesRelativePath));
    }
}
