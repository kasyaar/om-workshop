(ns gtd.core
  (:use 
    hbs.core
    compojure.core
    [compojure.handler :only [site]]
    [ring.middleware.cookies :only [wrap-cookies]]
    [ring.middleware.params :only [wrap-params]]
    [ring.middleware.keyword-params :only [wrap-keyword-params]]
    [twitter.oauth]
    [twitter.callbacks]
    [twitter.callbacks.handlers]
    [twitter.api.restful]
    [com.ashafa.clutch :only [with-db put-document update-document get-document]]
    [cheshire.core :only [generate-string]]
    org.httpkit.server)
  (:require 
    [ring.util.response :as response]
    [oauth.client :as oauth]
    [compojure.route :as route]
    [clojure.java.io :as io])
  (:gen-class))
(def cons-key     "V1okDMhoTeNqR4zTdd41dcY1L")
(def cons-sec    "Gf8uPEZ9R7ZgGdgB1RYSwitI1KhxygtsASFJaSHQWvCtaeRDip")
(def consumer
  (oauth/make-consumer
    cons-key
    cons-sec
    "https://api.twitter.com/oauth/request_token"
    "https://api.twitter.com/oauth/access_token"
    "https://api.twitter.com/oauth/authorize"
    :hmac-sha1)
  )

(def request-token (oauth/request-token consumer "http://localhost:3000/twitter_callback"))
(set-template-path! "/templates" ".html")
(def db "http://localhost:5984/simple")

(defn get-creds "doc-string" [{{token :access-token secret :access-token-secret} :auth}]
          (make-oauth-creds cons-key cons-sec token secret))

(defn index "doc-string" 
  [params {{sessid :value} "sessid"}]
    (str sessid)
    (if (= sessid nil)
      (render-file "signup" {:title "Sign Up with Twitter"})
      (let [user-data (with-db db (get-document sessid))]
        (render-file "index" {:title "Getting tweets done"}))))

(defn sync-tags "doc-string" [user-data] 
  (let [creds (get-creds user-data)
        lists (:body (lists-list :oauth-creds creds))
        tags (map (fn [lst] {:name (:slug lst) :members (:member_count lst)}) lists)
        {sessid :_id} user-data]
    (with-db db 
      (-> 
        (get-document sessid) 
        (update-document {:tags tags})))))

(defn tags "doc-string" [{{sessid :value} "sessid"}]
  (let [ user-data (with-db db (get-document sessid))
        {tags :tags} user-data]
    (response/header 
      (response/response 
        (if (= tags nil) 
          (generate-string (:tags (sync-tags user-data)) [:pretty true])
          (generate-string tags {:pretty true}))) 
      "Content-Type" "application/json")))


(defn signup "doc-string" [req]
  (let [auth-url 
        (oauth/user-approval-uri consumer (:oauth_token request-token))]
    (response/redirect auth-url)))

(defn twitter_callback "doc-string" [{token :oauth_token verifier :oauth_verifier}]
 (let [{access-token :oauth_token access-sec :oauth_token_secret} (oauth/access-token consumer request-token verifier)
       creds (make-oauth-creds cons-key cons-sec access-token access-sec)
       user-details (:body (account-verify-credentials :oauth-creds creds))
       user-name (:screen_name user-details)
       {sessid :_id} (with-db db 
                       (put-document 
                         {:type "user" 
                          :name user-name 
                          :details user-details 
                          :auth {:access-token access-token :access-token-secret access-sec}}))]
   (response/set-cookie 
     (response/redirect "/") "sessid" (str sessid))))

(defroutes all-routes
  (GET "/" {params :params cookies :cookies} (index params cookies))
  (GET "/signup" [] signup)
  (GET "/twitter_callback" {params :params} (twitter_callback params))
  (GET "/tags" {cookies :cookies} (tags cookies))
  (route/files "/")
  (route/resources "/")
  (route/not-found "Page not found"))


(defn -main [port] 
  (run-server (site (-> #'all-routes wrap-cookies wrap-keyword-params wrap-params))  {:port (Integer. port)})
  (println (str "Server started on " port " port")))
