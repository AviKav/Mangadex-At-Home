# Mangadex@Home

## To Build

### Requirements

- The Java(TM) SE JDK, version 8 or greater
- ~500MB of free space

### Instructions

- Run `./gradlew build` in order to build the entire project
- Find the generated jars in `build/libs`, where the `-all` jar is fat-jar with all dependencies

## Features

### V1.0
- [X] **implement [API calls](https://gitlab.com/mangadex/mangadex_at_home/-/wikis/backend)**
- [X] HTTPS
- [X] cache eviction (on max size)
- [X] cert refresh (when sent by API)
- [X] async server (for users and upstream)
- [X] streaming response (for users, upstream and in between)
- [X] logging files
- [X] config file
- [X] license GPL V3 (c) Mangadex.org
### V2.0
- [X] bandwidth limit
- [X] egress limit
- [X] max connections limit
- [X] graceful shutdown (finish in-flight requests)
- [X] cache encryption (encryption key = cache key)
### Stashed 
- [ ] IPv6
- [ ] HTTP/2 and HTTP/3