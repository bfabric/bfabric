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

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.bfabric.entity.Division;

@XmlRootElement(name = "division")
public class XMLDivision extends XMLAbstractNamedBaseEntity {

    @XmlElement
    private Integer bookings;

    @XmlElement
    private XMLCompany company;

    @XmlElement
    private Integer members;

    @XmlElement
    private Integer orders;

    @XmlElement
    private Integer projects;

    public XMLDivision() {

    }

    public XMLDivision(Division entity, boolean reference) {
        super(entity, reference);
    }

    public XMLDivision(Division entity) {
        super(entity);
        if (entity != null) {
            if (!entity.getBookings().isEmpty()) {
                setBookings(entity.getBookings().size());
            }
            if (entity.getCompany() != null) {
                setCompany(new XMLCompany(entity.getCompany(), true));
            }
            if (!entity.getMembers().isEmpty()) {
                setMembers(entity.getMembers().size());
            }
            if (!entity.getOrders().isEmpty()) {
                setOrders(entity.getOrders().size());
            }
            if (!entity.getProjects().isEmpty()) {
                setProjects(entity.getProjects().size());
            }
        }
    }

    public Integer getBookings() {
        return bookings;
    }

    public XMLCompany getCompany() {
        return company;
    }

    public Integer getMembers() {
        return members;
    }

    public Integer getOrders() {
        return orders;
    }

    public Integer getProjects() {
        return projects;
    }

    public void setBookings(Integer bookings) {
        this.bookings = bookings;
    }

    public void setCompany(XMLCompany company) {
        this.company = company;
    }

    public void setMembers(Integer members) {
        this.members = members;
    }

    public void setOrders(Integer orders) {
        this.orders = orders;
    }

    public void setProjects(Integer projects) {
        this.projects = projects;
    }
}
