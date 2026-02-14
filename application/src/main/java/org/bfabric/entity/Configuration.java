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


package org.bfabric.entity;

import java.io.File;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.enterprise.inject.spi.CDI;

import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.property.CalScale;
import net.fortuna.ical4j.model.property.ProdId;
import net.fortuna.ical4j.model.property.Version;
import org.bfabric.Constants;
import org.bfabric.Messages;
import org.bfabric.entity.api.NotEntityLoggable;
import org.bfabric.enums.EnvironmentEnum;
import org.bfabric.service.CountryService;
import org.bfabric.util.ConfigurationHelper;
import org.bfabric.util.StringHelper;

public class Configuration extends AbstractBaseEntity implements NotEntityLoggable {

    private static final long serialVersionUID = 1;

    private boolean aaiLoginEnabled = true;

    private String aaiResourceEntityId = "";

    private String absencesMailAddress;

    private String accessCardCodeGuestPattern = "^[G|Z][\\d]{5,7}+$";

    private String accessCardCodePattern = "^[\\d]{8}+$";

    private boolean accessCardExpiryReminderJobEnabled = false;

    private String accessCardNumberPattern = "^[\\d]{6}+$";

    private boolean accessRequestEnabled = true;

    private String accessRequestManagerAddress = "";

    private String accessRequestManagerDatePattern = Constants.DATE_PATTERN_EU;

    private String accessRequestManagerEmail = "";

    private String accessRequestManagerInstitute = "";

    private String accessRequestManagerInstituteDirector = "";

    private String accessRequestManagerInstituteExtension = "";

    private String accessRequestManagerName = "";

    private String accessRequestManagerOfficeTimes = "";

    private String accessRequestManagerPhone = "";

    private String accessRequestManagerPlace = "";

    private String accessRequestNotificationEmail = "";

    private String accessRequestPassword = "nottherealpassword";

    private String accessRequestUZHEmail = "";

    private boolean addAffiliationByUserEnabled = true;

    private boolean agendaEnabled = true;

    private int annualVacationCreditAboveAgeLimit = 30;

    private int annualVacationCreditAgeLimit = 50;

    private int annualVacationCreditBelowAgeLimit = 25;

    private String applicationName = "B-Fabric";

    private String applicationVersion;

    private String applicationVersionShort;

    private boolean askOldPasswordOnChangeRequest = false;

    private String baseUrl;

    private String bashAbsolutePath = "/bin/bash";

    private boolean bookerETHEnabled = false;

    private boolean bookingPdfChargeDateEnabled = false;

    private boolean bookingPdfProjectNameEnabled = false;

    private long bookingRequiredTotal = 2000;

    private boolean bookingTransferEnabled = false;

    private boolean browserDownloadEnabled = true;

    private boolean budgetLimitEnabled = true;

    private int checkComputerLoginValidity = 180;

    private int checkLinkValidityInterval = 1;

    private boolean companyAutocompleteEnabled = false;

    private String contactInfoCity;

    private String contactInfoCountryId;

    private String contactInfoEmail;

    private String contactInfoMapUrl;

    private String contactInfoOfficeHours;

    private String contactInfoPhone;

    private String contactInfoRoom;

    private String contactInfoStreet;

    private String contactInfoSupplement;

    private String contactInfoZip;

    private boolean contractExpiryReminderJobEnabled = false;

    private String coordinatorEmail = "";

    private int customOrderStatusAutocompleteMinimumSize = 5;

    private boolean dataManagementEnabled = true;

    private int dataScrollerChunkSize = 5;

    private int dataTableExportLimit = 10000;

    private boolean datasetTypeCheckEnabled = false;

    private long defaultBookingIssuerId = 21;

    private long defaultBudgetLimit = -1;

    private String defaultCharset = "UTF-8";

    private String defaultChartColors = "009900, 00CCFF, FFFF00, FF9900, FF0000, 00CC00, 0099FF, FFFF99, FF6633, 996600";

    private String defaultCompanyName = "PRIVATE USER";

    private String defaultCountryId = "CH";

    private String defaultCurrencyCode = "CHF";

    private String defaultDataScrollerChunkSizeTemplate = "5, 10, 20";

    private String defaultDatePattern = "yyyy-MM-dd";

    private String defaultDateTimePattern = "yyyy-MM-dd HH:mm:ss";

    private String defaultDivision = "n/a";

    private long defaultMasterExecutableIdStorage;

    private long defaultMasterExecutableIdSubmitter;

    private long defaultMasterExecutableIdWrapperCreator;

    private String defaultRowsPerPageTemplate = "5, 10, 20, 50, 100, 200";

    private String defaultTaxTypeName = "VAT exempted";

    private String defaultTimePattern = "HH:mm";

    private boolean deleteDeletableOffersJobEnabled = false;

    private boolean deleteDeletableUsersJobEnabled = false;

    private boolean deleteExpiredShibbolethMappingsEnabled = false;

    private boolean deleteUnassignedObjectsJobEnabled = false;

    private DeployerContextProperty deployer;

    private String deployerAbbreviation = "";

    private String deployerAddressCity = "";

    private String deployerAddressCountry = "";

    private String deployerAddressStreet = "";

    private String deployerAddressSupplement = "";

    private String deployerAddressZip = "";

    private String deployerColor = "#ea6b13";

    private String deployerCountry = "";

    private String deployerDefaultEmail = null;

    private String deployerEmail = "";

    private boolean deployerEthUzhEnabled = true;

    private String deployerHomeURL = "";

    private String deployerName = "";

    private String deployerPhoneNumber = "";

    private String deployerPhonePrefix = "";

    private String deployerZip = "";

    private String deploymentBranchName;

    private String deploymentCompilationDateTime;

    private String deploymentDateTime;

    private String deploymentGitRevisionId;

    private boolean doiEnabled = false;

    private String doiPrefix = "";

    private boolean doiUrlModified = false;

    private boolean downloadEnabled = true;

    private boolean downloadManagerEnabled = true;

    private int downloadManagerJNLPValidityDuration = 86400;

    private boolean employeePrivateInfoRequired = true;

    private String envAbsolutePath = "/usr/bin/env";

    private EnvironmentContextProperty environment;

    private boolean extensionReportReminderJobEnabled = false;

    private boolean feedbackEnabled = true;

    private String fromEmailAddress = "do-not-reply";

    private String headerBackgroundColor = "#333333";

    private boolean hotKeysEnabled = true;

    private boolean hotKeysPublicEnabled = true;

    private String indexPath = "b-fabric-index/";

    private InstanceContextProperty instance;

    private boolean instrumentReservationEnabled = true;

    private boolean instrumentReservationReminderJobEnabled = true;

    private int instrumentReservationSettingMaxHours = 8760;

    private int instrumentReservationSettingSlotDurationDefault = 720;

    private boolean instrumentReservationWeekendsEnabled = Boolean.TRUE;

    private boolean kpiHomePageEnabled = true;

    private boolean labEnabled = true;

    private boolean legacyEnabled = true;

    private int listingRows = 20;

    private boolean logPageAccesses = true;

    private String loginAutoComplete = "on";

    private boolean mailEnabled = true;

    private String mailSubjectPrefix = "[...]";

    private long masterExecutableIdContainerSync;

    private long masterExecutableIdUserSync;

    private long maxAttachmentFiles = 500;

    private long maxAttachmentSize = 52428800; // 50 Mb

    private int maxBatchEditItems = 100;

