(ns app-music.page
  (:require [app-music.model :as model]
            [mokuroku.catalog :as catalog]
            [mokuroku-ui.core :as mui]))

(def view-opts
  {:columns [:title :artist :duration]
   :formatters {:duration mui/human-duration
                :size mui/human-bytes
                :year str}
   :noun "tracks"
   :search-placeholder "Search library"
   :empty-title "No tracks"
   :empty-body "Nothing in this library matches the current filter."
   ;; A track whose artist differs from the album's is a compilation entry.
   ;; Showing it keeps the album readable without scattering it across the
   ;; library, which is what sorting by track artist would do.
   :badge (fn [it]
            (let [a (:artist (:item/attrs it))
                  t (:track-artist (:item/attrs it))]
              (when (and t (not= a t)) t)))
   :title "Music"
   :description "A track library, in album order."})

(defn render [cat] (mui/->page (catalog/view cat) view-opts))
(defn render-html [cat] (mui/->html (catalog/view cat) view-opts))

(defn total-time [cat]
  (model/total-time (:result/items (catalog/result cat))))
