(ns todo-reframe.db.core
    (:require [monger.core :as mg]
              [monger.collection :as mc]
              [monger.operators :refer :all]
              [mount.core :refer [defstate]]
              [todo-reframe.config :refer [env]]))

(defstate db*
  :start (-> env :database-url mg/connect-via-uri)
  :stop (-> db* :conn mg/disconnect))

(defstate db
  :start (:db db*))

(defn create-user [user]
  (mc/insert db "users" user))

(defn update-user [id first-name last-name email]
  (mc/update db "users" {:_id id}
             {$set {:first_name first-name
                    :last_name last-name
                    :email email}}))

(defn get-user [id]
  (mc/find-one-as-map db "users" {:_id id}))


;;;;;;;;;;;;; sam16's Pin Board

;; Inserts a New PIN
(defn add-pin [author title content assign]
  (mc/insert db "pins"
             {:author author :title title :content content :assign assign :status "pending"}))

;; Displays all pins from Db
(defn display-pins []
  (mc/find-maps db "pins"))

;; Update an Exisiting Pin
(defn update-pin [author title content assign]
  (mc/update db "pins" {:author author :title title}
             {$set {:content content :assign assign}}))

;; Mark Pin as Complete
(defn done-pin [author title]
  (mc/update db "pins" {:author author :title title} {$set {:status "done"}}))

(defn start-pin [author title]
  (mc/update db "pins" {:author author :title title} {$set {:status "started"}}))
