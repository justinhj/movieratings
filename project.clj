(defproject rottentomatoes "1.0.3-SNAPSHOT"
  :description "Clojure code to grab movie ratings from Rotten Tomatoes using their API or screen scraping if no api key is found"
  :url "http://example.com/FIXME"
  :dependencies [
		 [org.clojure/clojure "1.5.1"]
		 [http.async.client "0.5.2"]
                 [cheshire "5.3.0"]
		 ]
  :main rottentomatoes.core)

;; (defproject rottentomatoes/rottentomatoes "1.0.2-SNAPSHOT" 
;;   :dependencies [[org.clojure/clojure "1.2.0"]
;;                  [org.clojure/clojure-contrib "1.2.0"]
;;                  [http.async.client "0.2.1"]
;;                  [cheshire "4.0.1"]]
;;   :main rottentomatoes.core
;;   :min-lein-version "2.0.0"
;;   :plugins [[swank-clojure "1.4.2"]]
;;   :description "Clojure code to grab movie ratings from Rotten Tomatoes using their API or screen scraping if no api key is found")
