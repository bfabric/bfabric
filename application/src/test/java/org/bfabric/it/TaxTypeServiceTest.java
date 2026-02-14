package org.bfabric.it;

import java.io.File;

import javax.inject.Inject;

import org.bfabric.service.TaxTypeService;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class TaxTypeServiceTest {

    @Inject
    TaxTypeService service;

    @Deployment
    public static WebArchive createDeployment() {
        WebArchive archive = ShrinkWrap.createFromZipFile(WebArchive.class, new File("target/bfabric-13.0.0-SNAPSHOT.war"));
        System.out.println("Deploying WebArchive: " + archive.toString());
        return archive;
    }

    @Test
    public void helloRightSucceeds() {
        Assert.assertEquals("hello", service.hello());
    }

    @Test
    public void helloWrongFails() {
        Assert.assertNotEquals("hi", service.hello());
    }

    @Before
    public void setUp() {
        System.out.println("TaxTypeServiceTest.setup");
    }

    @After
    public void tearDown() {
        System.out.println("TaxTypeServiceTest.tearDown");
    }
}
