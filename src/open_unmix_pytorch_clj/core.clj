(ns open-unmix-pytorch-clj.core
  "
  Encapsulates main functionalities of library
  "
  (:require [open-unmix-pytorch-clj.model :refer :all]
            [open-unmix-pytorch-clj.validator :refer :all]
            [libpython-clj.require :refer [require-python]]
            [libpython-clj.python :refer [py. py.. get-item]]
            [open-unmix-pytorch-clj.convert :refer [istft]]))

(require-python '[numpy :as np])
(require-python '[norbert])
(require-python '[torch])
(require-python '[torch.hub])
(require-python '[builtins :refer [slice Ellipsis complex]])

(defn separate
  "
  Performs the separation on audio input

  audio-map : AudioMap
    AudioMap with audio data to separate

  targets : [string]
    Targets for separation ('vocals', 'drums', 'bass', 'other')

  model-name = 'umxhq' : string

  niter = 1 : int
    Number of EM steps for refining initial estimates in a
    post-processing stage

  alpha = 1.0 : float
    Exponent for building ratio masks
    if not equals 1.0, then the initial estimates for the sources will
    be obtained through a ratio mask of the mixture STFT, and not
    by using the default behavior of reconstructing waveforms
    by using the mixture phase, defaults to False

  residual-model = false : bool
    Computes a residual target, for custom separation scenarios
    when not all targets are available, defaults to False

  device = 'cpu' : str
    On what device will torch be run

  ->
  SeparationResult
  "
  [audio-map targets & {:keys [model-name niter alpha
                               residual-model device]
                        :or {model-name "umxhq", niter 1, alpha 1.0,
                             residual-model false, device "cpu"}}]
  {:pre [(AudioMap? audio-map)
         (every-str? targets)]}
  (let [audio-data (:audio-data audio-map)
        sample-rate (:sample-rate audio-map)
        use-softmask (not= alpha 1.0)
        ;; convert numpy audio data to torch
        audio-torch (-> audio-data
                        (py.. -T)
                        (get-item [nil Ellipsis])
                        torch/tensor
                        (py.. (float))
                        (py.. (to device)))
        ;; for each target load an open-unmix-pytorch model
        unmix-targets (mapv #(torch.hub/load
                              "sigsep/open-unmix-pytorch"
                              model-name
                              :target %
                              :device device
                              :pretrained true)
                            targets)
        first-unmix (first unmix-targets)
        ;; targets for separation
        target-names (cond-> targets
                       (= (count targets) 1) (conj "accompaniment")
                       residual-model (conj "residual"))
        X (-> first-unmix
              (py.. (stft audio-torch))
              (py.. (detach))
              (py.. (cpu))
              (py.. (numpy))
              ;; converts to complex numpy type
              (as-> x
                  ; X = X[..., 0] + X[..., 1]*1j
                    (py.. (get-item x [Ellipsis 0])
                          (__add__
                           (py.. (get-item x [Ellipsis 1])
                                 (__mul__
                                  (complex 0 1))))))
              (get-item [0])
              (py.. (transpose [2 1 0])))
        V (cond->
              (-> (mapv #(-> audio-torch
                             %
                             (py.. (cpu))
                             (py.. (detach))
                             (py.. (numpy))
                             (cond->
                                 ;; exponentiate the model if use a softmask V**alpha
                                 use-softmask (py.. (__pow__ alpha)))
                             ;; remove sample dim V[:, 0, ...]
                             (get-item [(slice nil) 0 Ellipsis]))
                        unmix-targets)
                  np/array
                  (np/transpose [1 3 2 0]))
            ;; calculate residual model if needed
            (or residual-model
                (= (count targets) 1))
            (norbert/residual_model X :alpha alpha))
        Y (norbert/wiener V
                          (py.. X (astype np/complex128))
                          niter
                          use-softmask)]
    (->SeparationResult
     (->> target-names
          (map-indexed
           (fn [idx target-name]
             [(keyword target-name)
              (-> Y
                  (get-item [Ellipsis idx])
                  (py.. -T)
                  (istft :sample-rate sample-rate
                         :n-fft (py.. first-unmix -stft -n_fft)
                         :n-hopsize (py.. first-unmix -stft -n_hop))
                  last
                  (py.. -T))]))
          (into {}))
     sample-rate)))
