(ns stacklinks.posts
  (:require clojure.contrib.lazy-xml clojure.string)
  (:use clojure.contrib.command-line clojure.java.io)
)


(defn- attrs
  [coll]
  (map #(% :attrs) coll)
  )

(defn posts
  [source]
  (attrs
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
