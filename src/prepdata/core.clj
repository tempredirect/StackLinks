(ns prepdata.core
  (:require clojure.contrib.lazy-xml clojure.string)
  (:use clojure.contrib.command-line clojure.java.io)
  ;  (:gen-class)
)

(import (java.io File)
        (java.io OutputStreamWriter)
        (org.apache.commons.io FileUtils))

(defn attrs
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

(defn links
  [body]
   (map #(second %) (re-seq #"<a href=\"(.*?)\"" body)))

(defn post-links
  [post-attrs]
    (links (post-attrs :Body)) post-attrs)

(defn println-err
  [& more]
  (binding [*out* (writer System/err)]
    (apply println more)))

(defn println-to
  [w & more]
  (binding [*out* w]
    (apply println more))
  )

(defn clean-directory
  [directory]
  (FileUtils/cleanDirectory (as-file directory)))

(defn file-name
  [name]
  (.getName (File. name)))

(defn minus-extension
  [name]
  (let [idx (.lastIndexOf name ".")]
    (if (> idx -1)
      (.substring name 0 idx)
      name)))

(defn writer-for-input
  [out input idx]
  (if out
    (writer (file out (str (minus-extension (file-name input)) "." (format "%05d" idx))))
    *out*))

(defn filtered-post-links
  [input]
  (filter #(not-empty (second %))
    (map #(list (% :Id) (links (% :Body)))
      (posts input))))

(defn to-post-chunks
  [all-posts partition-count]
  (if partition-count
    (partition-all (Integer. partition-count) all-posts)
    (list all-posts)))

(defn output-chunk-to-writer
  [chuck w]
    (doseq [post chuck]
      (println-to w post))
  )

(defn -main
  [& args]
  (with-command-line args
    "Command line usage"
    [[out "output directory"]
     [partition-count "number of posts per partition"]
     [clean-out? "clean output directory"]
     inputs]
    (if clean-out? (clean-directory out))
    (doseq [input inputs]
      (if (.exists (as-file input))
        (let [all-posts (filtered-post-links input)]
          (println-err "Processing :" input);;" count : " (count all-posts)
          (loop [post-chunks (to-post-chunks all-posts partition-count)
                 idx 0]
            (if (not-empty post-chunks)
              (do
                (with-open [w (writer-for-input out input idx)]
                  (output-chunk-to-writer (first post-chunks) w))
                (recur (rest post-chunks)
                  (inc idx)))
              )
            )
          (println-err "Finished :" input))
        (println-err "File not found : " input)))
    (System/exit 0)))

(if *command-line-args*
  (apply -main *command-line-args*))

