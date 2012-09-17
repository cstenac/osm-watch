osm-watch
=========

Advanced watching of OpenStreetMap changes

Features
--------

### Watching 

* Watch a bounding-box, get notified for changes that are truly within the bbox, not overlap
* Watch an admin relation, get notified within

* Watch a tag (within a bbox ?), get notified when
   * NWR with the tag is modified
   * Tag is added on a NWR
   * Tag is removed from a NWR
   * NWR with the tag is deleted
   
* Watch an OSM user, get notified whenever he/she makes a commit

  
### Notifications

Options:
* Get notified only if first matching edit by a given contributor
* Real-time / Scheduled
* Generated RSS feed


Implementation
--------------

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
