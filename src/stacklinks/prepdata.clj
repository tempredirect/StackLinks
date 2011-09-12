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
  [out input idx]
  (if out
    (writer (file out (str (minus-extension (file-name input)) "." (format "%05d" idx))))
    *out*))

(defn filtered-post-links
  [input]
  (filter #(not-empty (second %))
    (map #(list (% :Id) (extract-links (% :Body)))
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
      (if (.exists (file input))
        (let [all-posts (filtered-post-links (open-gzip-file input))]
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


