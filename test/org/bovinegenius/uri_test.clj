(ns org.bovinegenius.uri_test
  (:use (org.bovinegenius exploding-fish)
        (org.bovinegenius.exploding-fish query-string)
        (clojure test))
  (:import (java.net URI URL)))

(deftest uri-test
  (let [uri-string "http://www.fred.net/"]
    (is (= (into {} (seq (uri uri-string)))
           (into {} (seq (uri (URI. uri-string))))
           (into {} (seq (uri (URL. uri-string))))
           (into {} (seq (uri {:host "www.fred.net",
                               :path "/",
                               :authority "www.fred.net",
                               :scheme "http",
                               :scheme-specific-part "//www.fred.net/"})))
           {:host "www.fred.net",
            :path "/",
            :authority "www.fred.net",
            :scheme "http",
            :scheme-specific-part "//www.fred.net/"})))
  (let [uri-string "http://www.domain.net/with?query=and#fragment"]
    (is (= (into {} (seq (uri uri-string)))
           (into {} (seq (uri (URI. uri-string))))
           (into {} (seq (uri (URL. uri-string))))
           (into {} (seq (uri {:host "www.domain.net",
                               :query "query=and",
                               :path "/with",
                               :authority "www.domain.net",
                               :scheme "http",
                               :scheme-specific-part "//www.domain.net/with?query=and",
                               :fragment "fragment"})))           
           {:host "www.domain.net",
            :query "query=and",
            :path "/with",
            :authority "www.domain.net",
            :scheme "http",
            :scheme-specific-part "//www.domain.net/with?query=and",
            :fragment "fragment"})))
  (let [uri-string "http://www.domain.net:8080/with?query=and#fragment"]
    (is (= (into {} (seq (uri uri-string)))
           (into {} (seq (uri (URI. uri-string))))
           (into {} (seq (uri (URL. uri-string))))
           (into {} (seq (uri {:port 8080,
                               :host "www.domain.net",
                               :query "query=and",
                               :path "/with",
                               :authority "www.domain.net:8080",
                               :scheme "http",
                               :scheme-specific-part "//www.domain.net:8080/with?query=and",
                               :fragment "fragment"})))
           {:port 8080,
            :host "www.domain.net",
            :query "query=and",
            :path "/with",
            :authority "www.domain.net:8080",
            :scheme "http",
            :scheme-specific-part "//www.domain.net:8080/with?query=and",
            :fragment "fragment"}))))

(deftest protocol-fns
  (let [string "http://someone@www.pureawesomeness.org:8088/some/path?x=y&a=b#awesomeness"
        java-uri (URI. string)
        java-url (URL. string)
        uri-obj (uri string)]
    (is (= (scheme string)
           (scheme java-uri)
           (scheme java-url)
           (scheme uri-obj)
           "http"))
    (is (= (scheme-specific-part string)
           (scheme-specific-part java-uri)
           (scheme-specific-part java-url)
           (scheme-specific-part uri-obj)
           "//someone@www.pureawesomeness.org:8088/some/path?x=y&a=b"))
    (is (= (authority string)
           (authority java-uri)
           (authority java-url)
           (authority uri-obj)
           "someone@www.pureawesomeness.org:8088"))
    (is (= (user-info string)
           (user-info java-uri)
           (user-info java-url)
           (user-info uri-obj)
           "someone"))
    (is (= (host string)
           (host java-uri)
           (host java-url)
           (host uri-obj)
           "www.pureawesomeness.org"))
    (is (= (port string)
           (port java-uri)
           (port java-url)
           (port uri-obj)
           8088))
    (is (= (path string)
           (path java-uri)
           (path java-url)
           (path uri-obj)
           "/some/path"))
    (is (= (query string)
           (query java-uri)
           (query java-url)
           (query uri-obj)
           "x=y&a=b"))
    (is (= (fragment string)
           (fragment java-uri)
           (fragment java-url)
           (fragment uri-obj)
           "awesomeness"))))

(deftest uri->map-test
  (is (= (uri->map (URI. "http://www.test.com:8080/some/stuff.html#frag"))
         {:path "/some/stuff.html",
          :scheme "http",
          :authority "www.test.com:8080",
          :host "www.test.com",
          :port 8080,
          :scheme-specific-part "//www.test.com:8080/some/stuff.html",
          :fragment "frag"})))

