(ns demo.core
  (:require [io.pedestal.http :as http]
            [io.pedestal.http.route :as route]
            [io.pedestal.http.body-params :as body-params]
            [io.pedestal.http.route.definition :refer [defroutes]]
            [io.pedestal.http.jetty.util :as jetty-util]
            [io.pedestal.http.csrf :as csrf]
            [ring.util.response :as ring-resp]
            [clojure.string :as string])
  (:import (org.eclipse.jetty.server.handler.gzip GzipHandler)))

(defn about-page
  [request]
  (ring-resp/response (format "Clojure %s - served from %s"
                              (clojure-version)
                              (route/url-for ::about-page))))

(defn huge-text
  [req]
  {:body   (string/join (repeat 10000000 "_"))
   :status 200})

(defn home-page
  [{::csrf/keys [anti-forgery-token]}]
  (ring-resp/response (format "<!DOCTYPE html>
  <html>
  <head>
    <script>
    var anti_forgery_token = '%s'
    function myGet() {
      fetch('/huge')
    }

    function myPost() {
      fetch('/huge', { headers: {'X-CSRF-Token': anti_forgery_token}
                     , method: 'POST'})
    }

    function myGetWithToken() {
      fetch('/huge', { headers: {'X-CSRF-Token': anti_forgery_token}
                     , method: 'GET'})
    }

    </script>
  </head>
  <body>
  <button onClick='myGet()'>GET</button>
  <button onClick='myPost()'>POST</button>
  <button onClick='myGetWithToken()'>GET with token</button>
  </body>
  </html>



  " anti-forgery-token)))

(defroutes routes
  [[["/" {:get home-page}
     ;; Set default interceptors for /about and any other paths under /
     ^:interceptors [(body-params/body-params) http/html-body]
     ["/about" {:get about-page}]
     ["/huge" {:any huge-text}]]]])

(def service
  {:env                     :prod
   ::http/routes            routes
   ::http/enable-csrf       {}
   ::http/secure-headers    {:content-security-policy-settings (string/join " " ["script-src"
                                                                                 "'self'"
                                                                                 "'unsafe-inline'"
                                                                                 "'unsafe-eval'"])}
   ::http/enable-session    {}
   ::http/resource-path     "/public"
   ::http/type              :jetty
   ::http/container-options {:context-configurator (fn [c]
                                                     (let [gzip-handler (GzipHandler.)]
                                                       (.setGzipHandler ^GzipHandler c gzip-handler)
                                                       c))}
   ::http/port              8081})


; This is an adapted service map, that can be started and stopped
;; From the REPL you can call server/start and server/stop on this service
(defonce http-server (atom nil))

(defn -main
  "The entry-point for 'lein run-dev'"
  [& args]
  (println "\nCreating your [DEV] server...")
  (swap! http-server (fn [st]
                       (when st
                         (http/stop st))
                       (-> service
                           (merge {:env                   :dev
                                   ::http/join?           false
                                   ::http/routes          #(deref #'routes)
                                   ::http/allowed-origins {:creds true :allowed-origins (constantly true)}})
                           http/default-interceptors
                           http/dev-interceptors
                           http/create-server
                           http/start))))
