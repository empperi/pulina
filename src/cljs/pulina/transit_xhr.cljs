(ns pulina.transit-xhr
  (:require [cognitect.transit :as tr]
            [com.cognitect.transit.types :as trt]
            [goog.events :as events]
            [taoensso.timbre :as tre])
  (:import [goog.net XhrIo]))

(def http-methods
  "The HTTP request codes we support Transit payloads in."
  {:get "GET"
   :post "POST"})

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Encoding/decoding

(def read-handlers
  {"f"
   (tr/read-handler
     (fn [v] (trt/floatValue v)))})

(def transit-json-reader
  "A transit+json reader, using our custom handlers."
  (tr/reader :json {:handlers read-handlers}))

(def transit-json-writer
  "A transit+json writer, using our custom handlers."
  (tr/writer :json))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Public

(defn transit-xhr
  "Makes an asynchronous HTTP request, encoding/decoding the data
  sent/received as `transit+json`. Calls `on-complete` with the returned
  (decoded) data."
  [{:keys [method url data on-complete on-failure]}]
  (let [xhr (XhrIo.)]
    (events/listen
      xhr goog.net.EventType.COMPLETE
      (fn [e]
        (let [resp (.getResponseText xhr)
              content-type (.getResponseHeader xhr "Content-Type")
              status  (.getStatus xhr)
              handler (if (= 200 status)
                        #(on-complete %)
                        #((or on-failure identity) status %))]
          (if (= "application/transit+json" content-type)
            (handler (tr/read transit-json-reader resp))
            (handler resp)))))
    (. xhr
       (send url
             (http-methods method)
             (when data (tr/write transit-json-writer data))
             #js {"Content-Type" "application/transit+json"}))))
