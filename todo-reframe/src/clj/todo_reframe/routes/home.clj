(ns todo-reframe.routes.home
  (:require [todo-reframe.layout :as layout]
            [compojure.core :refer [defroutes GET]]
            [ring.util.http-response :as response]
            [clojure.java.io :as io]
            [todo-reframe.views :as views]
            [clj-http.client :as client]
            [clojure.data.json :as json]))

(defn home-page []
  (layout/render "home.html"))

(defn add-pin [author title content assign]
  (layout/render-json (views/add-pin author title content assign)))

(defn done-pin [author title]
  (layout/render-json (views/done-pin author title)))

(defn start-pin [author title]
  (layout/render-json (views/start-pin author title)))

(defn update-pin [author title content assign]
  (layout/render-json (views/update-pin author title content assign)))

(defn valid? [token]
  (let [token-str (client/get (str "https://www.googleapis.com/oauth2/v3/tokeninfo?id_token=" token))]
    (-> token-str
        :body
        (json/read-str :key-fn keyword)
        (select-keys [:given_name :family_name :email])
        (layout/render-json))))

(defroutes home-routes
  (GET "/" []
       (home-page))
  (GET "/docs" []
       (-> (response/ok (-> "docs/docs.md" io/resource slurp))
           (response/header "Content-Type" "text/plain; charset=utf-8")))
  (GET "/pin" [author title content assign] (add-pin author title content assign))
  (GET "/donepin" [author title] (done-pin author title))
  (GET "/startpin" [author title] (start-pin author title))
  (GET "/passtoken" [token] (valid? token)))
