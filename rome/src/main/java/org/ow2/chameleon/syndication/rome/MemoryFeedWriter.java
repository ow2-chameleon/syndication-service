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

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Property;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.ServiceProperty;
import org.apache.felix.ipojo.annotations.Validate;
import org.osgi.service.event.EventAdmin;
import org.ow2.chameleon.syndication.FeedWriter;

import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.feed.synd.SyndFeedImpl;

/**
 * Memory-based feed writer.
 */
@Component(name = "org.ow2.chameleon.syndication.rome.memwriter", immediate = true)
@Provides
public class MemoryFeedWriter extends AbstractFeedWriter {

    /**
     * The Event Admin service.
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
     * The underlying feed object.
     */
    private SyndFeed m_feed;

    /**
     * Creates a {@link MemoryFeedWriter} for testing purpose.
     * @param title the title
     * @param type the type
     * @param recent the number of recent items
     */
    public MemoryFeedWriter(String title, String type, int recent) {
        m_title = title;
        if (type == null) {
            m_type = "rss_2.0";
        } else {
            m_type = type;
        }

        setRecent(recent);
    }

    /**
     * Gets the event admin service.
     * @return the event admin service
     * @see org.ow2.chameleon.syndication.rome.AbstractFeedWriter#getEventAdmin()
     */
    public EventAdmin getEventAdmin() {
        return m_ea;
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
     * Gets the underlying feed. Creates it if not already created.
     * @return the managed feed
     * @see org.ow2.chameleon.syndication.rome.AbstractFeedWriter#getFeed()
     */
    public SyndFeed getFeed() {
        if (m_feed == null) {
            m_feed = new SyndFeedImpl();
            m_feed.setTitle(m_title);
            m_feed.setLink("http://chameleon.ow2.org");
            m_feed
                    .setDescription("This feed has been created using the Chameleon Syndication Service");
            m_feed.setFeedType(m_type);
            m_url = "mem://" + m_title;
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
     * Gets the feed url
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

}