(deftest query-list-test
  (is (= (query-list (uri "http://www.some-thing.com/a/path?one=1&two=2&x=&y"))
         '("one=1" "two=2" "x=" "y")))
  (is (= (query-list (uri "http://www.some-thing.com/a/path?one=1&two=2&x=&y"))
         (query-list "http://www.some-thing.com/a/path?one=1&two=2&x=&y")
         (query-list (URI. "http://www.some-thing.com/a/path?one=1&two=2&x=&y"))
         (query-list (URL. "http://www.some-thing.com/a/path?one=1&two=2&x=&y")))))

(deftest query-pairs-test
  (is (= (query-pairs (uri "http://www.some-thing.com/a/path?one=1&two=2&x=&y"))
         '(["one" "1"] ["two" "2"] ["x" ""] ["y" nil])))
  (is (= (query-pairs (uri "http://www.some-thing.com/a/path?one=1&two=2&x=&y&&=&=a"))
         '(["one" "1"] ["two" "2"] ["x" ""] ["y" nil] ["" nil] ["" ""] ["" "a"])))
  (is (= (alist->query-string
          (query-pairs (uri "http://www.some-thing.com/a/path?one=1&two=2&x=&y&&=&=a")))
         "one=1&two=2&x=&y&&=&=a"))
  (is (= (alist->query-string
          (query-pairs "http://www.some-thing.com/a/path?one=1&two=2&x=&y&&=&=a"))
         "one=1&two=2&x=&y&&=&=a")))

(deftest param-test
  (let [url "http://www.test.net/some/path?x=y&a=w&d%20x=m%3Df&a=x&m=2"]
    (is (= "http://www.test.net/some/path?x=y&a=7&d+x=m%3Df&m=2"
           (param url "a" 7)))
    (is (= "http://www.test.net/some/path?x=y&a=w&d+x=m%3Df&a=x&m=2&a=7"
           (param url "a" 7 2)))
    (is (= "http://www.test.net/some/path?x=y&a=w&d+x=7&a=x&m=2"
           (param url "d x" 7)))
    (is (= "http://www.test.net/some/path?x=y&a=w&d+x=m%3Df&a=7&m=2"
           (param url "a" 7 1)))))

(deftest param-raw-test
  (let [url "http://www.test.net/some/path?x=y&a=w&d%20x=m%3Df&a=x&m=2"]
    (is (= "http://www.test.net/some/path?x=y&a=7&d%20x=m%3Df&m=2"
           (param-raw url "a" 7)))
    (is (= "http://www.test.net/some/path?x=y&a=w&d%20x=m%3Df&a=x&m=2&a=7"
           (param-raw url "a" 7 2)))
    (is (= "http://www.test.net/some/path?x=y&a=w&d%20x=7&a=x&m=2"
           (param-raw url "d%20x" 7)))
    (is (= "http://www.test.net/some/path?x=y&a=w&d%20x=m%3Df&a=7&m=2"
           (param-raw url "a" 7 1)))))

(deftest params-test
  (let [url "http://www.test.net/some/path?x=y&a=w&d%20x=m%3Df&a=x&m=2"]
    (is (= ["w" "x"]
           (params url "a")))
    (is (= ["m=f"]
           (params url "d x")))))

(deftest params-raw-test
  (let [url "http://www.test.net/some/path?x=y&a=w&d%20x=m%3Df&a=x&m=2"]
    (is (= ["w" "x"]
           (params-raw url "a")))
    (is (= ["m%3Df"]
           (params-raw url "d%20x")))
    (is (= []
           (params-raw url "d x")))))

(deftest query-map-test
  (let [url "http://www.test.net/some/path?x=y&a=w&d%20x=m%3Df&a=x&m=2"]
    (is (= {"x" "y"
            "a" "x"
            "d x" "m=f"
            "m" "2"}
           (query-map url)))))

(deftest raw-keys-test
  (let [url "http://www.test.net/some/path?x=y&a=w&d%20x=m%3Df&a=x&m=2"]
    (is (= ["x" "a" "d%20x" "a" "m"]
           (raw-keys url)))))

(deftest query-keys-test
  (let [url "http://www.test.net/some/path?x=y&a=w&d%20x=m%3Df&a=x&m=2"]
    (is (= ["x" "a" "d x" "a" "m"]
           (query-keys url)))))

(deftest normalize-path-test
  (let [uri "http://www.example.com/some/path/../awesome/./thing/../path/here?x=y&a=b#a-fragment"]
    (is (= "http://www.example.com/some/awesome/path/here?x=y&a=b#a-fragment"
           (normalize-path uri)))))

(deftest resolve-path-test
  (let [uri "http://www.example.com/some/path/../awesome/./thing/../path/here?x=y&a=b#a-fragment"]
    (is (= "http://www.example.com/some/awesome/path/new/path/stuff?x=y&a=b#a-fragment"
           (resolve-path uri "new/path/stuff")))
    (is (= "http://www.example.com/some/awesome/path/new/path/stuff?x=y&a=b#a-fragment"
           (normalize-path (resolve-path uri "new/path/stuff"))))
    (is (= "http://www.example.com/new/path/stuff?x=y&a=b#a-fragment"
           (resolve-path uri "/new/path/stuff")))
    (is (= "http://www.example.com/some/awesome/path/here/new/path/stuff?x=y&a=b#a-fragment"
           (resolve-path "http://www.example.com/some/awesome/path/here/?x=y&a=b#a-fragment"
                         "new/path/stuff")))))

(deftest absolute?-test
  (is (absolute? "http://www.test.net/new/path?x=y&a=w"))
  (is (not (absolute? "/new/path?x=y&a=w"))))
