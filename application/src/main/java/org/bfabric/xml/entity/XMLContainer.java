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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

import org.bfabric.entity.Charge;
import org.bfabric.entity.Container;
import org.bfabric.entity.ContainerStatus;
import org.bfabric.entity.Order;
import org.bfabric.entity.Technology;
import org.bfabric.entity.User;

@XmlAccessorType(XmlAccessType.NONE)
public class XMLContainer extends XMLAbstractNamedBaseEntity {

    @XmlElement
    protected Boolean internal;

    @XmlElement
    protected Boolean orderdataonly;

    @XmlElement
    private String billingaccountcostcentre;

    @XmlElement
    private String billingaccounteugrant;

    @XmlElement
    private String billingaccountpspelement;

    @XmlElement
    private String billingaddress;

    @XmlElement
    private String billingcustomer;

    @XmlElement
    private String billingemail;

    @XmlElement
    private XMLUser bioinformatician;

    @XmlElement
    private String budgetlimit;

    @XmlElement
    private XMLUser budgetofficer;

    @XmlElement
    private String budgetremaining;

    @XmlElement
    private List<XMLCharge> charge = new ArrayList<>();

    @XmlElement
    private XMLUser coach;

    @XmlElement
    private XMLUser coachbackup;

    @XmlElement
    private XMLUser contact;

    @XmlElement
    private List<XMLContainerStatus> containerstatus = new ArrayList<>();

    @XmlElement
    private Integer countbookings;

    @XmlElement
    private Integer countcharges;

    @XmlElement
    private Integer countcomments;

    @XmlElement
    private Integer countdatasets;

    @XmlElement
    private Integer countformermembers;

    @XmlElement
    private Integer countinstrumentreservations;

    @XmlElement
    private Integer countmembers;

    @XmlElement
    private Integer countoffers;

    @XmlElement
    private Integer countresources;

    @XmlElement
    private Integer countsamples;

    @XmlElement
    private Integer countstates;

    @XmlElement
    private Integer countworkflows;

    @XmlElement
    private Integer countworkunits;

    @XmlElement
    private List<XMLUser> discussedwith = new ArrayList<>();

    @XmlElement
    private String division;

    @XmlElement
    private List<XMLUser> formermember = new ArrayList<>();

    @XmlElement
    private String institute;

    @XmlElement
    private String label;

    @XmlElement
    private XMLUser leader;

    @XmlElement
    private List<XMLUser> member = new ArrayList<>();

    @XmlElement
    private List<XMLOrder> order = new ArrayList<>();

    @XmlElement
    private XMLProject project;

    @XmlElement
    private String referencenumber;

    @XmlElement
    private XMLUser requester;

    @XmlElement
    private String status;

    @XmlElement
    private String statusmodified;

    @XmlElement
    private XMLUser statusmodifiedby;

    @XmlElement
    private String syncable;

    @XmlElement
    private List<String> technology = new ArrayList<>();

    @XmlElement
    private String vatnumber;

    public XMLContainer() {
    }

    public XMLContainer(Container entity, boolean reference) {
        super(entity, reference);
    }

