(ns open-unmix-pytorch-clj.generators-test
  "Encapsulates data generators utilized by tests"
  (:require [open-unmix-pytorch-clj.model :refer :all]
            [libpython-clj.require :refer [require-python]]
            [clojure.test.check.generators :as gen]
            [clojure.test.check.clojure-test :refer :all]))

(require-python '[numpy :as np])

(def nat>0-gen
  "Generator of natural numbers not including 0"
  (gen/such-that (partial < 0) gen/nat))

(def targets-gen
  "Generator of possible separation targets"
  (let [targets ["vocals" "drums" "bass" "other"]]
    (gen/vector-distinct (gen/elements targets) {:min-elements 1 :max-elements 4})))

(defn audio-data-gen
  "
  Creates generator of audio data - numpy.ndarray of shape = (samples, channels)

  samples : int
    Number of samples in audio data

  channels : int
    Number of channels in audio data
  "
  [samples channels]
  (let [buffer-size (* samples channels)]
    (gen/bind (gen/vector (gen/double* {:infinite? false
                                        :NaN? false
                                        :min -1.0
                                        :max 1.0}) buffer-size)
              (fn [buffer]
                (gen/return (np/ndarray :shape [samples channels]
                                        :buffer (np/array buffer)))))))

(defn audio-data-var-gen
  "Creates generator of audio data with variable num of samples and channels"
  [min-samples max-samples min-channels max-channels]
  (gen/bind (gen/tuple (gen/choose min-samples max-samples)
                       (gen/choose min-channels max-channels))
            (fn [[samples channels]]
              (audio-data-gen samples channels))))

(defn audio-map-gen
  "
  Creates generator of AudioMap instances

  min-samples : int
    Minimal size of samples in audio data

  max-samples : int
    Maximum size of samples in audio data

  min-channels : int
    Minimum size of channels in audio data

  max-channels : int
    Maximum size of channels in audio data
  "
  [min-samples max-samples min-channels max-channels]
  (gen/bind (gen/tuple (gen/choose min-samples max-samples)
                       (gen/choose min-channels max-channels))
            (fn [[samples channels]]
              (gen/fmap (partial apply ->AudioMap)
                        (gen/tuple (audio-data-gen samples channels)
                                   nat>0-gen
                                   (gen/return samples)
                                   (gen/return channels))))))
