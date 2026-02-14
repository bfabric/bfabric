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

import org.bfabric.entity.Charge;
import org.bfabric.entity.OrderItem;
import org.bfabric.entity.Sample;

@XmlRootElement(name = "charge")
public class XMLCharge extends XMLAbstractCharge {

    @XmlElement
    private String accounted;

    @XmlElement
    private XMLBooking booking;

    @XmlElement
    private XMLContainer container;

    @XmlElement
    private List<XMLOrderItem> orderItem;

    @XmlElement
    private String prepayment;

    @XmlElement
    private List<XMLSample> sample;

    @XmlElement
    private String tax;

    @XmlElement
    private String total;

    public XMLCharge() {
    }

    public XMLCharge(Charge entity, boolean reference) {
        super(entity, reference);
    }

    public XMLCharge(Charge entity) {
        super(entity);
        if (entity != null) {
            setBillable(Boolean.toString(entity.isBillable()));
            if (entity.getDate() != null) {
                setDate(entity.getDate().toString());
            }
            setPrepayment(Boolean.toString(entity.isPrePayment()));
            if (entity.getContainer() != null) {
                setContainer(new XMLContainer(entity.getContainer(), true));
            }
            if (entity.getBooking() != null) {
                setBooking(new XMLBooking(entity.getBooking(), true));
            }
            setTotal(entity.getTotal().toString());
            total = String.valueOf(entity.getTotal());
            accounted = String.valueOf(entity.getAccounted());
            if (entity.getOrderItems() != null && !entity.getOrderItems().isEmpty()) {
                orderItem = new ArrayList<>();
                for (OrderItem orderItem : entity.getOrderItems()) {
                    getOrderItem().add(new XMLOrderItem(orderItem, true));
                }
            }
            if (entity.getSamples() != null && !entity.getSamples().isEmpty()) {
                sample = new ArrayList<>();
                for (Sample sample : entity.getSamples()) {
                    getSample().add(new XMLSample(sample, true));
                }
            }
            if (entity.getTaxRate() != null) {
                setTaxrate(entity.getTaxRate().toString());
            }
            if (entity.getTax() != null) {
                setTaxrate(entity.getTax().toString());
            }
        }
    }

    public String getAccounted() {
        return accounted;
    }

    public XMLBooking getBooking() {
        return booking;
    }

    public XMLContainer getContainer() {
        return container;
    }

    public List<XMLOrderItem> getOrderItem() {
        return orderItem;
    }

    public String getPrepayment() {
        return prepayment;
    }

    public List<XMLSample> getSample() {
        return sample;
    }

    public String getTax() {
        return tax;
    }

    public String getTotal() {
        return total;
    }

    public void setAccounted(String accounted) {
        this.accounted = accounted;
    }

    public void setBooking(XMLBooking booking) {
        this.booking = booking;
    }

    public void setContainer(XMLContainer container) {
        this.container = container;
    }

    public void setOrderItem(List<XMLOrderItem> orderItem) {
        this.orderItem = orderItem;
    }

    public void setPrepayment(String prepayment) {
        this.prepayment = prepayment;
    }

    public void setSample(List<XMLSample> sample) {
        this.sample = sample;
    }

    public void setTax(String tax) {
        this.tax = tax;
    }

    public void setTotal(String total) {
        this.total = total;
    }

}
