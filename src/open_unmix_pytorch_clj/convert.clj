(ns open-unmix-pytorch-clj.convert
  "
  Encapsulates functionality for conversions
  "
  (:require [libpython-clj.require :refer [require-python]]
            [libpython-clj.python :refer [$a get-item]]
            [open-unmix-pytorch-clj.validator :refer [is-ndarray?]]
            [open-unmix-pytorch-clj.model :refer [AudioMap?]]))

(require-python '[scipy.signal :as sig])
(require-python '[builtins :refer [slice]])

(defn istft
  "
  Perform the inverse Short Time Fourier transform (iSTFT)

  X: numpy.ndarray

  sample-rate = 1.0: float
    Sampling frequency of the time series

  window = 'hann': str
    Desired window to use

  n-fft = 4096: int

  n-hopsize = 1024: int

  ->
  t : ndarray
    Array of output data times.
  x : ndarray
    iSTFT of Zxx.
  "
  [X & {:keys [sample-rate window n-fft n-hopsize]
        :or {sample-rate 44100.0, window "hann"
             n-fft 4096, n-hopsize 1024}}]
  {:pre [(is-ndarray? X)]}
  (let [Zxx ($a X "__truediv__" (/ n-fft 2))
        n-overlap (- n-fft n-hopsize)]
    (sig/istft Zxx
               sample-rate
               window
               n-fft
               n-overlap)))

(defn ->2-channels
  "
  Always returns 2 channel audio:
  - if passed 2 channel audio does nothing
  - if passed >2 channel audio returns only first 2 channels
  - if passed 1 channel audio, then the channel is duplicated

  audio-map : AudioMap

  ->
  AudioMap
  "
  [audio-map]
  {:pre [(AudioMap? audio-map)]}
  (condp = (:channels audio-map)
    2 audio-map
    1 (-> audio-map
          (update-in [:audio-data] np/repeat 2 :axis 1)
          (update-in [:channels] (fn [_] 2)))
    (-> audio-map
        (update-in [:audio-data]
                   get-item [(slice nil nil) (slice nil 2)])
        (update-in [:channels] (fn [_] 2)))))
