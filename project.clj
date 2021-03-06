(defproject dragoon/open-unmix-pytorch-clj "0.1.4-ALPHA"
  :description "Clojure bindings for PyTorch implementation of Open-Unmix"
  :url "https://github.com/dragoon000320/open-unmix-pytorch-clj"
  :license {:name "Eclipse Public License"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [clj-python/libpython-clj "1.46"]]
  :plugins [[lein-cloverage "1.0.13"]
            [lein-shell "0.5.0"]
            [lein-ancient "0.6.15"]
            [lein-changelog "0.3.2"]]
  :profiles {:dev {:dependencies [[org.clojure/test.check "0.10.0"]]}}
  :aliases {"update-readme-version"
            ["shell" "sed" "-i"
             "s/\\\\[dragoon\\\\/open-unmix-pytorch-clj \"[0-9.]*\"\\\\]/[dragoon\\\\/open-unmix-pytorch-clj \"${:version}\"]/"
             "README.md"]}
  :deploy-repositories [["releases" :clojars]
                        ["snapshots" :clojars]]
  :release-tasks [["shell" "git" "diff" "--exit-code"]
                  ["change" "version" "leiningen.release/bump-version"]
                  ["change" "version" "leiningen.release/bump-version" "release"]
                  ["changelog" "release"]
                  ["update-readme-version"]
                  ["vcs" "commit"]
                  ["vcs" "tag"]
                  ["deploy"]
                  ["vcs" "push"]]
  :bootclasspath true)
