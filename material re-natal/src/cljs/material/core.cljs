(ns material.core
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [secretary.core :as secretary :include-macros true]
            [goog.events :as events]
            [goog.history.EventType :as HistoryEventType]
            [markdown.core :refer [md->html]]
            [ajax.core :refer [GET POST]]
            [material.ajax :refer [load-interceptors!]]
            [material.handlers]
            [material.subscriptions]
            [reagent-material-ui.core :as ui]
            [accountant.core :as accountant])
  (:import goog.History))

(enable-console-print!)

(defn log [& msgs]
  (.log js/console (apply str msgs)))


;; some helpers
(def reactify r/as-element)
(defn icon [nme] [ui/FontIcon {:className "material-icons"} nme])
(defn color [nme] (aget ui/colors nme))

;; create a new theme based on the dark theme from Material UI
(defonce theme-defaults {:muiTheme (ui/getMuiTheme
                                    (-> ui/lightBaseTheme
                                        (js->clj :keywordize-keys true)
                                        (update :palette merge {:primary1Color (color "amber500")
                                                                :primary2Color (color "amber700")})
                                        clj->js))})

(defn simple-nav []
  (let [is-open? (r/atom false)
        close #(reset! is-open? false)]
    (fn []
      [:div
       [ui/AppBar {:zDepth 2 :title "sam16 Material UI Demo"
                   :onLeftIconButtonTouchTap #(reset! is-open? true)}]
       [ui/Drawer
        {:docked false :width 200 :open @is-open? :openSecondary true :onRequestChange #(close)}
        [ui/MenuItem {:onClick (fn [_] (secretary/dispatch! "/about") (close))} "About"]
        [ui/MenuItem {:onClick (fn [_] (secretary/dispatch! "/") (close))} "Home"]]
       [ui/DatePicker {:hintText "LandScape Dialog" :mode "landscape"}]])))

(defn home-page []
  (let [state (r/atom 0)
        select (fn [idx] (reset! state idx))]
    (fn []
      [:div
       [:h2 "Welcome to a simple, example application."]
       [ui/Paper {:zDepth 2 }
        [ui/BottomNavigation {:selectedIndex @state}
         (doall (map-indexed (fn [idx x]
                               ^{:key idx}
                               [ui/BottomNavigationItem {:label x
                                                         :icon (reactify [icon "restore"])
                                                         :onClick #(select idx)}])
                             ["Recents" "Favorites" "Nearby"]
                             #_["restore" "favorite" ]))]]])))

(defn about-page []
  [:div.container
   [:div.row
    [:div.col-md-12
     [:img {:src (str js/context "/img/warning_clojure.png")}]]]])

#_(defn home-page []
  [:div.container
   (when-let [docs @(rf/subscribe [:docs])]
     [:div.row>div.col-sm-12
      [:div {:dangerouslySetInnerHTML
             {:__html (md->html docs)}}]])])

(def pages
  {:home #'home-page
   :about #'about-page})

(defn page []
  [ui/MuiThemeProvider theme-defaults
   [:div
    [simple-nav]
    [:div
     [(pages @(rf/subscribe [:page]))]]]])

;; -------------------------
;; Routes
(secretary/set-config! :prefix "#")

(secretary/defroute "/" []
  (rf/dispatch [:set-active-page :home]))

(secretary/defroute "/about" []
  (rf/dispatch [:set-active-page :about]))

;; -------------------------
;; History
;; must be called after routes have been defined
(defn hook-browser-navigation! []
 #_(doto (History.)
    (events/listen
     HistoryEventType/NAVIGATE
     (fn [event]
       (log (.-token event))
       (secretary/dispatch! (.-token event))))
    (.setEnabled true))
  (accountant/configure-navigation!
   {:nav-handler
    (fn [path]
      (log path)
      #_(accountant/navigate! path)
      (secretary/dispatch! path))
    :path-exists?
    (fn [path]
      (log "LOCALTED ?" (secretary/locate-route path))
      (secretary/locate-route path))}))

;; -------------------------
;; Initialize app
(defn fetch-docs! []
  (GET "/docs" {:handler #(rf/dispatch [:set-docs %])}))

(defn mount-components []
  (rf/clear-subscription-cache!)
  (r/render [#'page] (.getElementById js/document "app")))

(defn init! []
  (rf/dispatch-sync [:initialize-db])
  (load-interceptors!)
  (fetch-docs!)
  (hook-browser-navigation!)
  (accountant/dispatch-current!)
  #_(accountant/navigate! "/about")
  (mount-components))
