(ns app-music.model
  "Music's domain: a track library.

  Two capabilities. `media/library` reads what is in the library — titles,
  artists, durations, file locations. `audio/playback` drives the output
  device. Separated because a player that only plays should not be able to
  enumerate someone's library, and a library browser that never plays should
  not hold the audio device."
  (:require [kotoba.lang.text :as str]
            [mokuroku.item :as item]
            [mokuroku.source :as source]))

(def library-capability "media/library")
(def playback-capability "audio/playback")

(def columns
  [(source/attribute :title "Title" :string)
   (source/attribute :artist "Artist" :string)
   (source/attribute :duration "Time" :duration)
   (source/attribute :album "Album" :string)
   (source/attribute :year "Year" :number)
   (source/attribute :sequence "Track" :number)
   (source/attribute :size "Size" :bytes)
   (source/attribute :path "Location" :string false)])

(def commands
  #{:play :open :copy-path :export})

(defn descriptor
  ([] (descriptor "Library"))
  ([scope]
   (source/descriptor
    {:id :app-music/library
     :item-kind :track
     :label scope
     :capability library-capability
     :commands commands
     :attributes columns})))

(defn sortable-artist
  "The name an album files under.

  A compilation's tracks each have their own artist, but the album belongs
  under its album-artist — sorting by track artist scatters one album across
  the whole library, which is the single most-reported music-library bug."
  [{:keys [album-artist artist compilation?]}]
  (cond
    (not (str/blank? (str album-artist))) album-artist
    compilation? "Various Artists"
    :else artist))

(defn sequence-key
  "Disc and track number as one sortable integer.

  Sorting an album by track number alone interleaves disc 2 track 1 with disc
  1 track 1. Multiplying the disc through keeps the discs in order without
  needing a second sort level the shared kernel would have to be told about."
  [{:keys [disc track]}]
  (when (number? track)
    (+ (* 1000 (or disc 1)) track)))

(defn entry->item
  "Normalise one provider row. The id is the path: it is what survives a
  re-sort and what a play proposal must name."
  [{:keys [path title artist album year duration size] :as entry}]
  (item/item path
             :track
             (or title path)
             {:title (or title path)
              :artist (sortable-artist entry)
              :track-artist artist
              :album album
              :year year
              :duration duration
              :sequence (sequence-key entry)
              :size size
              :path path}))

(defn listing->items [entries]
  (mapv entry->item entries))

(def album-order
  "How an album reads: by artist, then album, then disc/track.

  Not alphabetical by title — an album played in title order is not the album."
  [[:artist :asc] [:album :asc] [:sequence :asc]])

(def default-query
  {:query/sort album-order :query/text "" :query/filters []})

(defn total-time
  "Sum of the durations actually known.

  Tracks with no duration are excluded rather than counted as zero, and the
  count of those is returned so a view can say the total is a lower bound
  instead of stating it as fact."
  [items]
  (let [known (keep #(item/attr % :duration) items)]
    {:time/seconds (reduce + 0 known)
     :time/known (count known)
     :time/unknown (- (count items) (count known))}))
