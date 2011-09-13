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

(defn process-file
  "Process a file and output to out"
  [fname out]
  (do
    (println-err "Processing :" fname);;" count : " (count all-posts)
    (with-open [f (open-gzip-file fname)
                w (writer-for-input out fname)]
      (doseq [post (filtered-post-links (load-posts f))]
        ; for each item write it to the output
        (println-to w post)))
    (println-err "Finished :" fname))
  )

(defn -main
  [& args]
  (with-command-line args
    "Command line usage"
    [[out "output directory"]
     [clean-out? "clean output directory"]
     inputs]
    (if clean-out? (clean-directory out))
    (doseq [fname inputs]
      (if (file-exists? fname)
        (process-file fname out)
        (println-err "File not found : " fname)))
    (System/exit 0)))

(if *command-line-args*
  (apply -main *command-line-args*))


