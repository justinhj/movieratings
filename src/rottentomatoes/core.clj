(ns rottentomatoes.core
  (:gen-class)
  (:require
   [clojure.contrib.str-utils2 :as s] 
   [http.async.client :as c]))

(import [java.net URLEncoder]
	[java.lang.Character])

(def *base-url* "http://www.rottentomatoes.com")

(def *search-end-point* "/search/?search=")

;; Example http://www.rottentomatoes.com/search/?search=jaws&sitesearch=rt

(defn first-match-after [re1 re2 seq]
  "Splits the sequence SEQ using RE1 then searches after the first match and before the next match for the first occurence of RE2"
  (let [[_ _ after] (s/partition seq re1)]
    (re-find re2 after))) 

(defn response-status-code [resp]
  (:code (c/status resp)))

(defn scoop-url [url]
  "Use the http client to do a GET on the url"
  (prn url)
  (let [resp (c/GET url)]
    (c/await resp)
    (if (= 301 (response-status-code resp)) ; redirect
      (do
       (prn "page is redirected")
       [(response-status-code resp) nil])
      [(response-status-code resp) (c/string resp)])))

;; Get movie urls
;; Does a search of Rotten Tomatoes for the search text, then scrapes the results
;; for the page for each movie. Returns a collection of the movie urls
;; TODO need a logging interface for the warning

(defn get-movie-urls [search-text]
  (let [encoded-search-text (URLEncoder/encode search-text)
	[code body] (scoop-url (str *base-url* *search-end-point* encoded-search-text))
	anchor-text #"<h3 class=\"bottom_divider\">Movies</h3>"
	]
    (if (= code 200)
      (let [[_ _ after] (s/partition body anchor-text)]
	(if (nil? after)
	  (do
	   (print "ERROR: failed to find required text \"" anchor-text "\"")
	   nil)
	  (let [[_ & results] (s/partition after #"\"(/m/.*/)\"")]
	    (map #(str *base-url* (second %)) (take-nth 2 results)))))
      (prn "IO error " code))))

;; Given a movie url GET the page then scrape it for the citic and audience rating

(defn get-movie-rating [movie-url]
  (let [[code body] (scoop-url movie-url)]
    (if (= code 200)
      {:critics (second
		(first-match-after #"class=\"critic_side_container" #">([0-9]+)<" body))
       :audience (second
		  (first-match-after #"class=\"fan_side" #">([0-9]+)<" body))})))      

;; Finds the ratings for all Rotten Tomatoes movies that match the search string and prints them out

(defn get-movie-ratings [search-str]
  (let [urls (get-movie-urls search-str)]
    (when (> (count urls) 0)
      (doseq [url urls]
	(let [ratings (get-movie-rating url)]
	  (printf "movie url: %s\n\tAudience %s\n\tCritics %s\n" url (:audience ratings) (:critics ratings)))))))

;; Slight variant on above that uses pmap so that the requests are done in parallel 

(defn pmap-get-movie-ratings [search-str]
  (println "Search for " search-str)
  (let [urls (get-movie-urls search-str)]
    (when (> (count urls) 0)
      (let [ratings (pmap #(get-movie-rating %) urls)
	    url-and-ratings (map vector urls ratings)]
	(doseq [[url ratings] url-and-ratings]
	  (printf "movie url: %s\n\tAudience %s\n\tCritics %s\n" url (:audience ratings) (:critics ratings)))))))

(defn -main [& args]
  (if (= (count args) 1)
    (do
      (pmap-get-movie-ratings (first args))
      (println "Done"))
    (println "Enter a search string to match in movie titles")))