    public XMLContainer(Container entity) {
        super(entity);
        if (entity != null) {
            setSyncable(String.valueOf(entity.isStatusSyncable()));
            setLabel(entity.getClassLabel());
            if (entity.getProject() != null) {
                setProject(new XMLProject(entity.getProject(), true));
            }
            if (entity.getInstitute() != null) {
                setInstitute(entity.getInstitute().getIdString());
            }
            if (entity.getDivision() != null) {
                setDivision(entity.getDivision().getIdString());
            }
            if (entity.getBillingInfo() != null) {
                setBillingcustomer(entity.getBillingInfo().getBillingCustomerName());
                setBillingaddress(entity.getBillingInfo().getBillingAddressFull());
                setBillingemail(entity.getBillingInfo().getBillingEmail());
                setVatnumber(entity.getBillingInfo().getVatNumber());
                setReferencenumber(entity.getBillingInfo().getReferenceNumber());
            }
            if (entity.getCostCentre() != null) {
                setBillingaccountcostcentre(entity.getCostCentre());
            }
            if (entity.getPspElement() != null) {
                setBillingaccountpspelement(entity.getPspElement());
            }
            if (entity.getEuGrant() != null) {
                setBillingaccounteugrant(entity.getEuGrant().toString());
            }
            if (entity.getBioinformatician() != null) {
                setBioinformatician(new XMLUser(entity.getBioinformatician(), true));
            }
            if (entity.getCoach() != null) {
                setCoach(new XMLUser(entity.getCoach(), true));
            }
            if (entity.getCoachBackup() != null) {
                setCoachbackup(new XMLUser(entity.getCoachBackup(), true));
            }
            if (entity.getContact() != null) {
                setContact(new XMLUser(entity.getContact(), true));
            }
            if (entity.getBudgetOfficer() != null) {
                setBudgetofficer(new XMLUser(entity.getBudgetOfficer(), true));
            }
            if (entity.getBudgetLimit() != null) {
                setBudgetlimit(entity.getBudgetLimit().toString());
            }
            if (entity.getBudgetRemaining() != null) {
                setBudgetremaining(entity.getBudgetRemaining().toString());
            }
            if (entity.getRequester() != null) {
                setRequester(new XMLUser(entity.getRequester(), true));
            }
            if (entity.getLeader() != null) {
                setLeader(new XMLUser(entity.getLeader(), true));
            }
            if (entity.getStatus() != null) {
                setStatus(entity.getStatus().getLabel());
            }
            if (entity.getStatusModified() != null) {
                setStatusmodified(String.valueOf(entity.getStatusModified()));
            }
            if (entity.getStatusModifiedBy() != null) {
                setStatusmodifiedby(new XMLUser(entity.getStatusModifiedBy(), true));
            }
            setInternal(entity.isInternal());
            setOrderdataonly(entity.isOrderDataOnly());
            if (entity.getMembersTransitiveSorted() != null) {
                setCountmembers(entity.getMembersTransitiveSorted().size());
                for (User user : entity.getMembersTransitiveSorted()) {
                    getMember().add(new XMLUser(user, true));
                }
            }
            if (entity.getMembersFormerTransitiveSorted() != null) {
                setCountformermembers(entity.getMembersFormerTransitiveSorted().size());
                for (User user : entity.getMembersFormerTransitiveSorted()) {
                    getFormermember().add(new XMLUser(user, true));
                }
            }
            if (entity.getCharges() != null) {
                for (Charge aCharge : entity.getCharges()) {
                    getCharge().add(new XMLCharge(aCharge, true));
                }
            }
            if (entity.getDiscussedWith() != null) {
                for (User user : entity.getDiscussedWith()) {
                    getDiscussedwith().add(new XMLUser(user, true));
                }
            }
            if (entity.getOrders() != null) {
                for (Order aOrder : entity.getOrders()) {
                    getOrder().add(new XMLOrder(aOrder, true));
                }
            }
            if (entity.getTechnologies() != null) {
                for (Technology aTechnology : entity.getTechnologies()) {
                    getTechnology().add(aTechnology.getName());
                }
            }
            if (entity.getAllStates() != null) {
                for (ContainerStatus containerStatus : entity.getAllStates()) {
                    getContainerstatus().add(new XMLContainerStatus(containerStatus, true));
                }
                setCountstates(entity.getAllStates().size());
            }
            if (entity.getSamples() != null) {
                setCountsamples(entity.getSamples().size());
            }
            if (entity.getResources() != null) {
                setCountresources(entity.getResources().size());
            }
            if (entity.getWorkunits() != null) {
                setCountworkunits(entity.getWorkunits().size());
            }
            if (entity.getDatasets() != null) {
                setCountdatasets(entity.getDatasets().size());
            }
            if (entity.getCharges() != null) {
                setCountcharges(entity.getCharges().size());
            }
            if (entity.getBookings() != null) {
                setCountbookings(entity.getBookings().size());
            }
            if (entity.getOffers() != null) {
                setCountoffers(entity.getOffers().size());
            }
            if (entity.getInstrumentReservations() != null) {
                setCountinstrumentreservations(entity.getInstrumentReservations().size());
            }
            if (entity.getComments() != null) {
                setCountcomments(entity.getComments().size());
            }
            if (entity.getWorkflows() != null) {
                setCountworkflows(entity.getWorkflows().size());
            }
        }
    }

