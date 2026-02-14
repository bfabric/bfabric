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

package org.bfabric.service;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.inject.Named;
import javax.persistence.Query;

import org.bfabric.entity.AccessRequest;
import org.bfabric.entity.AccessRequestProfile;
import org.bfabric.entity.Annotation;
import org.bfabric.entity.Application;
import org.bfabric.entity.Booking;
import org.bfabric.entity.Charge;
import org.bfabric.entity.Comment;
import org.bfabric.entity.Company;
import org.bfabric.entity.Contract;
import org.bfabric.entity.Credit;
import org.bfabric.entity.Dataset;
import org.bfabric.entity.Department;
import org.bfabric.entity.Event;
import org.bfabric.entity.EventType;
import org.bfabric.entity.Executable;
import org.bfabric.entity.Institute;
import org.bfabric.entity.Instrument;
import org.bfabric.entity.InstrumentReservation;
import org.bfabric.entity.Link;
import org.bfabric.entity.Offer;
import org.bfabric.entity.Order;
import org.bfabric.entity.Organization;
import org.bfabric.entity.Plate;
import org.bfabric.entity.Project;
import org.bfabric.entity.Purchase;
import org.bfabric.entity.Resource;
import org.bfabric.entity.Run;
import org.bfabric.entity.Sample;
import org.bfabric.entity.Service;
import org.bfabric.entity.ServiceArea;
import org.bfabric.entity.ServiceType;
import org.bfabric.entity.ServiceTypeCollection;
import org.bfabric.entity.Storage;
import org.bfabric.entity.Submitter;
import org.bfabric.entity.Supplier;
import org.bfabric.entity.User;
import org.bfabric.entity.UserGroup;
import org.bfabric.entity.Workflow;
import org.bfabric.entity.Workunit;
import org.bfabric.entity.WrapperCreator;
import org.bfabric.enums.StatusEnum;
import org.bfabric.interceptors.CachedMethodResult;
import org.bfabric.util.ClassHelper;
import org.bfabric.util.CollectionHelper;
import org.bfabric.util.StringHelper;

@Named
@Stateless
public class StatisticsService extends AbstractService {

    private static final long serialVersionUID = 1;

    private static final Logger logger = Logger.getLogger(StatisticsService.class.getName());

    private static final String orderdurationtasksQuery = "FROM orderdurationtasks where year is not NULL AND year <> EXTRACT(YEAR FROM CURRENT_DATE)";

    private static final String orderdurationaverageperorderQuery = "FROM orderdurationaverageperorder where year is not NULL AND year <> EXTRACT(YEAR FROM CURRENT_DATE) ";

    // Important: use minus 4 years due to the fact that projects are typically completed within 4 years. Thus, it statistically makes not much sense to include projects that are younger than 4 years.
    private static final String projectDurationWhereClause = "WHERE year is not NULL AND year < cast (EXTRACT(YEAR FROM CURRENT_DATE)as integer) - 4 ";

    private static final String projectdurationtasksQuery = "FROM projectdurationtasks " + projectDurationWhereClause;

    private static final String projectdurationaverageperprojectQuery = "FROM projectdurationaverageperproject " + projectDurationWhereClause;

    private static final String orderPhaseColumns = "submittedtoaccepted, submittedtoarrived, acceptedtoarrived, arrivedtoprocessed, processedtoanalyzed, analyzedtofinished, finishedtoclosed, submittedtoclosed, submittedtoprocessing, submittedtoprocessed, submittedtoanalyzing, submittedtoanalyzed, submittedtofinished, arrivedtoprocessing, processingtoprocessed, processedtoanalyzing, analyzingtoanalyzed";

    public List<Object> getUsersPerYear() {
        return createNativeQuery("select year, total from users_per_year").getResultList();
    }

    private List<Object> createTechnologyQuery(String technology, String baseQuery) {
        StringBuilder queryBuilder = new StringBuilder(baseQuery);
        if (StringHelper.isNotEmpty(technology)) {
            queryBuilder.append(" WHERE technology = :technology");
        }
        queryBuilder.append(" GROUP BY status, sequential ORDER BY sequential");
        Query query = createNativeQuery(queryBuilder.toString());
        if (StringHelper.isNotEmpty(technology)) {
            query.setParameter("technology", technology);
        }
        return query.getResultList();
    }

    public void execute(String command) {
        createNativeQuery(command).executeUpdate();
    }

    public List<Object> getAccessRequestProfile(AccessRequestProfile accessRequestProfile) {
        return createNativeQuery("SELECT year, accessrequest FROM statisticsaccessrequestprofile WHERE accessRequestProfileid = :accessRequestProfileId")
            .setParameter("accessRequestProfileId", accessRequestProfile.getId()).getResultList();
    }

    public List<Object> getAccessRequestProfilePerYear(AccessRequestProfile accessRequestProfile) {
        return createNativeQuery("SELECT year, accessrequest, personalcard, guestcard FROM statisticsaccessrequestprofile WHERE accessRequestProfileid = :accessRequestProfileId")
            .setParameter("accessRequestProfileId", accessRequestProfile.getId()).getResultList();
    }

    public List<Object> getApplicationDataETHZUZHPerYear() {
        return getResult("SELECT year, workunit_count, application_id, application_name, application_type, technology, organization_name from applicationdataperyearethzuzh");
    }

    public List<Object> getApplicationDataPerYear() {
        return getResult("SELECT year, workunit_count, application_id, application_name, application_type, technology, organization_id, organization_name, company_id, company_name from applicationdataperyear");
    }

    public List<Object> getApplicationResourcesChart(Application application) {
        return createNativeQuery("SELECT year, resources FROM statistics_applicationid_year WHERE applicationid = :applicationId and year is not null").setParameter("applicationId", application.getId())
            .getResultList();
    }

    public List<Object> getApplicationRunsChart(Application application) {
        return createNativeQuery("SELECT year, coalesce(runs, 0) FROM statistics_applicationid_year WHERE applicationid = :applicationId and year is not null").setParameter("applicationId", application.getId())
            .getResultList();
    }

    public List<Object> getApplicationStatisticsPerYear() {
        return createNativeQuery("SELECT year, workunits, resources, size, applicationid FROM statistics_applicationid_year").getResultList();
    }

    public List<Object> getApplicationWorkunitsChart(Application application) {
        return createNativeQuery("SELECT year, workunits FROM statistics_applicationid_year WHERE applicationid = :applicationId and year is not null").setParameter("applicationId", application.getId())
            .getResultList();
    }

    public List<Object> getApplicationWorkunitsPerYear(Application application) {
        return createNativeQuery("SELECT year, workunits, resources, size, runs FROM statistics_applicationid_year WHERE applicationid = :applicationId").setParameter("applicationId", application.getId())
            .getResultList();
    }

    public List<Object> getAvgAnalyzingEfficiencyYearly() {
        return getAvgEfficiencyYearly("avg_analyzing");
    }

    public List<Object> getAvgArrivingEfficiencyYearly() {
        return getAvgEfficiencyYearly("avg_arriving");
    }

    public List<Object> getAvgClosingEfficiencyYearly() {
        return getAvgEfficiencyYearly("avg_closing");
    }

    public List<Object> getAvgEfficiencyYearly(String phase) {
        return getResult("SELECT year, " + phase + " FROM statisticsorderstatedurationefficiencyperyear");
    }

