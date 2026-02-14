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

package org.bfabric.xml.entity;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.bfabric.entity.Service;
import org.bfabric.entity.ServiceType;
import org.bfabric.entity.Technology;

@XmlRootElement(name = "servicetype")
public class XMLServiceType extends XMLAbstractEnabledBaseEntity {

    @XmlElement
    private List<XMLService> service = new ArrayList<>();

    @XmlElement
    private XMLServiceArea servicearea;

    @XmlElement
    private List<String> technology = new ArrayList<>();

    public XMLServiceType(ServiceType entity) {
        super(entity);
        if (entity != null) {
            if (entity.getServices() != null) {
                for (Service aService : entity.getServices()) {
                    getService().add(new XMLService(aService, true));
                }
            }
            if (entity.getServiceArea() != null) {
                setServicearea(new XMLServiceArea(entity.getServiceArea(), true));
            }
            if (entity.getTechnologies() != null) {
                for (Technology aTechnology : entity.getTechnologies()) {
                    getTechnology().add(aTechnology.getName());
                }
            }
        }
    }

    public XMLServiceType() {
    }

    public XMLServiceType(ServiceType entity, boolean reference) {
        super(entity, reference);
    }

    public List<XMLService> getService() {
        return service;
    }

    public XMLServiceArea getServicearea() {
        return servicearea;
    }

    public List<String> getTechnology() {
        return technology;
    }

    public void setService(List<XMLService> service) {
        this.service = service;
    }

    public void setServicearea(XMLServiceArea servicearea) {
        this.servicearea = servicearea;
    }

    public void setTechnology(List<String> technology) {
        this.technology = technology;
    }
}