(ns open-unmix-pytorch-clj.model
  "
  Encapsulates models used by other namespaces
  "
  (:require [open-unmix-pytorch-clj.validator :refer [is-ndarray? int>0?]]))

(defrecord AudioMap [audio-data sample-rate length channels])

(defn ->AudioMap
  "
  Creates AudioMap instance
  record that encapsulates audio file related information

  audio-data : numpy.ndarray
    Audio file's data

  sample-rate : float
    Sample rate of audio file

  length : int
    Number of samples in audio file

  channels : int
    Number of channels of audio file"
  [audio-data sample-rate length channels]
  {:pre [(is-ndarray? audio-data)
         (> sample-rate 0)
         (int>0? length)
         (int>0? channels)]}
  (AudioMap. audio-data sample-rate length channels))

(defn AudioMap?
  [input]
  (instance? AudioMap input))

(defrecord SeparationResult [separated sample-rate])

(defn ->SeparationResult
  "
  Creates SeparationResult instance
  record that encapsulates information related to result of
  audio source separation algorithm

  separated : {string : numpy.ndarray}
    map in a form - { Source of separation : Separation data }

  sample-rate : float
    Sample rate of an audio
  "
  [separated sample-rate]
  {:pre [(every? is-ndarray? (vals separated))
         (> sample-rate 0)]}
  (SeparationResult. separated sample-rate))

(defn SeparationResult?
  [input]
  (instance? SeparationResult input))