    public List<Object> getAvgProcessingEfficiencyYearly() {
        return getAvgEfficiencyYearly("avg_processing");
    }

    public List<Object> getBookedChargeCompany(long companyId) {
        return createNativeQuery("SELECT year, num_charges, price, discountedprice FROM bookedchargecompany WHERE companyid = :companyId").setParameter("companyId", companyId).getResultList();
    }

    public List<Object> getBookedChargeDepartment(long departmentId) {
        return createNativeQuery("SELECT year, num_charges, price, discountedprice FROM bookedchargedepartment WHERE departmentid = :departmentId").setParameter("departmentId", departmentId)
            .getResultList();
    }

    public List<Object> getBookedChargeDivision(long divisionId) {
        return createNativeQuery("SELECT year, num_charges, price, discountedprice FROM bookedchargedivision WHERE divisionid = :divisionId").setParameter("divisionId", divisionId).getResultList();
    }

    public List<Object> getBookedChargeInstitute(long instituteId) {
        return createNativeQuery("SELECT year, num_charges, price, discountedprice FROM bookedchargeinstitute WHERE instituteid = :instituteId").setParameter("instituteId", instituteId)
            .getResultList();
    }

    public List<Object> getBookedChargeOrganization(long organizationId) {
        return createNativeQuery("SELECT year, num_charges, price, discountedprice FROM bookedchargeorganization WHERE organizationid = :organizationId").setParameter("organizationId", organizationId)
            .getResultList();
    }

    public List<Object> getBookedChargeOrganizationType(long organizationtypeId) {
        return createNativeQuery("SELECT year, num_charges, price, discountedprice FROM bookedchargeorganizationtype WHERE organizationtypeid = :organizationtypeId")
            .setParameter("organizationtypeId", organizationtypeId).getResultList();
    }

    public List<Object> getBookedChargeService(long serviceId) {
        return createNativeQuery("SELECT year, num_charges, price, discountedprice FROM bookedchargeservice WHERE serviceid = :serviceId").setParameter("serviceId", serviceId).getResultList();
    }

    public List<Object> getBookedChargeServiceArea(long serviceareaId) {
        return createNativeQuery("SELECT year, num_charges, price, discountedprice FROM bookedchargeservicearea WHERE serviceareaid = :serviceareaId").setParameter("serviceareaId", serviceareaId)
            .getResultList();
    }

    public List<Object> getBookedChargeServiceAreaChart(long serviceareaId) {
        return createNativeQuery("SELECT year, num_charges, price, discountedprice FROM bookedchargeservicearea WHERE serviceareaid = :serviceareaId and year is not null")
            .setParameter("serviceareaId", serviceareaId).getResultList();
    }

    public List<Object> getBookedChargeServiceChart(long serviceId) {
        return createNativeQuery("SELECT year, num_charges, price, discountedprice FROM bookedchargeservice WHERE serviceid = :serviceId and year is not null").setParameter("serviceId", serviceId)
            .getResultList();
    }

    public List<Object> getBookedChargeServiceHierarchy() {
        return getResult("select year, servicename, servicetypename, serviceareaname, num_charges, price, discountedprice, serviceid, servicetypeid, serviceareaid, month from bookedchargeservicehierarchy");
    }

    public List<Object> getBookedChargeServiceType(long servicetypeId) {
        return createNativeQuery("SELECT year, num_charges, price, discountedprice FROM bookedchargeservicetype WHERE servicetypeid = :servicetypeId").setParameter("servicetypeId", servicetypeId)
            .getResultList();
    }

    public List<Object> getBookedChargeServiceTypeChart(long servicetypeId) {
        return createNativeQuery("SELECT year, num_charges, price, discountedprice FROM bookedchargeservicetype WHERE servicetypeid = :servicetypeId and year is not null")
            .setParameter("servicetypeId", servicetypeId).getResultList();
    }

    public List<Object> getBookingCompanyCostCentre() {
        return getResult("select year, quarter, month, week, companyid, companyname, costcentrecode, num_bookings, total, costcentreid from statisticsbookingcompanycostcentre");
    }

    public List<Object> getBookingOrganizationCostCentre() {
        return getResult("select year, quarter, month, week, organizationid, organizationname, costcentrecode, num_bookings, total, costcentreid from statisticsbookingorganizationcostcentre");
    }

    public List<Object> getBookingPerCompany() {
        return getResult("select year, quarter, month, week, companyid, companyname, divisionid, divisionname, num_bookings, totalbookings, totalcharges from statisticsbookingpercompany");
    }

    public List<Object> getBookingPerCompanyCostCentre() {
        return getResult("select year, quarter, month, week, companyid, companyname, costcentreid, costcentrecode, num_bookings, totalbookings, totalcharges from statisticsbookingpercompanycostcentre");
    }

    public List<Object> getBookingPerContainer() {
        return getResult("select year, quarter, month, week, discriminator, containerid, containername, num_bookings, totalbookings, totalcharges from statisticsbookingpercontainer");
    }

    public List<Object> getBookingPerCostCentre() {
        return getResult("select year, quarter, month, organizationtypeid, organizationtypename, organizationid, organizationname, organizationacademic, costcentreid, costcentrename, num_bookings, totalbookings, totalcharges from statisticsbookingpercostcentre");
    }

    public List<Object> getBookingPerInstrument() {
        return getResult("select year, quarter, month, week, instrumentid, instrumentname, num_bookings, totalbookings, totalcharges from statisticsbookingperinstrument");
    }

    public List<Object> getBookingPerOrganization() {
        return getResult("select year, quarter, month, week, organizationtypeid, organizationtypename, organizationid, organizationname, departmentid, departmentname, instituteid, institutename, num_bookings, totalbookings, totalcharges from statisticsbookingperorganization");
    }

    public List<Object> getBookingPerService() {
        return getResult("select year, quarter, month, week, servicetypeid, servicetypename, num_bookings, totalbookings, totalcharges from statisticsbookingperservice");
    }

    public List<Object> getBookingPerTechnology() {
        return getResult("select year, quarter, month, week, technologyid, technologyname, num_bookings, totalbookings, totalcharges from statisticsbookingpertechnology");
    }

    public List<Object> getBookingPerTechnologyPerYear() {
        return getResult("select year, technologyid, technologyname, num_bookings, totalbookings, totalcharges from statisticsbookingpertechnologyperyear");
    }

    public List<Object> getBookingPerYear() {
        return getResult("select year, quarter, month, week, num_bookings, totalbookings, totalcharges from statisticsbookingperyear");
    }

    public List<Object> getChargesPerInstrument() {
        return getResult("select year, instrumentid, instrumentname, num_charges, price, discountedprice from instrumentchargesperyear");
    }

    public List<Object> getCoachedProjectsPerYear() {
        return getResult("SELECT year, coachid, coach, coachedprojectsstartedingivenyear, totalcoachedactiveprojectsbyyear, totalcoachedprojectsbyyear FROM coachedprojectsperyear");
    }

    public List<Object> getCompaniesOrdersYearly() {
        return getResult("SELECT year, total FROM statisticsordersoverviewcompaniesperyear");
    }

    public List<Object> getConsumablesPerSupplier() {
        return getResult("SELECT year, month, supplierid, suppliername, instrumentid, instrumentname, consumables, price, currency FROM statisticsconsumablepersupplier");
    }

