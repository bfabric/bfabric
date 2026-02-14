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

package org.bfabric.forms;

import java.time.LocalDate;

import javax.enterprise.inject.spi.CDI;

import org.bfabric.entity.Country;
import org.bfabric.entity.Division;
import org.bfabric.entity.HomePhoneNumber;
import org.bfabric.entity.Institute;
import org.bfabric.entity.OrganizationType;
import org.bfabric.entity.PhoneNumber;
import org.bfabric.entity.User;
import org.bfabric.exception.InvalidDataException;
import org.bfabric.service.CountryService;
import org.bfabric.util.StringHelper;
import org.bfabric.webservice.request.parameter.XMLRequestParameterSaveUser;

public class MFUser extends AbstractMF {

    private final User user;

    private final XMLRequestParameterSaveUser xmlRequestSaveUser;

    public MFUser(User user, XMLRequestParameterSaveUser xmlRequestSaveUser) {
        this.user = user;
        this.xmlRequestSaveUser = xmlRequestSaveUser;
    }

    @Override
    public synchronized void apply() throws Exception {
        setAffiliationHelper();
        if (getXmlRequestSaveUser().getPassword() != null) {
            getUser().setPassword(getXmlRequestSaveUser().getPassword().toCharArray());
        }
        getUser().setLogin(getLogin());
        getUser().setAccountEnabled(isAccountEnabled());
        getUser().setComputerLoginEnabled(isComputerLoginEnabled());
        getUser().setDataAccessEnabled(isDataAccessEnabled());
        getUser().setEmailVerified(isEmailVerified());
        getUser().setEmailActive(isEmailActive());
        getUser().setDescription(getDescription());
        getUser().setEmail(getEmail());
        getUser().setPrivateEmail(getPrivateEmail());
        getUser().setSalutation(getSalutation());
        getUser().setTitle(getTitle());
        getUser().setFirstName(getFirstname());
        getUser().setLastName(getLastname());
        getUser().setBirthDate(getBirthdate());
        getUser().setMassMailEnabled(isMassMailEnabled());
        getUser().recomputeComputerLoginAndDataAccessEnabled();

        getUser().setAccessCardCode(getAccessCardCode());
        getUser().setAccessCardNumber(getAccessCardNumber());
        getUser().setAccessCardExpiryDate(getAccessCardExpiryDate());

        getUser().getAddress().setStreet(getAddressStreet());
        getUser().getAddress().setZip(getAddressZip());
        getUser().getAddress().setCity(getAddressCity());
        getUser().getAddress().setCountry(getCountry());
        getUser().getAddress().setRoom(getAddressRoom());
        getUser().getAddress().setSupplement(getAddressSupplement());
        getUser().getHomeAddress().setStreet(getHomeAddressStreet());
        getUser().getHomeAddress().setZip(getHomeAddressZip());
        getUser().getHomeAddress().setCity(getHomeAddressCity());
        getUser().getHomeAddress().setCountry(getHomeAddressCountry());
        getUser().getHomeAddress().setSupplement(getHomeAddressSupplement());

        getUser().setPhoneNumber(getPhoneNumber());
        getUser().setHomePhoneNumber(getHomePhoneNumber());

        getUser().setDivision(getDivision());
        getUser().setInstitute(getInstitute());
    }

    public String getAccessCardCode() {
        if (getXmlRequestSaveUser().getAccesscardcode() != null) {
            return getXmlRequestSaveUser().getAccesscardcode();
        }
        return getUser().getAccessCardCode();
    }

    public LocalDate getAccessCardExpiryDate() throws InvalidDataException {
        if (getXmlRequestSaveUser().getAccesscardexpirydate() != null) {
            return MFHelper.dateValueOf("accesscardexpirydate", getXmlRequestSaveUser().getAccesscardexpirydate());
        }
        return getUser().getAccessCardExpiryDate();
    }

    public String getAccessCardNumber() {
        if (getXmlRequestSaveUser().getAccesscardnumber() != null) {
            return getXmlRequestSaveUser().getAccesscardnumber();
        }
        return getUser().getAccessCardNumber();
    }

