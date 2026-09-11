# app-music

**Music, on [`mokuroku`](https://github.com/kotoba-lang/mokuroku).** A track
library, in album order.

Design: [ADR-2608035000](https://github.com/com-junkawasaki/root/blob/main/90-docs/adr/2608035000-app-standard-application-suite-on-a-shared-catalog-kernel.edn).

## Two capabilities

`media/library` reads what is in the library; `audio/playback` drives the
output device. A player that only plays should not be able to enumerate
someone's library, and a browser that never plays should not hold the audio
device. `source/playback-denied` is the state where every track is visible and
none can be played — a different grant from the one that made the list appear.

## Album order is not alphabetical order

```clojure
model/album-order  ;; => [[:artist :asc] [:album :asc] [:sequence :asc]]
```

**Disc number multiplies through the track number.** Sorting by track number
alone interleaves disc 2 track 1 with disc 1 track 1, silently shuffling every
multi-disc album. `sequence-key` returns `(+ (* 1000 disc) track)`, so the
shared kernel needs no special case.

**A compilation files under its album-artist.** Each track on a compilation has
its own artist; sorting by that scatters one album across the whole library —
the most-reported music-library bug there is. The track artist is kept, not
discarded, and shown as a per-row badge.

## A total that excludes unknowns says so

`total-time` returns `{:time/seconds :time/known :time/unknown}`. Tracks with
no duration are excluded rather than counted as zero, so a view can present
the total as a lower bound instead of stating it as fact.

## Test

```sh
kbb -M:local:test
kbb -M:lint
```

design-quality: 100.00 on library / compilation / awaiting-grant (2026-08-03).
