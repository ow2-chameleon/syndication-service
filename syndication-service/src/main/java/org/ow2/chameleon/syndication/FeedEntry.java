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
 * Feed Entry structure. This interface defines the methods to manipulate feed
 * entries. According to the implementation, some methods can be restricted (to
 * avoid modification...) This interface promotes invocation chaining.
 */
public interface FeedEntry {

    /**
     * Gets the entry title.
     * @return the title or <code>null</code> if not set.
     */
    public String title();

    /**
     * Sets the entry title.
     * @param t the title
     * @return the current entry
     */
    public FeedEntry title(String t);

    /**
     * Gets the entry url.
     * @return the entry url or <code>null</code> if not set.
     */
    public String url();

    /**
     * Sets the entry url.
     * @param t the url
     * @return the current entry
     */
    public FeedEntry url(String t);

    /**
     * Gets the entry author.
     * @return the entry author or <code>null</code> if not set.
     */
    public String author();

    /**
     * Sets the entry author.
     * @param t the author
     * @return the current entry
     */
    public FeedEntry author(String t);

    /**
     * Gets the entry publication date. Implementation automatically set this
     * value.
     * @return the entry publication date
     */
    public Date publicationDate();

    /**
     * Gets the entry content.
     * @return the entry content or <code>null</code> if not set.
     */
    public String content();

    /**
     * Sets the entry content.
     * @param t the content
     * @return the current entry
     */
    public FeedEntry content(String t);

    /**
     * Gets the entry categories.
     * @return the entry categories or an empty list if not set.
     */
    public List<String> categories();

    /**
     * Add a category to the entry.
     * @param t the category to add
     * @return the current entry
     */
    public FeedEntry category(String t);

}
