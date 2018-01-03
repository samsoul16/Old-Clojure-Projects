(ns material.env
  (:require [clojure.tools.logging :as log]))

(def defaults
  {:init
   (fn []
     (log/info "\n-=[material started successfully]=-"))
   :stop
   (fn []
     (log/info "\n-=[material has shut down successfully]=-"))
   :middleware identity})