    public String getAddressCity() throws InvalidDataException {
        if (!getUser().isManaged() && StringHelper.isEmpty(getXmlRequestSaveUser().getAddresscity())) {
            throw new InvalidDataException("addresscity required!");
        }
        if (getXmlRequestSaveUser().getAddresscity() != null) {
            return getXmlRequestSaveUser().getAddresscity();
        }
        return getUser().getAddress().getCity();
    }

    public String getAddressRoom() {
        if (getXmlRequestSaveUser().getAddressroom() != null) {
            return getXmlRequestSaveUser().getAddressroom();
        }
        return getUser().getAddress().getRoom();
    }

    public String getAddressStreet() throws InvalidDataException {
        if (!getUser().isManaged() && StringHelper.isEmpty(getXmlRequestSaveUser().getAddressstreet())) {
            throw new InvalidDataException("addressstreet required!");
        }
        if (getXmlRequestSaveUser().getAddressstreet() != null) {
            return getXmlRequestSaveUser().getAddressstreet();
        }
        return getUser().getAddress().getStreet();
    }

    public String getAddressSupplement() throws InvalidDataException {
        if (getXmlRequestSaveUser().getAddresssupplement() != null) {
            return getXmlRequestSaveUser().getAddresssupplement();
        }
        return getUser().getAddress().getSupplement();
    }

    public String getAddressZip() throws InvalidDataException {
        if (!getUser().isManaged() && StringHelper.isEmpty(getXmlRequestSaveUser().getAddresszip())) {
            throw new InvalidDataException("addresszip required!");
        }
        if (getXmlRequestSaveUser().getAddresszip() != null) {
            return getXmlRequestSaveUser().getAddresszip();
        }
        return getUser().getAddress().getZip();
    }

    public User getBackup() throws InvalidDataException {
        if (getXmlRequestSaveUser().getBackupuserid() != null) {
            return (User) fetch(User.class, MFHelper.positiveLongValueOf("backupuserid", getXmlRequestSaveUser().getBackupuserid()));
        }
        return getUser().getBackup();
    }

    public LocalDate getBirthdate() throws InvalidDataException {
        if (getXmlRequestSaveUser().getBirthdate() != null) {
            return MFHelper.dateValueOf("birthdate", getXmlRequestSaveUser().getBirthdate());
        }
        return getUser().getBirthDate();
    }

    public Country getCountry() throws InvalidDataException {
        if (getXmlRequestSaveUser().getAddresscountrycode() != null) {
            Country country = CDI.current().select(CountryService.class).get().getCountryByIdOrName(getXmlRequestSaveUser().getAddresscountrycode());
            if (country == null) {
                throw new InvalidDataException("Invalid country code: " + getXmlRequestSaveUser().getAddresscountrycode());
            }
            return country;
        }
        return getUser().getAddress().getCountry();
    }

    public String getDescription() {
        if (getXmlRequestSaveUser().getDescription() != null) {
            return getXmlRequestSaveUser().getDescription();
        }
        return getUser().getDescription();
    }

    public Division getDivision() throws InvalidDataException {
        if (getXmlRequestSaveUser().getDivisionid() != null) {
            if (getXmlRequestSaveUser().getInstituteid() != null) {
                throw new InvalidDataException("Divisionid and instituteid cannot be set together!");
            }
            return (Division) fetch(Division.class, MFHelper.positiveLongValueOf("divisionid", getXmlRequestSaveUser().getDivisionid()));
        }
        return getUser().getDivision();
    }

    public String getEmail() throws InvalidDataException {
        if (getXmlRequestSaveUser().getEmail() != null) {
            if (!getUser().isManaged() && getIdentityService().getUserByEmail(getXmlRequestSaveUser().getEmail()) != null) {
                throw new InvalidDataException("Email not unique!");
            }
            return getXmlRequestSaveUser().getEmail();
        }
        return getUser().getEmail();
    }

