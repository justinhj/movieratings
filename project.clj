(defproject rottentomatoes "1.0.2-SNAPSHOT"
  :description "Clojure code to grab movie ratings from Rotten Tomatoes using their API or screen scraping"
  :dependencies [
		 [org.clojure/clojure "1.2.0"]
                 [org.clojure/clojure-contrib "1.2.0"]
		 [http.async.client "0.2.1"]
                 [cheshire "4.0.1"]
		 ]
  :main rottentomatoes.core
  :dev-dependencies [
		     [swank-clojure "1.2.1"]
		      ]
  )
