package org.ow2.chameleon.syndication.rome.it;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.CoreOptions;
import org.ops4j.pax.exam.Inject;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.Configuration;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.ow2.chameleon.syndication.FeedEntry;
import org.ow2.chameleon.syndication.FeedReader;
import org.ow2.chameleon.testing.helpers.IPOJOHelper;
import org.ow2.chameleon.testing.helpers.OSGiHelper;

@RunWith(JUnit4TestRunner.class)
public class RomeReaderTest {

    @Inject
    private BundleContext context;

    private OSGiHelper osgi;

    private IPOJOHelper ipojo;

    @Configuration
    public static Option[] configure() throws Exception {

        Option[] opt = CoreOptions.options(CoreOptions.felix(),

        CoreOptions.provision(CoreOptions.mavenBundle().groupId(
                "org.apache.felix").artifactId("org.apache.felix.ipojo")
                .versionAsInProject(), CoreOptions.mavenBundle().groupId(
                "org.apache.felix").artifactId("org.apache.felix.configadmin")
                .versionAsInProject(), CoreOptions.mavenBundle().groupId(
                "org.ow2.chameleon.testing").artifactId("osgi-helpers")
                .versionAsInProject(), CoreOptions.mavenBundle().groupId(
                "org.ops4j.pax.logging").artifactId("pax-logging-api")
                .versionAsInProject(),

        CoreOptions.mavenBundle().groupId("org.ow2.chameleon.syndication")
                .artifactId("syndication-service").versionAsInProject(),
                CoreOptions.mavenBundle().groupId(
                        "org.ow2.chameleon.syndication").artifactId("rome")
                        .versionAsInProject(), CoreOptions.mavenBundle()
                        .groupId("jdom").artifactId(
                                "org.ow2.chameleon.commons.jdom")
                        .versionAsInProject(), CoreOptions.mavenBundle()
                        .groupId("javax.servlet").artifactId(
                                "org.ow2.chameleon.commons.servlet-api")
                        .versionAsInProject(), CoreOptions.mavenBundle()
                        .groupId("org.osgi").artifactId("org.osgi.compendium")
                        .versionAsInProject()));
        return opt;
    }

    @Before
    public void setup() {
        osgi = new OSGiHelper(context);
        ipojo = new IPOJOHelper(context);
    }

    @After
    public void tearDown() {
        osgi.dispose();
        ipojo.dispose();
    }

    @Test
    public void testReadingAkquinetBlog() {
        String ak = "http://blog.akquinet.de/feed/";
        // Create the instance
        Dictionary<String, String> props = new Hashtable<String, String>();
        props.put("feed.url", ak);
        ipojo.createComponentInstance(
                "org.ow2.chameleon.syndication.rome.reader", props);

        osgi.waitForService(FeedReader.class.getName(), /*
                                                         * "("+FeedReader.FEED_TITLE_PROPERTY
                                                         * +"=*)"
                                                         */null, 5000);

        // Check service properties
        ServiceReference ref = osgi.getServiceReference(FeedReader.class
                .getName());
        for (String k : ref.getPropertyKeys()) {
            System.out.println(k + " = " + ref.getProperty(k));
        }
        Assert.assertEquals(ak, ref.getProperty(FeedReader.FEED_URL_PROPERTY));
        Assert.assertEquals("akquinet-blog", ref
                .getProperty(FeedReader.FEED_TITLE_PROPERTY));

        FeedReader reader = (FeedReader) osgi.getServiceObject(ref);

        List<FeedEntry> list = reader.getEntries();
        for (FeedEntry entry : list) {
            Assert.assertNotNull(entry.title());
            Assert.assertNotNull(entry.author());
            Assert.assertNotNull(entry.publicationDate());
            System.out.println(entry.categories());
        }
    }

    @Test
    public void testGetRecentEntries() throws Exception {
        String ak = "http://blog.akquinet.de/category/all/osgi-and-mobile-solutions/feed/";
        // Create the instance
        Dictionary<String, String> props = new Hashtable<String, String>();
        props.put("feed.url", ak);
        props.put("feed.recent", "5");
        ipojo.createComponentInstance(
                "org.ow2.chameleon.syndication.rome.reader", props);

        osgi.waitForService(FeedReader.class.getName(), /*
                                                         * "("+FeedReader.FEED_TITLE_PROPERTY
                                                         * +"=*)"
                                                         */null, 5000);
        ServiceReference ref = osgi.getServiceReference(FeedReader.class
                .getName());
        FeedReader reader = (FeedReader) osgi.getServiceObject(ref);
        List<FeedEntry> list = reader.getRecentEntries();
        for (FeedEntry e : list) {
            System.out.println(e.title());
        }
        Assert.assertEquals(5, list.size());
        for (FeedEntry entry : list) {
            Assert.assertNotNull(entry.title());
            Assert.assertNotNull(entry.author());
            Assert.assertNotNull(entry.publicationDate());
        }
    }

    @Test
    public void testEventAdmin() throws Exception {
        context
                .installBundle(
                        "http://mirror.lwnetwork.org.uk/APACHE//felix/org.apache.felix.eventadmin-1.2.14.jar")
                .start();
        EventCollector collector = new EventCollector();
        collector.start(context, FeedReader.NEW_ENTRY_TOPIC);

        String ak = "http://blog.akquinet.de/category/all/osgi-and-mobile-solutions/feed/";
        // Create the instance
        Dictionary<String, String> props = new Hashtable<String, String>();
        props.put("feed.url", ak);
        props.put("feed.recent", "5");
        ipojo.createComponentInstance(
                "org.ow2.chameleon.syndication.rome.reader", props);

        osgi.waitForService(FeedReader.class.getName(), /*
                                                         * "("+FeedReader.FEED_TITLE_PROPERTY
                                                         * +"=*)"
                                                         */null, 5000);

        Thread.sleep(2000);

        collector.stop();
        Assert.assertTrue(!collector.m_events.isEmpty());
        for (Event event : collector.m_events) {
            Assert
                    .assertNotNull(event
                            .getProperty(FeedReader.ENTRY_AUTHOR_KEY));
            Assert.assertNotNull(event
                    .getProperty(FeedReader.ENTRY_CATEGORIES_KEY));
            Assert.assertNotNull(event
                    .getProperty(FeedReader.ENTRY_CONTENT_KEY));
            Assert.assertNotNull(event.getProperty(FeedReader.ENTRY_DATE_KEY));
            Assert.assertNotNull(event.getProperty(FeedReader.ENTRY_URL_KEY));
            Assert.assertNotNull(event.getProperty(FeedReader.FEED_TITLE_KEY));
            Assert.assertNotNull(event.getProperty(FeedReader.FEED_URL_KEY));
            Assert.assertNotNull(event.getProperty(FeedReader.ENTRY_TITLE_KEY));
            Assert.assertNotNull(event.getProperty(FeedReader.ENTRY_URL_KEY));
            System.out.println("Event : "
                    + event.getProperty(FeedReader.ENTRY_TITLE_KEY));
        }
    }

    private class EventCollector implements EventHandler {

        public List<Event> m_events = new ArrayList<Event>();

        private ServiceRegistration reg;

        public void start(BundleContext context, String topic) {
            Properties props = new Properties();
            props.put(EventConstants.EVENT_TOPIC, topic);
            reg = context.registerService(EventHandler.class.getName(), this,
                    props);
        }

        public void stop() {
            if (reg != null) {
                reg.unregister();
            }
        }

        public void handleEvent(Event event) {
            m_events.add(event);
        }

    }

}
