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