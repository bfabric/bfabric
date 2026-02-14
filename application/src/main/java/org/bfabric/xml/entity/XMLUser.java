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

import org.bfabric.entity.Container;
import org.bfabric.entity.Order;
import org.bfabric.entity.Project;
import org.bfabric.entity.Role;
import org.bfabric.entity.User;
import org.bfabric.util.StringHelper;

@XmlRootElement(name = "user")
public class XMLUser extends XMLAbstractDescriptionBaseEntity {

    @XmlElement
    private String accountenabled;

    @XmlElement
    private String active;

    @XmlElement
    private String address;

    @XmlElement
    private String addresscity;

    @XmlElement
    private String addresscountry;

    @XmlElement
    private String addresscountrycode;

    @XmlElement
    private String addressstreet;

    @XmlElement
    private String addresssupplement;

    @XmlElement
    private String addresszip;

    @XmlElement
    private List<XMLOrder> coachedorder = new ArrayList<>();

    @XmlElement
    private List<XMLProject> coachedproject = new ArrayList<>();

    @XmlElement
    private String company;

    @XmlElement
    private String computerloginactivated;

    @XmlElement
    private String computerloginenabled;

    @XmlElement
    private String dataaccessenabled;

    @XmlElement
    private String department;

    @XmlElement
    private String division;

    @XmlElement
    private String divisionid;

    @XmlElement
    private String email;

    @XmlElement
    private String emailverified;

    @XmlElement
    private String empdegree;

    @XmlElement
    private String firstname;

    @XmlElement
    private List<XMLProject> formerproject = new ArrayList<>();

    @XmlElement
    private String homeaddress;

    @XmlElement
    private String homeaddresscity;

    @XmlElement
    private String homeaddresscountry;

    @XmlElement
    private String homeaddressstreet;

    @XmlElement
    private String homeaddresssupplement;

    @XmlElement
    private String homeaddresszip;

    @XmlElement
    private String homeaddresscountrycode;

    @XmlElement
    private String homephoneareacode;

    @XmlElement
    private String homephonecountrycode;

    @XmlElement
    private String homephonelocalnumber;

    @XmlElement
    private String institute;

    @XmlElement
    private String instituteid;

    @XmlElement
    private String lastname;

    @XmlElement
    private String login;

    @XmlElement
    private String massmailenabled;

    @XmlElement
    private List<XMLOrder> order = new ArrayList<>();

    @XmlElement
    private String organzation;

    @XmlElement
    private String organzationtype;

    @XmlElement
    private String phoneareacode;

    @XmlElement
    private String phonecountrycode;

    @XmlElement
    private String phonelocalnumber;

    @XmlElement
    private String privateemail;

    @XmlElement
    private List<XMLProject> project = new ArrayList<>();

    @XmlElement
    private List<XMLRole> role = new ArrayList<>();

    @XmlElement
    private String room;

    @XmlElement
    private String salutation;

    @XmlElement
    private String secret;

    @XmlElement
    private String shibbolethid;

    @XmlElement
    private String sshpublickey;

    @XmlElement
    private String technology;

    @XmlElement
    private String title;

    @XmlElement
    private List<XMLContainer> trackablecontainer = new ArrayList<>();

