(ns ^:figwheel-no-load material.app
  (:require [material.core :as core]
            [devtools.core :as devtools]))

(enable-console-print!)

(devtools/install!)

(core/init!)
