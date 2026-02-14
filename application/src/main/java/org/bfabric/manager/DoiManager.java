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

package org.bfabric.manager;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.bfabric.Constants;
import org.bfabric.entity.Project;
import org.bfabric.entity.User;
import org.bfabric.enums.SystemPropertyDiscriminator;
import org.bfabric.interceptors.MeasureCalls;
import org.bfabric.service.ConfService;
import org.bfabric.service.ProjectService;
import org.bfabric.service.UserService;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

@MeasureCalls
@Named
@ViewScoped
public class DoiManager extends AbstractManager {

    private static final Logger logger = Logger.getLogger(DoiManager.class.getName());

    private static final String NAMESPACE_PREFIX = "dc";

    private static final String NAMESPACE_URI = "https://purl.org/dc/elements/1.1/";

    private static final long serialVersionUID = 1;

    @Inject
    protected ConfManager confManager;

    @Inject
    protected ConfService confService;

    @Inject
    protected UserService userService;

    @Inject
    private ProjectService projectService;

    // Example: 20010413165812
    private String sinceTimestampString;

    private String getCreatorFullName(Project project) {
        String creatorFullName = Constants.EMPTY_STRING;
        User user = userService.getUserByLogin(project.getDoiCreatedBy());
        if (user != null) {
            creatorFullName = user.getName();
        } else {
            logger.fine("No DOI creator for project " + project.getId() + " found.");
        }
        return creatorFullName;
    }

    public void getDoiFeed() {
        try {
            Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            Element root = document.createElement("bfabric-doi");
            document.appendChild(root);
            for (Project project : getProjects()) {
                Element record = document.createElement("record");
                root.appendChild(record);

                Element title = document.createElementNS(NAMESPACE_URI, NAMESPACE_PREFIX + ":title");
                record.appendChild(title);
                title.appendChild(document.createTextNode(project.getName()));

                Element description = document.createElementNS(NAMESPACE_URI, NAMESPACE_PREFIX + ":description");
                record.appendChild(description);
                description.appendChild(document.createTextNode(project.getSummary()));

                Element publisher = document.createElementNS(NAMESPACE_URI, NAMESPACE_PREFIX + ":publisher");
                record.appendChild(publisher);
                publisher.appendChild(document.createTextNode("Functional Genomics Center Zurich"));

                Element creator = document.createElementNS(NAMESPACE_URI, NAMESPACE_PREFIX + ":creator");
                record.appendChild(creator);
                creator.appendChild(document.createTextNode(getCreatorFullName(project)));

                Element language = document.createElementNS(NAMESPACE_URI, NAMESPACE_PREFIX + ":language");
                record.appendChild(language);
                language.appendChild(document.createTextNode("en"));

                Element date = document.createElementNS(NAMESPACE_URI, NAMESPACE_PREFIX + ":date");
                record.appendChild(date);
                date.appendChild(document.createTextNode(String.valueOf(LocalDate.now().getYear())));

                Element doi = document.createElementNS(NAMESPACE_URI, NAMESPACE_PREFIX + ":identifier");
                record.appendChild(doi);
                doi.appendChild(document.createTextNode("doi:" + getConfiguration().getDoiPrefix() + project.getId()));

                Element url = document.createElementNS(NAMESPACE_URI, NAMESPACE_PREFIX + ":identifier");
                record.appendChild(url);
                url.appendChild(document.createTextNode(getConfiguration().getBaseUrl() + "project/doi.html?projectId=" + project.getId()));
            }

            HttpServletResponse response = (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext().getResponse();

            Result result = new StreamResult(response.getWriter());
            TransformerFactory.newInstance().newTransformer().transform(new DOMSource(document), result);

            response.setContentType("text/xml");
            response.getWriter().flush();

            FacesContext.getCurrentInstance().responseComplete();
        } catch (ParserConfigurationException | IOException | TransformerException | TransformerFactoryConfigurationError e) {
            e.printStackTrace();
        }
    }

    private List<Project> getProjects() {
        List<Project> projects;
        if (getConfiguration() != null && getConfiguration().isDoiUrlModified()) {
            projects = projectService.getProjectsDoiCreated();
            confService.setProperty("doiUrlModified", SystemPropertyDiscriminator.B, "false", "Is the DOI Url modified", getConfiguration().getEnvironment().getValue(), getConfiguration()
                .getDeployer().getValue(), getConfiguration().getInstance().getValue(), null);
        } else {
            try {
                String timestamp = sinceTimestampString.substring(0, 8) + " " + sinceTimestampString.substring(8);
                projects = projectService.getProjectsDoiCreatedAfterTimestamp(timestamp);
            } catch (Exception e) {
                logger.warning("Unable to parse the date: " + sinceTimestampString + " , expected format=yyyyMMddHHmmSS");
                projects = new ArrayList<>();
            }
        }
        return projects;
    }

    public void setSinceTimestampString(String sinceTimestampString) {
        this.sinceTimestampString = sinceTimestampString;
    }
}
