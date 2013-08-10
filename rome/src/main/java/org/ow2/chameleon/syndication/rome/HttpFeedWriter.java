/*
 * Copyright 2009 OW2 Chameleon
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ow2.chameleon.syndication.rome;

import java.io.IOException;
import java.io.Writer;
import java.net.InetAddress;
import java.net.URL;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.felix.ipojo.annotations.Bind;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Property;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.ServiceProperty;
import org.apache.felix.ipojo.annotations.Unbind;
import org.apache.felix.ipojo.annotations.Validate;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.http.HttpContext;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.ow2.chameleon.syndication.FeedWriter;

import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.feed.synd.SyndFeedImpl;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedOutput;

/**
 * Feed Writer implementation publishing the feed in a servlet. (exposed using
 * an OSGi HTTP Service)
 */
@Component(name = "org.ow2.chameleon.syndication.rome.servlet", immediate = true)
@Provides
public class HttpFeedWriter extends AbstractFeedWriter {

    /**
     * The servlet alias (mandatory property).
     */
    @Property(name = "org.ow2.chameleon.syndication.feed.servlet.alias", mandatory = true)
    private String m_alias;

    /**
     * The event admin service.
     */
    @Requires(optional = true)
    protected EventAdmin m_ea;

    /**
     * The feed title (mandatory property).
     */
    @ServiceProperty(name = FeedWriter.FEED_TITLE_PROPERTY, mandatory = true)
    private String m_title;

    /**
     * The feed url.
     */
    @ServiceProperty(name = FeedWriter.FEED_URL_PROPERTY)
    private String m_url;

    /**
     * The feed type / format. Supported values are:
     * <ul>
     * <li>rss_0.90</li>
     * <li>rss_0.91</li>
     * <li>rss_0.92</li>
     * <li>rss_0.93</li>
     * <li>rss_0.94</li>
     * <li>rss_1.0</li>
     * <li>rss_2.0 (default value)</li>
     * <li>atom_0.3</li>
     * </ul>
     */
    @Property(name = "org.ow2.chameleon.syndication.feed.type", value = "rss_2.0")
    private String m_type;

    /**
     * the underlying feed.
     */
    protected SyndFeed m_feed;

    /**
     * The number of recent items. Default : 20.
     */
    @Property(name = "org.ow2.chameleon.syndication.feed.recent", value = "20")
    protected int m_recentCount;

    /**
     * HTTP Service.
     */
    private HttpService m_http;

    /**
     * The Bundle Context.
     */
    private BundleContext m_context;

    /**
     * Creates a {@link HttpFeedWriter} for testing purpose.
     * @param alias the alias
     * @param title the title
     * @param type the type
     * @param recent the number of recent item
     */
    public HttpFeedWriter(String alias, String title, String type, int recent) {
        m_title = title;
        setRecent(recent);
        m_type = type;
        m_alias = alias;
    }

    /**
     * Binds the HTTP Service.
     * @param http the http service
     * @param properties the service properties
     * @throws IOException if the host name cannot be determined
     * @throws ServletException if the servlet cannot be exposed
     * @throws NamespaceException if the alias is already used
     */
    @Bind
    public synchronized void bindHTTP(HttpService http,
            Map<String, ?> properties) throws IOException, ServletException,
            NamespaceException {
        m_http = http;
        if (properties.containsKey("org.osgi.service.http.port")) {
            String host = InetAddress.getLocalHost().getHostName();
            String port = (String) properties.get("org.osgi.service.http.port");
            m_url = "http://" + host + ":" + port + "/" + m_alias;
        } else {
            String port = System.getProperty("org.osgi.service.http.port");
            if (port == null) {
                port = "8080";
            }
            m_url = "http://localhost" + ":" + port + "/" + m_alias;
        }
        m_http.registerServlet(m_alias, new FeedServlet(), null,
                new HttpContext() {

                    public String getMimeType(String name) {
                        return null;
                    }

                    public URL getResource(String name) {
                        URL url = m_context.getBundle().getResource(name);
                        return url;
                    }

                    public boolean handleSecurity(HttpServletRequest request,
                            HttpServletResponse response) throws IOException {
                        return true;
                    }

                });
    }

    /**
     * Unbinds the HTTP Service.
     */
    @Unbind
    public synchronized void unbindHTTP() {
        m_http.unregister(m_alias);
    }

    /**
     * Sets the number of recent items.
     * @param recent the number of recent items
     */
    @Property(name = "org.ow2.chameleon.syndication.feed.recent", value = "20")
    public void setRecent(int recent) {
        setRecentItemsCount(recent);
    }

    /**
     * Gets the underlying Feed object. If not already created, this method
     * creates the feed.
     * @return the underlying feed object
     * @see org.ow2.chameleon.syndication.rome.AbstractFeedWriter#getFeed()
     */
    public SyndFeed getFeed() {
        if (m_feed == null) {
            m_feed = new SyndFeedImpl();
            m_feed.setTitle(m_title);
            m_feed.setLink("http://chameleon.ow2.org");
            m_feed
                    .setDescription("This feed has been created using the Chameleon"
                            + " Syndication Service");
            m_feed.setFeedType(m_type);
        }
        return m_feed;
    }

    /**
     * Initializes the feed.
     */
    @Validate
    public void init() {
        getFeed();
    }

    /**
     * Gets the Event Admin service.
     * @return the event admin
     * @see org.ow2.chameleon.syndication.rome.AbstractFeedWriter#getEventAdmin()
     */
    public EventAdmin getEventAdmin() {
        return m_ea;
    }

    /**
     * Gets the feed url.
     * @return the feed url
     * @see org.ow2.chameleon.syndication.FeedReader#getURL()
     */
    public String getURL() {
        return m_url;
    }

    /**
     * Gets the feed title.
     * @return the feed title
     * @see org.ow2.chameleon.syndication.rome.AbstractFeedWriter#getTitle()
     */
    public String getTitle() {
        return m_title;
    }

    /**
     * Servlet exposing the Feed.
     */
    private class FeedServlet extends HttpServlet {

        /**
         * UUID.
         */
        private static final long serialVersionUID = -8582988702950725976L;

        /**
         * Dump the feed according to the chosen type.
         * @param req the request
         * @param resp the response
         * @throws ServletException if the feed cannot be dumped correctly
         * @throws IOException if the servlet writer cannot be used.
         * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest,
         *      javax.servlet.http.HttpServletResponse)
         */
        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp)
                throws ServletException, IOException {
            try {
                getAllEntries(resp.getWriter());
            } catch (FeedException e) {
                throw new ServletException("Cannot render the feed", e);
            }
        }

        /**
         * Writes all entries to the given writer. This method switches the
         * Thread Context ClassLoaded and restores it after the dump.
         * @param writer the writer
         * @throws IOException if the feed cannot be dumped correctly.
         * @throws FeedException if the feed is incorrect.
         */
        private void getAllEntries(Writer writer) throws IOException,
                FeedException {
            ClassLoader bundle = this.getClass().getClassLoader();
            ClassLoader thread = Thread.currentThread().getContextClassLoader();
            try {
                // Switch
                Thread.currentThread().setContextClassLoader(bundle);
                SyndFeedOutput output = new SyndFeedOutput();
                output.output(getFeed(), writer, true);
                writer.flush();
                writer.close();
            } finally {
                // Restore
                Thread.currentThread().setContextClassLoader(thread);
            }
        }
    }
}
