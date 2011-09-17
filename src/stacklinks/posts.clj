(ns stacklinks.posts
  (:require clojure.contrib.lazy-xml clojure.string)
  (:use clojure.contrib.command-line clojure.java.io)
  (:use [stacklinks.files :only (open-gzip-file)] )
)

(defn load-posts
  "Parse an input source into a stream of post elements"
  [source]
  ; Id="3" PostTypeId="2" ParentId="1" CreationDate="2010-09-01T19:36:50.053" Score="26" ViewCount="0" Body=""
  ; OwnerUserId="11" LastEditorUserId="11" LastEditorDisplayName="" LastEditDate="2010-09-01T20:41:14.273"
  ; LastActivityDate="2010-09-01T20:41:14.273" CommentCount="16"
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

(defn post-lite-and-links
  "returns a list the post id and all links that follow : (id & links)"
  [post]
  (assoc (select-keys post [:Id :PostTypeId :ParentId])
          :Links (extract-links (post :Body))))


