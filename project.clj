(defproject rottentomatoes "1.0.0-SNAPSHOT"
  :description "Clojure code to grab movie ratings from Rotten Tomatoes"
  :dependencies [
		 [org.clojure/clojure "1.2.0"]
                 [org.clojure/clojure-contrib "1.2.0"]
		 [http.async.client "0.2.1"]
		 ]
  :main rottentomatoes.core
  :dev-dependencies [
		     [swank-clojure "1.2.1"]
		      ]
  )
