package org.bfabric.list;

import java.io.Serializable;
import java.util.List;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.bfabric.entity.AccessRequestProfile;
import org.bfabric.entity.Application;
import org.bfabric.entity.Instrument;
import org.bfabric.entity.ServiceType;
import org.bfabric.entity.ServiceTypeCollection;
import org.bfabric.entity.Supplier;
import org.bfabric.interceptors.CachedMethodResult;
import org.bfabric.service.StatisticsService;

@Named
@ViewScoped
public class StatisticsList implements Serializable {

    private static final long serialVersionUID = 1;

    @Inject
    private StatisticsService statisticsService;

    @CachedMethodResult
    public List<Object> getUsersPerYear() {
        return getService().getUsersPerYear();
    }

    @CachedMethodResult
    public List<Object> getAccessRequestProfile(AccessRequestProfile accessRequestProfile) {
        return getService().getAccessRequestProfile(accessRequestProfile);
    }

    @CachedMethodResult
    public List<Object> getAccessRequestProfilePerYear(AccessRequestProfile accessRequestProfile) {
        return getService().getAccessRequestProfilePerYear(accessRequestProfile);
    }

    @CachedMethodResult
    public List<Object> getApplicationDataETHZUZHPerYear() {
        return getService().getApplicationDataETHZUZHPerYear();
    }

    @CachedMethodResult
    public List<Object> getApplicationDataPerYear() {
        return getService().getApplicationDataPerYear();
    }

    @CachedMethodResult
    public List<Object> getApplicationResourcesChart(Application application) {
        return getService().getApplicationResourcesChart(application);
    }

    @CachedMethodResult
    public List<Object> getApplicationRunsChart(Application application) {
        return getService().getApplicationRunsChart(application);
    }

    @CachedMethodResult
    public List<Object> getApplicationStatisticsPerYear() {
        return getService().getApplicationStatisticsPerYear();
    }

    @CachedMethodResult
    public List<Object> getApplicationWorkunitsChart(Application application) {
        return getService().getApplicationWorkunitsChart(application);
    }

    @CachedMethodResult
    public List<Object> getApplicationWorkunitsPerYear(Application application) {
        return getService().getApplicationWorkunitsPerYear(application);
    }

    @CachedMethodResult
    public List<Object> getAvgAnalyzingEfficiencyYearly() {
        return getService().getAvgAnalyzingEfficiencyYearly();
    }

    @CachedMethodResult
    public List<Object> getAvgArrivingEfficiencyYearly() {
        return getService().getAvgArrivingEfficiencyYearly();
    }

    @CachedMethodResult
    public List<Object> getAvgClosingEfficiencyYearly() {
        return getService().getAvgClosingEfficiencyYearly();
    }

    @CachedMethodResult
    public List<Object> getAvgProcessingEfficiencyYearly() {
        return getService().getAvgProcessingEfficiencyYearly();
    }

    @CachedMethodResult
    public List<Object> getBookedChargeCompany(long companyId) {
        return getService().getBookedChargeCompany(companyId);
    }

    @CachedMethodResult
    public List<Object> getBookedChargeDepartment(long departmentId) {
        return getService().getBookedChargeDepartment(departmentId);
    }

    @CachedMethodResult
    public List<Object> getBookedChargeDivision(long divisionId) {
        return getService().getBookedChargeDivision(divisionId);
    }

    @CachedMethodResult
    public List<Object> getBookedChargeInstitute(long instituteId) {
        return getService().getBookedChargeInstitute(instituteId);
    }

    @CachedMethodResult
    public List<Object> getBookedChargeOrganization(long organizationId) {
        return getService().getBookedChargeOrganization(organizationId);
    }

    @CachedMethodResult
    public List<Object> getBookedChargeOrganizationType(long organizationTypeId) {
        return getService().getBookedChargeOrganizationType(organizationTypeId);
    }

    @CachedMethodResult
    public List<Object> getBookedChargeService(long serviceId) {
        return getService().getBookedChargeService(serviceId);
    }

