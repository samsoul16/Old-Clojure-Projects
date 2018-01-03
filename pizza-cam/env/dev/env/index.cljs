(ns env.index
  (:require [env.dev :as dev]))

;; undo main.js goog preamble hack
(set! js/window.goog js/undefined)

(-> (js/require "figwheel-bridge")
    (.withModules #js {"./assets/images/slice5.png" (js/require "../../../assets/images/slice5.png"), "./assets/images/pizza.png" (js/require "../../../assets/images/pizza.png"), "./assets/images/slice4.png" (js/require "../../../assets/images/slice4.png"), "./assets/images/slice3.png" (js/require "../../../assets/images/slice3.png"), "./assets/icons/loading.png" (js/require "../../../assets/icons/loading.png"), "expo" (js/require "expo"), "./assets/images/cljs.png" (js/require "../../../assets/images/cljs.png"), "./assets/icons/app.png" (js/require "../../../assets/icons/app.png"), "./assets/images/slice1.png" (js/require "../../../assets/images/slice1.png"), "react-native" (js/require "react-native"), "react" (js/require "react"), "./assets/images/slice6.png" (js/require "../../../assets/images/slice6.png"), "create-react-class" (js/require "create-react-class"), "./assets/images/slice2.png" (js/require "../../../assets/images/slice2.png")}
)
    (.start "main" "expo" "192.168.0.127"))
