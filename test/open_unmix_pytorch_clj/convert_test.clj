(ns open-unmix-pytorch-clj.convert-test
  (:require [open-unmix-pytorch-clj.generators-test :as gen-test]
            [open-unmix-pytorch-clj.convert :refer :all]
            [libpython-clj.python :refer [$.]]
            [clojure.test.check.generators :as gen]
            [clojure.test.check.properties :as prop]
            [clojure.test.check.clojure-test :refer :all]))

(defspec ->2-channels-test
  ;; generative test for ->2-channels func
  100
  (prop/for-all [audio-map (gen-test/audio-map-gen 1 500 1 5)]
                (let [audio-map-2-channels (->2-channels audio-map)]
                  (and (= (last ($. (:audio-data
                                        audio-map-2-channels)
                                       shape)) 2)
                       (= (:channels audio-map-2-channels) 2)))
                ))

(defspec istft-test
  ;; generative test for istft func
  100
  (prop/for-all [X (gen-test/audio-data-var-gen 1 500 1 1)
                 sample-rate gen-test/nat>0-gen
                 [n-fft n-hopsize] (gen/bind (gen/choose 0 1024)
                                                (fn [hopsize]
                                                  (gen/tuple (gen/choose hopsize 4096)
                                                             (gen/return hopsize))))]
                (istft ($. X T) :sample-rate sample-rate :n-fft n-fft :n-hopsize n-hopsize)))
