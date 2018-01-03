(ns todo-reframe.env
  (:require [clojure.tools.logging :as log]))

(def defaults
  {:init
   (fn []
     (log/info "\n-=[todo-reframe started successfully]=-"))
   :stop
   (fn []
     (log/info "\n-=[todo-reframe has shut down successfully]=-"))
   :middleware identity})
