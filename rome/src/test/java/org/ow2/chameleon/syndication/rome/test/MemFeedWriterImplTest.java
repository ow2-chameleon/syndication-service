/*
 * Copyright 2009 OW2 Chameleon
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ow2.chameleon.syndication.rome.test;

import junit.framework.Assert;

import org.junit.Test;
import org.ow2.chameleon.syndication.FeedEntry;
import org.ow2.chameleon.syndication.FeedWriter;
import org.ow2.chameleon.syndication.rome.MemoryFeedWriter;


public class MemFeedWriterImplTest {

    @Test
    public void createNewRSSFeed() throws Exception {
        FeedWriter writer = new MemoryFeedWriter("a feed", "rss_2.0", 20);
        writer.addEntry(writer.createFeedEntry().title("a title").author("me")
                .content("the content"));
        writer.addEntry(writer.createFeedEntry().title("a title 2")
                .author("me").content("the content"));

        Assert.assertEquals("mem://" + "a feed", writer.getURL());

        Assert.assertEquals("a feed", writer.getTitle());

        Assert.assertEquals(2, writer.getEntries().size());
        Assert.assertEquals(2, writer.getRecentEntries().size());
        FeedEntry entry = writer.getLastEntry();
        Assert.assertEquals("a title 2", entry.title());
        Assert.assertEquals("me", entry.author());
        Assert.assertEquals("the content", entry.content());
        Assert.assertNotNull(entry.publicationDate());
    }

    @Test
    public void createNewAtomFeed() throws Exception {
        FeedWriter writer = new MemoryFeedWriter("a feed", "atom_0.3", 20);
        writer.addEntry(writer.createFeedEntry().title("a title").author("me")
                .content("the content"));
        writer.addEntry(writer.createFeedEntry().title("a title 2")
                .author("me").content("the content"));

        Assert.assertEquals("mem://" + "a feed", writer.getURL());

        Assert.assertEquals("a feed", writer.getTitle());

        Assert.assertEquals(2, writer.getEntries().size());
        Assert.assertEquals(2, writer.getRecentEntries().size());
        FeedEntry entry = writer.getLastEntry();
        Assert.assertEquals("a title 2", entry.title());
        Assert.assertEquals("me", entry.author());
        Assert.assertEquals("the content", entry.content());
        Assert.assertNotNull(entry.publicationDate());
    }

    @Test
    public void createNewFeed() throws Exception {
        FeedWriter writer = new MemoryFeedWriter("a feed", null, 20);
        writer.addEntry(writer.createFeedEntry().title("a title").author("me")
                .content("the content"));
        writer.addEntry(writer.createFeedEntry().title("a title 2")
                .author("me").content("the content"));

        Assert.assertEquals("mem://" + "a feed", writer.getURL());

        Assert.assertEquals("a feed", writer.getTitle());

        Assert.assertEquals(2, writer.getEntries().size());
        Assert.assertEquals(2, writer.getRecentEntries().size());
        FeedEntry entry = writer.getLastEntry();
        Assert.assertEquals("a title 2", entry.title());
        Assert.assertEquals("me", entry.author());
        Assert.assertEquals("the content", entry.content());
        Assert.assertNotNull(entry.publicationDate());
    }

}
