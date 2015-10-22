#!/usr/bin/env python
# -*- coding: utf-8 -*-
# vim:

# ref : https://github.com/invernizzi/js-crawler/blob/master/crawler.py

import spynner
import pyquery
import pybloom
import Queue
import tempfile
import shutil
from lxml.html import document_fromstring
import re
import json
import os.path
import os, errno
from random import shuffle
import sys

class Crawler(object):
    ''' A single-process crawler, that uses Webkit and can execute Javascript.
        It selects which links to follow with a user-provided filter.
        It won't revisit the same page twice.
    # How to use:
    crawler = Crawler(gui=True,
                      is_link_interesting=lambda url, text: 'download' in url)
    crawler.crawl('http://firefox.com')
    crawler.close()
    '''


    def __init__(self, is_link_interesting, getlinks=True, crawlLocal=False, gui=False, timeout=5, **browser_kwargs):
        '''
        is_link_interesting(a_href, a_text): a function that looks at a link
                                             text and target url, and returns
                                             True if the crawler should follow
                                             the link
        gui: True if you want to see the crawler
        timeout: How much to wait for the url to be loaded and JS to execute
        browser_kwargs: these are passed directly to the spynner module
        '''
        mkdir_p("data/")
        mkdir_p("fullhtml/")
        self.crawlLocal = crawlLocal
        if self.crawlLocal:
            for root, directories, filenames in os.walk('fullhtml/'):
                for filename in filenames: 
                    if '.DS_Store' in filename:
                        continue
                    path = os.path.join(root,filename)
                    url = path.split('fullhtml')[1]
                    url = 'http://www.cbc.ca/news' + url
                    url = url[0:len(url)-5]
                    print url
                    f = open(path)
                    html = f.read()
                    f.close()
                    tree = document_fromstring(html)
                    process_article(url, tree)
            sys.exit(0)
        self.timeout = timeout
        self.is_link_interesting = is_link_interesting
        self.getlinks = getlinks
        # Setup the browser
        self.download_dir_tmp = tempfile.mkdtemp(prefix='crawler_')
        browser_config = {'debug_level': spynner.WARNING,
                          'download_directory': self.download_dir_tmp,
                          'user_agent':'Mozilla/5.0 (compatible; MSIE 9.0;'
                                       ' Windows NT 6.1; Trident/5.0)'}
        browser_config.update(browser_kwargs)
        self.browser = spynner.browser.Browser(**browser_kwargs)
        self.browser.set_html_parser(pyquery.PyQuery)
        if gui:
            self.browser.create_webview()
            self.browser.show()
        # Create the bloom filter
        self.bloom_filter = pybloom.ScalableBloomFilter()
        if os.path.isfile("filter"):
            f = open("filter")
            self.bloom_filter = pybloom.ScalableBloomFilter.fromfile(f)
            f.close()
        # Create the queue
        self.queue = Queue.Queue()
        if os.path.isfile("queue"):
            f = open("queue")
            elems = json.load(f)
            shuffle(elems)
            for elem in elems:
                print "Loaded: " + elem[0]
                if is_link_interesting(elem[0], '') and str(elem) not in self.bloom_filter:
                    self.queue.put(elem)
            f.close()

    def _visit_url(self, url):
        ''' Visits a url, and processes its links (if they are new) '''
        url = preprocess_link(url)
        print "Visiting %s" % url
        try:
            self.browser.load(url)
            self.browser.wait_load(self.timeout)
            self.browser.wait(self.timeout/2)
        except spynner.SpynnerTimeout:
            print "Timed out while waiting for the page to complete execution. That's ok."
            self.browser.wait_a_little(self.timeout)  # to force the wait, while
                                                      # JS events happen.
        tree = get_tree(self.browser)
        if is_news_article(tree):
            print("Found article")
            if not process_article(url, tree):
                # num shares were 0. so lets add this url back to queue just in case
                self._enqueue_visit(url)
            write_html(self.browser, url)
        if self.getlinks:
            for a in self.browser.soup('a'):
                a.make_links_absolute(base_url=self.browser.url)
                if 'href' in a.attrib:
                    link = a.attrib['href']
                    link = preprocess_link(link)
                    if link not in self.bloom_filter:
                        self.bloom_filter.add(link)
                        f = open("filter", "w")
                        self.bloom_filter.tofile(f)
                        f.close()
                        if self.is_link_interesting(link, a.text_content()):
                            print "Found interesting: %s" % link
                            self._enqueue_visit(link)
                        else:
                            print "Not   interesting: %s" % link
           #     else:
           #        print "In filter: %s" % link

    def _enqueue_visit(self, *args):
        ''' Remembers to visit a url later '''
        self.queue.put(args)
        elems = []
        for elem in list(self.queue.queue):
            elems.append(elem)
        f = open("queue", "w")
        json.dump(elems, f)
        f.close()

    def crawl(self, url):
        ''' Starts the crawl from the seed url '''
        if self.queue.empty:
            self._visit_url(url)
        while True:
            try:
                args = self.queue.get_nowait()
            except Queue.Empty:
                break
            self._visit_url(*args)
            self.queue.task_done()

    def close(self):
        ''' Cleanup '''
        shutil.rmtree(self.download_dir_tmp)

# https://github.com/makinacorpus/spynner/blob/master/examples/anothergoogle.py
def get_tree(h):
    if isinstance(h, file):
        h = h.read()
    if isinstance(h, spynner.browser.Browser):
        h = h.html
    if not isinstance(h, basestring):
        h = h.contents
    return document_fromstring(h)

def mkdir_p(path):
    try:
        os.makedirs(path)
    except OSError as exc: # Python >2.5
        if exc.errno == errno.EEXIST and os.path.isdir(path):
            pass
        else: raise

