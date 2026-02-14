package org.bfabric.it;

import java.io.File;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.logging.Logger;

import javax.inject.Inject;

import com.google.common.collect.Sets;
import org.bfabric.entity.Container;
import org.bfabric.entity.Instrument;
import org.bfabric.entity.InstrumentReservation;
import org.bfabric.entity.InstrumentReservationSetting;
import org.bfabric.entity.InstrumentReservationType;
import org.bfabric.entity.Technology;
import org.bfabric.entity.User;
import org.bfabric.service.EntityService;
import org.bfabric.service.InstrumentReservationService;
import org.bfabric.service.InstrumentService;
import org.bfabric.service.UserService;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class InstrumentReservationServiceTest {

    private static final Logger logger = Logger.getLogger(InstrumentReservationServiceTest.class.getName());

    private static final String defaultBookerLogin = "admin";

    private static final String defaultContainer = "informatics test";

    @Inject
    EntityService entityService;

    @Inject
    UserService userService;

    @Inject
    InstrumentService instrumentService;

    @Inject
    InstrumentReservationService instrumentReservationService;

    private User booker;

    private Set<Container> containers;

    @Deployment
    public static WebArchive createDeployment() {
        WebArchive archive = ShrinkWrap.createFromZipFile(WebArchive.class, new File("target/bfabric-13.0.0-SNAPSHOT.war"));
        System.out.println("Deploying WebArchive: " + archive.toString());
        return archive;
    }

    private Instrument createInstrumentWithWeekendAllowed() {
        User supervisorAndAdmin = userService.getUserByLogin("admin");
        Instrument nonDefaultInstrument = new Instrument();
        nonDefaultInstrument.setLabel("dummyinstrument");
        nonDefaultInstrument.setName("dummyinstrument");
        nonDefaultInstrument.setSupervisor(supervisorAndAdmin);
        nonDefaultInstrument.setAdmin(supervisorAndAdmin);
        nonDefaultInstrument.setTechnologies(Sets.newHashSet(entityService.find(Technology.class, 6L)));
        instrumentService.save(nonDefaultInstrument);
        return nonDefaultInstrument;
    }

    private InstrumentReservation getInstrumentReservation(Instrument newInstrument, LocalDate settingValidFrom) {
        InstrumentReservation instrumentReservation = new InstrumentReservation();
        instrumentReservation.setInstrument(newInstrument);
        instrumentReservation.setBooker(booker);
        instrumentReservation.setContainers(containers);
        instrumentReservation.setInstrumentReservationType(entityService.findByName(InstrumentReservationType.class, "Usage"));
        instrumentReservation.setStartDate(settingValidFrom.minusDays(3).atTime(LocalTime.MIDNIGHT));
        instrumentReservation.setEndDate(settingValidFrom.plusDays(3).atTime(LocalTime.MIDNIGHT));
        return instrumentReservation;
    }

    /**
     * Sets to default common attributes of instrument reservation.
     */
    private void setInstrumentReservationDefaultCommonEntities() {
        booker = userService.getUserByLogin(defaultBookerLogin);
        containers = Sets.newHashSet((Container) entityService.getFilteredOrderedByName(defaultContainer));
    }

    @Before
    public void setUp() {
        logger.fine("InstrumentReservationServiceTest.setup");
        setInstrumentReservationDefaultCommonEntities();
    }

    @Test
    public void shouldFailWhenStartEndDatesAreNotInTheSameSettingBoundary() {
        LocalDate settingValidFrom = LocalDate.now().plusWeeks(1).with(DayOfWeek.MONDAY);
        Instrument newInstrument = createInstrumentWithWeekendAllowed();
        InstrumentReservationSetting newSetting = new InstrumentReservationSetting(newInstrument);
        newSetting.setValidFrom(settingValidFrom);
        newInstrument.getReservationSettings().add(newSetting);
        InstrumentReservation instrumentReservation = getInstrumentReservation(newInstrument, settingValidFrom);
        LinkedHashMap<String, String> validationErrorMsg = instrumentReservationService.save(instrumentReservation);
        Assertions.assertFalse(validationErrorMsg.isEmpty());
        entityService.remove(newInstrument);
    }

    @After
    public void tearDown() {
        System.out.println("InstrumentReservationServiceTest.tearDown");
    }
}