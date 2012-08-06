(ns validateur.validation
  (:use [clojure.set :as cs]
        [clojurewerkz.support.core :only [assoc-with]])
  (:require clojure.string))


;;
;; Implementation
;;

(defn as-vec
  [arg]
  (if (sequential? arg)
    (vec arg)
    (vec [arg])))

(defn- concat-with-separator
  [v s]
  (apply str (interpose s v)))

(defn- member?
  [coll x]
  (some #(= x %) coll))


(defn- not-allowed-to-be-blank?
  [v ^Boolean allow-nil ^Boolean allow-blank]
  (or (and (nil? v)                  (not allow-nil))
      (and (clojure.string/blank? v) (not allow-blank))))

(defn- allowed-to-be-blank?
  [v ^Boolean allow-nil ^Boolean allow-blank]
  (or (and (nil? v)                  allow-nil)
      (and (clojure.string/blank? v) allow-blank)))


(defn- equal-length-of
  [attribute actual expected-length allow-nil allow-blank]
  (if (or (= expected-length (count actual))
          (allowed-to-be-blank? actual allow-nil allow-blank))
    [true {}]
    [false {attribute #{(str "must be " expected-length " characters long")}}]))

(defn- range-length-of
  [attribute actual xs allow-nil allow-blank]
  (if (or (member? xs (count actual))
          (allowed-to-be-blank? actual allow-nil allow-blank))
    [true {}]
    [false {attribute #{(str "must be from " (first xs) " to " (last xs) " characters long")}}]))



;;
;; API
;;

(defn presence-of
  [attribute]
  (let [f (if (vector? attribute) get-in get)]
    (fn [m]
      (let [v      (f m attribute)
            errors (if v {} {attribute #{"can't be blank"}})]
        [(empty? errors) errors]))))

(def ^{:private true}
  assoc-with-union (partial assoc-with cs/union))

(defn numericality-of
  [attribute & {:keys [allow-nil only-integer gt gte lt lte equal-to odd even] :or {allow-nil false
                                                                                     only-integer false
                                                                                     odd false
                                                                                     even false}}]
  (let [f (if (vector? attribute) get-in get)]
    (fn [m]
      (let [v      (f m attribute)
            errors (atom {})]
        ;; this code below is old, stupid and disgusting. It will be rewritten soon, please DO NOT use it as
        ;; example of how Clojure atoms should be used. MK.
        (if (and (nil? v) (not allow-nil))
          (swap! errors assoc attribute #{"can't be blank"}))
        (when (and v (not (number? v)))
          (swap! errors assoc-with-union attribute #{"should be a number"}))
        (when (and v only-integer (not (integer? v)))
          (swap! errors assoc-with-union attribute #{"should be an integer"}))
        (when (and v (number? v) odd (not (odd? v)))
          (swap! errors assoc-with-union attribute #{"should be odd"}))
        (when (and v (number? v) even (not (even? v)))
          (swap! errors assoc-with-union attribute #{"should be even"}))
        (when (and v (number? v) equal-to (not (= equal-to v)))
          (swap! errors assoc-with-union attribute #{(str "should be equal to " equal-to)}))
        (when (and v (number? v) gt (not (> v gt)))
          (swap! errors assoc-with-union attribute #{(str "should be greater than " gt)}))
        (when (and v (number? v) gte (not (>= v gte)))
          (swap! errors assoc-with-union attribute #{(str "should be greater than or equal to " gte)}))
        (when (and v (number? v) lt (not (< v lt)))
          (swap! errors assoc-with-union attribute #{(str "should be less than " lt)}))
        (when (and v (number? v) lte (not (<= v lte)))
          (swap! errors assoc-with-union attribute #{(str "should be less than or equal to " lte)}))
        [(empty? @errors) @errors]))))


(defn acceptance-of
  [attribute & {:keys [allow-nil accept] :or {allow-nil false accept #{true "true", "1"}}}]
  (let [f (if (vector? attribute) get-in get)]
    (fn [m]
      (let [v (f m attribute)]
        (if (and (nil? v) (not allow-nil))
          [false {attribute #{"can't be blank"}}]
          (if (accept v)
            [true {}]
            [false {attribute #{"must be accepted"}}]))))))



(defn inclusion-of
  [attribute & {:keys [allow-nil in] :or {allow-nil false}}]
  (let [f (if (vector? attribute) get-in get)]
    (fn [m]
      (let [v (f m attribute)]
        (if (and (nil? v) (not allow-nil))
          [false {attribute #{"can't be blank"}}]
          (if (in v)
            [true {}]
            [false {attribute #{(str "must be one of: " (concat-with-separator in ", "))}}]))))))



(defn exclusion-of
  [attribute & {:keys [allow-nil in] :or {allow-nil false}}]
  (let [f (if (vector? attribute) get-in get)]
    (fn [m]
      (let [v (f m attribute)]
        (if (and (nil? v) (not allow-nil))
          [false {attribute #{"can't be blank"}}]
          (if-not (in v)
            [true {}]
            [false {attribute #{(str "must not be one of: " (concat-with-separator in ", "))}}]))))))



(defn format-of
  [attribute & {:keys [allow-nil allow-blank format] :or {allow-nil false allow-blank false}}]
  (let [f (if (vector? attribute) get-in get)]
    (fn [m]
      (let [v (f m attribute)]
        (if (not-allowed-to-be-blank? v allow-nil allow-blank)
          [false {attribute #{"can't be blank"}}]
          (if (or (allowed-to-be-blank? v allow-nil allow-blank)
                  (re-find format v))
            [true {}]
            [false {attribute #{"has incorrect format"}}]))))))



(defn length-of
  [attribute & {:keys [allow-nil is within allow-blank] :or {allow-nil false allow-blank false}}]
  (let [f (if (vector? attribute) get-in get)]
    (fn [m]
      (let [v (f m attribute)]
        (if (not-allowed-to-be-blank? v allow-nil allow-blank)
          [false {attribute #{"can't be blank"}}]
          (if within
            (range-length-of attribute v within allow-nil allow-blank)
            (equal-length-of attribute v is     allow-nil allow-blank)))))))




(defn validation-set
  [& validators]
  (fn [m]
    (reduce (fn [accu f]
              (let [[ok errors] (f m)]
                (merge-with cs/union accu errors)))
            {}
            validators)))

(defn valid?
  [vs m]
  (empty? (vs m)))

(def invalid? (complement valid?))