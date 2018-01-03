(ns user
  (:require [mount.core :as mount]
            [todo-reframe.figwheel :refer [start-fw stop-fw cljs]]
            todo-reframe.core))

(defn start []
  (mount/start-without #'todo-reframe.core/repl-server))

(defn stop []
  (mount/stop-except #'todo-reframe.core/repl-server))

(defn restart []
  (stop)
  (start))


