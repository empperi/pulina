(ns pulina.form-components
  (:require [clojure.string :as st]))

(def ENTER_KEY 13)

(defn on-enter [f & args]
  (fn [e]
    (let [code (-> e .-charCode)]
      (if (= ENTER_KEY code)
        (do
          (.preventDefault e)
          (apply f args))
        e))))

(defn on-change-update
  ([ratom] (on-change-update ratom []))
  ([ratom ks]
   (fn [e]
     (.preventDefault e)
     (if (empty? ks)
       (reset! ratom (-> e .-target .-value))
       (swap! ratom assoc-in ks (-> e .-target .-value))))))

(defn input [ratom ks m]
  [:input
   (-> m
       (assoc :value (get-in @ratom ks))
       (assoc :on-change (on-change-update ratom ks)))])

(defn label-input [ratom ks m label]
  [:span.label-input
   [:label {:for (str label "-" (st/join "-" ks))} label]
   [input ratom ks (assoc m :id (str label "-" (st/join "-" ks)))]])