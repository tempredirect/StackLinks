(ns stacklinks.prepdata
  (:require clojure.contrib.lazy-xml clojure.string)
  (:use stacklinks.files)
  (:use stacklinks.posts)
  (:use [clojure.contrib.command-line :only (with-command-line)] )
  (:use clojure.java.io)
  (:gen-class)
)

(import (java.io File)
        (java.io OutputStreamWriter)
        (org.apache.commons.io FileUtils))

(defn println-err
  [& more]
  (binding [*out* (writer System/err)]
    (apply println more)))

(defn write-to-using
  [w fun & args]
  (binding [*out* w]
    (apply fun args))
  )

(defn println-to
  [w & more]
  (binding [*out* w]
    (apply println more))
  )

(defn writer-for
  [out input ext]
  (if out
    (writer (file out (str (minus-extension (file-name input)) "." ext)) :encoding "UTF-8")
    *out*))

(defn filtered-post-links
  [coll]
  (filter
    #(not-empty (% :Links))
    (map post-lite-and-links coll)))

(defn process-file
  "Process a file and output to out"
  [fname out]
  (do
    (println-err "Processing :" fname);;" count : " (count all-posts)
    (with-open [f (open-gzip-file fname)
                w (writer-for out fname)]
      (doseq [post (filtered-post-links (load-posts f))]
        ; for each item write it to the output
        (write-to-using w prn post)))
    (println-err "Finished :" fname))
  )

(defn update-summary
  [summaries post-id uris]
  (if (seq uris)
    (let [uri       (first (seq uris))
          post-ids  (if (contains? summaries uri)
                        (conj (summaries uri) post-id)
                        [post-id])]
      ;(println-err uri post-ids)
      (recur (assoc summaries uri post-ids) post-id (rest (seq uris))))
    ;; else it's the end of the recur
    summaries))

(defn summarise
  "Summarise a list of posts into a map of links -> post id's"
  [all-posts]
  (loop [summaries {}
         posts all-posts]
    (if (seq posts)
      (let [post (first posts)]
        ;(println-err post)
        (recur  (update-summary summaries (post :Id) (post :Links))
                (rest posts)))
      summaries)))

(defn summarise-file
  [fname]
  (with-open [f (open-gzip-file fname)]
    (loop [summaries {}
           posts (filtered-post-links (load-posts f))]
        (if (seq posts)
          (let [post (first posts)]
            ;(println-err post)
            (recur  (update-summary summaries (post :Id) (post :Links))
                    (rest posts)))
          summaries)))
)

(defn sort-summary
  [summary]
  (sort-by #(* (count (second %)) -1) summary))

(defn output-to-file
  "Output the data to out+file+ext"
  [data out fname ext]
  (with-open [outfile (writer-for out fname ext)]
    (write-to-using outfile prn data)))

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
        (let [ posts (filtered-post-links (load-posts (open-gzip-file fname)))
               summary (summarise posts)
               sorted (sort-summary summary)]
          (output-to-file posts out fname "all")
          (output-to-file summary out fname "sum")
          (output-to-file sorted out fname "top")
          ; only bother with the first 50 pages
          (doall (take 50 (map-indexed (fn [i part] (output-to-file part out fname (str "top." (format "%02d" i))))
                                (partition 20 sorted))))
          )
        ;(process-file fname out)
        (println-err "File not found : " fname)))
    (System/exit 0)))

(if *command-line-args*
  (apply -main *command-line-args*))


