package org.ow2.chameleon.syndication.rome.it;

import java.io.File;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
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
import org.ow2.chameleon.syndication.FeedWriter;
import org.ow2.chameleon.testing.helpers.IPOJOHelper;
import org.ow2.chameleon.testing.helpers.OSGiHelper;

@RunWith(JUnit4TestRunner.class)
@Ignore
public class RomeHTTPWriterTest {

    public static final String PORT = "8886"; // Hopefully not used.

    @Inject
    private BundleContext context;

    private OSGiHelper osgi;

    private IPOJOHelper ipojo;

    @Configuration
    public static Option[] configure() throws Exception {

        Option[] opt = CoreOptions
                .options(
                        CoreOptions.felix(),

                        CoreOptions
                                .provision(
                                        CoreOptions.mavenBundle().groupId(
                                                "org.apache.felix").artifactId(
                                                "org.apache.felix.ipojo")
                                                .versionAsInProject(),
                                        CoreOptions.mavenBundle().groupId(
                                                "org.apache.felix").artifactId(
                                                "org.apache.felix.configadmin")
                                                .versionAsInProject(),
                                        CoreOptions.mavenBundle().groupId(
                                                "org.ow2.chameleon.testing")
                                                .artifactId("osgi-helpers")
                                                .versionAsInProject(),
                                        CoreOptions.mavenBundle().groupId(
                                                "org.ops4j.pax.logging")
                                                .artifactId("pax-logging-api")
                                                .versionAsInProject(),

                                        CoreOptions.mavenBundle().groupId("org.ow2.chameleon.syndication").artifactId("syndication-service").versionAsInProject(),
                                        CoreOptions.mavenBundle().groupId("org.ow2.chameleon.syndication").artifactId("rome-syndication-service").versionAsInProject(),
                                        CoreOptions.mavenBundle().groupId("org.apache.servicemix.bundles").artifactId("org.apache.servicemix.bundles.jdom").versionAsInProject(),
                                        CoreOptions.mavenBundle().groupId("org.mortbay.jetty").artifactId("servlet-api-2.5").versionAsInProject(),
                                        CoreOptions.mavenBundle().groupId("org.osgi").artifactId("org.osgi" +"" +".compendium").versionAsInProject(),

                                        CoreOptions.mavenBundle().groupId(
                                                "org.apache.felix").artifactId(
                                                "org.apache.felix.http.jetty")
                                                .version("1.0.1")),

                        CoreOptions
                                .systemProperty("org.osgi.service.http.port")
                                .value(PORT));
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
    public void testHTTPWriter() throws Exception {
        // Create the instance
        Dictionary<String, String> props = new Hashtable<String, String>();
        props.put(FeedWriter.FEED_TITLE_PROPERTY, "my_feed");
        props.put("org.ow2.chameleon.syndication.feed.servlet.alias", "/feed");
        Assert.assertNotNull(ipojo.createComponentInstance(
                "org.ow2.chameleon.syndication.rome.servlet", props));

        osgi.waitForService(FeedWriter.class.getName(), /*
                                                         * "("+FeedReader.FEED_TITLE_PROPERTY
                                                         * +"=*)"
                                                         */null, 15000);

        // Check service properties
        ServiceReference ref = osgi.getServiceReference(FeedReader.class
                .getName());
        for (String k : ref.getPropertyKeys()) {
            System.out.println(k + " = " + ref.getProperty(k));
        }

        Assert.assertEquals("my_feed", ref
                .getProperty(FeedReader.FEED_TITLE_PROPERTY));

        FeedWriter writer = (FeedWriter) osgi.getServiceObject(FeedWriter.class
                .getName(), null);

        writer.addEntry(writer.createFeedEntry().title("a title").author("me")
                .content("the content"));
        writer.addEntry(writer.createFeedEntry().title("a title 2")
                .author("me").content("the content"));

        Assert.assertEquals("my_feed", writer.getTitle());

        Assert.assertEquals(2, writer.getEntries().size());
        Assert.assertEquals(2, writer.getRecentEntries().size());
        FeedEntry entry = writer.getLastEntry();
        Assert.assertEquals("a title 2", entry.title());
        Assert.assertEquals("me", entry.author());
        Assert.assertEquals("the content", entry.content());
        Assert.assertNotNull(entry.publicationDate());
    }

    @Test
    public void testHTTPWriterWithAReader() throws Exception {
        // Create the instance
        Dictionary<String, String> props = new Hashtable<String, String>();
        props.put(FeedWriter.FEED_TITLE_PROPERTY, "my_feed");
        props.put("org.ow2.chameleon.syndication.feed.servlet.alias", "/feed");
        Assert.assertNotNull(ipojo.createComponentInstance(
                "org.ow2.chameleon.syndication.rome.servlet", props));

        osgi.waitForService(FeedWriter.class.getName(), /*
                                                         * "("+FeedReader.FEED_TITLE_PROPERTY
                                                         * +"=*)"
                                                         */null, 15000);

        // Check service properties
        ServiceReference ref = osgi.getServiceReference(FeedReader.class
                .getName());
        for (String k : ref.getPropertyKeys()) {
            System.out.println(k + " = " + ref.getProperty(k));
        }

        Assert.assertEquals("my_feed", ref
                .getProperty(FeedReader.FEED_TITLE_PROPERTY));

        FeedWriter writer = (FeedWriter) osgi.getServiceObject(FeedWriter.class
                .getName(), null);
        writer.addEntry(writer.createFeedEntry().title("a title").author("me")
                .content("the content"));
        writer.addEntry(writer.createFeedEntry().title("a title 2")
                .author("me").content("the content"));

        String ak = "http://localhost:" + PORT + "/feed";
        // Create the instance
        props = new Hashtable<String, String>();
        props.put("feed.url", ak);
        ipojo.createComponentInstance(
                "org.ow2.chameleon.syndication.rome.reader", props);

        osgi.waitForService(FeedReader.class.getName(), "("
                + FeedReader.FEED_TITLE_PROPERTY + "=" + "my_feed" + ")", 5000);
        FeedReader reader = (FeedReader) osgi.getServiceObject(FeedReader.class
                .getName(), "(" + FeedReader.FEED_TITLE_PROPERTY + "="
                + "my_feed" + ")");

        Assert.assertEquals("my_feed", reader.getTitle());

        Assert.assertEquals(2, reader.getEntries().size());
        Assert.assertEquals(2, reader.getRecentEntries().size());
        FeedEntry entry = reader.getLastEntry();
        Assert.assertEquals("a title 2", entry.title());
        Assert.assertEquals("me", entry.author());
        Assert.assertEquals("the content", entry.content());
        Assert.assertNotNull(entry.publicationDate());
    }

    @Test
    public void testEventAdmin() throws Exception {
        context
                .installBundle(
                        "http://mirror.lwnetwork.org.uk/APACHE//felix/org.apache.felix.eventadmin-1.2.14.jar")
                .start();
        EventCollector collector = new EventCollector();
        collector.start(context, FeedReader.NEW_ENTRY_TOPIC);

        File file = new File("feed.xml");
        file.delete();
        // Create the instance
        Dictionary<String, String> props = new Hashtable<String, String>();
        props.put(FeedWriter.FEED_TITLE_PROPERTY, "a feed");
        props.put("org.ow2.chameleon.syndication.feed.file", "feed.xml");
        ipojo.createComponentInstance(
                "org.ow2.chameleon.syndication.rome.filewriter", props);

        osgi.waitForService(FeedWriter.class.getName(), /*
                                                         * "("+FeedReader.FEED_TITLE_PROPERTY
                                                         * +"=*)"
                                                         */null, 5000);

        // Check service properties
        ServiceReference ref = osgi.getServiceReference(FeedReader.class
                .getName());
        for (String k : ref.getPropertyKeys()) {
            System.out.println(k + " = " + ref.getProperty(k));
        }
        Assert.assertTrue(file.exists());
        Assert.assertEquals(file.toURI().toURL().toExternalForm(), ref
                .getProperty(FeedReader.FEED_URL_PROPERTY));

        Assert.assertEquals("a feed", ref
                .getProperty(FeedReader.FEED_TITLE_PROPERTY));

        FeedWriter writer = (FeedWriter) osgi.getServiceObject(FeedWriter.class
                .getName(), null);

        writer.addEntry(writer.createFeedEntry().title("a title").author("me")
                .content("the content"));
        writer.addEntry(writer.createFeedEntry().title("a title 2")
                .author("me").content("the content"));

        Thread.sleep(2000);

        collector.stop();
        Assert.assertEquals(2, collector.m_events.size());
        for (Event event : collector.m_events) {
            Assert
                    .assertNotNull(event
                            .getProperty(FeedReader.ENTRY_AUTHOR_KEY));
            Assert.assertNotNull(event
                    .getProperty(FeedReader.ENTRY_CONTENT_KEY));
            Assert.assertNotNull(event.getProperty(FeedReader.ENTRY_DATE_KEY));
            Assert.assertNotNull(event.getProperty(FeedReader.FEED_TITLE_KEY));
            Assert.assertNotNull(event.getProperty(FeedReader.FEED_URL_KEY));
            Assert.assertNotNull(event.getProperty(FeedReader.ENTRY_TITLE_KEY));
            System.out.println("Event : "
                    + event.getProperty(FeedReader.ENTRY_TITLE_KEY));
        }
    }

    private class EventCollector implements EventHandler {

        public List<Event> m_events = new ArrayList<Event>();

        private ServiceRegistration reg;

        public void start(BundleContext context, String topic) {
            Dictionary props = new Properties();
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
