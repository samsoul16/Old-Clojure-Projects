(ns material.env
  (:require [selmer.parser :as parser]
            [clojure.tools.logging :as log]
            [material.dev-middleware :refer [wrap-dev]]))

(def defaults
  {:init
   (fn []
     (parser/cache-off!)
     (log/info "\n-=[material started successfully using the development profile]=-"))
   :stop
   (fn []
     (log/info "\n-=[material has shut down successfully]=-"))
   :middleware wrap-dev})