    public List<Object> getContainerUsage() {
        List<Object> ret = new ArrayList<>();
        try {
            ret = getResult("SELECT id, " + "CASE WHEN samples is NULL THEN 0 ELSE samples END, " + "CASE WHEN workunits is NULL THEN 0 ELSE workunits END, "
                + "CASE WHEN resources is NULL THEN 0 ELSE resources END, " + "CASE WHEN datasets is NULL THEN 0 ELSE datasets END, " + "CASE WHEN charges is NULL THEN 0 ELSE charges END, "
                + "CASE WHEN bookings is NULL THEN 0 ELSE bookings END, " + "CASE WHEN members is NULL THEN 0 ELSE members END, " + "CASE WHEN comments is NULL THEN 0 ELSE comments END, "
                + "CASE WHEN instrumentreservations is NULL THEN 0 ELSE instrumentreservations END, " + "CASE WHEN importresources is NULL THEN 0 ELSE importresources END, "
                + "CASE WHEN orders is NULL THEN 0 ELSE orders END, discriminator FROM containerusage");
        } catch (Exception e) {
            logger.warning("Refresh materialized view required!");
        }
        return ret;
    }

    public List<Object> getContractCost() {
        return getResult("SELECT year, month, contracttypeid, contracttypename, contracts, contractprice, currency FROM statisticscostscontractperyear");
    }

    public List<Object> getCubePerYear(String className) {
        String tableName = ClassHelper.getTableName(className);
        if (StringHelper.isNotEmpty(tableName)) {
            List<Object> ret = getResult("SELECT * FROM cubePerYear('" + tableName + "')");
            if (ret.size() > 1) { // size <= 1 means that there is only total number
                return ret;
            }
        }
        return null;
    }

    public List<Object> getCubePerYearChart(String className) {
        String tableName = ClassHelper.getTableName(className);
        if (StringHelper.isNotEmpty(tableName)) {
            return getResult("SELECT * FROM cubePerYear('" + tableName + "') WHERE year IS NOT NULL");
        }
        return null;
    }

    public Long getEntityCountByClass(String clazz) {
        return (Long) createQuery("select count(*) as total from " + clazz).getSingleResult();
    }

    public List<Object> getEntityCounts() {
        final ArrayList<Object> results = new ArrayList<>();
        for (final String className : ClassHelper.getBaseEntityClassNames()) {
            final ArrayList<Object> r = new ArrayList<>();
            r.add(className);
            r.add(getEntityCountByClass(className));
            r.add(getEntitySinceByClass(className));
            results.add(r);
        }
        return results;
    }

    public String getEntitySinceByClass(String clazz) {
        return (String) createQuery("select cast(cast(min(created) as date) as string) as since from " + clazz).getSingleResult();
    }

    public List<Object> getIncomeContainer() {
        return getResult("SELECT totalbookings, discriminator FROM incomepercontainergraph");
    }

    public List<Object> getIncomeOrgCom() {
        return getResult("SELECT totalbookings, name FROM incomeperorgcomgraph");
    }

    public List<Object> getIncomeOrgEthzUniz() {
        return getResult("SELECT totalbookings, organizationname FROM incomeperorgethzunizgraph");
    }

    public List<Object> getIncomeOrgType() {
        return getResult("SELECT totalbookings, organizationtypename FROM incomeperorgtypegraph");
    }

    public List<Object> getIncomeTechnology() {
        return getResult("SELECT totalbookings, technologyname FROM incomepertechnologygraph order by technologyname");
    }

    public List<Object> getIncomeYearlyCounter() {
        return getResult("SELECT year, num_bookings FROM statisticsbookingoverview");
    }

    public List<Object> getInstrumentAllStatisticsDaysPerYear() {
        return getResult("SELECT year, instrumentid, instrumentname, instrumenttechnology, reservationhours/24 as reservationhours, maintenancehours/24 as maintenancehours, uptimehours/24 as uptimehours, bookabletimehours/24 as bookabletimehours, discountedprice, workunits, resources, size, nonbookablehours FROM instrumentstatisticsperyearcube");
    }

    public List<Object> getInstrumentBookableDaysPerYear(Instrument instrument) {
        return createNativeQuery("SELECT year, bookabletimehours/24 FROM instrumentstatisticsperyear WHERE instrumentid = :instrumentId").setParameter("instrumentId", instrument.getId())
            .getResultList();
    }

    public List<Object> getInstrumentMaintenanceDaysPerYear(Instrument instrument) {
        return createNativeQuery("SELECT year, maintenancehours/24 FROM instrumentstatisticsperyear WHERE instrumentid = :instrumentId").setParameter("instrumentId", instrument.getId())
            .getResultList();
    }

    public List<Object> getInstrumentNonBookableHoursPerYear(Instrument instrument) {
        return createNativeQuery("SELECT year, nonbookabledays FROM instrumentstatisticsperyear WHERE instrumentid = :instrumentId").setParameter("instrumentId", instrument.getId()).getResultList();
    }

    public List<Object> getInstrumentReservationChargedSumPerYear(Instrument instrument) {
        return createNativeQuery("SELECT year, discountedprice FROM instrumentstatisticsperyear WHERE instrumentid = :instrumentId").setParameter("instrumentId", instrument.getId()).getResultList();
    }

    public List<Object> getInstrumentReservationDaysPerYear(Instrument instrument) {
        return createNativeQuery("SELECT year, reservationhours/24 FROM instrumentstatisticsperyear WHERE instrumentid = :instrumentId").setParameter("instrumentId", instrument.getId())
            .getResultList();
    }

    public List<Object> getInstrumentReservationDaysPerYearTechnologyBudgetOfficer() {
        return getResult("SELECT year, budgetOfficer, budgetOfficername, containerid, instrumenttechnology, instrumentid, instrumentname, hours/24 FROM instrumentreservationhoursperyeartechnologybudgetofficer");
    }

    public List<Object> getInstrumentResourcesPerYear(Instrument instrument) {
        return createNativeQuery("SELECT year, resources FROM instrumentstatisticsperyear WHERE instrumentid = :instrumentId").setParameter("instrumentId", instrument.getId()).getResultList();
    }

    public List<Object> getInstrumentResourcesSizePerYear(Instrument instrument) {
        return createNativeQuery("SELECT year, size FROM instrumentstatisticsperyear WHERE instrumentid = :instrumentId").setParameter("instrumentId", instrument.getId()).getResultList();
    }

    public List<Object> getInstrumentRunsPerYear(Instrument instrument) {
        return createNativeQuery("SELECT year, runs FROM instrumentstatisticsperyear WHERE instrumentid = :instrumentId").setParameter("instrumentId", instrument.getId()).getResultList();
    }

    public List<Object> getInstrumentUpDaysPerYear(Instrument instrument) {
        return createNativeQuery("SELECT year, uptimehours/24 FROM instrumentstatisticsperyear WHERE instrumentid = :instrumentId").setParameter("instrumentId", instrument.getId()).getResultList();
    }

    public List<Object> getInstrumentWorkunitsPerYear(Instrument instrument) {
        return createNativeQuery("SELECT year, workunits FROM instrumentstatisticsperyear WHERE instrumentid = :instrumentId").setParameter("instrumentId", instrument.getId()).getResultList();
    }

    public List<Object> getInstrumentsCostsYearlyCHF() {
        return getResult("SELECT year, totalinstrumentchf FROM statisticscostsoverviewinstrumentperyear");
    }