    public Integer getEmpDegree() throws InvalidDataException {
        if (getXmlRequestSaveUser().getEmpdegree() != null) {
            Integer empDegree = MFHelper.integerValueOf("empdegree", getXmlRequestSaveUser().getEmpdegree());
            if (empDegree < 0 || empDegree > 100) {
                throw new InvalidDataException("Invalid empdegree: " + empDegree + "! Valid values are integers between 0 and 100.");
            }
            return empDegree;
        }
        return getUser().getEmpDegree();
    }

    public String getFirstname() {
        if (getXmlRequestSaveUser().getFirstname() != null) {
            return getXmlRequestSaveUser().getFirstname();
        }
        return getUser().getFirstName();
    }

    public String getHomeAddressCity() {
        if (getXmlRequestSaveUser().getHomeaddresscity() != null) {
            return getXmlRequestSaveUser().getHomeaddresscity();
        }
        return getUser().getHomeAddress().getCity();
    }

    public Country getHomeAddressCountry() throws InvalidDataException {
        if (getXmlRequestSaveUser().getHomeaddresscountrycode() != null) {
            Country country = CDI.current().select(CountryService.class).get().getCountryByIdOrName(getXmlRequestSaveUser().getHomeaddresscountrycode());
            if (country == null) {
                throw new InvalidDataException("Invalid homeaddresscountrycode: " + getXmlRequestSaveUser().getHomeaddresscountrycode());
            }
            return country;
        }
        return getUser().getHomeAddress().getCountry();
    }

    public String getHomeAddressStreet() {
        if (getXmlRequestSaveUser().getHomeaddressstreet() != null) {
            return getXmlRequestSaveUser().getHomeaddressstreet();
        }
        return getUser().getHomeAddress().getStreet();
    }

    public String getHomeAddressSupplement() {
        if (getXmlRequestSaveUser().getHomeaddresssupplement() != null) {
            return getXmlRequestSaveUser().getHomeaddresssupplement();
        }
        return getUser().getHomeAddress().getSupplement();
    }

    public String getHomeAddressZip() {
        if (getXmlRequestSaveUser().getHomeaddresszip() != null) {
            return getXmlRequestSaveUser().getHomeaddresszip();
        }
        return getUser().getHomeAddress().getZip();
    }

    public HomePhoneNumber getHomePhoneNumber() throws InvalidDataException {
        if (getXmlRequestSaveUser().getHomephonecountrycode() != null) {
            Integer countrycode = MFHelper.integerValueOf("homephonecountrycode", getXmlRequestSaveUser().getHomephonecountrycode());
            if (getXmlRequestSaveUser().getHomephoneareacode() != null) {
                Integer areacode = MFHelper.integerValueOf("homephoneareacode", getXmlRequestSaveUser().getHomephoneareacode());
                if (getXmlRequestSaveUser().getHomephonelocalnumber() != null) {
                    Integer localnumber = MFHelper.integerValueOf("homephonelocalnumber", getXmlRequestSaveUser().getHomephonelocalnumber());
                    return new HomePhoneNumber(countrycode, areacode, localnumber);
                }
            }
        }
        return getUser().getHomePhoneNumber();
    }

    public Institute getInstitute() throws InvalidDataException {
        if (getXmlRequestSaveUser().getInstituteid() != null) {
            if (getXmlRequestSaveUser().getDivisionid() != null) {
                throw new InvalidDataException("Divisionid and instituteid cannot be set together!");
            }
            return (Institute) fetch(Institute.class, MFHelper.positiveLongValueOf("instituteid", getXmlRequestSaveUser().getInstituteid()));
        }
        return getUser().getInstitute();
    }

    public String getLastname() {
        if (getXmlRequestSaveUser().getLastname() != null) {
            return getXmlRequestSaveUser().getLastname();
        }
        return getUser().getLastName();
    }

    public String getLogin() throws InvalidDataException {
        if (getXmlRequestSaveUser().getLogin() != null) {
            if (getUser().isManaged()) {
                throw new InvalidDataException("The login cannot be changed afterwards!");
            } else if (getIdentityService().getUserByLogin(getXmlRequestSaveUser().getLogin()) != null) {
                throw new InvalidDataException("login not unique!");
            }
            return getXmlRequestSaveUser().getLogin();
        }
        return getUser().getLogin();
    }

