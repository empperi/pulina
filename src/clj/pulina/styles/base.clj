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

(def body         (rule :body))
(def header       (rule :header))
(def app-wrapper  (rule :div#app))
(def content-wrap (rule :div#content))
(def view-wrapper (rule :div#view))
(def chan-btn     (rule :.channel-btn))
(def messages     (rule :div#messages))
(def msg-input    (rule :#msg-input))
(def new-chat     (rule :.new-chat))
(def current-user (rule :#current-user))

(defstyles all
  (body {:font-family "Helvetica"
         :padding     0
         :margin      0
         :position    "absolute"
         :left        0
         :top         0
         :right       0
         :bottom      0})

  (flex-wrap :column {})

  (app-wrapper {:display "flex"
                :height  "100%"})

  (content-wrap {:display        "flex"
                 :flex-direction "column"
                 :height         "100%"
                 :width          "100%"})

  (view-wrapper
    {:display        "flex"
     :flex-grow      1}
    [:.channels {:order            1
                 :border-right     "1px solid black"
                 :background-color bg-color-dark}
     [:ul (none-list {})]])

  (header {:width    "100%"
           :height   "35px"
           :background-color bg-color-dark})

  (new-chat
    {:width  "260px"
     :height "35px"}
    [:button {:margin "6px"}]
    [:input {:margin "6px"}])

  (current-user {:position "absolute"
                 :right    0
                 :top      0
                 :height   "35px"
                 :width    "125px"
                 :background-color "red"})

  (chan-btn {:width  "140px"
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

    [:#msg-input-field {:flex-grow 1}]))