    @CachedMethodResult
    public List<Object> getBookedChargeServiceArea(long serviceAreaId) {
        return getService().getBookedChargeServiceArea(serviceAreaId);
    }

    @CachedMethodResult
    public List<Object> getBookedChargeServiceAreaChart(long serviceAreaId) {
        return getService().getBookedChargeServiceAreaChart(serviceAreaId);
    }

    @CachedMethodResult
    public List<Object> getBookedChargeServiceChart(long serviceId) {
        return getService().getBookedChargeServiceChart(serviceId);
    }

    @CachedMethodResult
    public List<Object> getBookedChargeServiceHierarchy() {
        return getService().getBookedChargeServiceHierarchy();
    }

    @CachedMethodResult
    public List<Object> getBookedChargeServiceType(long serviceTypeId) {
        return getService().getBookedChargeServiceType(serviceTypeId);
    }

    @CachedMethodResult
    public List<Object> getBookedChargeServiceTypeChart(long serviceTypeId) {
        return getService().getBookedChargeServiceTypeChart(serviceTypeId);
    }

    @CachedMethodResult
    public List<Object> getBookingCompanyCostCentre() {
        return getService().getBookingCompanyCostCentre();
    }

    @CachedMethodResult
    public List<Object> getBookingOrganizationCostCentre() {
        return getService().getBookingOrganizationCostCentre();
    }

    @CachedMethodResult
    public List<Object> getBookingPerCompany() {
        return getService().getBookingPerCompany();
    }

    @CachedMethodResult
    public List<Object> getBookingPerCompanyCostCentre() {
        return getService().getBookingPerCompanyCostCentre();
    }

    @CachedMethodResult
    public List<Object> getBookingPerContainer() {
        return getService().getBookingPerContainer();
    }

    @CachedMethodResult
    public List<Object> getBookingPerCostCentre() {
        return getService().getBookingPerCostCentre();
    }

    @CachedMethodResult
    public List<Object> getBookingPerInstrument() {
        return getService().getBookingPerInstrument();
    }

    @CachedMethodResult
    public List<Object> getBookingPerOrganization() {
        return getService().getBookingPerOrganization();
    }

    @CachedMethodResult
    public List<Object> getBookingPerService() {
        return getService().getBookingPerService();
    }

    @CachedMethodResult
    public List<Object> getBookingPerTechnology() {
        return getService().getBookingPerTechnology();
    }

    @CachedMethodResult
    public List<Object> getBookingPerTechnologyPerYear() {
        return getService().getBookingPerTechnologyPerYear();
    }

    @CachedMethodResult
    public List<Object> getBookingPerYear() {
        return getService().getBookingPerYear();
    }

    @CachedMethodResult
    public List<Object> getChargesPerInstrument() {
        return getService().getChargesPerInstrument();
    }

    @CachedMethodResult
    public List<Object> getCoachedProjectsPerYear() {
        return getService().getCoachedProjectsPerYear();
    }

    @CachedMethodResult
    public List<Object> getCompaniesOrdersYearly() {
        return getService().getCompaniesOrdersYearly();
    }

    @CachedMethodResult
    public List<Object> getConsumablesPerSupplier() {
        return getService().getConsumablesPerSupplier();
    }

    @CachedMethodResult
    public List<Object> getContainerUsage() {
        return getService().getContainerUsage();
    }

    @CachedMethodResult
    public List<Object> getContractCost() {
        return getService().getContractCost();
    }

    @CachedMethodResult
    public List<Object> getCubePerYear(String className) {
        return getService().getCubePerYear(className);
    }

    @CachedMethodResult
    public List<Object> getCubePerYearChart(String className) {
        return getService().getCubePerYearChart(className);
    }

    @CachedMethodResult
    public List<Object> getEntityCounts() {
        return getService().getEntityCounts();
    }

    @CachedMethodResult
    public List<Object> getIncomeContainer() {
        return getService().getIncomeContainer();
    }

    @CachedMethodResult
    public List<Object> getIncomeOrgCom() {
        return getService().getIncomeOrgCom();
    }

