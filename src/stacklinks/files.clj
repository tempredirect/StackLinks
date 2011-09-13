(ns stacklinks.files
  (:use [clojure.java.io :only (file input-stream)]))

(import (java.io File)
        (java.io OutputStreamWriter)
        (java.io FileInputStream)
        (java.util.zip GZIPInputStream)
        (org.apache.commons.io FileUtils))

(defn clean-directory
  [directory]
  (FileUtils/cleanDirectory (file directory)))

(defn file-name
  [name]
  (.getName (File. name)))

(defn minus-extension
  [name]
  (let [idx (.lastIndexOf name ".")]
    (if (> idx -1)
      (.substring name 0 idx)
      name)))

(defn extension
  [name]
  (let [idx (.lastIndexOf name ".")]
    (if (> idx -1)
      (.substring name (inc idx))
      "")))

(defn open-gzip-file
  [name]
  (let [fileinput (input-stream name)]
    (if (= (extension name) "gz")
      (GZIPInputStream. fileinput)
      fileinput)))