    public List<Object> getInstrumentsCostsYearlyEUR() {
        return getResult("SELECT year, totalinstrumenteur FROM statisticscostsoverviewinstrumentperyear");
    }

    public List<Object> getInstrumentsCostsYearlyGBP() {
        return getResult("SELECT year, totalinstrumentgbp FROM statisticscostsoverviewinstrumentperyear");
    }

    public List<Object> getInstrumentsCostsYearlyUSD() {
        return getResult("SELECT year, totalinstrumentusd FROM statisticscostsoverviewinstrumentperyear");
    }

    public List<Object> getInstrumentsPricePerYear() {
        return getResult("SELECT year, id, name, label, purchaseddate, purchasedprice, currency as integer FROM statisticscostsinstrumentsperyear");
    }

    public List<Object> getInstrumentsStatisticsDaysPerYear(Instrument instrument) {
        return createNativeQuery("SELECT year, reservationhours/24 as reservationhours, maintenancehours/24 as maintenancehours, uptimehours/24 as uptimehours, bookabletimehours/24 as bookabletimehours, discountedprice, workunits, resources, size, runs FROM instrumentstatisticsperyear WHERE instrumentid = :instrumentId")
            .setParameter("instrumentId", instrument.getId()).getResultList();
    }

    public List<Object> getLastApprovedProjects(String timeframe, int timeframeMultiplicator) {
        List<String> status = new ArrayList<>();
        status.add(StatusEnum.PENDING.name());
        status.add(StatusEnum.REVIEW.name());
        status.add(StatusEnum.REJECTED.name());
        return createNativeQuery("select distinct project.id, project.name, project.status, to_char(max(review.created), 'YYYY-MM-DD HH24:MI') as approvaldate  from container project left join review review on (review.projectid=project.id)  where status not in (:status)  and age(review.created) < '"
            + timeframeMultiplicator + " " + timeframe + "' and review.approved=true group by project.id, project.name, project.status order by approvaldate desc").setParameter("status", status)
            .getResultList();
    }

    public List<Object> getLastCreatedUsers(String timeframe, int timeframeMultiplicator) {
        return getTimeframeElementsByClass(User.class, "created", "id, login, created, title, firstname, lastname", timeframe, timeframeMultiplicator);
    }

    public List<Object> getMaintenanceInstrumentsYearly() {
        return getResult("SELECT year, totalmaintenance FROM instrumentstatisticsutilizationperyear");
    }

    public List<Object> getNonBookedBillableCharges() {
        return getResult("select year, quarter, month, organizationtypeid, organizationtypename, organizationid, organizationname, organizationacademic, technologies, num_charges, totalcharges from statisticsnonbookedbillablecharges");
    }

    public List<Object> getNumberOfOrdersPerYearByTechnology(String technology) {
        StringBuilder queryBuilder = new StringBuilder();
        if (StringHelper.isEmpty(technology)) {
            queryBuilder.append(" SELECT year, cast (SUM(counter) as integer) ");
            queryBuilder.append(orderdurationtasksQuery);
            queryBuilder.append(" GROUP BY year ORDER BY year ");
        } else {
            queryBuilder.append(" SELECT year, cast(counter as integer) ");
            queryBuilder.append(orderdurationtasksQuery);
            queryBuilder.append(" AND technology = :technology ");
        }
        Query query = createNativeQuery(queryBuilder.toString());
        if (StringHelper.isNotEmpty(technology)) {
            query.setParameter("technology", technology);
        }
        return query.getResultList();
    }

    public List<Object> getNumberOfProjectsPerYearByTechnology(String technology) {
        StringBuilder queryBuilder = new StringBuilder();
        if (StringHelper.isEmpty(technology)) {
            queryBuilder.append(" SELECT year, cast (SUM(counter) as integer) ");
            queryBuilder.append(projectdurationtasksQuery);
            queryBuilder.append(" GROUP BY year ORDER BY year ");
        } else {
            queryBuilder.append(" SELECT year, cast(counter as integer) ");
            queryBuilder.append(projectdurationtasksQuery);
            queryBuilder.append(" AND technology = :technology ");
        }
        Query query = createNativeQuery(queryBuilder.toString());
        if (StringHelper.isNotEmpty(technology)) {
            query.setParameter("technology", technology);
        }
        return query.getResultList();
    }

    public List<Object> getOrderCharges() {
        return getResult("select year, month, all_charges, all_price, all_discountedprice, booked_charges, booked_price, booked_discountedprice, billable_charges, billable_price, billable_discountedprice from statisticsordercharges");
    }

    public List<Object> getOrderCompanyCharges() {
        return getResult("select year, month, companyname, all_charges, all_price, all_discountedprice, booked_charges, booked_price, booked_discountedprice, billable_charges, billable_price, billable_discountedprice, servicename, servicetypename, serviceareaname, costcentre, companyid, serviceid, servicetypeid, serviceareaid from statisticsordercompanycharges");
    }

    public List<Object> getOrderCountPerCustomStatusAndServiceType(ServiceType serviceType, ServiceTypeCollection serviceTypeCollection) {
        String query = "select customstatus, total, orderitems, usersamples, samples, servicetypeid from OrderCountPerCustomStatusAndServiceType where serviceTypeId ";
        if (serviceType != null) {
            query += " = " + serviceType.getId();
        } else if (serviceTypeCollection == null) {
            query += " IS NULL";
        } else {
            query = "select customstatus, sum(total) as total, sum(orderitems) as orderitems, sum(usersamples) as usersamples, sum(samples) as samples from OrderCountPerCustomStatusAndServiceType where serviceTypeId ";
            query += " in ( " + CollectionHelper.printIds(serviceTypeCollection.getServiceTypes()) + ")";
            query += " group by customstatus order by customstatus";
        }
        return getResult(query);
    }

    public List<Object> getOrderCountPerStatusAndServiceType(ServiceType serviceType, ServiceTypeCollection serviceTypeCollection) {
        String query = "select status, total, orderitems, usersamples, samples, servicetypeid from OrderCountPerStatusAndServiceType where serviceTypeId ";
        if (serviceType != null) {
            query += " = " + serviceType.getId();
        } else if (serviceTypeCollection == null) {
            query += " IS NULL";
        } else {
            query = "select status, sum(total) as total, sum(orderitems) as orderitems, sum(usersamples) as usersamples, sum(samples) as samples from OrderCountPerStatusAndServiceType where serviceTypeId ";
            query += " in ( " + CollectionHelper.printIds(serviceTypeCollection.getServiceTypes()) + ")";
            query += " group by status order by status";
        }
        return getResult(query);
    }

    public List<Object> getOrderData(String orderPhase, String technology) {
        return getStateDurationsByTechnology(orderPhase, technology, orderdurationaverageperorderQuery);
    }

    public List<Object> getOrderDurationPieChartByTechnology(String technology) {
        return createTechnologyQuery(technology, "SELECT SUM(total_days), status FROM orderdurationpiechart");
    }

    public List<Object> getOrderDurationsAverageTasks() {
        return getResult("SELECT year, month, counter, " + orderPhaseColumns + ", technology, samplepreparationprotocolid, samplepreparationprotocolname, servicetypeid, servicetypename, sequencingapplicationid, sequencingapplicationname, instrumentid, instrumentname FROM orderdurationaverageperorder");
    }

    public List<Object> getOrderDurationsData() {
        return getResult("SELECT year, month, id, " + orderPhaseColumns + ", technology, samplepreparationprotocolid, samplepreparationprotocolname, servicetypeid, servicetypename, sequencingapplicationid, sequencingapplicationname, instrumentid, instrumentname FROM orderduration");
    }