    @CachedMethodResult
    public List<Object> getIncomeOrgEthzUniz() {
        return getService().getIncomeOrgEthzUniz();
    }

    @CachedMethodResult
    public List<Object> getIncomeOrgType() {
        return getService().getIncomeOrgType();
    }

    @CachedMethodResult
    public List<Object> getIncomeTechnology() {
        return getService().getIncomeTechnology();
    }

    @CachedMethodResult
    public List<Object> getIncomeYearlyCounter() {
        return getService().getIncomeYearlyCounter();
    }

    @CachedMethodResult
    public List<Object> getInstrumentAllStatisticsDaysPerYear() {
        return getService().getInstrumentAllStatisticsDaysPerYear();
    }

    @CachedMethodResult
    public List<Object> getInstrumentBookableDaysPerYear(Instrument instrument) {
        return getService().getInstrumentBookableDaysPerYear(instrument);
    }

    @CachedMethodResult
    public List<Object> getInstrumentMaintenanceDaysPerYear(Instrument instrument) {
        return getService().getInstrumentMaintenanceDaysPerYear(instrument);
    }

    @CachedMethodResult
    public List<Object> getInstrumentNonBookableHoursPerYear(Instrument instrument) {
        return getService().getInstrumentNonBookableHoursPerYear(instrument);
    }

    @CachedMethodResult
    public List<Object> getInstrumentReservationChargedSumPerYear(Instrument instrument) {
        return getService().getInstrumentReservationChargedSumPerYear(instrument);
    }

    @CachedMethodResult
    public List<Object> getInstrumentReservationDaysPerYear(Instrument instrument) {
        return getService().getInstrumentReservationDaysPerYear(instrument);
    }

    @CachedMethodResult
    public List<Object> getInstrumentReservationDaysPerYearTechnologyBudgetOfficer() {
        return getService().getInstrumentReservationDaysPerYearTechnologyBudgetOfficer();
    }

    @CachedMethodResult
    public List<Object> getInstrumentResourcesPerYear(Instrument instrument) {
        return getService().getInstrumentResourcesPerYear(instrument);
    }

    @CachedMethodResult
    public List<Object> getInstrumentResourcesSizePerYear(Instrument instrument) {
        return getService().getInstrumentResourcesSizePerYear(instrument);
    }

    @CachedMethodResult
    public List<Object> getInstrumentRunsPerYear(Instrument instrument) {
        return getService().getInstrumentRunsPerYear(instrument);
    }

    @CachedMethodResult
    public List<Object> getInstrumentUpDaysPerYear(Instrument instrument) {
        return getService().getInstrumentUpDaysPerYear(instrument);
    }

    @CachedMethodResult
    public List<Object> getInstrumentWorkunitsPerYear(Instrument instrument) {
        return getService().getInstrumentWorkunitsPerYear(instrument);
    }

    @CachedMethodResult
    public List<Object> getInstrumentsCostsYearlyCHF() {
        return getService().getInstrumentsCostsYearlyCHF();
    }

    @CachedMethodResult
    public List<Object> getInstrumentsCostsYearlyEUR() {
        return getService().getInstrumentsCostsYearlyEUR();
    }

    @CachedMethodResult
    public List<Object> getInstrumentsCostsYearlyGBP() {
        return getService().getInstrumentsCostsYearlyGBP();
    }

    @CachedMethodResult
    public List<Object> getInstrumentsCostsYearlyUSD() {
        return getService().getInstrumentsCostsYearlyUSD();
    }

    @CachedMethodResult
    public List<Object> getInstrumentsPricePerYear() {
        return getService().getInstrumentsPricePerYear();
    }

    @CachedMethodResult
    public List<Object> getInstrumentsStatisticsDaysPerYear(Instrument instrument) {
        return getService().getInstrumentsStatisticsDaysPerYear(instrument);
    }

    @CachedMethodResult
    public List<Object> getLastApprovedProjects(String timeframe, int timeframeMultiplicator) {
        return getService().getLastApprovedProjects(timeframe, timeframeMultiplicator);
    }