    public String getBillingaccountcostcentre() {
        return billingaccountcostcentre;
    }

    public String getBillingaccounteugrant() {
        return billingaccounteugrant;
    }

    public String getBillingaccountpspelement() {
        return billingaccountpspelement;
    }

    public String getBillingaddress() {
        return billingaddress;
    }

    public String getBillingcustomer() {
        return billingcustomer;
    }

    public String getBillingemail() {
        return billingemail;
    }

    public XMLUser getBioinformatician() {
        return bioinformatician;
    }

    public String getBudgetlimit() {
        return budgetlimit;
    }

    public XMLUser getBudgetofficer() {
        return budgetofficer;
    }

    public String getBudgetremaining() {
        return budgetremaining;
    }

    public List<XMLCharge> getCharge() {
        return charge;
    }

    public XMLUser getCoach() {
        return coach;
    }

    public XMLUser getCoachbackup() {
        return coachbackup;
    }

    public XMLUser getContact() {
        return contact;
    }

    public List<XMLContainerStatus> getContainerstatus() {
        return containerstatus;
    }

    public Integer getCountbookings() {
        return countbookings;
    }

    public Integer getCountcharges() {
        return countcharges;
    }

    public Integer getCountcomments() {
        return countcomments;
    }

    public Integer getCountdatasets() {
        return countdatasets;
    }

    public Integer getCountformermembers() {
        return countformermembers;
    }

    public Integer getCountinstrumentreservations() {
        return countinstrumentreservations;
    }

    public Integer getCountmembers() {
        return countmembers;
    }

    public Integer getCountoffers() {
        return countoffers;
    }

    public Integer getCountresources() {
        return countresources;
    }

    public Integer getCountsamples() {
        return countsamples;
    }

    public Integer getCountstates() {
        return countstates;
    }

    public Integer getCountworkflows() {
        return countworkflows;
    }

    public Integer getCountworkunits() {
        return countworkunits;
    }

    public List<XMLUser> getDiscussedwith() {
        return discussedwith;
    }

    public String getDivision() {
        return division;
    }

    public List<XMLUser> getFormermember() {
        return formermember;
    }

    public String getInstitute() {
        return institute;
    }

    public Boolean getInternal() {
        return internal;
    }

    public String getLabel() {
        return label;
    }

    public XMLUser getLeader() {
        return leader;
    }

    public List<XMLUser> getMember() {
        return member;
    }

    public List<XMLOrder> getOrder() {
        return order;
    }

    public Boolean getOrderdataonly() {
        return orderdataonly;
    }

    public XMLProject getProject() {
        return project;
    }

    public String getReferencenumber() {
        return referencenumber;
    }

    public XMLUser getRequester() {
        return requester;
    }

    public String getStatus() {
        return status;
    }

    public String getStatusmodified() {
        return statusmodified;
    }

    public XMLUser getStatusmodifiedby() {
        return statusmodifiedby;
    }

    public String getSyncable() {
        return syncable;
    }

    public List<String> getTechnology() {
        return technology;
    }

    public String getVatnumber() {
        return vatnumber;
    }

    public void setBillingaccountcostcentre(String billingaccountcostcentre) {
        this.billingaccountcostcentre = billingaccountcostcentre;
    }

    public void setBillingaccounteugrant(String billingaccounteugrant) {
        this.billingaccounteugrant = billingaccounteugrant;
    }

    public void setBillingaccountpspelement(String billingaccountpspelement) {
        this.billingaccountpspelement = billingaccountpspelement;
    }

