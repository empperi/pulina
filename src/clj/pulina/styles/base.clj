(ns pulina.styles.base
  (:require [garden.def :refer [defrule defstyles]]
            [garden.stylesheet :refer [rule rgb]]))

(def bg-color-dark (rgb 61 61 88))
(def bg-color-light (rgb 193 179 164))
(def bg-color-msg (rgb 245 241 237))

(defn none-list
  ([] (none-list {}))
  ([m]
   (-> m
       (assoc :list-style "none")
       (assoc :margin 0)
       (assoc :padding 0))))

(defn flex-wrap [dir m]
  [:.flex-wrap
   (merge m {:display        "flex"
             :flex-direction dir})])

(defstyles all
  (let [body         (rule :body)
        app-wrapper  (rule :div#app)
        view-wrapper (rule :div#view)
        chan-btn     (rule :.channel-btn)
        messages     (rule :div#messages)
        msg-input    (rule :#msg-input)]
    [(body {:font-family "Helvetica"
            :padding     0
            :margin      0
            :position    "absolute"
            :left        0
            :top         0
            :right       0
            :bottom      0})
     (flex-wrap :column {})
     (app-wrapper {:display        "flex"
                   :height         "100%"})
     (view-wrapper
       {:display        "flex"
        :flex-grow      1}
       [:.channels (none-list
                     {:order            1
                      :width            "140px"
                      :border-right     "1px solid black"
                      :background-color bg-color-dark})])
     (chan-btn {:width "100%"
                :height "1.8rem"})
     (messages
       {:order            1
        :flex-grow        2
        :background-color bg-color-light
        :display          "flex"
        :flex-direction   "column"}
       [:.messages (none-list
                     {:flex-grow 1
                      :overflow-y "auto"})
        [:&>li
         {:padding "5px"
          :margin-bottom "2px"
          :background-color bg-color-msg}]])
     (msg-input
       {:padding 0
        :margin 0
        :border 0
        :background-color "white"
        :display "flex"}

       [:.chan-name {:font-size "0.8rem"
                     :margin "5px 5px"}]

       [:#msg-input-field {:flex-grow 1}])]))