# open-unmix-pytorch-clj
Clojure bindings for PyTorch implementation of __Open-Unmix__,
a deep neural network reference implementation for music source separation,
applicable for researchers, audio engineers and artists.

[![Clojars Project](https://img.shields.io/clojars/v/dragoon/open-unmix-pytorch-clj.svg)](https://clojars.org/dragoon/open-unmix-pytorch-clj)

* utilizes <a href="https://github.com/cnuernber/libpython-clj">great JNA libpython bindings library</a>.
* implementation based on <a href="https://github.com/sigsep/open-unmix-pytorch">related python implementation</a>.

## Get Started

* install python 3.6, pip

* install python dependencies
```bash
pip3 install -r requirements.txt
```

* add dependency to the project.clj
```clj
[dragoon000320/open-unmix-pytorch-clj "0.1.4-ALPHA"]
```

## Usage

A little demo how to use it
```clj
;; require namespaces
(require '[open-unmix-pytorch-clj.core :refer :all])

(require '[open-unmix-pytorch-clj.io :refer :all])

(require '[open-unmix-pytorch-clj.convert :refer :all])

(-> "your-audio-file.wav"
    ;; reads audio file
    soundfile-read
    ;; converts audio data to 2 channel one
    ->2-channels
    ;; separates audio data into required audio sources
    (separate ["vocals" "drums" "other" "bass"]
        :device "cpu"
        ;; or if you have cuda enabled uncomment line below
        ;; :device "cuda"
        )
    ;; writes estimates for each audio source to the output directory
    (write-estimates "out-dir"))
```

## Disclaimer

The library is in __alpha__ at the moment, current state of library is described below:
* for now only __separation__ of audio source implemented
* so there is no implementation of __model training__
* performance must be further improved
* a lot more to do...

## Contributions

It is an open-source project so contributions are welcomed (pull-requests, issue reports).

## Special Thanks To

* <a href="https://github.com/cnuernber/libpython-clj">Libpython-clj</a>
* <a href="https://numpy.org">Numpy</a>
* <a href="https://sigsep.github.io/open-unmix/">Open-Unmix</a>
* <a href="https://sigsep.github.io/open-unmix/norbert.html">Norbert</a>
* <a href="https://sigsep.github.io/open-unmix/museval.html">Museval</a>
* <a href="https://sigsep.github.io/datasets/musdb.html">Musdb18</a>
* <a href="https://pytorch.org">PyTorch</a>

## License

Copyright © 2020

Distributed under the <a href="https://www.eclipse.org/legal/epl-2.0/">Eclipse Public License 2.0</a>
