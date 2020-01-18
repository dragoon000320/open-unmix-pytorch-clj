(ns open-unmix-pytorch-clj.io
  "
  Encapsulates functionality for I/O
  "
  (:require [open-unmix-pytorch-clj.model :refer :all]
            [open-unmix-pytorch-clj.validator :refer :all]
            [libpython-clj.require :refer [require-python]]
            [libpython-clj.python :refer [$.]]
            [clojure.zip :as zip]))

(require-python '[soundfile :as sf])

(defn mkdir-tree
  "
  Creates directory tree

  tree: [str [str [str ...]]
    Tree of directories

  ->
  tree: [str [str [str ...]]
    Tree of directories
  "
  [tree]
  (loop [loc (zip/vector-zip tree)]
    (if (zip/end? loc)
      (zip/root loc)
      (recur (zip/next
              (do (if (not (vector? (zip/node loc)))
                    (let [path (apply str (->> loc
                                               zip/path
                                               (map first)
                                               butlast
                                               (interpose "/")))
                          name (zip/node loc)]
                      (if (empty? path)
                        (.mkdir (java.io.File. name))
                        (.mkdir (java.io.File. (str path "/" name))))))
                  loc))))))

(defn write-estimates
  "
  Writes estimates from separation result to files

  dir : string
    directory in which results will be written

  separation-res : SeparationResult

  ->
  nil
  "
  [separation-res out-dir]
  {:pre [(SeparationResult? separation-res)]}
  (let [separated (:separated separation-res)
        sample-rate (:sample-rate separation-res)
        path (apply str (->> ["resources" [out-dir]]
                             mkdir-tree
                             flatten
                             (interpose "/")))]
    (doseq [[target estimate] separated]
      (sf/write (str path "/" (name target) ".wav")
                estimate
                sample-rate))))

(defn soundfile-read
  "
  Reads the audio file and returns AudioMap

  file-path : string
    relative path to the audio file

  ->
  AudioMap
  "
  [file-path]
  (let [[audio sample-rate] (sf/read file-path :always_2d true)
        shape ($. audio shape)]
    (->AudioMap audio sample-rate (first shape) (last shape))))