    public XMLUser(User entity) {
        super(entity);
        if (entity != null) {
            setAccountenabled(Boolean.toString(entity.isAccountEnabled()));
            setComputerloginenabled(Boolean.toString(entity.isComputerLoginEnabled()));
            setComputerloginactivated(Boolean.toString(entity.isComputerLoginActivated()));
            setDataaccessenabled(Boolean.toString(entity.isDataAccessEnabled()));
            setActive(Boolean.toString(entity.isEmailActive()));
            setEmailverified(Boolean.toString(entity.isEmailVerified()));
            if (entity.getInstitute() != null) {
                setInstituteid(entity.getInstitute().getIdString());
                setInstitute(entity.getInstitute().getName());
                setDepartment(entity.getInstitute().getDepartmentName());
                setOrganzation(entity.getInstitute().getOrganizationName());
                setOrganzationtype(entity.getInstitute().getOrganizationTypeName());
            }
            if (entity.getDivision() != null) {
                setDivisionid(entity.getDivision().getIdString());
                setDivision(entity.getDivision().getName());
                setCompany(entity.getDivision().getCompanyName());
                setOrganzationtype(entity.getDivision().getOrganizationTypeName());
            }
            if (entity.getAddress() != null) {
                setAddress(entity.getAddress().getFullAddress());
                setAddressstreet(entity.getAddress().getStreet());
                setAddresssupplement(entity.getAddress().getSupplement());
                setAddresscity(entity.getAddress().getCity());
                setAddresscountry(entity.getAddress().getCountry().getId());
                setAddresszip(entity.getAddress().getZip());
                setRoom(entity.getAddress().getRoom());
                if (entity.getAddress().getCountry() != null) {
                    setAddresscountrycode(entity.getAddress().getCountry().getId());
                    setAddresscountry(entity.getAddress().getCountry().getName());
                }
            }
            if (entity.getCoachedContainers() != null) {
                for (Container container : entity.getCoachedContainers()) {
                    if (container.isContainerProject()) {
                        getCoachedproject().add(new XMLProject(container, true));
                    } else {
                        getCoachedorder().add(new XMLOrder(container, true));
                    }
                }
            }
            if (entity.getEmail() != null) {
                setEmail(entity.getEmail());
            }
            if (entity.getPrivateEmail() != null) {
                setPrivateemail(entity.getPrivateEmail());
            }
            if (entity.getEmpDegree() != null) {
                setEmpdegree(String.valueOf(entity.getEmpDegree()));
            }
            if (entity.getFirstName() != null) {
                setFirstname(entity.getFirstName());
            }
            if (entity.getSalutation() != null) {
                setSalutation(entity.getSalutation());
            }
            if (entity.getHomeAddress() != null) {
                setHomeaddress(entity.getHomeAddress().getFullAddress());
                if (entity.getHomeAddress().getStreet() != null) {
                    setHomeaddressstreet(entity.getHomeAddress().getStreet());
                }
                if (entity.getHomeAddress().getZip() != null) {
                    setHomeaddresszip(entity.getHomeAddress().getZip());
                }
                if (entity.getHomeAddress().getCity() != null) {
                    setHomeaddresscity(entity.getHomeAddress().getCity());
                }
                if (entity.getHomeAddress().getCountry() != null) {
                    setHomeaddresscountrycode(entity.getHomeAddress().getCountry().getId());
                    setHomeaddresscountry(entity.getHomeAddress().getCountry().getName());
                }
                if (entity.getHomeAddress().getSupplement() != null) {
                    setHomeaddresssupplement(entity.getHomeAddress().getSupplement());
                }
            }
            if (entity.getHomePhoneNumber() != null) {
                if (entity.getHomePhoneNumber().getAreaCode() != null) {
                    setHomephoneareacode(String.valueOf(entity.getHomePhoneNumber().getAreaCode()));
                }
                if (entity.getHomePhoneNumber().getCountryCode() != null) {
                    setHomephonecountrycode(String.valueOf(entity.getHomePhoneNumber().getCountryCode()));
                }
                if (entity.getHomePhoneNumber().getLocalNumber() != null) {
                    setHomephonelocalnumber(String.valueOf(entity.getHomePhoneNumber().getLocalNumber()));
                }
            }
            if (entity.getLastName() != null) {
                setLastname(entity.getLastName());
            }
            if (entity.getLogin() != null) {
                setLogin(entity.getLogin());
            }
            setMassmailenabled(Boolean.toString(entity.isMassMailEnabled()));
            if (entity.getPhoneNumber() != null) {
                setPhoneareacode(String.valueOf(entity.getPhoneNumber().getAreaCode()));
                setPhonecountrycode(String.valueOf(entity.getPhoneNumber().getCountryCode()));
                setPhonelocalnumber(String.valueOf(entity.getPhoneNumber().getLocalNumber()));
            }
            if (entity.getProjects() != null) {
                for (Project aProject : entity.getProjects()) {
                    getProject().add(new XMLProject(aProject, true));
                }
            }
            if (entity.getOrdersTransitive() != null) {
                for (Order aOrder : entity.getOrdersTransitive()) {
                    getOrder().add(new XMLOrder(aOrder, true));
                }
            }
            if (entity.getContainersFormer() != null) {
                for (Container container : entity.getContainersFormer()) {
                    if (container.isContainerProject()) {
                        getFormerproject().add(new XMLProject(container, true));
                    }
                }
            }
            if (entity.getRoles() != null) {
                for (Role aRole : entity.getRoles()) {
                    getRole().add(new XMLRole(aRole, true));
                }
            }
            if (entity.getPasswordADSecret() != null) {
                setSecret(entity.getPasswordADSecret());
            }
            if (entity.getSshPublicKey() != null && StringHelper.isNotEmpty(entity.getSshPublicKey())) {
                setSshpublickey(entity.getSshPublicKeySecret());
            }
            if (entity.getShibbolethId() != null) {
                setShibbolethid(entity.getShibbolethId());
            }
            if (entity.getTitle() != null) {
                setTitle(entity.getTitle());
            }
            if (entity.getTechnology() != null) {
                setTechnology(entity.getTechnology().getName());
            }
            if (entity.getTrackedContainers() != null) {
                for (Container container : entity.getTrackedContainers()) {
                    getTrackablecontainer().add(new XMLContainer(container, true));
                }
            }
        }
    }

