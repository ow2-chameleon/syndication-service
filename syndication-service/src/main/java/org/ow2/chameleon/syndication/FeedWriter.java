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

/**
 * Service defining methods to populate and manipulate a specific feed. The
 * format used by the feed depends on the implementation as well as the storage
 * support and the remote accessibility. This interface extends
 * {@link FeedReader} and inherits the definition of feed reader. So, the
 * service providers must published the following service properties:
 * <ul>
 * <li><code>org.ow2.chameleon.syndication.feed.title</code>: the feed title</li>
 * <li><code>org.ow2.chameleon.syndication.feed.url</code>: the feed url
 * (String)</li>
 * </ul>
 * Providers must also post an event when a new entry is added to the feed.
 * @version 1.0.0
 * @author <a href="mailto:chameleon-dev@ow2.org">Chameleon Project Team</a>
 */
public interface FeedWriter extends FeedReader {

    /**
     * Adds an entry to the feed. This entry will be added as the latest one.
     * @param entry the entry
     * @throws exception if the entry is incomplete or the entry cannot be
     *         added.
     */
    public void addEntry(FeedEntry entry) throws Exception;

    /**
     * Removes an entry.
     * @param entry the entry to remove.
     */
    public void removeEntry(FeedEntry entry);

    /**
     * Removes an entry.
     * @param title the title of the entry.
     */
    public void removeEntryByTitle(String title);

    /**
     * Factory methods to create a new feed entry.
     * @return a new feed entry.
     */
    public FeedEntry createFeedEntry();

}