    @CachedMethodResult
    public List<Object> getMaintenanceInstrumentsYearly() {
        return getService().getMaintenanceInstrumentsYearly();
    }

    @CachedMethodResult
    public List<Object> getNonBookedBillableCharges() {
        return getService().getNonBookedBillableCharges();
    }

    @CachedMethodResult
    public List<Object> getOrderCharges() {
        return getService().getOrderCharges();
    }

    @CachedMethodResult
    public List<Object> getOrderCompanyCharges() {
        return getService().getOrderCompanyCharges();
    }

    @CachedMethodResult
    public List<Object> getOrderCountPerCustomStatusAndServiceType(ServiceType serviceType, ServiceTypeCollection serviceTypeCollection) {
        return getService().getOrderCountPerCustomStatusAndServiceType(serviceType, serviceTypeCollection);
    }

    @CachedMethodResult
    public List<Object> getOrderCountPerStatusAndServiceType(ServiceType serviceType, ServiceTypeCollection serviceTypeCollection) {
        return getService().getOrderCountPerStatusAndServiceType(serviceType, serviceTypeCollection);
    }

    @CachedMethodResult
    public List<Object> getOrderDurationsAverageTasks() {
        return getService().getOrderDurationsAverageTasks();
    }

    @CachedMethodResult
    public List<Object> getOrderDurationsData() {
        return getService().getOrderDurationsData();
    }

    @CachedMethodResult
    public List<Object> getOrderDurationsTasks() {
        return getService().getOrderDurationsTasks();
    }

    @CachedMethodResult
    public List<Object> getOrderOrganizationCharges() {
        return getService().getOrderOrganizationCharges();
    }

    @CachedMethodResult
    public List<Object> getOrderPerCompany() {
        return getService().getOrderPerCompany();
    }

    @CachedMethodResult
    public List<Object> getOrderPerOrgType() {
        return getService().getOrderPerOrgType();
    }

    @CachedMethodResult
    public List<Object> getOrderPerOrganization() {
        return getService().getOrderPerOrganization();
    }

    @CachedMethodResult
    public List<Object> getOrderPerOrganizationZH() {
        return getService().getOrderPerOrganizationZH();
    }

    @CachedMethodResult
    public List<Object> getOrderPerRequester() {
        return getService().getOrderPerRequester();
    }

    @CachedMethodResult
    public List<Object> getOrderPerTechnology() {
        return getService().getOrderPerTechnology();
    }

    @CachedMethodResult
    public List<Object> getOrderPerYear() {
        return getService().getOrderPerYear();
    }

    @CachedMethodResult
    public List<Object> getOrderStateDurations() {
        return getService().getOrderStateDurations();
    }

    @CachedMethodResult
    public List<Object> getOrderTechnology() {
        return getService().getOrderTechnology();
    }

    @CachedMethodResult
    public List<Object> getOrdersByCompany() {
        return getService().getOrdersByCompany();
    }

    @CachedMethodResult
    public List<Object> getOrdersByOrganization() {
        return getService().getOrdersByOrganization();
    }

    @CachedMethodResult
    public List<Object> getOrdersByRequester() {
        return getService().getOrdersByRequester();
    }

    @CachedMethodResult
    public List<Object> getOrdersCreatedUntil() {
        return getService().getOrdersCreatedUntil();
    }

    @CachedMethodResult
    public List<Object> getOrganizationTops() {
        return getService().getOrganizationTops();
    }

    @CachedMethodResult
    public List<Object> getOrganizationsETHOrdersYearly() {
        return getService().getOrganizationsETHOrdersYearly();
    }

    @CachedMethodResult
    public List<Object> getOrganizationsETHProjectsYearly() {
        return getService().getOrganizationsETHProjectsYearly();
    }

    @CachedMethodResult
    public List<Object> getOrganizationsETHUZHOrdersYearly() {
        return getService().getOrganizationsETHUZHOrdersYearly();
    }

    @CachedMethodResult
    public List<Object> getOrganizationsETHUZHProjectsYearly() {
        return getService().getOrganizationsETHUZHProjectsYearly();
    }

