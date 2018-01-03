(ns todo-reframe.subscriptions
  (:require [re-frame.core :refer [reg-sub]]))

(reg-sub
  :page
  (fn [db _]
    (:page db)))

(reg-sub
 :docs
 (fn [db _]
   (:docs db)))

(reg-sub
 :get-counter
 (fn [db _]
   (or (:counter db) 0)))

(reg-sub
 :get-pins
 (fn [db _]
   (or (:pins db) [{:author "AUTHOR" :title "TITLE" :content "CONTENT" :assign "assign"}])))
