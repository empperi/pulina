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
(def login        (rule :#login))
(def header       (rule :header))
(def app-wrapper  (rule :#app))
(def content-wrap (rule :#content))
(def view-wrapper (rule :#view))
(def chan-btn     (rule :.channel-btn))
(def messages     (rule :#messages))
(def msg-input    (rule :#msg-input))
(def new-chat     (rule :.new-chat))
(def current-user (rule :#current-user))

; ---- General styles -----------------------------

(defstyles layout
  (body {:font-family "Helvetica"
         :padding     0
         :margin      0
         :position    "absolute"
         :left        0
         :top         0
         :right       0
         :bottom      0})

  [:h1 {:font-size "1.2rem"}]

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
                 :width    "170px"
                 :background-color (rgb 90 82 70)}
    [:span
     {:position    "absolute"
      :color       "white"
      :left        "50%"
      :margin-left "-25%"
      :top         "0.5rem"
      :display     "inline-block"
      :font-weight "bold"}]))

(defstyles form-components
  [:.label-input
   {:display         "flex"
    :width           "100%"
    :justify-content "space-between"
    :margin-bottom   "4px"}
   [:input {:width "70%"}]])


; ---- View styles -------------------------------

(defstyles login-styles
  (login
    {:position         "absolute"
     :width            "100%"
     :height           "100%"
     :background-color bg-color-dark}

    [:.login-box
     {:width            "400px"
      :margin-left      "auto"
      :margin-right     "auto"
      :margin-top       "50px"
      :padding          "10px"
      :border-radius    "5px"
      :background-color bg-color-light}]

    [:.login-btn
     {:margin-left "auto"
      :margin-right "auto"
      :width "140px"
      :display "block"
      :margin-top "15px"}]

    [:.no-account
     {:width "150px"
      :display  "block"
      :margin-top  "15px"
      :margin-left "auto"
      :margin-right "auto"}]

    [:.btns
     {:margin-top "15px"
      :display    "flex"
      :justify-content "space-between"}]))

(defstyles chat-view
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
      {:display "block"
       :margin-bottom "2px"
       :background-color bg-color-msg}

      [:&.own-msg
       {:background-color (rgb 236 247 223)}

       [:.user
        {:font-weight "bold"}]]

      [:.user
       {:display "inline-block"
        :padding "4px"
        :height "100%"
        :margin "0px 5px 0px 0px"
        :background-color (rgb 230 230 230)
        :border-right "solid black 1px"}]]])

  (msg-input
    {:padding 0
     :margin 0
     :border 0
     :background-color "white"
     :display "flex"}

    [:.chan-name {:font-size "0.8rem"
                  :margin "5px 5px"}]

    [:#msg-input-field {:flex-grow 1}]))

(defstyles all
  layout
  form-components
  chat-view
  login-styles)