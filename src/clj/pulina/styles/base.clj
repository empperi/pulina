(ns pulina.styles.base
  (:require [garden.def :refer [defrule defstyles]]
            [garden.stylesheet :refer [rule rgb]]))

(def bg-color-dark (rgb 61 61 88))
(def bg-color-light (rgb 193 179 164))
(def bg-color-msg (rgb 245 241 237))

(defn none-list [m]
  (-> m
      (assoc :list-style "none")
      (assoc :margin 0)
      (assoc :padding 0)))

(defstyles all
  (let [body         (rule :body)
        app-wrapper  (rule :div#app)
        view-wrapper (rule :div#view)]
    [(body {:font-family "Helvetica"
            :padding     0
            :margin      0
            :position    "absolute"
            :left        0
            :top         0
            :right       0
            :bottom      0})
     (app-wrapper {:display        "flex"
                   :flex-direction "column"
                   :height         "100%"})
     (view-wrapper
       {:display        "flex"
        :flex-direction "row"
        :order          1
        :flex-grow      1}
       [:.channels (none-list
                     {:order            1
                      :width            "140px"
                      :border-right     "1px solid black"
                      :background-color bg-color-dark})
        [:&>button
         {:width "100%"
          :height "1.8rem"}]]
       [:.messages (none-list
                     {:order            2
                      :flex-grow        2
                      :background-color bg-color-light})
        [:&>li
         {:padding "5px"
          :margin-bottom "2px"
          :background-color bg-color-msg}]])]))