(ns movieratings.core
  (:gen-class)
  (:require
   [clojure.string :as cs]
   [clj-http.client :as client]
   [cheshire.core :as cheshire])
  (:use 
   [slingshot.slingshot :only [throw+ try+]]))

(import [java.net URLEncoder])

(def base-url "http://www.rottentomatoes.com")

(def search-end-point "/search/?search=")

;; Rotten Tomatoes API key
;; Need to set it as an environment variable in your system

(def ^:dynamic *api-key* (System/getenv "ROTTEN_TOMATOES_API_KEY"))

;; note if you're not able to set an environment variable can fix at runtime for example like this:
;; (binding [*api-key* "YOUR KEY HERE"] (-main "jaws"))

;; Example http://www.rottentomatoes.com/search/?search=jaws&sitesearch=rt

(defn first-match-after [re1 re2 seq]
  "Splits the sequence SEQ using RE1 then searches after the first match and before the next match for the first occurence of RE2"
  (let [[_ _ after] (cs/split seq re1)] ;;; note changed s/partition to split
    (if after
     (re-find re2 after)
     nil)))

(defn scoop-url [url]
  "Use the http client to do a GET on the url and return the http response code and the body or nil"
  "Note that you'll get nil as the status code if for example there is no network connection and you get UnknownHostException"
  (try+
   (let [r (client/get url)
         code (:status r)
         body (:body r)]
     [code body])
   (catch Object o
     [(:status o) nil])))

;; Get movie urls
;; Does a search of Rotten Tomatoes for the search text, then scrapes the results
;; for the page for each movie. Returns a collection of the movie urls
;; TODO need a better way to handle errors
;; there are network errors and scraping errors if the page changes
;; perhaps the double return code of [err, result] 

(defn get-movie-urls [search-text]
  (let [encoded-search-text (URLEncoder/encode search-text)
	[code body] (scoop-url (str base-url search-end-point encoded-search-text))
	anchor-text #"<h3 class=\"bottom_divider\">Movies</h3>"
	]
    (if (= code 200)
      (let [[_ _ after] (cs/split body anchor-text)]
	(if (nil? after)
	  (do
	   (print "ERROR: failed to find required text \"" anchor-text "\"")
	   nil)
	  (let [movie-urls (re-seq #"\"(/m/.*/)\"" after)]
	    (reduce #(conj %1 %2) #{} (map #(str base-url (second %)) movie-urls)))))
      (println "HTTP request failed with code " code))))

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

;; The API makes us find the movies query URL before we can start

(defn get-rt-api-url []
  (let [[code body] (scoop-url (str "http://api.rottentomatoes.com/api/public/v1.0.json?apikey=" *api-key*))]
    (when (= code 200) 
      (:movies (:links (cheshire/parse-string body true))))))

(defn get-rt-api-template [movies-url]
  (let [[code body] (scoop-url (str movies-url "?apikey=" *api-key*))]
    (when (= code 200) 
      (:link_template (cheshire/parse-string body true)))))

(defn get-rt-api-movies [template search-term]
  (let [str0 (cs/replace template "{search-term}" (URLEncoder/encode search-term))
        str1 (cs/replace str0 "{results-per-page}" "50")
        str2 (cs/replace str1 "{page-number}" "1")
        fin (str str2 "&apikey=" *api-key*)]
    fin))

;; get the data returned by a movie search URL as Clojure data
(defn get-rt-movie-data [movie-url]
  (let [[code body] (scoop-url movie-url)]
    (if (= code 200)
      (:movies (cheshire/parse-string body true))
      nil)))

;; get the data returned by a movie search URL as JSON
(defn get-rt-movie-data-json [movie-url]
  (let [[code body] (scoop-url movie-url)]
    (if (= code 200)
      body
      nil)))

;; pass in the similar link from a movie json search response
;; not this is not the recommended way of doing this
;; TODO use the template system, write a nice way to fill out the template
;; also write a memoize class with timeout so they're only looked up once
;; in a while
(defn get-rt-similar-movie-data-json [rt-movie-id]
  (let [url (str "http://api.rottentomatoes.com/api/public/v1.0/movies/" rt-movie-id "/similar?apikey=" *api-key*)
        [code body] (scoop-url url)]
    (if (= code 200)
      body
      nil)))

; total: count , movies [] 
; ratings: critics_rating, audience_rating

(defn print-rt-movie-data [md]
  (doseq [movie md]
    (let [ratings (:ratings movie)]
      (printf "movie title: %s\n\tAudience %s\n\tCritics %s\n" (:title movie) (:audience_score ratings) (:critics_score ratings)))))

;; If there's an API KEY then use the Rotten Tomatoes API
;; otherwise fall back to just scraping the regular
;; Rotten Tomatoes web pages
(defn -main [& args]
  (if (= (count args) 1)
    (do
      (println "Query Rotten Tomatoes API with key " *api-key*)
      (if *api-key*
        (let [movies-url (get-rt-api-url)
              template (get-rt-api-template movies-url)
              movie-search-url (get-rt-api-movies template (first args))
              movie-data (get-rt-movie-data movie-search-url)]
          (print-rt-movie-data movie-data))
        (do
          (println "Using web scraping method")
          (pmap-get-movie-ratings (first args))
          (println "Done"))))
    (println "Enter a search string to match in movie titles")))
