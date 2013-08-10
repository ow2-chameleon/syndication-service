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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Dictionary;
import java.util.List;
import java.util.Properties;

import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.ow2.chameleon.syndication.FeedEntry;
import org.ow2.chameleon.syndication.FeedReader;
import org.ow2.chameleon.syndication.FeedWriter;

import com.sun.syndication.feed.synd.SyndCategory;
import com.sun.syndication.feed.synd.SyndCategoryImpl;
import com.sun.syndication.feed.synd.SyndContent;
import com.sun.syndication.feed.synd.SyndContentImpl;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndEntryImpl;
import com.sun.syndication.feed.synd.SyndFeed;

/**
 * Provides common behavior of feed writer. It mostly implements the reading
 * features.
 */
public abstract class AbstractFeedWriter implements FeedWriter {

    /**
     * Number of recent items.
     */
    private int m_recent;

    /**
     * Sets the number of recent items.
     * @param recent the number of recent item
     */
    protected void setRecentItemsCount(int recent) {
        m_recent = recent;
    }

    /**
     * Gets the {@link SyndFeed}. This provides a callback implemented by the
     * concrete implementation
     * @return the maintained/written SyndFeed
     */
    public abstract SyndFeed getFeed();

    /**
     * Gets the Event Admin service. This callback is implemented by the
     * concrete class to give access to the injected service.
     * @return the Event Admin
     */
    public abstract EventAdmin getEventAdmin();

    /**
     * Adds an entry to the managed feed.
     * @param entry the entry to add
     * @see org.ow2.chameleon.syndication.FeedWriter#addEntry(org.ow2.chameleon.syndication.FeedEntry)
     */
    @SuppressWarnings("unchecked")
    public synchronized void addEntry(FeedEntry entry) {
        SyndEntry theEntry = new SyndEntryImpl();
        SyndContent theContent = new SyndContentImpl();
        theEntry.setAuthor(entry.author());
        theEntry.setTitle(entry.title());
        theEntry.setLink(entry.url());
        theEntry.setPublishedDate(new Date()); // Set now as published date.
        theEntry.setUpdatedDate(new Date());

        theContent.setValue(entry.content());
        theEntry.setDescription(theContent);

        List<SyndCategory> theList = new ArrayList<SyndCategory>();
        for (String cat : entry.categories()) {
            SyndCategoryImpl theCategory = new SyndCategoryImpl();
            theCategory.setName(cat);
            theList.add(theCategory);
        }
        theEntry.setCategories(theList);

        getFeed().getEntries().add(0, theEntry);

        postEvent(new FeedEntryImpl(theEntry));
    }

    /**
     * Removes an entry of the feed.
     * @param entry the entry
     * @see org.ow2.chameleon.syndication.FeedWriter#removeEntry(org.ow2.chameleon.syndication.FeedEntry)
     */
    @SuppressWarnings("unchecked")
    public synchronized void removeEntry(FeedEntry entry) {
        List<SyndEntry> entries = getFeed().getEntries();
        SyndEntry target = null;
        for (SyndEntry e : entries) {
            // Look for the title and the date.
            if (e.getTitle() != null && e.getTitle().equals(entry.title())) {
                if (e.getPublishedDate() != null
                        && e.getPublishedDate().equals(entry.publicationDate())) {
                    target = e;
                    break;
                } else if (e.getUpdatedDate() != null
                        && e.getUpdatedDate().equals(entry.publicationDate())) {
                    target = e;
                    break;
                }
            }
        }
        if (target != null) {
            entries.remove(target);
        }
    }

    /**
     * Removes an entry based on its title.
     * @param title the feed title
     * @see org.ow2.chameleon.syndication.FeedWriter#removeEntryByTitle(java.lang.String)
     */
    @SuppressWarnings("unchecked")
    public synchronized void removeEntryByTitle(String title) {
        List<SyndEntry> entries = getFeed().getEntries();
        SyndEntry target = null;
        for (SyndEntry e : entries) {
            target = e;
            break;
        }
        if (target != null) {
            entries.remove(target);
        }
    }

    /**
     * Gets all the entries of the feed.
     * @return the list of entries
     * @see org.ow2.chameleon.syndication.FeedReader#getEntries()
     */
    @SuppressWarnings("unchecked")
    public synchronized List<FeedEntry> getEntries() {
        List<SyndEntry> entries = getFeed().getEntries();
        List<FeedEntry> result = new ArrayList<FeedEntry>();
        for (SyndEntry e : entries) {
            result.add(new FeedEntryImpl(e));
        }
        return result;
    }

    /**
     * Gets the last entry of the feed if any.
     * @return the last feed entry of <code>null</code> if the feed is empty.
     * @see org.ow2.chameleon.syndication.FeedReader#getLastEntry()
     */
    @SuppressWarnings("unchecked")
    public synchronized FeedEntry getLastEntry() {
        List<SyndEntry> entries = getFeed().getEntries();
        if (entries.isEmpty()) {
            return null;
        } else {
            return new FeedEntryImpl(entries.get(0));
        }
    }

    /**
     * Gets the recent entries. The number of returned entries depends on
     * {@link AbstractFeedWriter#m_recent}.
     * @return the list of recent entries
     * @see org.ow2.chameleon.syndication.FeedReader#getRecentEntries()
     */
    @SuppressWarnings("unchecked")
    public synchronized List<FeedEntry> getRecentEntries() {
        List<SyndEntry> entries = getFeed().getEntries();
        List<FeedEntry> result = new ArrayList<FeedEntry>();
        int count = 0;
        for (SyndEntry e : entries) {
            if (count == m_recent) {
                return result;
            }
            result.add(new FeedEntryImpl(e));
            count++;
        }
        return result;
    }

