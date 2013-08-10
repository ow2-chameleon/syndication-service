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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Property;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.ServiceProperty;
import org.apache.felix.ipojo.annotations.Validate;
import org.osgi.service.event.EventAdmin;
import org.ow2.chameleon.syndication.FeedEntry;
import org.ow2.chameleon.syndication.FeedWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.feed.synd.SyndFeedImpl;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.SyndFeedOutput;
import com.sun.syndication.io.XmlReader;

/**
 * Feed Writer using a file to dump the feed.
 */
@Component(name = "org.ow2.chameleon.syndication.rome.filewriter", immediate = true)
@Provides
public class FileFeedWriter extends AbstractFeedWriter {

    /**
     * The file used to store the feed.
     */
    private File m_file;

    /**
     * The Event Admin service.
     */
    @Requires(optional = true)
    protected EventAdmin m_ea;

    /**
     * The feed title. (mandatory property)
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
     * The underlying feed.
     */
    protected SyndFeed m_feed;

    /**
     * The number of recent items. Default : 20.
     */
    @Property(name = "org.ow2.chameleon.syndication.feed.recent", value = "20")
    protected int m_recentCount;

    /**
     * the name (path) of the file used to store the feed.
     */
    @Property(name = "org.ow2.chameleon.syndication.feed.file", mandatory = true)
    private String m_fileName;

    /**
     * Logger.
     */
    private Logger m_logger = LoggerFactory.getLogger(this.toString());

    /**
     * Creates a {@link FileFeedWriter} for testing purpose.
     * @param file the file
     * @param title the title
     * @param type the type
     * @param recent the number of recent items
     */
    public FileFeedWriter(File file, String title, String type, int recent) {
        m_title = title;
        setRecent(recent);
        m_type = type;
        m_fileName = file.getAbsolutePath();
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
     * Gets the underlying feed. If the feed is not created, it creates the
     * feed.
     * @return the created feed object
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

            File file = new File(m_fileName);
            if (!file.exists()) {
                m_file = file;
                if (m_file.getParentFile() != null) {
                    m_file.getParentFile().mkdirs();
                }
                try {
                    m_file.createNewFile();
                } catch (IOException e) {
                    throw new IllegalArgumentException(
                            "Cannot create the feed : " + e.getMessage());
                }
            } else {
                if (file.isDirectory()) {
                    // Create the new file.
                    m_file = new File(file, m_title + ".xml");
                    try {
                        m_file.createNewFile();
                    } catch (IOException e) {
                        throw new IllegalArgumentException(
                                "Cannot create the feed : " + e.getMessage());
                    }
                } else {
                    // Reload the file.
                    m_file = file;
                    SyndFeedInput input = new SyndFeedInput();
                    try {
                        m_feed = input.build(new XmlReader(m_file));
                    } catch (Exception e) {
                        // Cannot reload the file, override it.
                    }
                }
            }

            if (!m_file.canWrite()) {
                throw new IllegalArgumentException(
                        "Cannot write the feed file "
                                + m_file.getAbsolutePath());
            }
            try {
                m_url = m_file.toURI().toURL().toExternalForm();
            } catch (IOException e) {
                // Cannot happen
            }
        }
        return m_feed;
    }

    /**
     * Initializes the writer. This methods creates the feed.
     * @throws IOException
     */
    @Validate
    public void init() throws IOException {
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
     * @return the title
     * @see org.ow2.chameleon.syndication.rome.AbstractFeedWriter#getTitle()
     */
    public String getTitle() {
        return m_title;
    }

    /**
     * Writes the feed to the file.
     */
    private void write() {
        Writer writer = null;
        try {
            writer = new FileWriter(m_file);
            SyndFeedOutput output = new SyndFeedOutput();
            output.output(m_feed, writer);
        } catch (Exception e) {
            m_logger.error("Cannot write feed to " + m_file.getAbsolutePath(),
                    e);
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    // Ignored.
                }
            }
        }
    }

    /**
     * Adds an entry to the feed. This method writes the file.
     * @param entry the entry to add
     * @see org.ow2.chameleon.syndication.rome.AbstractFeedWriter#addEntry(org.ow2.chameleon.syndication.FeedEntry)
     */
    public synchronized void addEntry(FeedEntry entry) {
        super.addEntry(entry);
        write();

    }

    /**
     * Removes an entry from the feed. This method writes the file.
     * @param entry the entry to remove
     * @see org.ow2.chameleon.syndication.rome.AbstractFeedWriter#removeEntry(org.ow2.chameleon.syndication.FeedEntry)
     */
    public synchronized void removeEntry(FeedEntry entry) {
        super.removeEntry(entry);
        write();
    }

    /**
     * Removes an entry from the feed, based on its name. This method writes the
     * file.
     * @param title the feed title
     * @see org.ow2.chameleon.syndication.rome.AbstractFeedWriter#removeEntryByTitle(java.lang.String)
     */
    public synchronized void removeEntryByTitle(String title) {
        super.removeEntryByTitle(title);
        write();
    }

}
