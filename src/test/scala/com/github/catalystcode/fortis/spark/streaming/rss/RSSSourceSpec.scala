package com.github.catalystcode.fortis.spark.streaming.rss

import java.util
import java.util.Date

import com.rometools.rome.feed.synd.{SyndEntry, SyndFeed}
import org.mockito.Mockito
import org.mockito.internal.verification.Times
import org.scalatest.{BeforeAndAfter, FlatSpec}

import scala.collection.mutable

class RSSSourceSpec extends FlatSpec with BeforeAndAfter {

  it should "reset ingest dates to MinValue" in {
    val url = "http://bing.com"
    val source = new RSSSource(Seq(url), Map[String, String]())

    assert(source.lastIngestedDates == mutable.Map[String, Long]())
    source.reset()
    assert(source.lastIngestedDates == mutable.Map[String, Long](
      url -> Long.MinValue
    ))
  }

  it should "return a single entry for rss feed" in {
    val url = "http://bing.com"
    val source = new RSSSource(Seq(url), Map[String, String]())
    val sourceSpy = Mockito.spy(source)

    val feed = Mockito.mock(classOf[SyndFeed])
    val entry = Mockito.mock(classOf[SyndEntry])
    val publishedDate = new Date

    Mockito.when(entry.getPublishedDate).thenReturn(publishedDate)
    Mockito.when(entry.getUpdatedDate).thenReturn(new Date(0))
    Mockito.when(feed.getEntries).thenReturn(util.Arrays.asList(entry))
    Mockito.doReturn(Seq(Some((url, feed))), null).when(sourceSpy).fetchFeeds()

    assert(source.lastIngestedDates.get(url).isEmpty)
    val entries0 = sourceSpy.fetchEntries()
    assert(Math.abs(source.lastIngestedDates(url) - publishedDate.getTime) < 1000)
    assert(entries0 == Seq(
      RSSEntry(
        RSSFeed(null,"http://bing.com",null,null,null),
        null,
        null,
        List(),
        List(),
        null,
        List(),
        publishedDate.getTime,
        0,
        List(),
        List()
      )
    ))
    Mockito.verify(sourceSpy, new Times(1)).fetchFeeds()

    Mockito.when(feed.getEntries).thenReturn(util.Arrays.asList(entry))
    Mockito.doReturn(Seq(Some((url, feed))), null).when(sourceSpy).fetchFeeds()
    val entries1 = sourceSpy.fetchEntries()
    assert(entries1 == Seq())
    Mockito.verify(sourceSpy, new Times(2)).fetchFeeds()
  }

  it should "store published date of newest entry" in {
    val feed = Mockito.mock(classOf[SyndFeed])
    val entry0 = Mockito.mock(classOf[SyndEntry])
    val publishedDate0 = new Date
    Mockito.when(entry0.getPublishedDate).thenReturn(publishedDate0)
    Mockito.when(entry0.getUpdatedDate).thenReturn(new Date(0))

    val entry1 = Mockito.mock(classOf[SyndEntry])
    val publishedDate1 = new Date(publishedDate0.getTime - 100)
    Mockito.when(entry1.getPublishedDate).thenReturn(publishedDate1)
    Mockito.when(entry1.getUpdatedDate).thenReturn(new Date(0))

    Mockito.when(feed.getEntries).thenReturn(util.Arrays.asList(
      entry0,
      entry1
    ))
  }
}
