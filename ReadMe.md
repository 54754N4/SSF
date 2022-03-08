# Selenium Scraping Framework

## Browsers

Allows crawling/scraping through any of the major browsers (dependencies and drivers are downloaded on-the-fly): Firefox, Chrome, Edge, Safari, Opera, Internet Explorer.

## Crawling

Implements generic crawlers that can be extended to retrieve any kind of crawl frontier (e.g FolderCrawler, WebCrawler, PageRankCrawler and multi-threaded counterparts).

## Scraping

Creates an executable graph that allows transforming data the same way ETL frameworks do. `Control Flow` tasks are used to chain execution of tasks, while `Data Flow` tasks handle data transformation and multi-pipelining.