    public List<Object> getOrderDurationsTasks() {
        return getResult("SELECT year, month, counter, " + orderPhaseColumns + ", technology, samplepreparationprotocolid, samplepreparationprotocolname, servicetypeid, servicetypename, sequencingapplicationid, sequencingapplicationname, instrumentid, instrumentname FROM orderdurationtasks");
    }

    public List<Object> getOrderOrganizationCharges() {
        return getResult("select year, month, organizationname, all_charges, all_price, all_discountedprice, booked_charges, booked_price, booked_discountedprice, billable_charges, billable_price, billable_discountedprice, servicename, servicetypename, serviceareaname, costcentre, organizationid, serviceid, servicetypeid, serviceareaid from statisticsorderorganizationcharges");
    }

    public List<Object> getOrderPerCompany() {
        return getResult("select year, companyid, companyname, divisionid, divisionname, total, accepted, arrived, processing, processed, analyzing, analyzed, finished, closed, canceled, pending, submitted from statisticsorderperCompany");
    }

    public List<Object> getOrderPerOrgType() {
        return getResult("SELECT total, organizationtypename FROM orderperorgtype");
    }

    public List<Object> getOrderPerOrganization() {
        return getResult("select year, organizationtypeid, organizationtypename, organizationid, organizationname, departmentid, departmentname, instituteid, institutename, total, accepted, arrived, processing, processed, analyzing, analyzed, finished, closed, canceled, pending, submitted from statisticsorderperorganization")
            ;
    }

    public List<Object> getOrderPerOrganizationZH() {
        return getResult("SELECT total, name FROM orderperorganizationzh");
    }

    public List<Object> getOrderPerRequester() {
        return getResult("select year, requesterid, firstname, lastname, total, accepted, arrived, processing, processed, analyzing, analyzed, finished, closed, canceled, pending, submitted from statisticsorderperRequester");
    }

    public List<Object> getOrderPerTechnology() {
        return getResult("select year, technologyid, technologyname, total, accepted, arrived, processing, processed, analyzing, analyzed, finished, closed, canceled, pending, submitted from statisticsorderpertechnology");
    }

    public List<Object> getOrderPerYear() {
        return getResult("select year, total, accepted, arrived, processing, processed, analyzing, analyzed, finished, closed, canceled, pending, submitted from statisticsorderperyear");
    }

    public List<Object> getOrderStateDurations() {
        return getResult("SELECT year, counter, avg_accepting, max_accepting, avg_arriving, max_arriving, avg_processing, max_processing,avg_analyzing, max_analyzing, avg_finalizing, max_finalizing, avg_closing, max_closing, avg_all, max_all FROM orderstateduration ");
    }

    public List<Object> getOrderTechnology() {
        return getResult("SELECT total, technologyname FROM orderpertechnology");
    }

    public List<Object> getOrdersByCompany() {
        return getResult("SELECT year, company, orders FROM ordercompanies");
    }

    public List<Object> getOrdersByOrganization() {
        return getResult("SELECT year, organization, department, institute, orders FROM orderorganization");
    }

    public List<Object> getOrdersByRequester() {
        return getResult("SELECT year, requesterid, firstname, lastname, orders FROM orderrequesters");
    }

    public List<Object> getOrdersCreatedUntil() {
        return getResult("SELECT year, total FROM statisticsorderscreateduntil WHERE year <> extract(year from current_date)");
    }

    public List<Object> getOrganizationTops() {
        return getResult("SELECT total, name FROM statisticscontainerorganization");
    }

    public List<Object> getOrganizationsETHOrdersYearly() {
        return getResult("SELECT year, totaleth FROM statisticsordersoverviewperyear");
    }

    public List<Object> getOrganizationsETHProjectsYearly() {
        return getResult("SELECT year, totaleth FROM statisticsprojectsoverviewperyear");
    }

    public List<Object> getOrganizationsETHUZHOrdersYearly() {
        return getResult("SELECT year, totalethzuzh FROM statisticsordersoverviewperyear");
    }

    public List<Object> getOrganizationsETHUZHProjectsYearly() {
        return getResult("SELECT year, totalethzuzh FROM statisticsprojectsoverviewperyear");
    }

    public List<Object> getOrganizationsOtherOrgOrdersYearly() {
        return getResult("SELECT year, totalother FROM statisticsordersoverviewperyear");
    }

    public List<Object> getOrganizationsUZHOrdersYearly() {
        return getResult("SELECT year, totaluzh FROM statisticsordersoverviewperyear");
    }

    public List<Object> getOrganizationsUZHProjectsYearly() {
        return getResult("SELECT year, totaluzh FROM statisticsprojectsoverviewperyear");
    }

    public List<Object> getProjectCharges() {
        return getResult("select year, month, all_charges, all_price, all_discountedprice, booked_charges, booked_price, booked_discountedprice, billable_charges, billable_price, billable_discountedprice from statisticsprojectcharges");
    }

    public List<Object> getProjectCountPerStatusAndServiceType(ServiceType serviceType) {
        String query = "select status, count from ProjectCountPerStatusAndServiceType where serviceTypeId ";
        if (serviceType != null) {
            query += " = " + serviceType.getId();
        } else {
            query += " IS NULL";
        }
        return getResult(query);
    }

    @CachedMethodResult
    public List<Object> getProjectData(String orderStatus, String technology) {
        return getStateDurationsByTechnology(orderStatus, technology, projectdurationaverageperprojectQuery);
    }

    public List<Object> getProjectDurationPieChartByTechnology() {
        return getResult("SELECT total_days, status FROM projectdurationpiechart");
    }

    @CachedMethodResult
    public List<Object> getProjectDurationPieChartByTechnology(String technology) {
        return createTechnologyQuery(technology, "SELECT SUM(total_days), status FROM projectdurationpiechart");
    }

    public List<Object> getProjectDurations() {
        return getResult("SELECT cast(year as integer) as year, id, cast (pendingtoreview as integer), cast (reviewtorunning as integer), cast (pendingtorunning as integer), cast (runningtofinished as integer), cast (pendingtofinished as integer), cast (finishedtoclosed as integer), cast (pendingtoclosed as integer), cast (pendingtorejected as integer), cast(pendingsince as text), technology FROM projectduration");
    }

    public List<Object> getProjectDurationsAverageTasks() {
        return getResult("SELECT year, counter, pendingtoreview, reviewtorunning, pendingtorunning, runningtofinished, pendingtofinished, finishedtoclosed, pendingtoclosed, pendingtorejected, technology FROM projectdurationaverageperproject");
    }

    public List<Object> getProjectPerCompany() {
        return getResult("select year, companyid, companyname, divisionid, divisionname, total, running, finished, private, published, rejected, pending from statisticsprojectpercompany");
    }

    public List<Object> getProjectPerOrgCom() {
        return getResult("SELECT total, name FROM projectperorgcomgraph");
    }

    public List<Object> getProjectPerOrganization() {
        return getResult("select year, organizationtypeid, organizationtypename, organizationid, organizationname, departmentid, departmentname, instituteid, institutename, total, running, finished, private, published, rejected, pending from statisticsprojectperorganization");
    }

    public List<Object> getProjectPerOrganizationZH() {
        return getResult("SELECT total, name FROM projectperorganizationzh");
    }

