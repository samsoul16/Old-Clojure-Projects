(ns todo-reframe.handlers
  (:require [todo-reframe.db :as db]
            [re-frame.core :refer [dispatch reg-event-db]]))

(reg-event-db
  :initialize-db
  (fn [_ _]
    db/default-db))

(reg-event-db
  :set-active-page
  (fn [db [_ page]]
    (assoc db :page page)))

(reg-event-db
 :set-docs
 (fn [db [_ docs]]
   (assoc db :docs docs)))

(reg-event-db
 :set-counter
 (fn [db [_]]
   (update db :counter inc)))

(reg-event-db
 :reset-counter
 (fn [db [_]]
   (assoc db :counter 0)))

(reg-event-db
 :set-pins
 (fn [db [_ pins]]
   (assoc db :pins pins)))
