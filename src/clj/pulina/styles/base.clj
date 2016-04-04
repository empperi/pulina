(ns pulina.styles.base
  (:require [garden.def :refer [defrule defstyles]]
            [garden.stylesheet :refer [rule]]))

(defstyles all
  (let [body (rule :body)]
    (body
      {:font-family "Helvetica"})))