    @CachedMethodResult
    public List<Object> getOrganizationsOtherOrgOrdersYearly() {
        return getService().getOrganizationsOtherOrgOrdersYearly();
    }

    @CachedMethodResult
    public List<Object> getOrganizationsUZHOrdersYearly() {
        return getService().getOrganizationsUZHOrdersYearly();
    }

    @CachedMethodResult
    public List<Object> getOrganizationsUZHProjectsYearly() {
        return getService().getOrganizationsUZHProjectsYearly();
    }

    @CachedMethodResult
    public List<Object> getProjectCharges() {
        return getService().getProjectCharges();
    }

    @CachedMethodResult
    public List<Object> getProjectCountPerStatusAndServiceType(ServiceType serviceType) {
        return getService().getProjectCountPerStatusAndServiceType(serviceType);
    }

    @CachedMethodResult
    public List<Object> getProjectDurations() {
        return getService().getProjectDurations();
    }

    @CachedMethodResult
    public List<Object> getProjectDurationsAverageTasks() {
        return getService().getProjectDurationsAverageTasks();
    }

    @CachedMethodResult
    public List<Object> getProjectPerCompany() {
        return getService().getProjectPerCompany();
    }

    @CachedMethodResult
    public List<Object> getProjectPerOrgCom() {
        return getService().getProjectPerOrgCom();
    }

    @CachedMethodResult
    public List<Object> getProjectPerOrganization() {
        return getService().getProjectPerOrganization();
    }

    @CachedMethodResult
    public List<Object> getProjectPerOrganizationZH() {
        return getService().getProjectPerOrganizationZH();
    }

    @CachedMethodResult
    public List<Object> getProjectPerTechnology() {
        return getService().getProjectPerTechnology();
    }

    @CachedMethodResult
    public List<Object> getProjectPerYear() {
        return getService().getProjectPerYear();
    }

    @CachedMethodResult
    public List<Object> getProjectTechnology() {
        return getService().getProjectTechnology();
    }

    @CachedMethodResult
    public List<Object> getProjectsCreatedUntil() {
        return getService().getProjectsCreatedUntil();
    }

    @CachedMethodResult
    public List<Object> getPurchaseCost() {
        return getService().getPurchaseCost();
    }

    @CachedMethodResult
    public List<Object> getPurchasesCostsYearlyCHF() {
        return getService().getPurchasesCostsYearlyCHF();
    }

    @CachedMethodResult
    public List<Object> getPurchasesCostsYearlyEUR() {
        return getService().getPurchasesCostsYearlyEUR();
    }

    @CachedMethodResult
    public List<Object> getPurchasesCostsYearlyGBP() {
        return getService().getPurchasesCostsYearlyGBP();
    }

    @CachedMethodResult
    public List<Object> getPurchasesCostsYearlyUSD() {
        return getService().getPurchasesCostsYearlyUSD();
    }

    @CachedMethodResult
    public List<Object> getPurchasesPerSupplier() {
        return getService().getPurchasesPerSupplier();
    }

    @CachedMethodResult
    public List<Object> getRefineInstrumentReservationDays() {
        return getService().getRefineInstrumentReservationDays();
    }

    @CachedMethodResult
    public List<Object> getRefineInstrumentReservations() {
        return getService().getRefineInstrumentReservations();
    }

    @CachedMethodResult
    public List<Object> getRefineServices() {
        return getService().getRefineServices();
    }

    @CachedMethodResult
    public List<Object> getReservationdaysInstrumentsYearly() {
        return getService().getReservationHoursInstrumentsYearly();
    }

    @CachedMethodResult
    public List<Object> getResourceStatusCounts() {
        return getService().getResourceStatusCounts();
    }

    @CachedMethodResult
    public List<Object> getResourcesCreatedUntil() {
        return getService().getResourcesCreatedUntil();
    }

    @CachedMethodResult
    public List<Object> getSamplesByCompany() {
        return getService().getSamplesByCompany();
    }

    @CachedMethodResult
    public List<Object> getSamplesByOrganization() {
        return getService().getSamplesByOrganization();
    }