    public void setBillingaddress(String billingaddress) {
        this.billingaddress = billingaddress;
    }

    public void setBillingcustomer(String billingcustomer) {
        this.billingcustomer = billingcustomer;
    }

    public void setBillingemail(String billingemail) {
        this.billingemail = billingemail;
    }

    public void setBioinformatician(XMLUser bioinformatician) {
        this.bioinformatician = bioinformatician;
    }

    public void setBudgetlimit(String budgetlimit) {
        this.budgetlimit = budgetlimit;
    }

    public void setBudgetofficer(XMLUser budgetofficer) {
        this.budgetofficer = budgetofficer;
    }

    public void setBudgetremaining(String budgetremaining) {
        this.budgetremaining = budgetremaining;
    }

    public void setCharge(List<XMLCharge> charge) {
        this.charge = charge;
    }

    public void setCoach(XMLUser coach) {
        this.coach = coach;
    }

    public void setCoachbackup(XMLUser coachbackup) {
        this.coachbackup = coachbackup;
    }

    public void setContact(XMLUser contact) {
        this.contact = contact;
    }

    public void setContainerstatus(List<XMLContainerStatus> containerstatus) {
        this.containerstatus = containerstatus;
    }

    public void setCountbookings(Integer countbookings) {
        this.countbookings = countbookings;
    }

    public void setCountcharges(Integer countcharges) {
        this.countcharges = countcharges;
    }

    public void setCountcomments(Integer countcomments) {
        this.countcomments = countcomments;
    }

    public void setCountdatasets(Integer countdatasets) {
        this.countdatasets = countdatasets;
    }

    public void setCountformermembers(Integer countformermembers) {
        this.countformermembers = countformermembers;
    }

    public void setCountinstrumentreservations(Integer countinstrumentreservations) {
        this.countinstrumentreservations = countinstrumentreservations;
    }

    public void setCountmembers(Integer countmembers) {
        this.countmembers = countmembers;
    }

    public void setCountoffers(Integer countoffers) {
        this.countoffers = countoffers;
    }

    public void setCountresources(Integer countresources) {
        this.countresources = countresources;
    }

    public void setCountsamples(Integer countsamples) {
        this.countsamples = countsamples;
    }

    public void setCountstates(Integer countstates) {
        this.countstates = countstates;
    }

    public void setCountworkflows(Integer countworkflows) {
        this.countworkflows = countworkflows;
    }

    public void setCountworkunits(Integer countworkunits) {
        this.countworkunits = countworkunits;
    }

    public void setDiscussedwith(List<XMLUser> discussedwith) {
        this.discussedwith = discussedwith;
    }

    public void setDivision(String division) {
        this.division = division;
    }

    public void setFormermember(List<XMLUser> formermember) {
        this.formermember = formermember;
    }

    public void setInstitute(String institute) {
        this.institute = institute;
    }

    public void setInternal(Boolean internal) {
        this.internal = internal;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public void setLeader(XMLUser leader) {
        this.leader = leader;
    }

    public void setMember(List<XMLUser> member) {
        this.member = member;
    }

    public void setOrder(List<XMLOrder> order) {
        this.order = order;
    }

    public void setOrderdataonly(Boolean orderdataonly) {
        this.orderdataonly = orderdataonly;
    }

    public void setProject(XMLProject project) {
        this.project = project;
    }

    public void setReferencenumber(String referencenumber) {
        this.referencenumber = referencenumber;
    }

    public void setRequester(XMLUser requester) {
        this.requester = requester;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setStatusmodified(String statusmodified) {
        this.statusmodified = statusmodified;
    }

    public void setStatusmodifiedby(XMLUser statusmodifiedby) {
        this.statusmodifiedby = statusmodifiedby;
    }

    public void setSyncable(String syncable) {
        this.syncable = syncable;
    }

    public void setTechnology(List<String> technology) {
        this.technology = technology;
    }

    public void setVatnumber(String vatnumber) {
        this.vatnumber = vatnumber;
    }
}
