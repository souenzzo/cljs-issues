(ns demo.core
    (:require [graphql-voyager :as voyager]
              [react :as react]
              [react-dom :as react-dom]))

(defn introspection-provider
  [query]
  (-> (js/fetch (str (-> js/window .-location .-origin) "/graphql")
                #js {:method  "post"
                     :headers #js {:Content-Type "application/json"}
                     :body    (.stringify js/JSON #js {:query query})})
      (.then (fn [response] (.json response)))))


(enable-console-print!)

(.render react-dom (.createElement react voyager/Voyager #js{:introspection introspection-provider} nil))




(println "This text is printed from src/demo/core.cljs. Go ahead and edit it and see reloading in action.")

;; define your app data so that it doesn't get over-written on reload

(defonce app-state (atom {:text "Hello world!"}))


(defn on-js-reload []

)