    @CachedMethodResult
    public List<Object> getSamplesCreatedUntil() {
        return getService().getSamplesCreatedUntil();
    }

    @CachedMethodResult
    public List<Object> getSamplesOverviewYearly() {
        return getService().getSamplesOverviewYearly();
    }

    protected StatisticsService getService() {
        return statisticsService;
    }

    @CachedMethodResult
    public List<Object> getServiceChargesPerYear() {
        return getService().getServiceChargesPerYear();
    }

    @CachedMethodResult
    public List<Object> getServiceCodeChargesPerYear() {
        return getService().getServiceCodeChargesPerYear();
    }

    @CachedMethodResult
    public List<Object> getServicecodeCharges() {
        return getService().getServicecodeCharges();
    }

    @CachedMethodResult
    public List<Object> getServicesCharges() {
        return getService().getServicesCharges();
    }

    @CachedMethodResult
    public List<Object> getServicesCube() {
        return getService().getServicesCube();
    }

    @CachedMethodResult
    public List<Object> getServicesOverviewYearly() {
        return getService().getServicesOverviewYearly();
    }

    @CachedMethodResult
    public List<Object> getServicesPerTechnology() {
        return getService().getServicesPerTechnology();
    }

    @CachedMethodResult
    public List<Object> getSupplierConsumableYearlyCHF(Supplier supplier) {
        return getService().getSupplierConsumableYearlyCHF(supplier);
    }

    @CachedMethodResult
    public List<Object> getSupplierConsumableYearlyEUR(Supplier supplier) {
        return getService().getSupplierConsumableYearlyEUR(supplier);
    }

    @CachedMethodResult
    public List<Object> getSupplierConsumableYearlyGBP(Supplier supplier) {
        return getService().getSupplierConsumableYearlyGBP(supplier);
    }

    @CachedMethodResult
    public List<Object> getSupplierConsumableYearlyUSD(Supplier supplier) {
        return getService().getSupplierConsumableYearlyUSD(supplier);
    }

    @CachedMethodResult
    public List<Object> getSupplierCost() {
        return getService().getSupplierCost();
    }

    @CachedMethodResult
    public List<Object> getSupplierPurchasesYearlyCHF(Supplier supplier) {
        return getService().getSupplierPurchasesYearlyCHF(supplier);
    }

    @CachedMethodResult
    public List<Object> getSupplierPurchasesYearlyEUR(Supplier supplier) {
        return getService().getSupplierPurchasesYearlyEUR(supplier);
    }

    @CachedMethodResult
    public List<Object> getSupplierPurchasesYearlyGBP(Supplier supplier) {
        return getService().getSupplierPurchasesYearlyGBP(supplier);
    }

    @CachedMethodResult
    public List<Object> getSupplierPurchasesYearlyUSD(Supplier supplier) {
        return getService().getSupplierPurchasesYearlyUSD(supplier);
    }

    @CachedMethodResult
    public List<Object> getSupplierStatisticsConsumables(Supplier supplier) {
        return getService().getSupplierStatisticsConsumables(supplier);
    }

    @CachedMethodResult
    public List<Object> getSupplierStatisticsPerYear(Supplier supplier) {
        return getService().getSupplierStatisticsPerYear(supplier);
    }

    @CachedMethodResult
    public List<Object> getSupplierStatisticsPurchases(Supplier supplier) {
        return getService().getSupplierStatisticsPurchases(supplier);
    }

    @CachedMethodResult
    public List<Object> getTimeframeElementsByClass(Class<?> clazz, String col, String columns, String timeframe, int timeframeMultiplicator) {
        return getService().getTimeframeElementsByClass(clazz, col, columns, timeframe, timeframeMultiplicator);
    }

    @CachedMethodResult
    public List<Object> getTimeframeEntities(String timeframe, int timeframeMultiplicator) {
        return getService().getTimeframeEntities(timeframe, timeframeMultiplicator);
    }

    @CachedMethodResult
    public List<Object> getTopCoachPerYear(Long year) {
        return getService().getTopCoachPerYear(year);
    }

