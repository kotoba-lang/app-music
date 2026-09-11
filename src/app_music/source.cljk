(ns app-music.source
  "The `media/library` seam. Nothing here decodes audio or touches a device."
  (:require [app-music.model :as model]
            [mokuroku.source :as source]))

(defrecord LibrarySource [scope read-fn]
  source/ISource
  (-descriptor [_] (model/descriptor scope))
  (-fetch [_] (model/listing->items (read-fn scope))))

(defn library-source [scope read-fn] (->LibrarySource scope read-fn))
(defn fixture-source [scope entries] (library-source scope (constantly entries)))

(def denied
  {:music/state :denied
   :music/capability model/library-capability
   :music/entries []})

(defn granted [entries]
  {:music/state :granted
   :music/capability model/library-capability
   :music/entries (vec entries)})

(defn denied? [r] (= :denied (:music/state r)))

(def playback-denied
  "The library is readable but the output device was not granted.

  A distinct state: every track is visible and none can be played, and the
  fix is a different grant from the one that made the list appear."
  {:music/state :playback-denied
   :music/capability model/playback-capability})

(defn playback-denied? [r] (= :playback-denied (:music/state r)))
