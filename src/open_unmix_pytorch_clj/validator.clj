(ns open-unmix-pytorch-clj.validator
  "
  Encapsulates common validators used by other namespaces
  "
  (:require [libpython-clj.require :refer [require-python]]
            [libpython-clj.python :refer [is-instance?]]))

(require-python '[numpy :as np])

(defn is-ndarray?
  "
  Checks whether an input is of type numpy.ndarray
  "
  [input]
  (is-instance? input np/ndarray))

(defn int>0?
  "
  Checks whether an input is int and >0
  "
  [input]
  (and (int? input) (> input 0)))

(defn every-str?
  "
  Checks whether an input is (vector/list) of strings
  "
  [input]
  (every? string? input))
