(ns stacklinks.posts-test
  (:use [stacklinks.posts] :reload)
  (:use [clojure.test]))

(deftest extract-links-test
  (is (= "http://example.com/foo"
          (first (extract-links "<p> Wibble wible <a href=\"http://example.com/foo\">Foo</a></p>")))
    "was expecting link to be extracted from body"))