    public List<Object> getProjectPerTechnology() {
        return getResult("select year, technologyid, technologyname, total, running, finished, private, published, rejected, pending from statisticsprojectpertechnology");
    }

    public List<Object> getProjectPerYear() {
        return getResult("select year, total, running, finished, private, published, rejected, pending from statisticsprojectperyear");
    }

    public List<Object> getProjectTechnology() {
        return getResult("SELECT totalprojects, technologyname FROM projectpertechnologygraph");
    }

    public List<Object> getProjectsCreatedUntil() {
        return getResult("SELECT year, total FROM statisticsprojectscreateduntil WHERE year <> extract(year from current_date)");
    }

    public List<Object> getPurchaseCost() {
        return getResult("SELECT year, month, purchases, orderprice, ordercurrency, invoicedprice, invoicedcurrency  FROM statisticscostspurchaseperyear");
    }

    public List<Object> getPurchasesCostsYearlyCHF() {
        return getResult("SELECT year, totalpurchasechf FROM statisticscostsoverviewpurchaseperyear");
    }

    public List<Object> getPurchasesCostsYearlyEUR() {
        return getResult("SELECT year, totalpurchaseeur FROM statisticscostsoverviewpurchaseperyear");
    }

    public List<Object> getPurchasesCostsYearlyGBP() {
        return getResult("SELECT year, totalpurchasegbp FROM statisticscostsoverviewpurchaseperyear");
    }

    public List<Object> getPurchasesCostsYearlyUSD() {
        return getResult("SELECT year, totalpurchaseusd FROM statisticscostsoverviewpurchaseperyear");
    }

    public List<Object> getPurchasesPerSupplier() {
        return getResult("SELECT year, month, supplierid, suppliername, technologies, purchases, orderprice, ordercurrency, invoicedprice, invoicedcurrency FROM statisticspurchasepersupplier");
    }

    public List<Object> getRefineInstrumentReservationDays() {
        return getResult("select year, instrumentid, instrumentlabel, all_total, all_total_not_charged, all_total_charged, all_duration_not_charged, usage_duration_not_charged, maintenance_duration_not_charged, non_bookable_duration_not_charged, all_duration_charged, usage_duration_charged, maintenance_duration_charged, non_bookable_duration_charged, all_total_not_chargeable, all_total_chargeable, all_duration_not_chargeable, usage_duration_not_chargeable, maintenance_duration_not_chargeable, non_bookable_duration_not_chargeable, all_duration_chargeable, usage_duration_chargeable, maintenance_duration_chargeable, non_bookable_duration_chargeable from statisticsrefineinstrumentreservationdays");
    }

    public List<Object> getRefineInstrumentReservations() {
        return getResult("select year, instrumentid, instrumentlabel, all_charges, all_accounted, all_notaccounted, booked_charges, booked_accounted, booked_notaccounted, billable_charges, billable_accounted, billable_notaccounted, non_billable_charges, non_billable_accounted, non_billable_notaccounted, all_charges_internal, all_accounted_internal, all_notaccounted_internal, booked_charges_internal, booked_accounted_internal, booked_notaccounted_internal, billable_charges_internal, billable_accounted_internal, billable_notaccounted_internal, non_billable_charges_internal, non_billable_accounted_internal, non_billable_notaccounted_internal from statisticsrefineinstrumentreservations");
    }

    public List<Object> getRefineServices() {
        return getResult("select year, serviceid, servicename, servicecode, all_charges, all_accounted, all_notaccounted, booked_charges, booked_accounted, booked_notaccounted, billable_charges, billable_accounted, billable_notaccounted, non_billable_charges, non_billable_accounted, non_billable_notaccounted, all_charges_internal, all_accounted_internal, all_notaccounted_internal, booked_charges_internal, booked_accounted_internal, booked_notaccounted_internal, billable_charges_internal, billable_accounted_internal, billable_notaccounted_internal, non_billable_charges_internal, non_billable_accounted_internal, non_billable_notaccounted_internal from statisticsrefineservices");
    }

    public List<Object> getReservationHoursInstrumentsYearly() {
        return getResult("SELECT year, totalreservation FROM instrumentstatisticsutilizationperyear");
    }

    public List<Object> getResourceStatusCounts() {
        return getResult("SELECT status, totalresource FROM resourcestatuscounter");
    }

    public List<Object> getResourcesCreatedUntil() {
        return getResult("SELECT year, total FROM statisticsresourcescreateduntil WHERE year <> extract(year from current_date)");
    }

    public List<Object> getSamplesByCompany() {
        return getResult("SELECT year, companyid, companyname, divisionid, divisionname, samples, accounted, notaccounted from statisticssamplepercompany");
    }

    public List<Object> getSamplesByOrganization() {
        return getResult(
            "SELECT year, organizationid, organizationname, departmentid, departmentname, instituteid, institutename, samples, accounted, notaccounted from statisticssampleperorganization");
    }

    public List<Object> getSamplesCreatedUntil() {
        return getResult("SELECT year, total FROM statisticssamplescreateduntil WHERE year <> extract(year from current_date)");
    }

    public List<Object> getSamplesOverviewYearly() {
        return getResult("SELECT year, total FROM statisticssamplesoverviewperyear");
    }

    public List<Object> getServiceChargesPerYear() {
        return getResult("select year, serviceid, servicecode, all_charges, all_price, all_discountedprice, booked_charges, booked_price, booked_discountedprice, billable_charges, billable_price, billable_discountedprice, servicename from statisticsservicechargesperyear");
    }

    public List<Object> getServiceCodeChargesPerYear() {
        return getResult("select year, serviceid, servicename, servicecodename, all_charges, all_price, all_discountedprice, booked_charges, booked_price, booked_discountedprice, billable_charges, billable_price, billable_discountedprice from statisticsservicecodecharges");
    }

    public List<Object> getServicecodeCharges() {
        return getResult("select year, quarter, month, week, serviceid, servicecode, all_charges, all_price, all_discountedprice, booked_charges, booked_price, booked_discountedprice, billable_charges, billable_price, billable_discountedprice, servicename from statisticsserviceservicecodecharges");
    }

    public List<Object> getServicesCharges() {
        return getResult(
            "select year, month, serviceareaid, serviceareaname, servicetypeid, servicetypename, serviceid, servicename, all_charges, all_price, all_discountedprice, booked_charges, booked_price, booked_discountedprice, billable_charges, billable_price, billable_discountedprice from statisticsservicecharges");
    }

    public List<Object> getServicesCube() {
        return getResult("SELECT serviceareaid, serviceareaname, serviceareaenabled, servicetypeid, servicetypename, servicetypeenabled, serviceid, servicename, serviceenabled, servicecount FROM statisticsservice");
    }

    public List<Object> getServicesOverviewYearly() {
        return getResult("SELECT year, total FROM statisticsservicesoverviewperyear");
    }

    public List<Object> getServicesPerTechnology() {
        return getResult("SELECT year, technologyid, technologyname,serviceareaid, serviceareaname, servicetypeid, servicetypename, serviceid, servicename, total FROM statisticsservicepertechnology");
    }

