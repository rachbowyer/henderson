;; Copyright ©️ Rachel Bowyer 2015, 2021.
;; Please see the LICENSE file for more information

(defproject henderson "0.1.0"
  :description "Henderson Picture Language for Clojure"
  :url "https://github.com/rachbowyer/henderson"

  :license {:name "CC BY-SA 4.0"
            :url "https://creativecommons.org/licenses/by-sa/4.0/"}

  :dependencies [[org.clojure/clojure "1.10.1"]]

  :resource-paths ["resources"]
  :source-paths ["src" "src"]
  :test-paths ["test" "test"]
  :target-path "target/%s"

  :profiles {:dev {:global-vars {*warn-on-reflection* false
                                 *assert* true}}

             :uberjar {:jvm-opts ^:replace ["-Dclojure.compiler.direct-linking=true"]
                       :aot :all}})
