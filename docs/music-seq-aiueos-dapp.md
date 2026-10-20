# app-music（曲順・capability）— itonami（dapp）上の分散型設計

状態: 第 1 スライス実装（2026-10-20）。正本はこの文書。
追跡: Linear JUN-128（親 JUN-116）。

## 位置づけ

app-music はトラックライブラリ。**2 つの capability を双方向に分ける**:
`media/library`（列挙）と `audio/playback`（出力装置）。「再生だけの
プレイヤーにライブラリの列挙をさせず、再生しないライブラリブラウザに
オーディオ装置を握らせない」。

## このスライス: 曲順・capability 判定コア

| 層 | 実体 | 状態 |
|---|---|---|
| 判定コア guest | `aiueos/music_seq.cljk` — sequence-key（**1000 x disc + track**、disc 既定 1、track 無しは不明）/ artist-code（**album-artist > Various Artists（コンピ）> artist**）/ grant-judgment（**双方向の over/under 付与拒否**）/ id-is-path（**id は path**） | **実装済み・実測** |
| 記述子 | `aiueos/music-seq.edn` | **実装済み** |
| parity | `test/app_music/seq_parity_test.cljk` — **実物の model.cljk**（mokuroku+amu+kotoba-sema クラスパス）と一致（sortable-artist / sequence-key / capability 分割 / commands） | **実装済み・実測** |

実測: `amu test aiueos/music_seq.cljk` → **18/18**（jvm-kir / js / wasm）。
parity → 3 tests / 12 assertions / 0 failures。

## 正本の正確な規則（guest はこれに追従 —— 明文の設計意図つき）

- **sequence-key = (+ (* 1000 (or disc 1)) track)**: 「トラック番号だけで
  ソートすると disc 2 track 1 が disc 1 track 1 と交互に並ぶ。disc を
  掛けるとディスクが順を保ち、共有カーネルに第 2 のソート軸を教える必要が
  無い」。track が無ければ nil（不明）。
- **sortable-artist**: 「トラックの artist でソートすると 1 枚のアルバムが
  ライブラリ全体に散らばる — 最も報告の多い音楽ライブラリのバグ」。
  album-artist 優先 → コンピレーションは "Various Artists" → artist。
- **id は path**: 「ソートし直しを生き残り、play 提案が名指しすべきもの」。

## 実測メモ

- `sortable-artist` は全フィールド空で **nil**（空文字ではない）——
  parity の assert を nil? 形に実測で訂正。

## 未実装境界

- play 提案（audio/playback の実体）と出力装置の確保。
- duration / year 等の表示整形。album / source の UI 面判定。
- mon（DNA）化、aiueos session 載せ（fleet 共通）。
