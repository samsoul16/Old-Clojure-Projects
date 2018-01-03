(ns todo-reframe.core
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [secretary.core :as secretary]
            [goog.events :as events]
            [goog.history.EventType :as HistoryEventType]
            [markdown.core :refer [md->html]]
            [ajax.core :refer [GET POST]]
            [todo-reframe.ajax :refer [load-interceptors!]]
            [todo-reframe.handlers]
            [todo-reframe.subscriptions]
            [cljs.core.async :refer [put! chan <! >! buffer]])
  (:import goog.History))


;; Server address for requests
(def server "http://localhost:3000/")

;; Global get by id
(defn get-by-id [id] (.-value (.getElementById js/document id)))

;; Log it is
(defn log [& msgs]
  (.log js/console (apply str msgs)))

(defn nav-link [uri title page collapsed?]
  (let [selected-page (rf/subscribe [:page])]
    #_(.log js/console @selected-page)
    (when (= @selected-page :update)
      (rf/dispatch [:reset-counter]))
    [:li.nav-item
     {:class (when (= page @selected-page) "active")}
     [:a.nav-link
      {:href uri
       :on-click #(reset! collapsed? true)} title]]))

(defn navbar []
  (r/with-let [collapsed? (r/atom true)]
    [:nav.navbar.navbar-dark.bg-primary
     [:button.navbar-toggler.hidden-sm-up
      {:on-click #(swap! collapsed? not)} "☰"]
     [:div.collapse.navbar-toggleable-xs
      (when-not @collapsed? {:class "in"})
      [:a.navbar-brand {:href "#/"} "PIN-BOARD"]
      [:ul.nav.navbar-nav
       [nav-link "#/" "Add PIN" :home collapsed?]
       [nav-link "#/about" "All PINS" :about collapsed?]
       #_[nav-link "#/update" "Completed PINS" :update collapsed?]]]]))

;; If error log in console
(defn error-handler [params]
  (log "ERRORS >>>>>" params))

(defn handled-call [params]
  (log "HANDLED >>>>>>>>>>" params))

(defn completed-pin [response]
  (do (log (:pins response))
      (rf/dispatch [:set-pins (:pins response)])
      (js/alert "Completed Pin")))

(defn started-pin [response]
  (do (log (:pins response))
      (rf/dispatch [:set-pins (:pins response)])
      (js/alert "Started Pin")))

(defn about-page []
  [:div
   (let [pins @(rf/subscribe [:get-pins])]
     (log "PIN SUBSCRIBER : " pins)
     [:div.pin-board
      (doall
       (map (fn [x]
              ^{:key x} [:div.card {:className (condp = (:status x)
                                                 "pending" "default"
                                                 "started" "started"
                                                 "done" "completed")}
                         [:div.card-header.bg-inverse.text-white  (:author x)]
                         [:div.card-body
                          [:h4.card-title (:title x)]
                          [:p.card-text (:content x)]
                          (:assign x)
                          [:div.option
                           [:button {:className "btn btn-warning"
                                     :on-click (fn [e]
                                                 (let [author (:author x)
                                                       title (:title x)]
                                                   (log "DONE >>>" author title)
                                                   (GET (str server "startpin")
                                                        {:params {:author author :title title}
                                                         :format :json
                                                         :response-format :json
                                                         :keywords? true
                                                         :handler started-pin
                                                         :error-handler error-handler})))} "STARTED"]
                           [:button {:className "btn btn-danger"
                                     :on-click (fn [e]
                                                 (let [author (:author x)
                                                       title (:title x)]
                                                   (log "DONE >>>" author title)
                                                   (GET (str server "donepin")
                                                        {:params {:author author :title title}
                                                         :format :json
                                                         :response-format :json
                                                         :keywords? true
                                                         :handler completed-pin
                                                         :error-handler error-handler})))} "COMPLETED"]]]])
            pins))])])

(defn update-page []
  [:div.container
   [:div.row>div.col-sm-12
    [:h2 "The Value of Counter is : " @(rf/subscribe [:get-counter])]
    [:button {:on-click #(rf/dispatch [:set-counter]) } "CLICK ME"]]])

;; Add Todo handler
(defn added-pin? [response]
  (if (:status response)
    (do (log (:pins response))
        (rf/dispatch [:set-pins (:pins response)])
        (js/alert "Added a New ToDo"))
    (js/alert "Failed to add  a New ToDo")))


;;;;;;;;;;;;;;;;NEW HOME PAGE
(enable-console-print!)
(def user (r/atom {}))

(defn load-gapi-auth2 []
  (let [c (chan)]
    (.load js/gapi "auth2" #(go (>! c true)))
    c))

(defn auth-instance []
  (.getAuthInstance js/gapi.auth2))

(defn get-google-token []
  (-> (auth-instance) .-currentUser .get .getAuthResponse .-id_token))

(defn handle-user-change
  [token]
  (GET (str server "passtoken")
       {:params {:token (get-google-token)}
        :format :json
        :response-format :json
        :keywords? true
        :handler handled-call
        :error-handler error-handler})
  (let [profile (.getBasicProfile token)]
    (reset! user
            {:name       (if profile (.getName profile) nil)
             :image-url  (if profile (.getImageUrl profile) nil)
             :token      (get-google-token)
             :signed-in? (.isSignedIn token)})
    (println (str {:name       (if profile (.getName profile) nil)
                   :image-url  (if profile (.getImageUrl profile) nil)
                   :token      (get-google-token)
                   :signed-in? (.isSignedIn token)}))))

(defonce _ (go
             (<! (load-gapi-auth2))
             (.init js/gapi.auth2
                    (clj->js {"client_id"
                      "820624577284-pnnnj9h7reov4pkk0i3n3fkiiq6s28o4.apps.googleusercontent.com" "scope" "profile"}))
             (let [current-user (.-currentUser (auth-instance))]
               (.listen current-user handle-user-change))))

(defn home-page []
  [:div
   (if-not (:signed-in? @user) [:a {:href "#" :on-click #(.signIn (auth-instance))} "Sign in with Google"]
           [:div
            [:p
             [:strong (:name @user)]
             [:br]
             [:img {:src (:image-url @user)}]]
            [:a {:href "#" :on-click #(.signOut (auth-instance))} "Sign Out"]])])




#_(defn home-page []
  [:div.container
   [:div.row>div.col-sm-8
    [:div {:className "input-group input-group-lg"} [:input {:type "text" :className "form-control" :id "author" :placeholder "Author Name?"}]]
    [:div {:className "input-group input-group-lg"} [:input {:type "text" :className "form-control" :id "title" :placeholder "Enter Title"}]]
    [:div {:className "input-group input-group-lg"} [:textarea {:className "form-control" :rows "6" :id "content" :placeholder "Enter Description"}]]
    [:div {:className "input-group input-group-lg"} [:input {:type "text" :className "form-control" :id "assign" :placeholder "Assigned to?"}]]
    [:button {:className "btn btn-primary"
              :on-click (fn [e]
                          (let [author (get-by-id "author")
                                title (get-by-id "title")
                                content (get-by-id "content")
                                assign (get-by-id "assign")]
                            (log "IP TO ADDPIn >>>" author title content assign)
                            (GET (str server "pin")
                                 {:params {:author author :title title
                                           :content content :assign assign}
                                  :format :json
                                  :response-format :json
                                  :keywords? true
                                  :handler added-pin?
                                  :error-handler error-handler})))} "Add Pin To Pin Board"]]])

(def pages
  {:home #'home-page
   :about #'about-page
   :update #'update-page})

(defn page []
  [:div
   [navbar]
   [(pages @(rf/subscribe [:page]))]])

;; -------------------------
;; Routes
(secretary/set-config! :prefix "#")

(secretary/defroute "/" []
  (rf/dispatch [:set-active-page :home]))

(secretary/defroute "/about" []
  (rf/dispatch [:set-active-page :about]))

(secretary/defroute "/update" []
  (rf/dispatch [:set-active-page :update]))

;; -------------------------
;; History
;; must be called after routes have been defined
(defn hook-browser-navigation! []
  (doto (History.)
    (events/listen
      HistoryEventType/NAVIGATE
      (fn [event]
        (secretary/dispatch! (.-token event))))
    (.setEnabled true)))

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
  (mount-components))
