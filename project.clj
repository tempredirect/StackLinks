(defproject stacklinks/prepdata "1.0.0-SNAPSHOT"
  :description "Data prep script to parse the data dump from stackoverflow to be consumed by the hadoop job"
  :dependencies [[org.clojure/clojure "1.2.0"]
                 [org.clojure/clojure-contrib "1.2.0"]
                 [commons-io/commons-io "2.0.1"]]
  :dev-dependencies [[lein-hadoop "1.0.0"]]
  :main prepdata.core)
