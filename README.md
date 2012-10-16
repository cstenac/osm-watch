osm-watch
=========

Advanced watching of OpenStreetMap changes

Installation
------------

### Pre-requisites:

 * Java SDK
 * Ant
 * Servlet container (Tomcat 7.0 for example)

### Get the code and build


    % git clone https://github.com/cstenac/osm-watch
    % cd osm-watch
    % ant 

The default ant target fetches ivy, fetches the dependencies, compiles the code and creates `dist/osm-watch.war`

### Create the configuration file

Create in the home folder of the user running tomcat a file called `osm-watch.properties`, with the following content:

    baseurl=http://localhost:8080/osm-watch
    dialect=HIBERNATE DIALECT CLASS
    driver=JDBC driver class
    jdbcurl=JDBC connection string

For example, to use an embedded SQLite database (good for development), use:

    dialect=com.applerao.hibernatesqlite.dialect.SQLiteDialect
    driver=org.sqlite.JDBC
    jdbcurl=jdbc:sqlite:/tmp/alert.db

To use PostgreSQL:

    dialect=org.hibernate.dialect.PostgreSQLDialect
    driver=org.postgresql.driver
    jdbcurl=jdbc:postgresql:dbname


"baseurl" must be the URL of your instance. It is used by the OAuth authentication.


### Install and run

* Copy `dist/osm-watch.war` in the webapps/ folder of your Tomcat
* Start it ! `./bin/catalina.sh start`
* Go to http://localhost:8080/osm-watch/

To process the updates, you need to access the following URL: http://localhost:8080/osm-watch/debug/next_augmented_diff

It is recommended to create in the db and index on `changeset(username)`

TODO: explain how to load the previous changesets

Design
------

### Features

#### Watching 

* Watch a bounding-box, get notified for changes that are truly within the bbox, not overlap
* Watch an admin relation, get notified within

* Watch a tag (within a bbox ?), get notified when
   * NWR with the tag is modified
   * Tag is added on a NWR
   * Tag is removed from a NWR
   * NWR with the tag is deleted
   
* Watch an OSM user, get notified whenever he/she makes a commit

  
#### Notifications

Options:
* Get notified only if first matching edit by a given contributor
* Real-time / Scheduled
* Generated RSS feed


### Implementation

In order to provide the "tag is removed from a NWR" feature, it builds on the augmented diffs from overpass: http://overpass-api.de/augmented_diffs/

The OSC files are fetched, and processed:
* The geometry of ways is reconstructed
* The really deleted objects are identified and segregated
* For modified objects, a previous/new version is created
* Aggregated modifications are sent to the matchers

This backend is in Java, and is API only. A frontend TBD exists to set up the alerts


Third party code
----------------

OSM-Watch embeds some code from the JOSM project for the filtering part.
The JOSM code is Copyright 2007 by Immanuel Scholz and others, Copyright 2008 by Petr Nejedly, and is
used according to the terms of the GPL license