    public XMLUser() {
    }

    public XMLUser(User entity, boolean reference) {
        super(entity, reference);
    }

    public String getAccountenabled() {
        return accountenabled;
    }

    public String getActive() {
        return active;
    }

    public String getAddress() {
        return address;
    }

    public String getAddresscity() {
        return addresscity;
    }

    public String getAddresscountry() {
        return addresscountry;
    }

    public String getAddresscountrycode() {
        return addresscountrycode;
    }

    public String getAddressstreet() {
        return addressstreet;
    }

    public String getAddresssupplement() {
        return addresssupplement;
    }

    public String getAddresszip() {
        return addresszip;
    }

    public List<XMLOrder> getCoachedorder() {
        return coachedorder;
    }

    public List<XMLProject> getCoachedproject() {
        return coachedproject;
    }

    public String getCompany() {
        return company;
    }

    public String getComputerloginactivated() {
        return computerloginactivated;
    }

    public String getComputerloginenabled() {
        return computerloginenabled;
    }

    public String getDataaccessenabled() {
        return dataaccessenabled;
    }

    public String getDepartment() {
        return department;
    }

    public String getDivision() {
        return division;
    }

    public String getDivisionid() {
        return divisionid;
    }

    public String getEmail() {
        return email;
    }

    public String getEmailverified() {
        return emailverified;
    }

    public String getEmpdegree() {
        return empdegree;
    }

    public String getFirstname() {
        return firstname;
    }

    public List<XMLProject> getFormerproject() {
        return formerproject;
    }

    public String getHomeaddress() {
        return homeaddress;
    }

    public String getHomeaddresscity() {
        return homeaddresscity;
    }

    public String getHomeaddresscountry() {
        return homeaddresscountry;
    }

    public String getHomeaddressstreet() {
        return homeaddressstreet;
    }

    public String getHomeaddresssupplement() {
        return homeaddresssupplement;
    }

    public String getHomeaddresszip() {
        return homeaddresszip;
    }

    public String getHomeaddresscountrycode() {
        return homeaddresscountrycode;
    }

    public String getHomephoneareacode() {
        return homephoneareacode;
    }

    public String getHomephonecountrycode() {
        return homephonecountrycode;
    }

    public String getHomephonelocalnumber() {
        return homephonelocalnumber;
    }

    public String getInstitute() {
        return institute;
    }

    public String getInstituteid() {
        return instituteid;
    }

    public String getLastname() {
        return lastname;
    }

    public String getLogin() {
        return login;
    }

    public String getMassmailenabled() {
        return massmailenabled;
    }

    public List<XMLOrder> getOrder() {
        return order;
    }

    public String getOrganzation() {
        return organzation;
    }

    public String getOrganzationtype() {
        return organzationtype;
    }

    public String getPhoneareacode() {
        return phoneareacode;
    }

    public String getPhonecountrycode() {
        return phonecountrycode;
    }

    public String getPhonelocalnumber() {
        return phonelocalnumber;
    }

    public String getPrivateemail() {
        return privateemail;
    }

    public List<XMLProject> getProject() {
        return project;
    }

    public List<XMLRole> getRole() {
        return role;
    }

    public String getRoom() {
        return room;
    }

    public String getSalutation() {
        return salutation;
    }

    public String getSecret() {
        return secret;
    }

    public String getShibbolethid() {
        return shibbolethid;
    }

    public String getSshpublickey() {
        return sshpublickey;
    }

    public String getTechnology() {
        return technology;
    }

    public String getTitle() {
        return title;
    }

    public List<XMLContainer> getTrackablecontainer() {
        return trackablecontainer;
    }

    public void setAccountenabled(String accountenabled) {
        this.accountenabled = accountenabled;
    }