    @CachedMethodResult
    public List<Object> getTopEntityCounts() {
        return getService().getTopEntityCounts();
    }

    @CachedMethodResult
    public List<Object> getTopRequesterPerYear(Long year) {
        return getService().getTopRequesterPerYear(year);
    }

    @CachedMethodResult
    public List<Object> getTopServiceTypePerYear(Long year) {
        return getService().getTopServiceTypePerYear(year);
    }

    @CachedMethodResult
    public List<Object> getTopSupplierPerYear(Long year) {
        return getService().getTopSupplierPerYear(year);
    }

    @CachedMethodResult
    public List<Object> getUsersCreatedUntil() {
        return getService().getUsersCreatedUntil();
    }

    @CachedMethodResult
    public List<Object> getWorkunitStatusCounts() {
        return getService().getWorkunitStatusCounts();
    }

    @CachedMethodResult
    public List<Object> getWorkunitsCreatedUntil() {
        return getService().getWorkunitsCreatedUntil();
    }

    @CachedMethodResult
    public List<Object> getWorkunitsETHZUZHPerYear() {
        return getService().getWorkunitsETHZUZHPerYear();
    }

    @CachedMethodResult
    public List<Integer> getYearCoach() {
        return getService().getYearCoach();
    }

    @CachedMethodResult
    public List<Integer> getYearIncome() {
        return getService().getYearIncome();
    }

    @CachedMethodResult
    public List<Integer> getYearRequester() {
        return getService().getYearRequester();
    }

    @CachedMethodResult
    public List<Integer> getYearSupplier() {
        return getService().getYearSupplier();
    }

    @CachedMethodResult
    public List<Object> getYearlyBooking() {
        return getService().getYearlyBooking();
    }

    @CachedMethodResult
    public List<Object> getYearlyBookingCompany() {
        return getService().getYearlyBookingCompany();
    }

    @CachedMethodResult
    public List<Object> getYearlyBookingContainerCounter() {
        return getService().getYearlyBookingContainerCounter();
    }

    @CachedMethodResult
    public List<Object> getYearlyBookingContainerOrders() {
        return getService().getYearlyBookingContainerOrders();
    }

    @CachedMethodResult
    public List<Object> getYearlyBookingContainerProjects() {
        return getService().getYearlyBookingContainerProjects();
    }

    @CachedMethodResult
    public List<Object> getYearlyBookingOrganization() {
        return getService().getYearlyBookingOrganization();
    }

    @CachedMethodResult
    public List<Object> getYearlyBookingTechGenomics() {
        return getService().getYearlyBookingTechGenomics();
    }

    @CachedMethodResult
    public List<Object> getYearlyBookingTechMetabolomics() {
        return getService().getYearlyBookingTechMetabolomics();
    }

    @CachedMethodResult
    public List<Object> getYearlyBookingTechProteomics() {
        return getService().getYearlyBookingTechProteomics();
    }

    @CachedMethodResult
    public List<Object> getYearlyBookingTechnologyCounter() {
        return getService().getYearlyBookingTechnologyCounter();
    }

    @CachedMethodResult
    public List<Object> getYearlyOrderTechGenomics() {
        return getService().getYearlyOrderTechGenomics();
    }

    @CachedMethodResult
    public List<Object> getYearlyOrderTechMetabolomics() {
        return getService().getYearlyOrderTechMetabolomics();
    }

    @CachedMethodResult
    public List<Object> getYearlyOrderTechProteomics() {
        return getService().getYearlyOrderTechProteomics();
    }

    @CachedMethodResult
    public List<Object> getYearlyProjectTechGenomics() {
        return getService().getYearlyProjectTechGenomics();
    }

    @CachedMethodResult
    public List<Object> getYearlyProjectTechMetabolomics() {
        return getService().getYearlyProjectTechMetabolomics();
    }

    @CachedMethodResult
    public List<Object> getYearlyProjectTechProteomics() {
        return getService().getYearlyProjectTechProteomics();
    }

    @CachedMethodResult
    public List<Object> view(String view) {
        return getService().view(view);
    }
}