    private int maxBatchEditItemsPlates = 100;

    private long maxItemsOnShowDetails = 20;

    private int maxLoginAttempts = 0;

    private boolean measureCallsFilterEnabled = false;

    private LocalDate offerValidityDate = LocalDate.of(2021, 6, 30);

    private int offerValidityDuration = 90;

    private boolean oneTimeTokenEnabled = false;

    private boolean orderEnabled = true;

    private int parentSamplesMaximumDisplayAmount = 15;

    private int pollInterval = 2000;

    private String pubtktGeneratorFilePath;

    private String pwEncPublicKeyFilePath;

    private boolean refreshMaterializedViewsJobEnabled = true;

    private boolean reindexJobEnabled = true;

    private boolean resetUserAvailableJobEnabled = false;

    private int resourceBasketLimit = 1000;

    private boolean reviewRequired = true;

    private int sessionTimeoutWarningTime = 300; // seconds

    private boolean showSamplesLaneButton = false;

    private boolean showSamplesLaneButtonAndCheckbox = false;

    private boolean showSamplesLaneSeparated = false;

    private boolean statisticsEnabled = true;

    private String supportEmail = "";

    private boolean synchronizeWithADEnabled = true;

    private String technicalSupportEmail = "";

    private boolean trustAllCertificates = false;

    private boolean unarchiveEnabled = true;

    private String urlFAQ = "";

    private String urlIntranet = "";

    private String urlIssueTracker = "";

    private String urlProjectDescriptionGuidelines = "";

    private String urlReleaseNotes = "";

    private String urlSupport = "";

    private String urlTermsAndConditions = "";

    private String urlUserManual = "";

    private boolean userRegistrationEnabled = true;

    private boolean virusScannerDisabled = false;

    private int webAppTokenExpirationTime = 3600;

    private int webServiceQueryMaxElements = 100;

    private int webServiceQueryResultMaxEntitiesPerPage = 100;

    private boolean workflowEnabled = true;

    public Configuration() {
    }

    public String getAaiResourceEntityId() {
        return aaiResourceEntityId;
    }

    public String getAbsencesMailAddress() {
        return absencesMailAddress;
    }

    public String getAccessCardCodeGuestPattern() {
        return accessCardCodeGuestPattern;
    }

    public String getAccessCardCodePattern() {
        return accessCardCodePattern;
    }

    public String getAccessCardNumberPattern() {
        return accessCardNumberPattern;
    }

    public String getAccessRequestManagerAddress() {
        return accessRequestManagerAddress;
    }

    public String getAccessRequestManagerDatePattern() {
        return accessRequestManagerDatePattern;
    }

    public String getAccessRequestManagerEmail() {
        return getEmailOrDeployerDefaultEmail(accessRequestManagerEmail);
    }

    public String getAccessRequestManagerInstitute() {
        return accessRequestManagerInstitute;
    }

    public String getAccessRequestManagerInstituteDirector() {
        return accessRequestManagerInstituteDirector;
    }

    public String getAccessRequestManagerInstituteExtension() {
        return accessRequestManagerInstituteExtension;
    }

    public String getAccessRequestManagerName() {
        return accessRequestManagerName;
    }

    public String getAccessRequestManagerOfficeTimes() {
        return accessRequestManagerOfficeTimes;
    }

    public String getAccessRequestManagerPhone() {
        return accessRequestManagerPhone;
    }

    public String getAccessRequestManagerPlace() {
        return accessRequestManagerPlace;
    }

    public String getAccessRequestNotificationEmail() {
        return getEmailOrDeployerDefaultEmail(accessRequestNotificationEmail);
    }

    public String getAccessRequestPassword() {
        return accessRequestPassword;
    }

    public String getAccessRequestUZHEmail() {
        return getEmailOrDeployerDefaultEmail(accessRequestUZHEmail);
    }

    public int getAnnualVacationCreditAboveAgeLimit() {
        return annualVacationCreditAboveAgeLimit;
    }

    public int getAnnualVacationCreditAgeLimit() {
        return annualVacationCreditAgeLimit;
    }

