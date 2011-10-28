(ns grain.css)

(def ^{:private true}
  empty-rule {:selectors [] :body [] :nested []})

(defprotocol CSSRule
  "A protocol to implement how different types add to the css data structure"
  (merge-rule [v rule] "Takes the css structure and modifies with value"))

(defn- assoc-map 
  "Allows you to run a function over a value in map"
  [m key func]
  (assoc m key (func (m key))))

(defn- rule-reduce 
  [rule v]
  (if (vector? v)
    (assoc-map rule :nested #(conj % (merge-rule v empty-rule)))
    (merge-rule v rule)))

(defn- zip-selectors
  "Make sure our list of selectors get all their parents"
  [parents selectors]
  (if-not (empty? parents)
    (flatten (map (fn [x] (map #(str % " " x) parents)) selectors))
    selectors))

(defn- render-rule 
  "Render the rule data structure"
  [parents rule]
    (let [zipped-sel (zip-selectors parents (rule :selectors))
          bod (->> (rule :body) (interpose ";") (apply str))
          nes (->> (rule :nested) (map #(render-rule zipped-sel %)) (apply str))
          sel (->> zipped-sel (interpose ",") (apply str))]
      (if-not (empty? (rule :body))
        (str sel "{" bod "}" nes)
        nes))) 

(extend-protocol CSSRule
  clojure.lang.IPersistentMap
    (merge-rule [v rule]
      (let [func (fn [[n r]] (str (name n) ":" r))]
        (assoc-map rule :body #(concat % (map func v)))))

  clojure.lang.IPersistentVector
    (merge-rule [v rule]
      (reduce rule-reduce rule v))
  
  clojure.lang.Keyword
    (merge-rule [v rule]
      (assoc-map rule :selectors #(conj % (name v))))
  
  String
    (merge-rule [v rule]
      (assoc-map rule :selectors #(conj % v))))

(defn css
  "Generate css from a bunch of vectors"
  ([vec]
    (render-rule [] (merge-rule vec empty-rule)))
  ([vec & more]
    (->> (cons vec more) (map css) (apply str))))