    public PhoneNumber getPhoneNumber() throws InvalidDataException {
        if (!getUser().isManaged()) {
            if (StringHelper.isEmpty(getXmlRequestSaveUser().getPhonecountrycode())) {
                throw new InvalidDataException("phonecountrycode required!");
            }
            if (StringHelper.isEmpty(getXmlRequestSaveUser().getPhoneareacode())) {
                throw new InvalidDataException("phonecareacode required!");
            }
            if (StringHelper.isEmpty(getXmlRequestSaveUser().getPhonelocalnumber())) {
                throw new InvalidDataException("phonelocalnumber required!");
            }
        }
        if (getXmlRequestSaveUser().getPhonecountrycode() != null) {
            Integer countrycode = MFHelper.integerValueOf("phonecountrycode", getXmlRequestSaveUser().getPhonecountrycode());
            if (getXmlRequestSaveUser().getPhoneareacode() != null) {
                Integer areacode = MFHelper.integerValueOf("phoneareacode", getXmlRequestSaveUser().getPhoneareacode());
                if (getXmlRequestSaveUser().getPhonelocalnumber() != null) {
                    Integer localnumber = MFHelper.integerValueOf("phonelocalnumber", getXmlRequestSaveUser().getPhonelocalnumber());
                    return new PhoneNumber(countrycode, areacode, localnumber);
                }
            }
        }
        return getUser().getPhoneNumber();
    }

    public String getPrivateEmail() {
        if (getXmlRequestSaveUser().getPrivateemail() != null) {
            return getXmlRequestSaveUser().getPrivateemail();
        }
        return getUser().getPrivateEmail();
    }

    public String getSalutation() throws InvalidDataException {
        if (getXmlRequestSaveUser().getSalutation() != null) {
            if (!getXmlRequestSaveUser().getSalutation().equalsIgnoreCase("Ms") && !getXmlRequestSaveUser().getSalutation().equalsIgnoreCase("Mr")) {
                throw new InvalidDataException("invalid saluation: must be 'Ms' or 'Mr'!");
            }
            return getXmlRequestSaveUser().getSalutation().equalsIgnoreCase("Ms") ? "Ms" : "Mr";
        }
        return getUser().getSalutation();
    }

    public String getTitle() throws InvalidDataException {
        if (getXmlRequestSaveUser().getTitle() != null) {
            if (!getXmlRequestSaveUser().getTitle().equalsIgnoreCase("Prof. Dr.") && !getXmlRequestSaveUser().getTitle().equalsIgnoreCase("Dr.")) {
                throw new InvalidDataException("invalid title: must be 'Prof. Dr.' or 'Dr.'!");
            }
            return getXmlRequestSaveUser().getTitle().equalsIgnoreCase("Dr.") ? "Dr." : "Prof. Dr.";
        }
        return getUser().getTitle();
    }

    public User getUser() {
        return user;
    }

    public XMLRequestParameterSaveUser getXmlRequestSaveUser() {
        return xmlRequestSaveUser;
    }

    public Boolean isAccountEnabled() throws Exception {
        if (getXmlRequestSaveUser().getAccountenabled() != null) {
            return MFHelper.booleanValueOf("accountenabled", getXmlRequestSaveUser().getAccountenabled());
        }
        return getUser().isAccountEnabled();
    }

    public Boolean isComputerLoginEnabled() throws Exception {
        if (getXmlRequestSaveUser().getComputerloginenabled() != null) {
            return MFHelper.booleanValueOf("computerloginenabled", getXmlRequestSaveUser().getComputerloginenabled());
        }
        return getUser().isComputerLoginEnabled();
    }

    public Boolean isDataAccessEnabled() throws Exception {
        if (getXmlRequestSaveUser().getDataaccessenabled() != null) {
            return MFHelper.booleanValueOf("dataaccessenabled", getXmlRequestSaveUser().getDataaccessenabled());
        }
        return getUser().isDataAccessEnabled();
    }

