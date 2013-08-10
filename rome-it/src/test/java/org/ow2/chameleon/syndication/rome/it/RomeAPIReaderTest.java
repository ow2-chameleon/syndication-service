package org.ow2.chameleon.syndication.rome.it;

import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;
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
import org.ow2.chameleon.testing.helpers.IPOJOHelper;
import org.ow2.chameleon.testing.helpers.OSGiHelper;

import java.io.IOException;
import java.net.URL;
import java.util.List;

@RunWith(JUnit4TestRunner.class)
public class RomeAPIReaderTest {

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

                        CoreOptions.mavenBundle().groupId("org.ow2.chameleon.syndication").artifactId("syndication-service").versionAsInProject(),
                        CoreOptions.mavenBundle().groupId("org.ow2.chameleon.syndication").artifactId("rome-syndication-service").versionAsInProject(),
                        CoreOptions.mavenBundle().groupId("org.apache.servicemix.bundles").artifactId("org.apache.servicemix.bundles.jdom").versionAsInProject(),
                        CoreOptions.mavenBundle().groupId("org.mortbay.jetty").artifactId("servlet-api-2.5").versionAsInProject(),
                        CoreOptions.mavenBundle().groupId("org.osgi").artifactId("org.osgi.compendium").versionAsInProject()));
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
    public void testReadingAkquinetBlog() throws IOException, FeedException {
        String ak = "http://blog.akquinet.de/feed/";

        SyndFeedInput input = new SyndFeedInput();
        SyndFeed feed = input.build(new XmlReader(new URL(ak)));
        Assert.assertNotNull(feed.getEntries());

        List<SyndEntry> list = feed.getEntries();
        for (SyndEntry entry : list) {
            System.out.println(entry.getTitle());
            Assert.assertNotNull(entry.getTitle());
        }
    }

}
