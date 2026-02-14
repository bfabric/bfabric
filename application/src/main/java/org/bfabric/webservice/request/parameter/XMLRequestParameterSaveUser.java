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

package org.bfabric.webservice.request.parameter;

import javax.xml.bind.annotation.XmlElement;

public class XMLRequestParameterSaveUser extends XMLRequestParameterSaveAbstractDescriptionBaseEntity {

    @XmlElement
    private String accesscardcode;

    @XmlElement
    private String accesscardexpirydate;

    @XmlElement
    private String accesscardnumber;

    @XmlElement
    private String accountenabled;

    @XmlElement
    private String addresscity;

    @XmlElement
    private String addresscountrycode;

    @XmlElement
    private String addressroom;

    @XmlElement
    private String addressstreet;

    @XmlElement
    private String addresssupplement;

    @XmlElement
    private String addresszip;

    private String backupuserid;

    @XmlElement
    private String birthdate;

    @XmlElement
    private String company;

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
    private String emailactive;

    @XmlElement
    private String emailverified;

    @XmlElement
    private String empdegree;

    @XmlElement
    private String firstname;

    @XmlElement
    private String homeaddresscity;

    @XmlElement
    private String homeaddresscountrycode;

    @XmlElement
    private String homeaddressstreet;

    @XmlElement
    private String homeaddresssupplement;

    @XmlElement
    private String homeaddresszip;

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
    private String organization;

    @XmlElement
    private String organizationtype;

    @XmlElement
    private String organizationtypeid;

    @XmlElement
    private String password;

    @XmlElement
    private String phoneareacode;

    @XmlElement
    private String phonecountrycode;

    @XmlElement
    private String phonelocalnumber;

    @XmlElement
    private String privateemail;

    @XmlElement
    private String salutation;

    @XmlElement
    private String title;

    public String getAccesscardcode() {
        return accesscardcode;
    }

    public String getAccesscardexpirydate() {
        return accesscardexpirydate;
    }

    public String getAccesscardnumber() {
        return accesscardnumber;
    }

    public String getAccountenabled() {
        return accountenabled;
    }

    public String getAddresscity() {
        return addresscity;
    }

    public String getAddresscountrycode() {
        return addresscountrycode;
    }

    public String getAddressroom() {
        return addressroom;
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

    public String getBackupuserid() {
        return backupuserid;
    }

    public String getBirthdate() {
        return birthdate;
    }

    public String getCompany() {
        return company;
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

    public String getEmailactive() {
        return emailactive;
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

    public String getHomeaddresscity() {
        return homeaddresscity;
    }

    public String getHomeaddresscountrycode() {
        return homeaddresscountrycode;
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

    public String getOrganization() {
        return organization;
    }

    public String getOrganizationtype() {
        return organizationtype;
    }

    public String getOrganizationtypeid() {
        return organizationtypeid;
    }

    public String getPassword() {
        return password;
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

    public String getSalutation() {
        return salutation;
    }

    public String getTitle() {
        return title;
    }

    public void setAccesscardcode(String accesscardcode) {
        this.accesscardcode = accesscardcode;
    }

    public void setAccesscardexpirydate(String accesscardexpirydate) {
        this.accesscardexpirydate = accesscardexpirydate;
    }

    public void setAccesscardnumber(String accesscardnumber) {
        this.accesscardnumber = accesscardnumber;
    }

    public void setAccountenabled(String accountenabled) {
        this.accountenabled = accountenabled;
    }

    public void setAddresscity(String addresscity) {
        this.addresscity = addresscity;
    }

    public void setAddresscountrycode(String addresscountrycode) {
        this.addresscountrycode = addresscountrycode;
    }

    public void setAddressroom(String addressroom) {
        this.addressroom = addressroom;
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

    public void setBackupuserid(String backupuserid) {
        this.backupuserid = backupuserid;
    }

    public void setBirthdate(String birthdate) {
        this.birthdate = birthdate;
    }

    public void setCompany(String company) {
        this.company = company;
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

    public void setEmailactive(String emailactive) {
        this.emailactive = emailactive;
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

    public void setHomeaddresscity(String homeaddresscity) {
        this.homeaddresscity = homeaddresscity;
    }

    public void setHomeaddresscountrycode(String homeaddresscountrycode) {
        this.homeaddresscountrycode = homeaddresscountrycode;
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

    public void setOrganization(String organization) {
        this.organization = organization;
    }

    public void setOrganizationtype(String organizationtype) {
        this.organizationtype = organizationtype;
    }

    public void setOrganizationtypeid(String organizationtypeid) {
        this.organizationtypeid = organizationtypeid;
    }

    public void setPassword(String password) {
        this.password = password;
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

    public void setSalutation(String salutation) {
        this.salutation = salutation;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