    private List<Object> getStateDurationsByTechnology(String column, String technology, String stateDurationsAverageQuery) {
        StringBuilder queryBuilder = new StringBuilder();
        if (StringHelper.isEmpty(technology)) {
            queryBuilder.append(" SELECT year, CAST(SUM(").append(column).append(" * counter) / NULLIF(SUM(counter),0) AS NUMERIC(5,1)) ");
            queryBuilder.append(stateDurationsAverageQuery);
            queryBuilder.append(" GROUP BY year ORDER BY year ");
        } else {
            queryBuilder.append(" SELECT year, CAST(").append(column).append(" AS NUMERIC(5,1)) ");
            queryBuilder.append(stateDurationsAverageQuery);
            queryBuilder.append(" AND technology = :technology ");
        }
        Query query = createNativeQuery(queryBuilder.toString());
        if (StringHelper.isNotEmpty(technology)) {
            query.setParameter("technology", technology);
        }
        return query.getResultList();
    }

    public List<Object> getSupplierConsumableYearlyCHF(Supplier supplier) {
        return createNativeQuery("SELECT year, totalconsumablechf FROM statisticssupplieryearpercurrency WHERE supplierid = :supplierId")
            .setParameter("supplierId", supplier.getId()).getResultList();
    }

    public List<Object> getSupplierConsumableYearlyEUR(Supplier supplier) {
        return createNativeQuery("SELECT year, totalconsumableeur FROM statisticssupplieryearpercurrency WHERE supplierid = :supplierId")
            .setParameter("supplierId", supplier.getId()).getResultList();
    }

    public List<Object> getSupplierConsumableYearlyGBP(Supplier supplier) {
        return createNativeQuery("SELECT year, totalconsumablegbp FROM statisticssupplieryearpercurrency WHERE supplierid = :supplierId")
            .setParameter("supplierId", supplier.getId()).getResultList();
    }

    public List<Object> getSupplierConsumableYearlyUSD(Supplier supplier) {
        return createNativeQuery("SELECT year, totalconsumableusd FROM statisticssupplieryearpercurrency WHERE supplierid = :supplierId")
            .setParameter("supplierId", supplier.getId()).getResultList();
    }

    public List<Object> getSupplierCost() {
        return getResult("SELECT year, supplierid, suppliername, purchases, purchasedprice, purchasescurrency, consumables, consumableprice, consumablescurrency FROM statisticscostssupplierperyear");
    }

    public List<Object> getSupplierPurchasesYearlyCHF(Supplier supplier) {
        return createNativeQuery("SELECT year, totalpurchasechf FROM statisticssupplieryearpercurrency WHERE supplierid = :supplierId")
            .setParameter("supplierId", supplier.getId()).getResultList();
    }

    public List<Object> getSupplierPurchasesYearlyEUR(Supplier supplier) {
        return createNativeQuery("SELECT year, totalpurchaseeur FROM statisticssupplieryearpercurrency WHERE supplierid = :supplierId")
            .setParameter("supplierId", supplier.getId()).getResultList();
    }

    public List<Object> getSupplierPurchasesYearlyGBP(Supplier supplier) {
        return createNativeQuery("SELECT year, totalpurchasegbp FROM statisticssupplieryearpercurrency WHERE supplierid = :supplierId")
            .setParameter("supplierId", supplier.getId()).getResultList();
    }

    public List<Object> getSupplierPurchasesYearlyUSD(Supplier supplier) {
        return createNativeQuery("SELECT year, totalpurchaseusd FROM statisticssupplieryearpercurrency WHERE supplierid = :supplierId")
            .setParameter("supplierId", supplier.getId()).getResultList();
    }

    public List<Object> getSupplierStatisticsConsumables(Supplier supplier) {
        return createNativeQuery("SELECT year, totalconsumables FROM statisticssupplieryearpercurrency WHERE supplierid = :supplierId")
            .setParameter("supplierId", supplier.getId()).getResultList();
    }

    public List<Object> getSupplierStatisticsPerYear(Supplier supplier) {
        return createNativeQuery("SELECT year, purchases, purchasedprice, purchasescurrency, consumables, consumableprice, consumablescurrency FROM statisticssupplieryear WHERE supplierid = :supplierId")
            .setParameter("supplierId", supplier.getId()).getResultList();
    }

    public List<Object> getSupplierStatisticsPurchases(Supplier supplier) {
        return createNativeQuery("SELECT year, totalpurchases FROM statisticssupplieryearpercurrency WHERE supplierid = :supplierId")
            .setParameter("supplierId", supplier.getId()).getResultList();
    }

    public List<Object> getTimeframeElementsByClass(Class<?> clazz, String col, String columns, String timeframe, int timeframeMultiplicator) {
        return getResult("select " + columns + " from " + ClassHelper.getTableName(clazz) + " where age(" + col + ") < '" + timeframeMultiplicator + " " + timeframe + "' order by " + col + " desc");
    }

    public List<Object> getTimeframeEntities(String timeframe, int timeframeMultiplicator) {
        // Set<Class<?>> entityClasses = ClassHelper.getEntityClasses();
        Class<?>[] entityClasses = { Sample.class, Project.class, Workunit.class, Dataset.class, Application.class, Annotation.class, EventType.class, Event.class, Credit.class, Offer.class,
            Order.class, Comment.class, Storage.class, Executable.class, Submitter.class, WrapperCreator.class, Institute.class, Department.class, Organization.class, Company.class,
            Service.class, ServiceType.class, ServiceArea.class, User.class, Charge.class, Booking.class, Contract.class, Instrument.class, InstrumentReservation.class, Link.class, UserGroup.class,
            Purchase.class, Resource.class };

        StringBuilder query = new StringBuilder();
        String name;
        String union = "UNION ";
        for (Class<?> entityClass : entityClasses) {
            name = "name";
            if (entityClass == Order.class) {
                name = "billingcustomerfirstname || ' ' || billingcustomerlastname AS name";
            } else if (entityClass == Offer.class) {
                name = "(CASE WHEN requesterid IS NOT NULL THEN (SELECT firstname || ' ' || lastname FROM user_ u WHERE u.id = requesterid) ELSE requestername END) AS name";
            } else if (entityClass == Charge.class) {
                name = "serviceName AS name";
            } else if (entityClass == Credit.class) {
                name = "year || ' ' || days AS name";
            } else if (entityClass == Event.class || entityClass == InstrumentReservation.class) {
                name = "startdate || '-' || enddate AS name";
            } else if (entityClass == Comment.class) {
                name = "initcap(discriminator) || ' ' || parentid AS name";
            } else if (entityClass == Link.class) {
                name = "initcap(parentclassname) || ' ' || parentid AS name";
            } else if (entityClass == User.class) {
                name = "firstName || ' ' || lastName AS name";
            } else if (entityClass == AccessRequest.class) {
                name = "userFirstName || ' ' || userLastName AS name";
            } else if (entityClass == Booking.class) {
                name = "billingCustomerFirstName || ' ' || billingCustomerLastName AS name";
            } else if (entityClass == Purchase.class) {
                name = "(SELECT name FROM supplier s WHERE s.id = supplierid) AS name";
            }
            String columns = "id, " + name + ", to_char(modified, 'YYYY-MM-DD HH24:MI') as modified, TEXT('" + entityClass.getSimpleName() + "') AS className, TEXT('" + entityClass
                .getSimpleName() + "') AS printClassName";
            query.append("SELECT ").append(columns).append(" FROM ").append(ClassHelper.getTableName(entityClass)).append(" WHERE age(").append("modified").append(") < '")
                .append(timeframeMultiplicator).append(" ").append(timeframe).append("' ").append(union);
        }

        return getResult(query.replace(query.lastIndexOf(union), query.lastIndexOf(union) + union.length(), "ORDER BY modified DESC").toString());
    }

