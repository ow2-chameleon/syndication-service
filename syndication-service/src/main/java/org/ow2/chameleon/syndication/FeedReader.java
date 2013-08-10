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
package org.ow2.chameleon.syndication;

import java.util.Date;
import java.util.List;

/**
 * Service defining methods to read a syndicated feed. This service exposed
 * methods to read one specific feed. Providers must also publish the following
 * properties:
 * <ul>
 * <li><code>org.ow2.chameleon.syndication.feed.title</code>: the feed title</li>
 * <li><code>org.ow2.chameleon.syndication.feed.url</code>: the feed url
 * (String)</li>
 * </ul>
 * Providers may also polled the feed for new entries periodically. If new
 * entries are found, the providers must post en event (using the event admin)
 * to the topic: <code>org/ow2/chameleon/syndication</code>. The event must
 * contains the following properties
 * <ul>
 * <li><code>feed.url</code>: the feed url (String)</li>
 * <li><code>feed.title</code>: the feed title</li>
 * <li><code>entry.title</code>: the entry title</li>
 * <li><code>entry.url</code>: the entry url (String) [if exists]</li>
 * <li><code>entry.content</code>: the entry content</li>
 * <li><code>entry.date</code>: the entry publication ({@link Date} object)</li>
 * <li><code>entry.categories</code>: the entry categories [if exists]</li>
 * <li><code>entry.author</code>: the entry author</li>
 * </ul>
 * @version 1.0.0
 * @author <a href="mailto:chameleon-dev@ow2.org">Chameleon Project Team</a>
 */
public interface FeedReader {

    /**
     * Feed Title Service Property.
     */
    public static final String FEED_TITLE_PROPERTY = "org.ow2.chameleon.syndication.feed.title";

    /**
     * Feed URL Service Property.
     */
    public static final String FEED_URL_PROPERTY = "org.ow2.chameleon.syndication.feed.url";

    /**
     * Event Admin topic to post new entry event.
     */
    public static final String NEW_ENTRY_TOPIC = "org/ow2/chameleon/syndication";

    /**
     * Feed URL event key.
     */
    public static final String FEED_URL_KEY = "feed.url";

    /**
     * Feed title event key.
     */
    public static final String FEED_TITLE_KEY = "feed.title";

    /**
     * Entry title event key.
     */
    public static final String ENTRY_TITLE_KEY = "entry.title";

    /**
     * Entry url event key.
     */
    public static final String ENTRY_URL_KEY = "entry.url";

    /**
     * Entry content event key.
     */
    public static final String ENTRY_CONTENT_KEY = "entry.content";

    /**
     * Entry date event key.
     */
    public static final String ENTRY_DATE_KEY = "entry.date";

    /**
     * Entry categories event key.
     */
    public static final String ENTRY_CATEGORIES_KEY = "entry.categories";

    /**
     * Entry author event key.
     */
    public static final String ENTRY_AUTHOR_KEY = "entry.author";

    /**
     * Gets the feed entries.
     * @return a copy of entries of the feed or an empty list if the feed has no
     *         entry.
     */
    public List<FeedEntry> getEntries();

    /**
     * Gets the recent entries. The number of entries returned by this method
     * depends on the implementation. It may be configurable.
     * @return a copy of the recent entries of the feed or an empty list if the
     *         feed has no entry.
     */
    public List<FeedEntry> getRecentEntries();

    /**
     * Gets the last entry (more recent).
     * @return the copy of last entry of <code>null<code>
     * if the feed has no entry.
     */
    public FeedEntry getLastEntry();

    /**
     * Gets the feed url.
     * @return the feed url
     */
    public String getURL();

    /**
     * gets the feed title.
     * @return the feed title
     */
    public String getTitle();

}
