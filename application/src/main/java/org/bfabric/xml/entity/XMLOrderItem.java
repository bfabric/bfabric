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

import org.bfabric.entity.OrderItem;

@XmlRootElement(name = "orderitem")
public class XMLOrderItem extends XMLAbstractDescriptionBaseEntity {

    @XmlElement
    private String chargeable;

    @XmlElement
    private String insertsize;

    @XmlElement
    private String librarytype;

    @XmlElement
    private String multiplexing;

    @XmlElement
    private XMLOrder order;

    @XmlElement
    private XMLPlate plate;

    @XmlElement
    private String readtype;

    @XmlElement
    private String region;

    @XmlElement
    private XMLSample sample;

    @XmlElement
    private String serviceid;

    @XmlElement
    private String tubeid;

    public XMLOrderItem() {
    }

    public XMLOrderItem(OrderItem entity, boolean reference) {
        super(entity, reference);
    }

    public XMLOrderItem(OrderItem orderItem) {
        super(orderItem);
        if (orderItem != null) {
            setChargeable(Boolean.toString(orderItem.isChargeable()));
            if (orderItem.getInsertSize() != null) {
                setInsertsize(String.valueOf(orderItem.getInsertSize()));
            }
            setLibrarytype(orderItem.getLibraryType());
            if (orderItem.getMultiplexing() != null) {
                setMultiplexing(orderItem.getMultiplexing());
            }
            if (orderItem.getOrder() != null) {
                setOrder(new XMLOrder(orderItem.getOrder(), true));
            }
            setReadtype(orderItem.getReadType());
            setRegion(orderItem.getRegion());
            if (orderItem.getSample() != null) {
                setSample(new XMLSample(orderItem.getSample(), true));
            }
            if (orderItem.getPlate() != null) {
                setPlate(new XMLPlate(orderItem.getPlate(), true));
            }
            if (orderItem.getService() != null) {
                setServiceid(String.valueOf(orderItem.getService().getId()));
            }
            if (orderItem.getTubeId() != null) {
                setTubeid(orderItem.getTubeId());
            }
        }
    }

    public String getChargeable() {
        return chargeable;
    }

    public String getInsertsize() {
        return insertsize;
    }

    public String getLibrarytype() {
        return librarytype;
    }

    public String getMultiplexing() {
        return multiplexing;
    }

    public XMLOrder getOrder() {
        return order;
    }

    public XMLPlate getPlate() {
        return plate;
    }

    public String getReadtype() {
        return readtype;
    }

    public String getRegion() {
        return region;
    }

    public XMLSample getSample() {
        return sample;
    }

    public String getServiceid() {
        return serviceid;
    }

    public String getTubeid() {
        return tubeid;
    }

    public void setChargeable(String chargeable) {
        this.chargeable = chargeable;
    }

    public void setInsertsize(String insertsize) {
        this.insertsize = insertsize;
    }

    public void setLibrarytype(String librarytype) {
        this.librarytype = librarytype;
    }

    public void setMultiplexing(String multiplexing) {
        this.multiplexing = multiplexing;
    }

    public void setOrder(XMLOrder order) {
        this.order = order;
    }

    public void setPlate(XMLPlate plate) {
        this.plate = plate;
    }

    public void setReadtype(String readtype) {
        this.readtype = readtype;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public void setSample(XMLSample sample) {
        this.sample = sample;
    }

    public void setServiceid(String serviceid) {
        this.serviceid = serviceid;
    }

    public void setTubeid(String tubeid) {
        this.tubeid = tubeid;
    }
}
