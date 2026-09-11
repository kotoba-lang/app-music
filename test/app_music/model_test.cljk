(ns app-music.model-test
  (:require [app-music.model :as model]
            [app-music.page :as page]
            [app-music.source :as source]
            [clojure.test :refer [deftest is testing]]
            [design-quality.audit :as dq]
            [mokuroku.catalog :as catalog]
            [mokuroku.item :as item]))

(def kind-of-blue
  [{:path "/m/kob-1" :title "So What" :artist "Miles Davis" :album "Kind of Blue"
    :year 1959 :disc 1 :track 1 :duration 545 :size 13421772}
   {:path "/m/kob-2" :title "Freddie Freeloader" :artist "Miles Davis"
    :album "Kind of Blue" :year 1959 :disc 1 :track 2 :duration 578 :size 14000000}
   {:path "/m/kob-3" :title "Blue in Green" :artist "Miles Davis"
    :album "Kind of Blue" :year 1959 :disc 2 :track 1 :duration 337 :size 8388608}])

(def compilation
  [{:path "/m/v-1" :title "Alpha" :artist "Someone" :album-artist "Various Artists"
    :album "Now 1" :compilation? true :disc 1 :track 1 :duration 200}
   {:path "/m/v-2" :title "Beta" :artist "Another" :album-artist "Various Artists"
    :album "Now 1" :compilation? true :disc 1 :track 2 :duration 210}])

(defn- cat-of [es]
  (catalog/refresh (catalog/catalog (source/fixture-source "Library" es)
                                    model/default-query)))

(deftest an-album-plays-in-album-order
  ;; An album sorted by title is not the album.
  (is (= ["So What" "Freddie Freeloader" "Blue in Green"]
         (mapv :item/label (:result/items (catalog/result (cat-of kind-of-blue)))))
      "disc 1 track 1, disc 1 track 2, then disc 2 track 1"))

(deftest disc-number-multiplies-through-the-track-number
  ;; Sorting by track number alone interleaves disc 2 track 1 with disc 1
  ;; track 1, which silently shuffles every multi-disc album.
  (is (= 1001 (model/sequence-key {:disc 1 :track 1})))
  (is (= 2001 (model/sequence-key {:disc 2 :track 1})))
  (is (< (model/sequence-key {:disc 1 :track 12})
         (model/sequence-key {:disc 2 :track 1})))
  (testing "a missing disc means disc 1"
    (is (= 1005 (model/sequence-key {:track 5}))))
  (testing "a track with no number has no sequence rather than a fake zero"
    (is (nil? (model/sequence-key {:disc 1})))))

(deftest a-compilation-files-under-its-album-artist
  ;; Sorting by track artist scatters one album across the whole library.
  (is (= "Various Artists" (model/sortable-artist (first compilation))))
  (is (= "Miles Davis" (model/sortable-artist (first kind-of-blue))))
  (testing "a compilation with no album-artist still files together"
    (is (= "Various Artists" (model/sortable-artist {:artist "Someone" :compilation? true}))))
  (testing "and the track artist is kept, not discarded"
    (let [it (model/entry->item (first compilation))]
      (is (= "Various Artists" (item/attr it :artist)))
      (is (= "Someone" (item/attr it :track-artist)))))
  (testing "the compilation stays contiguous when sorted"
    (is (= ["Alpha" "Beta"]
           (mapv :item/label (:result/items (catalog/result (cat-of compilation))))))))

(deftest total-time-does-not-count-unknowns-as-zero
  (let [t (model/total-time (model/listing->items
                             (conj kind-of-blue {:path "/m/x" :title "Untimed"})))]
    (is (= 1460 (:time/seconds t)))
    (is (= 3 (:time/known t)))
    (is (= 1 (:time/unknown t))
        "so a view can say the total is a lower bound instead of stating it as fact")))

(deftest the-two-capabilities-are-separate
  ;; A player that only plays should not enumerate the library; a browser
  ;; that never plays should not hold the audio device.
  (is (not= model/library-capability model/playback-capability))
  (is (= "media/library" (:source/capability (model/descriptor))))
  (is (source/playback-denied? source/playback-denied))
  (is (not (source/denied? source/playback-denied))
      "every track visible, none playable — a different grant from the one that made the list appear")
  (is (= "audio/playback" (:music/capability source/playback-denied))))

(deftest play-is-a-proposal-naming-the-library-grant
  (let [c (catalog/select (cat-of kind-of-blue) "/m/kob-1")
        p (catalog/propose c :play)]
    (is (= :audio/play (:proposal/effect p)))
    (is (= ["/m/kob-1"] (:proposal/targets p)))
    (is (false? (:proposal/destructive? p)))
    (testing "playing a multi-selection is a queue, and still asks"
      (let [q (catalog/propose (catalog/select-all c) :play)]
        (is (= 3 (count (:proposal/targets q))))
        (is (true? (:proposal/requires-confirmation? q)))))))

(deftest window-meets-the-design-quality-floor
  (let [pages {"library" (page/render (cat-of kind-of-blue))
               "compilation" (page/render (cat-of compilation))
               "awaiting-grant" (page/render
                                 (catalog/catalog (source/fixture-source "Library" [])
                                                  model/default-query))}
        {:keys [overall pages] :as report} (dq/audit pages {:extra-axes dq/extra-axes})]
    (println "design-quality: aggregate" overall)
    (doseq [[nm r] (sort-by key pages)] (println " " nm (:overall r)))
    (is (>= overall 98.0) (pr-str (:findings report)))
    (doseq [[nm r] pages] (is (>= (:overall r) 98.0) nm))))
