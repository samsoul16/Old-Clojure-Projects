(ns todo-reframe.views
  (:require [todo-reframe.db.core :as db]
            [monger.result :refer [acknowledged?]]))

(defn display-pins []
  (let [result (db/display-pins)]
    (mapv #(select-keys % [:author :title :content :assign :status]) result)))

(defn add-pin [author title content assign]
  (let [result (db/add-pin author title content assign)]
    (if (acknowledged? result)
      (do {:status true
           :pins (display-pins)})
      {:status false})))

(defn done-pin [author title]
  (db/done-pin author title)
  {:pins (display-pins)})

(defn start-pin [author title]
  (db/start-pin author title)
  {:pins (display-pins)})

(defn update-pin [author title content assign]
  (db/update-pin author title content assign)
  {:pins (display-pins)})
