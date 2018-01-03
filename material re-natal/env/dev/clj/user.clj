(ns user
  (:require [mount.core :as mount]
            [material.figwheel :refer [start-fw stop-fw cljs]]
            material.core))

(defn start []
  (mount/start-without #'material.core/repl-server))

(defn stop []
  (mount/stop-except #'material.core/repl-server))

(defn restart []
  (stop)
  (start))