    /**
     * Gets the feed title.
     * @return the feed title
     * @see org.ow2.chameleon.syndication.FeedReader#getTitle()
     */
    public String getTitle() {
        return getFeed().getTitle();
    }

    /**
     * Creates a Feed Entry.
     * @return the new feed entry
     * @see org.ow2.chameleon.syndication.FeedWriter#createFeedEntry()
     */
    public FeedEntry createFeedEntry() {
        return new FeedEntryImpl();
    }

    /**
     * Send the event associated to the given (new) entry. This event is sent
     * with the event admin if the service is available
     * @param entry the entry to send
     */
    @SuppressWarnings("unchecked")
    public void postEvent(FeedEntry entry) {
        EventAdmin ea = getEventAdmin();
        if (ea == null) {
            return;
        } else {
            Dictionary data = new Properties();
            if (entry.author() != null) {
                data.put(FeedReader.ENTRY_AUTHOR_KEY, entry.author());
            }
            if (entry.categories() != null && !entry.categories().isEmpty()) {
                data.put(FeedReader.ENTRY_CATEGORIES_KEY, entry.categories());
            }
            if (entry.content() != null) {
                data.put(FeedReader.ENTRY_CONTENT_KEY, entry.content());
            }
            if (entry.publicationDate() != null) {
                data.put(FeedReader.ENTRY_DATE_KEY, entry.publicationDate());
            }
            if (entry.title() != null) {
                data.put(FeedReader.ENTRY_TITLE_KEY, entry.title());
            }
            if (entry.url() != null) {
                data.put(FeedReader.ENTRY_URL_KEY, entry.url());
            }
            if (getTitle() != null) {
                data.put(FeedReader.FEED_TITLE_KEY, getTitle());
            }
            if (getURL() != null) {
                data.put(FeedReader.FEED_URL_KEY, getURL());
            }

            Event event = new Event(FeedReader.NEW_ENTRY_TOPIC, data);
            ea.postEvent(event);
        }
    }

    /**
     * Feed Entry Implementation.
     */
    private class FeedEntryImpl implements FeedEntry {

        /**
         * Author.
         */
        private String m_author;

        /**
         * Content.
         */
        private String m_content;

        /**
         * Date.
         */
        private Date m_date;

        /**
         * Title.
         */
        private String m_title;

        /**
         * Entry URL.
         */
        private String m_url;

        /**
         * Entry categories.
         */
        private final List<String> m_categories;

        /**
         * Creates a {@link FeedEntryImpl} from a {@link SyndEntry}.
         * @param e the {@link SyndEntry}
         */
        @SuppressWarnings("unchecked")
        public FeedEntryImpl(SyndEntry e) {
            m_author = e.getAuthor();
            m_content = e.getDescription().getValue();
            Date d = e.getPublishedDate();
            if (d == null) {
                m_date = e.getUpdatedDate();
            } else {
                m_date = d;
            }
            m_title = e.getTitle();
            m_url = e.getLink();

            List<SyndCategory> categories = e.getCategories();
            m_categories = new ArrayList<String>();
            for (SyndCategory cat : categories) {
                m_categories.add(cat.getName());
            }
        }

        /**
         * Creates an empty Feed Entry.
         */
        public FeedEntryImpl() {
            m_categories = new ArrayList<String>();
        }

        /**
         * Gets the entry author.
         * @return the entry author
         * @see org.ow2.chameleon.syndication.FeedEntry#author()
         */
        public String author() {
            return m_author;
        }

        /**
         * Gets the categories.
         * @return the categories.
         * @see org.ow2.chameleon.syndication.FeedEntry#categories()
         */
        public List<String> categories() {
            return Collections.unmodifiableList(m_categories);
        }

        /**
         * Gets the content.
         * @return the content.
         * @see org.ow2.chameleon.syndication.FeedEntry#content()
         */
        public String content() {
            return m_content;
        }

        /**
         * Gets the publication date.
         * @return the publication date.
         * @see org.ow2.chameleon.syndication.FeedEntry#publicationDate()
         */
        public Date publicationDate() {
            return m_date;
        }

        /**
         * Gets the title.
         * @return the title
         * @see org.ow2.chameleon.syndication.FeedEntry#title()
         */
        public String title() {
            return m_title;
        }

        /**
         * Gets the URL.
         * @return the url
         * @see org.ow2.chameleon.syndication.FeedEntry#url()
         */
        public String url() {
            return m_url;
        }

        /**
         * Sets the feed entry author.
         * @param t the author
         * @return the current Feed Entry
         * @see org.ow2.chameleon.syndication.FeedEntry#author(java.lang.String)
         */
        public FeedEntry author(String t) {
            m_author = t;
            return this;
        }

        /**
         * Adds a category to the current feed entry.
         * @param t the category to add
         * @return the current Feed Entry
         * @see org.ow2.chameleon.syndication.FeedEntry#category(java.lang.String)
         */
        public FeedEntry category(String t) {
            m_categories.add(t);
            return this;
        }

        /**
         * Sets the feed entry content.
         * @param t the content
         * @return the current Feed Entry
         * @see org.ow2.chameleon.syndication.FeedEntry#content(java.lang.String)
         */
        public FeedEntry content(String t) {
            m_content = t;
            return this;
        }

        /**
         * Sets the feed entry title.
         * @param t the title
         * @return the current Feed Entry
         * @see org.ow2.chameleon.syndication.FeedEntry#title(java.lang.String)
         */
        public FeedEntry title(String t) {
            m_title = t;
            return this;
        }

        /**
         * Sets the feed entry url.
         * @param t the url
         * @return the current Feed Entry
         * @see org.ow2.chameleon.syndication.FeedEntry#url(java.lang.String)
         */
        public FeedEntry url(String t) {
            m_url = t;
            return this;
        }

    }

}
