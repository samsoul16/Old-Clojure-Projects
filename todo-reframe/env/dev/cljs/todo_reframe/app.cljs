(ns ^:figwheel-no-load todo-reframe.app
  (:require [todo-reframe.core :as core]
            [devtools.core :as devtools]))

(enable-console-print!)

(devtools/install!)

(core/init!)
