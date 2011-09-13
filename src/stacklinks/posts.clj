(ns stacklinks.posts
  (:require clojure.contrib.lazy-xml clojure.string)
  (:use clojure.contrib.command-line clojure.java.io)
  (:use [stacklinks.files :only (open-gzip-file)] )
)

(defn load-posts
  "Parse an input source into a stream of post elements"
  [source]
  (map #(% :attrs)
    (filter
      #(and (= (% :name) :row)
            (= (% :type) :start-element))
        (clojure.contrib.lazy-xml/parse-seq source)))
  )

(defn extract-links
  [body]
   (map #(second %) (re-seq #"<a href=\"(.*?)\"" body)))

(defn- post-links
  [post-attrs]
    (extract-links (post-attrs :Body)) post-attrs)

(defn post-id-and-links
  "returns a list the post id and all links that follow : (id & links)"
  [post]
  (list (post :Id) (extract-links (post :Body))))

