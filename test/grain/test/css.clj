(ns grain.test.css
  (:use [grain.css])
  (:use [clojure.test]))

(defn rounded-corners [px] 
  { :-webkit-border-radius px
    :-moz-border-radius px
    :border-radius px })

(def css-structure
  (css 
    [:div :h2 {:color "#FFF"}
     [:.test {:background "#000"}]]
    [:.box 
     {:background "green"}
     (rounded-corners "12px")]))

(def expected-result
  "div,h2{color:#FFF}div .test,h2 .test{background:#000}.box{background:green;-webkit-border-radius:12px;-moz-border-radius:12px;border-radius:12px}")

(deftest css-test
  (is (= css-structure expected-result)))
