(defproject justinhj/movieratings "1.0.4-SNAPSHOT"
  :description "Clojure code to grab movie ratings from Rotten Tomatoes using their API or screen scraping if no api key is found"
  :min-lein-version "2.0.0"
  :dependencies [
		 [org.clojure/clojure "1.5.1"]
		 [http.async.client "0.5.2"]
                 [cheshire "5.3.0"]
		 ]
  :main movieratings.core)