    public void setActive(String active) {
        this.active = active;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setAddresscity(String addresscity) {
        this.addresscity = addresscity;
    }

    public void setAddresscountry(String addresscountry) {
        this.addresscountry = addresscountry;
    }

    public void setAddresscountrycode(String addresscountrycode) {
        this.addresscountrycode = addresscountrycode;
    }

    public void setAddressstreet(String addressstreet) {
        this.addressstreet = addressstreet;
    }

    public void setAddresssupplement(String addresssupplement) {
        this.addresssupplement = addresssupplement;
    }

    public void setAddresszip(String addresszip) {
        this.addresszip = addresszip;
    }

    public void setCoachedorder(List<XMLOrder> coachedorder) {
        this.coachedorder = coachedorder;
    }

    public void setCoachedproject(List<XMLProject> coachedproject) {
        this.coachedproject = coachedproject;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public void setComputerloginactivated(String computerloginactivated) {
        this.computerloginactivated = computerloginactivated;
    }

    public void setComputerloginenabled(String computerloginenabled) {
        this.computerloginenabled = computerloginenabled;
    }

    public void setDataaccessenabled(String dataaccessenabled) {
        this.dataaccessenabled = dataaccessenabled;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public void setDivision(String division) {
        this.division = division;
    }

    public void setDivisionid(String divisionid) {
        this.divisionid = divisionid;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setEmailverified(String emailverified) {
        this.emailverified = emailverified;
    }

    public void setEmpdegree(String empdegree) {
        this.empdegree = empdegree;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public void setFormerproject(List<XMLProject> formerproject) {
        this.formerproject = formerproject;
    }

    public void setHomeaddress(String homeaddress) {
        this.homeaddress = homeaddress;
    }

    public void setHomeaddresscity(String homeaddresscity) {
        this.homeaddresscity = homeaddresscity;
    }

    public void setHomeaddresscountry(String homeaddresscountry) {
        this.homeaddresscountry = homeaddresscountry;
    }

    public void setHomeaddressstreet(String homeaddressstreet) {
        this.homeaddressstreet = homeaddressstreet;
    }

    public void setHomeaddresssupplement(String homeaddresssupplement) {
        this.homeaddresssupplement = homeaddresssupplement;
    }

    public void setHomeaddresszip(String homeaddresszip) {
        this.homeaddresszip = homeaddresszip;
    }

    public void setHomeaddresscountrycode(String homeaddresscountrycode) {
        this.homeaddresscountrycode = homeaddresscountrycode;
    }

    public void setHomephoneareacode(String homephoneareacode) {
        this.homephoneareacode = homephoneareacode;
    }

    public void setHomephonecountrycode(String homephonecountrycode) {
        this.homephonecountrycode = homephonecountrycode;
    }

    public void setHomephonelocalnumber(String homephonelocalnumber) {
        this.homephonelocalnumber = homephonelocalnumber;
    }

    public void setInstitute(String institute) {
        this.institute = institute;
    }

    public void setInstituteid(String instituteid) {
        this.instituteid = instituteid;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public void setMassmailenabled(String massmailenabled) {
        this.massmailenabled = massmailenabled;
    }

    public void setOrder(List<XMLOrder> order) {
        this.order = order;
    }

    public void setOrganzation(String organzation) {
        this.organzation = organzation;
    }

    public void setOrganzationtype(String organzationtype) {
        this.organzationtype = organzationtype;
    }

    public void setPhoneareacode(String phoneareacode) {
        this.phoneareacode = phoneareacode;
    }

    public void setPhonecountrycode(String phonecountrycode) {
        this.phonecountrycode = phonecountrycode;
    }

    public void setPhonelocalnumber(String phonelocalnumber) {
        this.phonelocalnumber = phonelocalnumber;
    }

    public void setPrivateemail(String privateemail) {
        this.privateemail = privateemail;
    }

    public void setProject(List<XMLProject> project) {
        this.project = project;
    }

    public void setRole(List<XMLRole> role) {
        this.role = role;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    public void setSalutation(String salutation) {
        this.salutation = salutation;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public void setShibbolethid(String shibbolethid) {
        this.shibbolethid = shibbolethid;
    }

    public void setSshpublickey(String sshpublickey) {
        this.sshpublickey = sshpublickey;
    }

    public void setTechnology(String technology) {
        this.technology = technology;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setTrackablecontainer(List<XMLContainer> trackablecontainer) {
        this.trackablecontainer = trackablecontainer;
    }
}