    public List<Object> getTopCoachPerYear(Long year) {
        Query query;
        if (year != null) {
            query = createNativeQuery("SELECT coach, totalcoachedprojectsbyyear FROM coachedprojectsperyear WHERE coachid IS NOT NULL and year = :year order by totalcoachedprojectsbyyear desc").setParameter("year", year);
        } else {
            query = createNativeQuery("SELECT coach, max(totalcoachedprojectsbyyear) as total, coachid FROM coachedprojectsperyear WHERE coachid IS NOT NULL group by coachid, coach order by total desc");
        }
        return query.setMaxResults(10).getResultList();
    }

    public List<Object> getTopEntityCounts() {
        final Class<?>[] classes = { User.class, Organization.class, Department.class, Institute.class, Company.class, Project.class, Order.class, Sample.class, Workunit.class, Resource.class,
            Dataset.class, Application.class, Instrument.class, InstrumentReservation.class, ServiceType.class, Service.class, Charge.class, Booking.class, Plate.class, Run.class, Workflow.class };

        final ArrayList<Object> results = new ArrayList<>();
        for (final Class<?> aClass : classes) {
            final ArrayList<Object> r = new ArrayList<>();
            r.add(aClass.getSimpleName());
            r.add(getEntityCountByClass(aClass.getSimpleName()));
            results.add(r);
        }

        return results;
    }

    public List<Object> getTopRequesterPerYear(Long year) {
        Query query;
        if (year != null) {
            query = createNativeQuery("SELECT firstname ||  ' ' || lastname, total FROM statisticsorderperrequester WHERE requesterid IS NOT NULL and year = :year order by total desc").setParameter("year", year);
        } else {
            query = createNativeQuery("SELECT firstname ||  ' ' || lastname, max(total) as total, requesterid FROM statisticsorderperrequester WHERE requesterid IS NOT NULL group by requesterid, firstname, lastname order by total desc");
        }
        return query.setMaxResults(10).getResultList();
    }

    public List<Object> getTopServiceTypePerYear(Long year) {
        Query query;
        if (year != null) {
            query = createNativeQuery("SELECT servicetypename, totalbookings as total FROM incometopservicesyear WHERE servicetypename IS NOT NULL and year = :year order by total desc").setParameter("year", year);
        } else {
            query = createNativeQuery("SELECT servicetypename, sum(totalbookings) as total FROM incometopservicesyear WHERE servicetypename IS NOT NULL group by servicetypename order by total desc");
        }
        return query.setMaxResults(10).getResultList();
    }

    public List<Object> getTopSupplierPerYear(Long year) {
        Query query;
        if (year != null) {
            query = createNativeQuery("SELECT suppliername, purchasedprice as total FROM statisticscostssupplierperyear WHERE suppliername IS NOT NULL and purchasedprice is not null and year = :year order by total desc").setParameter("year", year);
        } else {
            query = createNativeQuery("SELECT suppliername, sum(purchasedprice) as total FROM statisticscostssupplierperyear WHERE suppliername IS NOT NULL and purchasedprice is not null group by suppliername order by total desc");
        }
        return query.setMaxResults(10).getResultList();
    }

    public List<Object> getUsersCreatedUntil() {
        return getResult("SELECT year, total FROM statisticsuserscreateduntil WHERE year <> extract(year from current_date)");
    }

    public List<Object> getWorkunitStatusCounts() {
        return getResult("SELECT status, totalworkunits FROM workunitstatuscounter");
    }

    public List<Object> getWorkunitsCreatedUntil() {
        return getResult("SELECT year, total FROM statisticsworkunitscreateduntil WHERE year <> extract(year from current_date)");
    }

    public List<Object> getWorkunitsETHZUZHPerYear() {
        return getResult("SELECT year, workunit_count, organization_name from workunitsperyearethzuzh");
    }

    public List<Integer> getYear(String tableName) {
        return createNativeQuery("SELECT distinct year FROM " + tableName + " where year is not null order by year").getResultList();
    }

    public List<Integer> getYearCoach() {
        return getYear("coachedprojectsperyear");
    }

    public List<Integer> getYearIncome() {
        return createNativeQuery("SELECT year FROM incomeserviceyearselection").getResultList();
    }

    public List<Integer> getYearRequester() {
        return getYear("statisticsorderperrequester");
    }

    public List<Integer> getYearSupplier() {
        return getYear("statisticscostssupplierperyear");
    }

    public List<Object> getYearlyBooking() {
        return getResult("SELECT year, ytotalbookings FROM statisticsbookingoverview");
    }

    public List<Object> getYearlyBookingCompany() {
        return getResult("SELECT year, ctotalbookings FROM statisticsbookingoverview");
    }

    public List<Object> getYearlyBookingContainerCounter() {
        return getResult("SELECT year, c_num_bookings FROM statisticsbookingoverview_cosete");
    }

    public List<Object> getYearlyBookingContainerOrders() {
        return getResult("SELECT year, totalbookings_orders FROM incomepercontainergraphline");
    }

    public List<Object> getYearlyBookingContainerProjects() {
        return getResult("SELECT year, totalbookings_projects FROM incomepercontainergraphline");
    }

    public List<Object> getYearlyBookingOrganization() {
        return getResult("SELECT year, ototalbookings FROM statisticsbookingoverview");
    }

    public List<Object> getYearlyBookingTechGenomics() {
        return getResult("SELECT year, totalbookings_genomics FROM incomepertechnologygraphline");
    }

    public List<Object> getYearlyBookingTechMetabolomics() {
        return getResult("SELECT year, totalbookings_metabolomics FROM incomepertechnologygraphline");
    }

    public List<Object> getYearlyBookingTechProteomics() {
        return getResult("SELECT year, totalbookings_proteomics FROM incomepertechnologygraphline");
    }

    public List<Object> getYearlyBookingTechnologyCounter() {
        return getResult("SELECT year, t_num_bookings FROM statisticsbookingoverview_cosete");
    }

    public List<Object> getYearlyOrderTechGenomics() {
        return getResult("SELECT year, totalorders_genomics FROM orderpertechnologyandyear");
    }

    public List<Object> getYearlyOrderTechMetabolomics() {
        return getResult("SELECT year, totalorders_metabolomics FROM orderpertechnologyandyear");
    }

    public List<Object> getYearlyOrderTechProteomics() {
        return getResult("SELECT year, totalorders_proteomics FROM orderpertechnologyandyear");
    }

    public List<Object> getYearlyProjectTechGenomics() {
        return getResult("SELECT year, totalprojects_genomics FROM projectpertechnologygraphline");
    }

    public List<Object> getYearlyProjectTechMetabolomics() {
        return getResult("SELECT year, totalprojects_metabolomics FROM projectpertechnologygraphline");
    }

    public List<Object> getYearlyProjectTechProteomics() {
        return getResult("SELECT year, totalprojects_proteomics FROM projectpertechnologygraphline");
    }

    public void refreshMaterializedView(String view) {
        execute("REFRESH MATERIALIZED VIEW " + view);
    }

    public void refreshMaterializedViews() {
        // Note: SQL function call needs to be cast to text or must be quoted.
        getResult("select cast(refreshMaterializedViews() as text)");
    }

    public List<Object> view(String view) {
        return getResult("SELECT * FROM " + view);
    }
}