def write_html(b, u):
    usplit = u.split("http://www.cbc.ca/news/")
    if len(usplit) > 1:
        filename = "fullhtml/" + usplit[1] + ".html"
        dirs = filename[0:filename.rfind('/')]
        mkdir_p(dirs)
        f = open(filename, "w")
        f.write(b.html.encode('latin-1'))
        f.close()

def preprocess_link(url):
    if '#' in url:
        url = url.split('#')[0]
    if '?' in url:
        url = url.split('?')[0]
    return url

def is_link_interesting(url, text):
#    print(url)
    url = make_plain(url)
    url = str(url)
 #   print "New link? : " + url
    if url == None or url[0] == '#':
 #       print "nourl"
        return False
    if url[0] == '/':
        url = 'http://www.cbc.ca' + url
    if 'http://www.cbc.ca/news' not in str(url):
 #       print "no cbc"
        return False
    if '/radio' in url or '/archives' in url or '/player' in url or '/sports' in url or '/accessibility' in url or '/doczone' in url or '/cgi-bin' in url or '/newsblogs' in url or '/books' in url or '/metromorning' in url or '/news2' in url or '/television' in url or '/beta' in url or '/eyeopener' in url or '/episode' in url or '/liveradio' in url or '/weather' in url or '/contact' in url or '/includes' in url or '/photos' in url or '/punchline' in url or '/parents' in url or '/kids' in url or '/connects' in url or '/mediacentre' in url or '/kidscbc2' in url or '/kidsscores' in url or '/contests' in url or '/laughoutloud' in url or '/interactives' in url:
  #      print "disallow"
        return False
    if '#' in url:
  #      print "a #"
        return False
    if 'http://www.cbc.ca/' in url and len(url) > 22 and url[21].isdigit():
  #      print url + " ...is digit"
        return False
    return True

def is_news_article(tree):
    return len(tree.xpath('//*[@class="story-title"]')) > 0
    
def make_plain(text):
    text = text.encode('ascii', 'ignore').decode('ascii')
    return text
    
def process_article(url, tree):
    # ref : http://manual.calibre-ebook.com/xpath.html
    tmp = tree.xpath('//*[contains(@class, "level1")]')
    category = ""
    if (len(tmp) > 0):
        category = tmp[0][0].text
    else:
        tmp = tree.xpath('//*[@class="logo"]')
        if (len(tmp) > 0):
            category = tmp[0][0].text
        else:
            category = tree.xpath('//*[@id="zone"]/h2/span')[0].text
    tmp = tree.xpath('//*[@class="level2"]')
    sub_category = ""
    if (len(tmp) > 0):
        sub_category = tmp[0][0].text
    tmp = tree.xpath('//*[contains(@class, "story-flag")]')
    story_flag = ""
    if (len(tmp) > 0):
        story_flag = tmp[0].text
    title = tree.xpath('//*[@class="story-title"]')[0].text
    title = make_plain(title)
    num_words_title = len(str(title).split(' '))
    desc = tree.xpath('//*[@class="story-deck"]')[0].text
    author = tree.xpath('//*[@class="spaced"]')[0].text
    if author is not None and 'By' in author:
        author = str(make_plain(author).replace(u'\xa0', ' '))
        print author
        author = author.split('By')[1]
        author = author[0:len(author)-2]
    else:
        author = 'None'
    posted_date = str(tree.xpath('//*[@class="delimited"]')[0].text).split(': ')[1]
    num_sub_headlines = len(tree.xpath('//*[@class="story-content"]/h2'))
    num_paragraphs = len(tree.xpath('//*[@class="story-content"]/*/p')) + len(tree.xpath('//*[@class="story-content"]/p'))
    # note : num_paragraphs not completely accurate since some <p> tags were empty (in one case, 4 of 22 were empty)
    #num_images = len(tree.xpath('//*[@class="images"]'))
    num_inline_figures = len(tree.xpath('//*[@class="figure-caption"]'))
    num_videos = len(tree.xpath('//*[@class="storymedia"]//*[@class="videolink-thumbnail"]'))
    tmp = tree.xpath('//*[@id="totalshares"]')
    num_shares = 0
    if (len(tmp) > 0 and 'shares' in str(tmp[0].text)):
        num_shares = int(str(tmp[0].text).split(' ')[0])
    else:
        num_shares = "n/a"
    num_comments = 0
    if (len(tree.xpath('//*[@id="commentwrapper"]')) > 0): # some pages do not have comment sections
        tmp = tree.xpath('//*[@class="vf-total-comments"]')
        if (len(tmp) > 0):
            num_comments = int(tmp[0].text)
    else:
        num_comments = "n/a"
    num_inline_links = len(tree.xpath('//*[@class="story-content"]//*/a'))
    crawler_version = 3
    
    article = [url, category, sub_category, story_flag, title, num_words_title, desc, author, posted_date, num_sub_headlines, num_paragraphs, num_inline_figures, num_videos, num_shares, num_comments, crawler_version, num_inline_links]

    print(article)
    
    filename = title
    filename = filename.replace(' ', '')
    regex = re.compile('[^a-zA-Z]')
    filename = regex.sub('', filename)
    filename = "data/" + filename
    f = open(filename, "w")
    json.dump(article, f)
    f.close()
    
    if (num_shares == 0):
        return False
    return True

# This is how you start the crawler
if __name__ == "__main__":
    crawler = Crawler(is_link_interesting=is_link_interesting, getlinks=True, crawlLocal=False)
    crawler.crawl('http://www.cbc.ca/news/')
    crawler.close()
