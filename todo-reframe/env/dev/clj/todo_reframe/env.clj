(ns todo-reframe.env
  (:require [selmer.parser :as parser]
            [clojure.tools.logging :as log]
            [todo-reframe.dev-middleware :refer [wrap-dev]]))

(def defaults
  {:init
   (fn []
     (parser/cache-off!)
     (log/info "\n-=[todo-reframe started successfully using the development profile]=-"))
   :stop
   (fn []
     (log/info "\n-=[todo-reframe has shut down successfully]=-"))
   :middleware wrap-dev})
