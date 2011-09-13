(ns stacklinks.prepdata
  (:require clojure.contrib.lazy-xml clojure.string)
  (:use stacklinks.files)
  (:use stacklinks.posts)
  (:use [clojure.contrib.command-line :only (with-command-line)] )
  (:use clojure.java.io)
)

(import (java.io File)
        (java.io OutputStreamWriter)
        (org.apache.commons.io FileUtils))

(defn println-err
  [& more]
  (binding [*out* (writer System/err)]
    (apply println more)))

(defn println-to
  [w & more]
  (binding [*out* w]
    (apply println more))
  )

(defn writer-for-input
  [out input]
  (if out
    (writer (file out (minus-extension (file-name input))))
    *out*))

(defn filtered-post-links
  [coll]
  (filter #(not-empty (second %)) (map post-id-and-links coll)))

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
     [clean-out? "clean output directory"]
     inputs]
    (if clean-out? (clean-directory out))
    (doseq [input inputs]
      (if (.exists (file input))
        (do
          (println-err "Processing :" input);;" count : " (count all-posts)
          (with-open [f (open-gzip-file input)
                      w (writer-for-input out input)]
            (doseq [post (filtered-post-links (load-posts f))]
              ; for each item write it to the output
              (println-to w post)))
          (println-err "Finished :" input))

        (println-err "File not found : " input)))
    (System/exit 0)))

(if *command-line-args*
  (apply -main *command-line-args*))