    public Boolean isEmailActive() throws Exception {
        if (getXmlRequestSaveUser().getEmailactive() != null) {
            return MFHelper.booleanValueOf("emailactive", getXmlRequestSaveUser().getEmailactive());
        }
        return getUser().isEmailActive();
    }

    public Boolean isEmailVerified() throws Exception {
        if (getXmlRequestSaveUser().getEmailverified() != null) {
            return MFHelper.booleanValueOf("emailverified", getXmlRequestSaveUser().getEmailverified());
        }
        return getUser().isEmailVerified();
    }

    public Boolean isMassMailEnabled() throws Exception {
        if (getXmlRequestSaveUser().getMassmailenabled() != null) {
            return MFHelper.booleanValueOf("massmailenabled", getXmlRequestSaveUser().getMassmailenabled());
        }
        return getUser().isMassMailEnabled();
    }

    public void setAffiliationHelper() throws InvalidDataException {
        if (getXmlRequestSaveUser().getCompany() != null || getXmlRequestSaveUser().getDivision() != null) {
            if (getXmlRequestSaveUser().getDivisionid() != null) {
                throw new InvalidDataException("Invalid: use either divisionid or company/division!");
            }
            if (getXmlRequestSaveUser().getOrganization() != null || getXmlRequestSaveUser().getDepartment() != null || getXmlRequestSaveUser().getInstitute() != null) {
                throw new InvalidDataException("Invalid: use either institute/department/organization or company/division!");
            }
            if (StringHelper.isEmpty(getXmlRequestSaveUser().getCompany())) {
                throw new InvalidDataException("Division requires company!");
            }
            if (getXmlRequestSaveUser().getOrganizationtypeid() == null) {
                throw new InvalidDataException("Organizationtypeid required!");
            } else {
                OrganizationType organizationType = (OrganizationType) fetch(OrganizationType.class, MFHelper.positiveLongValueOf("organizationtypeid", getXmlRequestSaveUser().getOrganizationtypeid()));
                if (organizationType == null) {
                    throw new InvalidDataException("No organization type exists with id=" + getXmlRequestSaveUser().getOrganizationtypeid());
                }
                getUser().setOrganizationType(organizationType);
            }
            getUser().setCompanyName(getXmlRequestSaveUser().getCompany());
            if (getXmlRequestSaveUser().getDivision() != null) {
                getUser().setDivisionName(getXmlRequestSaveUser().getDivision());
            } else {
                getUser().setDivisionName("N/A");
            }
        } else if (getXmlRequestSaveUser().getOrganization() != null || getXmlRequestSaveUser().getDepartment() != null || getXmlRequestSaveUser().getInstitute() != null) {
            if (getXmlRequestSaveUser().getInstituteid() != null) {
                throw new InvalidDataException("Invalid: use either instituteid or institute/department/organization!");
            }
            if (StringHelper.isEmpty(getXmlRequestSaveUser().getOrganization()) || StringHelper.isEmpty(getXmlRequestSaveUser().getDepartment()) || StringHelper.isEmpty(getXmlRequestSaveUser().getInstitute())) {
                throw new InvalidDataException("Invalid: institute/department/organization are required together!");
            }
            if (getXmlRequestSaveUser().getOrganizationtypeid() == null) {
                throw new InvalidDataException("Organizationtypeid required!");
            } else {
                OrganizationType organizationType = (OrganizationType) fetch(OrganizationType.class, MFHelper.positiveLongValueOf("organizationtypeid", getXmlRequestSaveUser().getOrganizationtypeid()));
                if (organizationType == null) {
                    throw new InvalidDataException("There exists no organization type with the id: " + getXmlRequestSaveUser().getOrganizationtypeid());
                }
                getUser().setOrganizationType(organizationType);
            }
            getUser().setInstituteName(getXmlRequestSaveUser().getInstitute());
            getUser().setDepartmentName(getXmlRequestSaveUser().getDepartment());
            getUser().setOrganizationName(getXmlRequestSaveUser().getOrganization());
        }
    }
}