    public int getAnnualVacationCreditBelowAgeLimit() {
        return annualVacationCreditBelowAgeLimit;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public String getApplicationNameSymbolsReplaced() {
        return getApplicationName().replaceAll("[^A-Za-z0-9]", " ");
    }

    public String getApplicationNameVariations() {
        return getApplicationNameVariations(getApplicationName()) + " " + getApplicationNameVariations(getApplicationNameWithoutSymbols()) + " " + getApplicationNameVariations(getApplicationNameSymbolsReplaced());
    }

    public String getApplicationNameVariations(String name) {
        return name != null ? name + " " + name.toLowerCase() + " " + name.toUpperCase() : Constants.EMPTY_STRING;
    }

    public String getApplicationNameWithoutSymbols() {
        return getApplicationName().replaceAll("[^A-Za-z0-9]", Constants.EMPTY_STRING);
    }

    public String getApplicationVersion() {
        if (applicationVersion == null) {
            try {
                applicationVersion = getClass().getPackage().getImplementationVersion();
            } catch (Exception e) {
                // do nothing!
                logger.warning("Properties file could not be read!");
                e.printStackTrace();
            }
        }
        return applicationVersion != null ? applicationVersion : "13";
    }

    public String getApplicationVersionShort() {
        if (applicationVersionShort == null && getApplicationVersion() != null) {
            int pos = getApplicationVersion().indexOf(".");
            applicationVersionShort = pos > 0 ? getApplicationVersion().substring(0, pos) : getApplicationVersion();
        }
        return applicationVersionShort;
    }

    public String getApplicationVersionShortName() {
        return StringHelper.isNotEmpty(getApplicationVersionShort()) ? getApplicationVersionShort() : Constants.EMPTY_STRING;
    }

    public String getBaseUrl() {
        if (baseUrl == null) {
            baseUrl = ConfigurationHelper.getConfManager().getBaseURL();
        }
        return baseUrl;
    }

    public String getBashAbsolutePath() {
        return bashAbsolutePath;
    }

    public long getBookingRequiredTotal() {
        return bookingRequiredTotal;
    }

    public int getCheckComputerLoginValidity() {
        return checkComputerLoginValidity;
    }

    public int getCheckLinkValidityInterval() {
        return checkLinkValidityInterval;
    }

    public String getContactInfoCity() {
        return contactInfoCity;
    }

    public Country getContactInfoCountry() {
        try {
            return getEntityService().find(Country.class, getContactInfoCountryId());
        } catch (Exception e) {
            return null;
        }
    }

    public String getContactInfoCountryId() {
        return contactInfoCountryId;
    }

    public String getContactInfoEmail() {
        return contactInfoEmail;
    }

    public String getContactInfoMapUrl() {
        return contactInfoMapUrl;
    }

    public String getContactInfoOfficeHours() {
        return contactInfoOfficeHours;
    }

    public String getContactInfoPhone() {
        return contactInfoPhone;
    }

    public String getContactInfoRoom() {
        return contactInfoRoom;
    }

    public String getContactInfoStreet() {
        return contactInfoStreet;
    }

    public String getContactInfoSupplement() {
        return contactInfoSupplement;
    }

    public String getContactInfoZip() {
        return contactInfoZip;
    }

    public String getCoordinatorEmail() {
        return getEmailOrDeployerDefaultEmail(coordinatorEmail);
    }

    public int getCustomOrderStatusAutocompleteMinimumSize() {
        return customOrderStatusAutocompleteMinimumSize;
    }

    public int getDataScrollerChunkSize() {
        return dataScrollerChunkSize;
    }

    public int getDataTableExportLimit() {
        return dataTableExportLimit;
    }

    public long getDefaultBookingIssuerId() {
        return defaultBookingIssuerId;
    }

    public long getDefaultBudgetLimit() {
        return defaultBudgetLimit;
    }

    public String getDefaultCharset() {
        return defaultCharset;
    }

    public String getDefaultChartColors() {
        return defaultChartColors;
    }

    public String getDefaultCompanyName() {
        return defaultCompanyName;
    }

    public String getDefaultCountryId() {
        return defaultCountryId;
    }

    public String getDefaultCurrencyCode() {
        return defaultCurrencyCode;
    }

    public String getDefaultDataScrollerChunkSizeTemplate() {
        return defaultDataScrollerChunkSizeTemplate;
    }

    public List<Integer> getDefaultDataScrollerChunkSizeTemplateList() {
        return Stream.of(getDefaultDataScrollerChunkSizeTemplate().split(",")).map(String::trim).map(Integer::parseInt).collect(Collectors.toList());
    }

    public String getDefaultDatePattern() {
        return defaultDatePattern;
    }

    public String getDefaultDateTimePattern() {
        return defaultDateTimePattern;
    }

    public String getDefaultDivision() {
        return defaultDivision;
    }

    public long getDefaultMasterExecutableIdStorage() {
        return defaultMasterExecutableIdStorage;
    }

    public long getDefaultMasterExecutableIdSubmitter() {
        return defaultMasterExecutableIdSubmitter;
    }

    public long getDefaultMasterExecutableIdWrapperCreator() {
        return defaultMasterExecutableIdWrapperCreator;
    }

    public String getDefaultRowsPerPageTemplate() {
        return defaultRowsPerPageTemplate;
    }

    public List<Integer> getDefaultRowsPerPageTemplateList() {
        return Stream.of(getDefaultRowsPerPageTemplate().split(",")).map(String::trim).map(Integer::parseInt).collect(Collectors.toList());
    }

    public String getDefaultTaxTypeName() {
        return defaultTaxTypeName;
    }

    public String getDefaultTimePattern() {
        return defaultTimePattern;
    }

    public DeployerContextProperty getDeployer() {
        return deployer;
    }

    public String getDeployerAbbreviation() {
        return deployerAbbreviation;
    }

    public String getDeployerAbbreviationName() {
        return StringHelper.isNotEmpty(getDeployerAbbreviation()) ? getDeployerAbbreviation() : Constants.EMPTY_STRING;
    }

    public String getDeployerAddressCity() {
        return deployerAddressCity;
    }

    public String getDeployerAddressCityZip() {
        StringBuilder cityZip = new StringBuilder();
        if (StringHelper.isNotEmpty(getDeployerAddressZip())) {
            cityZip.append(getDeployerAddressZip()).append(" ");
        }
        if (StringHelper.isNotEmpty(getDeployerAddressCity())) {
            cityZip.append(getDeployerAddressCity());
        }
        return cityZip.toString();
    }

    public String getDeployerAddressCountry() {
        return deployerAddressCountry;
    }

    public String getDeployerAddressStreet() {
        return deployerAddressStreet;
    }

    public String getDeployerAddressSupplement() {
        return deployerAddressSupplement;
    }

    public String getDeployerAddressZip() {
        return deployerAddressZip;
    }

    public String getDeployerColor() {
        return deployerColor;
    }

    public String getDeployerCountry() {
        return deployerCountry;
    }

    public String getDeployerDefaultEmail() {
        return deployerDefaultEmail;
    }

    public String getDeployerEmail() {
        return deployerEmail;
    }

    public String getDeployerFullAddress() {
        try {
            return StringHelper.getFullAddress(null, null, getDeployerAddressStreet(), getDeployerAddressZip(), getDeployerAddressCity(), CDI.current().select(CountryService.class).get()
                .getCountryByIdOrName(getDeployerAddressCountry()), 0);
        } catch (Exception e) {
            return Constants.EMPTY_STRING;
        }
    }

    public String getDeployerHomeURL() {
        return deployerHomeURL;
    }

    public String getDeployerName() {
        return deployerName;
    }

    public String getDeployerPhoneNumber() {
        return deployerPhoneNumber;
    }

    public String getDeployerPhonePrefix() {
        return deployerPhonePrefix;
    }

    public String getDeployerUpperCase() {
        return getDeployer().getValue().toUpperCase();
    }

    public String getDeployerZip() {
        return deployerZip;
    }

    public String getDeploymentBranchName() {
        return deploymentBranchName;
    }

    public String getDeploymentBranchNamePrinted() {
        return "master".equals(deploymentBranchName) ? Constants.EMPTY_STRING : deploymentBranchName;
    }

    public String getDeploymentBranchVersion() {
        return (getDeployerAbbreviationName() + " " + getEnvironmentName() + " " + getApplicationVersionShortName() + " " + getDeploymentBranchNamePrinted()).replaceAll("\\s+", " ");
    }

    public String getDeploymentCompilationDateTime() {
        return deploymentCompilationDateTime;
    }

    public String getDeploymentDateTime() {
        return deploymentDateTime;
    }

    public String getDeploymentGitRevisionId() {
        return deploymentGitRevisionId;
    }

    public String getDoiPrefix() {
        return doiPrefix;
    }

    public int getDownloadManagerJNLPValidityDuration() {
        return downloadManagerJNLPValidityDuration;
    }

    public String getEmailOrDeployerDefaultEmail(String email) {
        return email != null ? StringHelper.format(email) : getDeployerDefaultEmail();
    }

    public String getEnvAbsolutePath() {
        return envAbsolutePath;
    }

    public EnvironmentContextProperty getEnvironment() {
        return environment;
    }

    public String getEnvironmentName() {
        return getEnvironment() != null && getEnvironment().getValue() != null ? getEnvironment().getValue().toUpperCase() : Constants.EMPTY_STRING;
    }

    public String getFromEmailAddress() {
        return getEmailOrDeployerDefaultEmail(fromEmailAddress);
    }

    public String getFullContactInfo() {
        return StringHelper.getFullAddress(getContactInfoSupplement(), getContactInfoRoom(), getContactInfoStreet(), getContactInfoZip(), getContactInfoCity(), getContactInfoCountry(), 1);
    }

    public String getHeaderBackgroundColor() {
        return headerBackgroundColor;
    }

    public Calendar getIcsCalendar() {
        Calendar calendar = new Calendar();
        calendar.add(new ProdId(getApplicationName()));
        calendar.add(Version.VERSION_2_0);
        calendar.add(CalScale.GREGORIAN);
        return calendar;
    }

    public String getIndexPath() {
        return indexPath;
    }

    public InstanceContextProperty getInstance() {
        return instance;
    }

    public int getInstrumentReservationSettingMaxHours() {
        return instrumentReservationSettingMaxHours;
    }

    public int getInstrumentReservationSettingSlotDurationDefault() {
        return instrumentReservationSettingSlotDurationDefault;
    }

    public Duration getInstrumentReservationSettingSlotDurationDefaultAsDuration() {
        return Duration.ofMinutes(instrumentReservationSettingSlotDurationDefault);
    }

    public int getListingRows() {
        return listingRows;
    }

    public String getLoginAutoComplete() {
        return loginAutoComplete;
    }

    public String getMailSubjectPrefix() {
        return mailSubjectPrefix;
    }

    public long getMasterExecutableIdContainerSync() {
        return masterExecutableIdContainerSync;
    }

    public long getMasterExecutableIdUserSync() {
        return masterExecutableIdUserSync;
    }

    public long getMaxAttachmentFiles() {
        return maxAttachmentFiles;
    }

    public long getMaxAttachmentSize() {
        return maxAttachmentSize;
    }

    public int getMaxBatchEditItems() {
        return maxBatchEditItems;
    }

    public int getMaxBatchEditItemsPlates() {
        return maxBatchEditItemsPlates;
    }

    public long getMaxItemsOnShowDetails() {
        return maxItemsOnShowDetails;
    }

    public int getMaxLoginAttempts() {
        return maxLoginAttempts;
    }

    public String getOfferDisclaimer() {
        StringBuilder disclaimer = new StringBuilder();
        if (isOfferValidityDatePassed()) {
            disclaimer.append(Messages.get("offerValidityDurationDisclaimer").replace("{0}", String.valueOf(getOfferValidityDuration())));
        } else {
            disclaimer.append(Messages.get("offerValidityDateDisclaimer").replace("{0}", getOfferValidityDate().toString()));
        }
        disclaimer.append(" ").append(Messages.get("offerPricesDisclaimer").replace("{0}", "Swiss Frances (CHF)"));
        return disclaimer.toString();
    }

    public LocalDate getOfferValidityDate() {
        return offerValidityDate;
    }

    public int getOfferValidityDuration() {
        return offerValidityDuration;
    }

    public int getParentSamplesMaximumDisplayAmount() {
        return parentSamplesMaximumDisplayAmount;
    }

    public int getPollInterval() {
        return pollInterval;
    }

    public String getPubtktGeneratorFilePath() {
        return pubtktGeneratorFilePath;
    }

    public String getPwEncPublicKeyFilePath() {
        return pwEncPublicKeyFilePath;
    }

    public int getResourceBasketLimit() {
        return resourceBasketLimit;
    }

    public int getSessionTimeoutWarningTime() {
        return sessionTimeoutWarningTime;
    }

    public String getSupportEmail() {
        return getEmailOrDeployerDefaultEmail(supportEmail);
    }

    public String getTechnicalSupportEmail() {
        return getEmailOrDeployerDefaultEmail(technicalSupportEmail);
    }

    public String getUrlFAQ() {
        return urlFAQ;
    }

    public String getUrlIntranet() {
        return urlIntranet;
    }

    public String getUrlIssueTracker() {
        return urlIssueTracker;
    }

    public String getUrlProjectDescriptionGuidelines() {
        return urlProjectDescriptionGuidelines;
    }

    public String getUrlReleaseNotes() {
        return urlReleaseNotes;
    }

    public String getUrlSupport() {
        return urlSupport;
    }

    public String getUrlTermsAndConditions() {
        return urlTermsAndConditions;
    }

    public String getUrlUserManual() {
        return urlUserManual;
    }

    public int getWebAppTokenExpirationTime() {
        return webAppTokenExpirationTime;
    }

    public int getWebServiceQueryMaxElements() {
        return webServiceQueryMaxElements;
    }

    public int getWebServiceQueryResultMaxEntitiesPerPage() {
        return webServiceQueryResultMaxEntitiesPerPage;
    }

    public boolean isAaiLoginEnabled() {
        return aaiLoginEnabled;
    }

    public boolean isAccessCardExpiryReminderJobEnabled() {
        return accessCardExpiryReminderJobEnabled;
    }

    public boolean isAccessRequestEnabled() {
        return accessRequestEnabled;
    }

    public boolean isAddAffiliationByUserEnabled() {
        return addAffiliationByUserEnabled;
    }

    public boolean isAgendaEnabled() {
        return agendaEnabled;
    }

    public boolean isAskOldPasswordOnChangeRequest() {
        return askOldPasswordOnChangeRequest;
    }

    public boolean isBookerETHEnabled() {
        return bookerETHEnabled;
    }

    public boolean isBookingPdfChargeDateEnabled() {
        return bookingPdfChargeDateEnabled;
    }

    public boolean isBookingPdfProjectNameEnabled() {
        return bookingPdfProjectNameEnabled;
    }

    public boolean isBookingTransferEnabled() {
        return bookingTransferEnabled;
    }

    public boolean isBrowserDownloadEnabled() {
        return browserDownloadEnabled;
    }

    public boolean isBudgetLimitEnabled() {
        return budgetLimitEnabled;
    }

    public boolean isCompanyAutocompleteEnabled() {
        return companyAutocompleteEnabled;
    }

    public boolean isContractExpiryReminderJobEnabled() {
        return contractExpiryReminderJobEnabled;
    }

    public boolean isDataManagementEnabled() {
        return dataManagementEnabled;
    }

    public boolean isDatasetTypeCheckEnabled() {
        return datasetTypeCheckEnabled;
    }

    public boolean isDeleteDeletableOffersJobEnabled() {
        return deleteDeletableOffersJobEnabled;
    }

    public boolean isDeleteDeletableUsersJobEnabled() {
        return deleteDeletableUsersJobEnabled;
    }

    public boolean isDeleteExpiredShibbolethMappingsEnabled() {
        return deleteExpiredShibbolethMappingsEnabled;
    }

    public boolean isDeleteUnassignedObjectsJobEnabled() {
        return deleteUnassignedObjectsJobEnabled;
    }

    public boolean isDeployer(String deployer) {
        return deployer != null && deployer.equals(getDeployer().getValue());
    }

    public boolean isDeployerEthUzhEnabled() {
        return deployerEthUzhEnabled;
    }

    public boolean isDeployerFGCZ() {
        return isDeployer(Constants.DEPLOYER_FGCZ);
    }

    public boolean isDoiEnabled() {
        return doiEnabled;
    }

    public boolean isDoiUrlModified() {
        return doiUrlModified;
    }

    public boolean isDownloadEnabled() {
        return downloadEnabled;
    }

    public boolean isDownloadManagerEnabled() {
        return downloadManagerEnabled;
    }

    public boolean isEmployeePrivateInfoRequired() {
        return employeePrivateInfoRequired;
    }

    public boolean isEnvironmentLocal() {
        return EnvironmentEnum.LOCAL.getName().equals(getEnvironment().getValue());
    }

    public boolean isEnvironmentProduction() {
        return EnvironmentEnum.PRODUCTION.getName().equals(getEnvironment().getValue());
    }

    public boolean isExtensionReportReminderJobEnabled() {
        return extensionReportReminderJobEnabled;
    }

    public boolean isFeedbackEnabled() {
        return feedbackEnabled;
    }

    public boolean isHotKeysEnabled() {
        return hotKeysEnabled;
    }

    public boolean isHotKeysPublicEnabled() {
        return hotKeysPublicEnabled;
    }

    public boolean isIndexDirectoryExisting() {
        if (getIndexPath() != null) {
            File indexDirectory = new File(getIndexPath());
            if (indexDirectory.exists()) {
                return true;
            } else if (isReindexJobEnabled()) {
                logger.info("Lucene index does not exist!");
            }
        }
        return false;
    }

    public boolean isInstrumentReservationEnabled() {
        return instrumentReservationEnabled;
    }

    public boolean isInstrumentReservationReminderJobEnabled() {
        return instrumentReservationReminderJobEnabled;
    }

    public boolean isInstrumentReservationWeekendsEnabled() {
        return instrumentReservationWeekendsEnabled;
    }

    public boolean isKpiHomePageEnabled() {
        return kpiHomePageEnabled;
    }

    public boolean isLabEnabled() {
        return labEnabled;
    }

    public boolean isLegacyEnabled() {
        return legacyEnabled;
    }

    public boolean isLogPageAccesses() {
        return logPageAccesses;
    }

    public boolean isMailEnabled() {
        return mailEnabled;
    }

    public boolean isMeasureCallsFilterEnabled() {
        return measureCallsFilterEnabled;
    }

    public boolean isOfferValidityDatePassed() {
        return getOfferValidityDate() == null || getOfferValidityDate().isBefore(LocalDate.now());
    }

    public boolean isOneTimeTokenEnabled() {
        return oneTimeTokenEnabled;
    }

    public boolean isOrderEnabled() {
        return orderEnabled;
    }

    public boolean isRefreshMaterializedViewsJobEnabled() {
        return refreshMaterializedViewsJobEnabled;
    }

    public boolean isReindexJobEnabled() {
        return reindexJobEnabled;
    }

    public boolean isResetUserAvailableJobEnabled() {
        return resetUserAvailableJobEnabled;
    }

    public boolean isReviewRequired() {
        return reviewRequired;
    }

    public boolean isShowSamplesLaneButton() {
        return showSamplesLaneButton;
    }

    public boolean isShowSamplesLaneButtonAndCheckbox() {
        return showSamplesLaneButtonAndCheckbox;
    }

    public boolean isShowSamplesLaneSeparated() {
        return showSamplesLaneSeparated;
    }

    public boolean isStatisticsEnabled() {
        return statisticsEnabled;
    }

    public boolean isSynchronizeWithADEnabled() {
        return synchronizeWithADEnabled;
    }

    public boolean isTrustAllCertificates() {
        return trustAllCertificates;
    }

    public boolean isUnarchiveEnabled() {
        return unarchiveEnabled;
    }

    public boolean isUserRegistrationEnabled() {
        return userRegistrationEnabled;
    }

    public boolean isVirusScannerDisabled() {
        return virusScannerDisabled;
    }

    public boolean isWorkflowEnabled() {
        return workflowEnabled;
    }

    public void setAaiLoginEnabled(boolean aaiLoginEnabled) {
        this.aaiLoginEnabled = aaiLoginEnabled;
    }

    public void setAaiResourceEntityId(String aaiResourceEntityId) {
        this.aaiResourceEntityId = StringHelper.format(aaiResourceEntityId);
    }

    public void setAbsencesMailAddress(String absencesMailAddress) {
        this.absencesMailAddress = StringHelper.format(absencesMailAddress);
    }

    public void setAccessCardCodeGuestPattern(String accessCardCodeGuestPattern) {
        this.accessCardCodeGuestPattern = accessCardCodeGuestPattern;
    }

    public void setAccessCardCodePattern(String accessCardCodePattern) {
        this.accessCardCodePattern = accessCardCodePattern;
    }

    public void setAccessCardExpiryReminderJobEnabled(boolean accessCardExpiryReminderJobEnabled) {
        this.accessCardExpiryReminderJobEnabled = accessCardExpiryReminderJobEnabled;
    }

    public void setAccessCardNumberPattern(String accessCardNumberPattern) {
        this.accessCardNumberPattern = accessCardNumberPattern;
    }

    public void setAccessRequestEnabled(boolean accessRequestEnabled) {
        this.accessRequestEnabled = accessRequestEnabled;
    }

    public void setAccessRequestManagerAddress(String accessRequestManagerAddress) {
        this.accessRequestManagerAddress = accessRequestManagerAddress;
    }

    public void setAccessRequestManagerDatePattern(String accessRequestManagerDatePattern) {
        this.accessRequestManagerDatePattern = accessRequestManagerDatePattern;
    }

    public void setAccessRequestManagerEmail(String accessRequestManagerEmail) {
        this.accessRequestManagerEmail = getEmailOrDeployerDefaultEmail(accessRequestManagerEmail);
    }

    public void setAccessRequestManagerInstitute(String accessRequestManagerInstitute) {
        this.accessRequestManagerInstitute = accessRequestManagerInstitute;
    }

    public void setAccessRequestManagerInstituteDirector(String accessRequestManagerInstituteDirector) {
        this.accessRequestManagerInstituteDirector = accessRequestManagerInstituteDirector;
    }

    public void setAccessRequestManagerInstituteExtension(String accessRequestManagerInstituteExtension) {
        this.accessRequestManagerInstituteExtension = accessRequestManagerInstituteExtension;
    }

    public void setAccessRequestManagerName(String accessRequestManagerName) {
        this.accessRequestManagerName = accessRequestManagerName;
    }

    public void setAccessRequestManagerOfficeTimes(String accessRequestManagerOfficeTimes) {
        this.accessRequestManagerOfficeTimes = accessRequestManagerOfficeTimes;
    }

    public void setAccessRequestManagerPhone(String accessRequestManagerPhone) {
        this.accessRequestManagerPhone = accessRequestManagerPhone;
    }

    public void setAccessRequestManagerPlace(String accessRequestManagerPlace) {
        this.accessRequestManagerPlace = accessRequestManagerPlace;
    }

    public void setAccessRequestNotificationEmail(String accessRequestNotificationEmail) {
        this.accessRequestNotificationEmail = getEmailOrDeployerDefaultEmail(accessRequestNotificationEmail);
    }

    public void setAccessRequestPassword(String accessRequestPassword) {
        this.accessRequestPassword = accessRequestPassword;
    }

    public void setAccessRequestUZHEmail(String accessRequestUZHEmail) {
        this.accessRequestUZHEmail = getEmailOrDeployerDefaultEmail(accessRequestUZHEmail);
    }

    public void setAddAffiliationByUserEnabled(boolean addAffiliationByUserEnabled) {
        this.addAffiliationByUserEnabled = addAffiliationByUserEnabled;
    }

    public void setAgendaEnabled(boolean agendaEnabled) {
        this.agendaEnabled = agendaEnabled;
    }

    public void setAnnualVacationCreditAboveAgeLimit(int annualVacationCreditAboveAgeLimit) {
        this.annualVacationCreditAboveAgeLimit = annualVacationCreditAboveAgeLimit;
    }

    public void setAnnualVacationCreditAgeLimit(int annualVacationCreditAgeLimit) {
        this.annualVacationCreditAgeLimit = annualVacationCreditAgeLimit;
    }

    public void setAnnualVacationCreditBelowAgeLimit(int annualVacationCreditBelowAgeLimit) {
        this.annualVacationCreditBelowAgeLimit = annualVacationCreditBelowAgeLimit;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = StringHelper.format(applicationName);
    }

    public void setAskOldPasswordOnChangeRequest(boolean askOldPasswordOnChangeRequest) {
        this.askOldPasswordOnChangeRequest = askOldPasswordOnChangeRequest;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = StringHelper.format(baseUrl);
    }

    public void setBashAbsolutePath(String bashAbsolutePath) {
        this.bashAbsolutePath = StringHelper.format(bashAbsolutePath);
    }

    public void setBookerETHEnabled(boolean bookerETHEnabled) {
        this.bookerETHEnabled = bookerETHEnabled;
    }

    public void setBookingPdfChargeDateEnabled(boolean bookingPdfChargeDateEnabled) {
        this.bookingPdfChargeDateEnabled = bookingPdfChargeDateEnabled;
    }

    public void setBookingPdfProjectNameEnabled(boolean bookingPdfProjectNameEnabled) {
        this.bookingPdfProjectNameEnabled = bookingPdfProjectNameEnabled;
    }

    public void setBookingRequiredTotal(long bookingRequiredTotal) {
        this.bookingRequiredTotal = bookingRequiredTotal;
    }

    public void setBookingTransferEnabled(boolean bookingTransferEnabled) {
        this.bookingTransferEnabled = bookingTransferEnabled;
    }

    public void setBrowserDownloadEnabled(boolean browserDownloadEnabled) {
        this.browserDownloadEnabled = browserDownloadEnabled;
    }

    public void setBudgetLimitEnabled(boolean budgetLimitEnabled) {
        this.budgetLimitEnabled = budgetLimitEnabled;
    }

    public void setCheckComputerLoginValidity(int checkComputerLoginValidity) {
        this.checkComputerLoginValidity = checkComputerLoginValidity;
    }

    public void setCheckLinkValidityInterval(int checkLinkValidityInterval) {
        this.checkLinkValidityInterval = checkLinkValidityInterval;
    }

    public void setCompanyAutocompleteEnabled(boolean companyAutocompleteEnabled) {
        this.companyAutocompleteEnabled = companyAutocompleteEnabled;
    }

    public void setContactInfoCity(String contactInfoCity) {
        this.contactInfoCity = contactInfoCity;
    }

    public void setContactInfoCountryId(String contactInfoCountryId) {
        this.contactInfoCountryId = contactInfoCountryId;
    }

    public void setContactInfoEmail(String contactInfoEmail) {
        this.contactInfoEmail = contactInfoEmail;
    }

    public void setContactInfoMapUrl(String contactInfoMapUrl) {
        this.contactInfoMapUrl = contactInfoMapUrl;
    }

    public void setContactInfoOfficeHours(String contactInfoOfficeHours) {
        this.contactInfoOfficeHours = contactInfoOfficeHours;
    }

    public void setContactInfoPhone(String contactInfoPhone) {
        this.contactInfoPhone = contactInfoPhone;
    }

    public void setContactInfoRoom(String contactInfoRoom) {
        this.contactInfoRoom = contactInfoRoom;
    }

    public void setContactInfoStreet(String contactInfoStreet) {
        this.contactInfoStreet = contactInfoStreet;
    }

    public void setContactInfoSupplement(String contactInfoSupplement) {
        this.contactInfoSupplement = contactInfoSupplement;
    }

    public void setContactInfoZip(String contactInfoZip) {
        this.contactInfoZip = contactInfoZip;
    }

    public void setContractExpiryReminderJobEnabled(boolean contractExpiryReminderJobEnabled) {
        this.contractExpiryReminderJobEnabled = contractExpiryReminderJobEnabled;
    }

    public void setCoordinatorEmail(String coordinatorEmail) {
        this.coordinatorEmail = getEmailOrDeployerDefaultEmail(coordinatorEmail);
    }

    public void setCustomOrderStatusAutocompleteMinimumSize(int customOrderStatusAutocompleteMinimumSize) {
        this.customOrderStatusAutocompleteMinimumSize = customOrderStatusAutocompleteMinimumSize;
    }

    public void setDataManagementEnabled(boolean dataManagementEnabled) {
        this.dataManagementEnabled = dataManagementEnabled;
    }

    public void setDataScrollerChunkSize(int dataScrollerChunkSize) {
        this.dataScrollerChunkSize = dataScrollerChunkSize;
    }

    public void setDataTableExportLimit(int dataTableExportLimit) {
        this.dataTableExportLimit = dataTableExportLimit;
    }

    public void setDatasetTypeCheckEnabled(boolean datasetTypeCheckEnabled) {
        this.datasetTypeCheckEnabled = datasetTypeCheckEnabled;
    }

    public void setDefaultBookingIssuerId(long defaultBookingIssuerId) {
        this.defaultBookingIssuerId = defaultBookingIssuerId;
    }

    public void setDefaultBudgetLimit(long defaultBudgetLimit) {
        this.defaultBudgetLimit = defaultBudgetLimit;
    }

    public void setDefaultCharset(String defaultCharset) {
        this.defaultCharset = defaultCharset;
    }

    public void setDefaultChartColors(String defaultChartColors) {
        this.defaultChartColors = defaultChartColors;
    }

    public void setDefaultCompanyName(String defaultCompanyName) {
        this.defaultCompanyName = defaultCompanyName;
    }

    public void setDefaultCountryId(String defaultCountryId) {
        this.defaultCountryId = defaultCountryId;
    }

    public void setDefaultCurrencyCode(String defaultCurrencyCode) {
        this.defaultCurrencyCode = defaultCurrencyCode;
    }

    public void setDefaultDataScrollerChunkSizeTemplate(String defaultDataScrollerChunkSizeTemplate) {
        this.defaultDataScrollerChunkSizeTemplate = defaultDataScrollerChunkSizeTemplate;
    }

    public void setDefaultDatePattern(String defaultDatePattern) {
        this.defaultDatePattern = defaultDatePattern;
    }

    public void setDefaultDateTimePattern(String defaultDateTimePattern) {
        this.defaultDateTimePattern = defaultDateTimePattern;
    }

    public void setDefaultDivision(String defaultDivision) {
        this.defaultDivision = defaultDivision;
    }

    public void setDefaultMasterExecutableIdStorage(long defaultMasterExecutableIdStorage) {
        this.defaultMasterExecutableIdStorage = defaultMasterExecutableIdStorage;
    }

    public void setDefaultMasterExecutableIdSubmitter(long defaultMasterExecutableIdSubmitter) {
        this.defaultMasterExecutableIdSubmitter = defaultMasterExecutableIdSubmitter;
    }

    public void setDefaultMasterExecutableIdWrapperCreator(long defaultMasterExecutableIdWrapperCreator) {
        this.defaultMasterExecutableIdWrapperCreator = defaultMasterExecutableIdWrapperCreator;
    }

    public void setDefaultRowsPerPageTemplate(String defaultRowsPerPageTemplate) {
        this.defaultRowsPerPageTemplate = defaultRowsPerPageTemplate;
    }

    public void setDefaultTaxTypeName(String defaultTaxTypeName) {
        this.defaultTaxTypeName = defaultTaxTypeName;
    }

    public void setDefaultTimePattern(String defaultTimePattern) {
        this.defaultTimePattern = defaultTimePattern;
    }

    public void setDeleteDeletableOffersJobEnabled(boolean deleteDeletableOffersJobEnabled) {
        this.deleteDeletableOffersJobEnabled = deleteDeletableOffersJobEnabled;
    }

    public void setDeleteDeletableUsersJobEnabled(boolean deleteDeletableUsersJobEnabled) {
        this.deleteDeletableUsersJobEnabled = deleteDeletableUsersJobEnabled;
    }

    public void setDeleteExpiredShibbolethMappingsEnabled(boolean deleteExpiredShibbolethMappingsEnabled) {
        this.deleteExpiredShibbolethMappingsEnabled = deleteExpiredShibbolethMappingsEnabled;
    }

    public void setDeleteUnassignedObjectsJobEnabled(boolean deleteUnassignedObjectsJobEnabled) {
        this.deleteUnassignedObjectsJobEnabled = deleteUnassignedObjectsJobEnabled;
    }

    public void setDeployer(DeployerContextProperty deployer) {
        this.deployer = deployer;
    }

    public void setDeployerAbbreviation(String deployerAbbreviation) {
        this.deployerAbbreviation = deployerAbbreviation;
    }

    public void setDeployerAddressCity(String deployerAddressCity) {
        this.deployerAddressCity = StringHelper.format(deployerAddressCity);
    }

    public void setDeployerAddressCountry(String deployerAddressCountry) {
        this.deployerAddressCountry = StringHelper.format(deployerAddressCountry);
    }

    public void setDeployerAddressStreet(String deployerAddressStreet) {
        this.deployerAddressStreet = StringHelper.format(deployerAddressStreet);
    }

    public void setDeployerAddressSupplement(String deployerAddressSupplement) {
        this.deployerAddressSupplement = deployerAddressSupplement;
    }

    public void setDeployerAddressZip(String deployerAddressZip) {
        this.deployerAddressZip = StringHelper.format(deployerAddressZip);
    }

    public void setDeployerColor(String deployerColor) {
        this.deployerColor = deployerColor;
    }

    public void setDeployerCountry(String deployerCountry) {
        this.deployerCountry = StringHelper.format(deployerCountry);
    }

    public void setDeployerDefaultEmail(String deployerDefaultEmail) {
        this.deployerDefaultEmail = StringHelper.format(deployerDefaultEmail);
    }

    public void setDeployerEmail(String deployerEmail) {
        this.deployerEmail = StringHelper.format(deployerEmail);
    }

    public void setDeployerEthUzhEnabled(boolean deployerEthUzhEnabled) {
        this.deployerEthUzhEnabled = deployerEthUzhEnabled;
    }

    public void setDeployerHomeURL(String deployerHomeURL) {
        this.deployerHomeURL = StringHelper.format(deployerHomeURL);
    }

    public void setDeployerName(String deployerName) {
        this.deployerName = StringHelper.format(deployerName);
    }

    public void setDeployerPhoneNumber(String deployerPhoneNumber) {
        this.deployerPhoneNumber = StringHelper.format(deployerPhoneNumber);
    }

    public void setDeployerPhonePrefix(String deployerPhonePrefix) {
        this.deployerPhonePrefix = deployerPhonePrefix;
    }

    public void setDeployerZip(String deployerZip) {
        this.deployerZip = StringHelper.format(deployerZip);
    }

    public void setDeploymentBranchName(String deploymentBranchName) {
        this.deploymentBranchName = StringHelper.format(deploymentBranchName);
    }

    public void setDeploymentCompilationDateTime(String deploymentCompilationDateTime) {
        this.deploymentCompilationDateTime = StringHelper.format(deploymentCompilationDateTime);
    }

    public void setDeploymentDateTime(String deploymentDateTime) {
        this.deploymentDateTime = StringHelper.format(deploymentDateTime);
    }

    public void setDeploymentGitRevisionId(String deploymentGitRevisionId) {
        this.deploymentGitRevisionId = StringHelper.format(deploymentGitRevisionId);
    }

    public void setDoiEnabled(boolean doiEnabled) {
        this.doiEnabled = doiEnabled;
    }

    public void setDoiPrefix(String doiPrefix) {
        this.doiPrefix = StringHelper.format(doiPrefix);
    }

    public void setDoiUrlModified(boolean doiUrlModified) {
        this.doiUrlModified = doiUrlModified;
    }

    public void setDownloadEnabled(boolean downloadEnabled) {
        this.downloadEnabled = downloadEnabled;
    }

    public void setDownloadManagerEnabled(boolean downloadManagerEnabled) {
        this.downloadManagerEnabled = downloadManagerEnabled;
    }

    public void setDownloadManagerJNLPValidityDuration(int downloadManagerJNLPValidityDuration) {
        this.downloadManagerJNLPValidityDuration = downloadManagerJNLPValidityDuration;
    }

    public void setEmployeePrivateInfoRequired(boolean employeePrivateInfoRequired) {
        this.employeePrivateInfoRequired = employeePrivateInfoRequired;
    }

    public void setEnvAbsolutePath(String envAbsolutePath) {
        this.envAbsolutePath = StringHelper.format(envAbsolutePath);
    }

    public void setEnvironment(EnvironmentContextProperty environment) {
        this.environment = environment;
    }

    public void setExtensionReportReminderJobEnabled(boolean extensionReportReminderJobEnabled) {
        this.extensionReportReminderJobEnabled = extensionReportReminderJobEnabled;
    }

    public void setFeedbackEnabled(boolean feedbackEnabled) {
        this.feedbackEnabled = feedbackEnabled;
    }

    public void setFromEmailAddress(String fromEmailAddress) {
        this.fromEmailAddress = getEmailOrDeployerDefaultEmail(fromEmailAddress);
    }

    public void setHeaderBackgroundColor(String headerBackgroundColor) {
        this.headerBackgroundColor = headerBackgroundColor;
    }

    public void setHotKeysEnabled(boolean hotKeysEnabled) {
        this.hotKeysEnabled = hotKeysEnabled;
    }

    public void setHotKeysPublicEnabled(boolean hotKeysPublicEnabled) {
        this.hotKeysPublicEnabled = hotKeysPublicEnabled;
    }

    public void setIndexPath(String indexPath) {
        this.indexPath = StringHelper.format(indexPath);
    }

    public void setInstance(InstanceContextProperty instance) {
        this.instance = instance;
    }

    public void setInstrumentReservationEnabled(boolean instrumentReservationEnabled) {
        this.instrumentReservationEnabled = instrumentReservationEnabled;
    }

    public void setInstrumentReservationReminderJobEnabled(boolean instrumentReservationReminderJobEnabled) {
        this.instrumentReservationReminderJobEnabled = instrumentReservationReminderJobEnabled;
    }

    public void setInstrumentReservationSettingMaxHours(int instrumentReservationSettingMaxHours) {
        this.instrumentReservationSettingMaxHours = instrumentReservationSettingMaxHours;
    }

    public void setInstrumentReservationSettingSlotDurationDefault(int instrumentReservationSettingSlotDurationDefault) {
        this.instrumentReservationSettingSlotDurationDefault = instrumentReservationSettingSlotDurationDefault;
    }

    public void setInstrumentReservationWeekendsEnabled(boolean instrumentReservationWeekendsEnabled) {
        this.instrumentReservationWeekendsEnabled = instrumentReservationWeekendsEnabled;
    }

    public void setKpiHomePageEnabled(boolean kpiHomePageEnabled) {
        this.kpiHomePageEnabled = kpiHomePageEnabled;
    }

    public void setLabEnabled(boolean labEnabled) {
        this.labEnabled = labEnabled;
    }

    public void setLegacyEnabled(boolean legacyEnabled) {
        this.legacyEnabled = legacyEnabled;
    }

    public void setListingRows(int listingRows) {
        this.listingRows = listingRows;
    }

    public void setLogPageAccesses(boolean logPageAccesses) {
        this.logPageAccesses = logPageAccesses;
    }

    public void setLoginAutoComplete(String loginAutoComplete) {
        this.loginAutoComplete = StringHelper.format(loginAutoComplete);
    }

    public void setMailEnabled(boolean mailEnabled) {
        this.mailEnabled = mailEnabled;
    }

    public void setMailSubjectPrefix(String mailSubjectPrefix) {
        this.mailSubjectPrefix = StringHelper.format(mailSubjectPrefix);
    }

    public void setMasterExecutableIdContainerSync(long masterExecutableIdContainerSync) {
        this.masterExecutableIdContainerSync = masterExecutableIdContainerSync;
    }

    public void setMasterExecutableIdUserSync(long masterExecutableIdUserSync) {
        this.masterExecutableIdUserSync = masterExecutableIdUserSync;
    }

    public void setMaxAttachmentFiles(long maxAttachmentFiles) {
        this.maxAttachmentFiles = maxAttachmentFiles;
    }

    public void setMaxAttachmentSize(long maxAttachmentSize) {
        this.maxAttachmentSize = maxAttachmentSize;
    }

    public void setMaxBatchEditItems(int maxBatchEditItems) {
        this.maxBatchEditItems = maxBatchEditItems;
    }

    public void setMaxBatchEditItemsPlates(int maxBatchEditItemsPlates) {
        this.maxBatchEditItemsPlates = maxBatchEditItemsPlates;
    }

    public void setMaxItemsOnShowDetails(long maxItemsOnShowDetails) {
        this.maxItemsOnShowDetails = maxItemsOnShowDetails;
    }

    public void setMaxLoginAttempts(int maxLoginAttempts) {
        this.maxLoginAttempts = maxLoginAttempts;
    }

    public void setMeasureCallsFilterEnabled(boolean measureCallsFilterEnabled) {
        this.measureCallsFilterEnabled = measureCallsFilterEnabled;
    }

    public void setOfferValidityDate(LocalDate offerValidityDate) {
        this.offerValidityDate = offerValidityDate;
    }

    public void setOfferValidityDuration(int offerValidityDuration) {
        this.offerValidityDuration = offerValidityDuration;
    }

    public void setOneTimeTokenEnabled(boolean oneTimeTokenEnabled) {
        this.oneTimeTokenEnabled = oneTimeTokenEnabled;
    }

    public void setOrderEnabled(boolean orderEnabled) {
        this.orderEnabled = orderEnabled;
    }

    public void setParentSamplesMaximumDisplayAmount(int parentSamplesMaximumDisplayAmount) {
        this.parentSamplesMaximumDisplayAmount = parentSamplesMaximumDisplayAmount;
    }

    public void setPollInterval(int pollInterval) {
        this.pollInterval = pollInterval;
    }

    public void setPubtktGeneratorFilePath(String pubtktGeneratorFilePath) {
        this.pubtktGeneratorFilePath = StringHelper.format(pubtktGeneratorFilePath);
    }

    public void setPwEncPublicKeyFilePath(String pwEncPublicKeyFilePath) {
        this.pwEncPublicKeyFilePath = StringHelper.format(pwEncPublicKeyFilePath);
    }

    public void setRefreshMaterializedViewsJobEnabled(boolean refreshMaterializedViewsJobEnabled) {
        this.refreshMaterializedViewsJobEnabled = refreshMaterializedViewsJobEnabled;
    }

    public void setReindexJobEnabled(boolean reindexJobEnabled) {
        this.reindexJobEnabled = reindexJobEnabled;
    }

    public void setResetUserAvailableJobEnabled(boolean resetUserAvailableJobEnabled) {
        this.resetUserAvailableJobEnabled = resetUserAvailableJobEnabled;
    }

    public void setResourceBasketLimit(int resourceBasketLimit) {
        this.resourceBasketLimit = resourceBasketLimit;
    }

    public void setReviewRequired(boolean reviewRequired) {
        this.reviewRequired = reviewRequired;
    }

    public void setSessionTimeoutWarningTime(int sessionTimeoutWarningTime) {
        this.sessionTimeoutWarningTime = sessionTimeoutWarningTime;
    }

    public void setShowSamplesLaneButton(boolean showSamplesLaneButton) {
        this.showSamplesLaneButton = showSamplesLaneButton;
    }

    public void setShowSamplesLaneButtonAndCheckbox(boolean showSamplesLaneButtonAndCheckbox) {
        this.showSamplesLaneButtonAndCheckbox = showSamplesLaneButtonAndCheckbox;
    }

    public void setShowSamplesLaneSeparated(boolean showSamplesLaneSeparated) {
        this.showSamplesLaneSeparated = showSamplesLaneSeparated;
    }

    public void setStatisticsEnabled(boolean statisticsEnabled) {
        this.statisticsEnabled = statisticsEnabled;
    }

    public void setSupportEmail(String supportEmail) {
        this.supportEmail = getEmailOrDeployerDefaultEmail(supportEmail);
    }

    public void setSynchronizeWithADEnabled(boolean synchronizeWithADEnabled) {
        this.synchronizeWithADEnabled = synchronizeWithADEnabled;
    }

    public void setTechnicalSupportEmail(String technicalSupportEmail) {
        this.technicalSupportEmail = getEmailOrDeployerDefaultEmail(technicalSupportEmail);
    }

    public void setTrustAllCertificates(boolean trustAllCertificates) {
        this.trustAllCertificates = trustAllCertificates;
    }

    public void setUnarchiveEnabled(boolean unarchiveEnabled) {
        this.unarchiveEnabled = unarchiveEnabled;
    }

    public void setUrlFAQ(String urlFAQ) {
        this.urlFAQ = StringHelper.format(urlFAQ);
    }

    public void setUrlIntranet(String urlIntranet) {
        this.urlIntranet = StringHelper.format(urlIntranet);
    }

    public void setUrlIssueTracker(String urlIssueTracker) {
        this.urlIssueTracker = urlIssueTracker;
    }

    public void setUrlProjectDescriptionGuidelines(String urlProjectDescriptionGuidelines) {
        this.urlProjectDescriptionGuidelines = StringHelper.format(urlProjectDescriptionGuidelines);
    }

    public void setUrlReleaseNotes(String urlReleaseNotes) {
        this.urlReleaseNotes = urlReleaseNotes;
    }

    public void setUrlSupport(String urlSupport) {
        this.urlSupport = urlSupport;
    }

    public void setUrlTermsAndConditions(String urlTermsAndConditions) {
        this.urlTermsAndConditions = StringHelper.format(urlTermsAndConditions);
    }

    public void setUrlUserManual(String urlUserManual) {
        this.urlUserManual = StringHelper.format(urlUserManual);
    }

    public void setUserRegistrationEnabled(boolean userRegistrationEnabled) {
        this.userRegistrationEnabled = userRegistrationEnabled;
    }

    public void setVirusScannerDisabled(boolean virusScannerDisabled) {
        this.virusScannerDisabled = virusScannerDisabled;
    }

    public void setWebAppTokenExpirationTime(int webAppTokenExpirationTime) {
        this.webAppTokenExpirationTime = webAppTokenExpirationTime;
    }

    public void setWebServiceQueryMaxElements(int webServiceQueryMaxElements) {
        this.webServiceQueryMaxElements = webServiceQueryMaxElements;
    }

    public void setWebServiceQueryResultMaxEntitiesPerPage(int webServiceQueryResultMaxEntitiesPerPage) {
        this.webServiceQueryResultMaxEntitiesPerPage = webServiceQueryResultMaxEntitiesPerPage;
    }

    public void setWorkflowEnabled(boolean workflowEnabled) {
        this.workflowEnabled = workflowEnabled;
    }
}