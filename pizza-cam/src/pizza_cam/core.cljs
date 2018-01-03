(ns pizza-cam.core
    (:require [reagent.core :as r :refer [atom]]
              [re-frame.core :refer [subscribe dispatch dispatch-sync]]
              [pizza-cam.handlers]
              [pizza-cam.subs]))

(def ReactNative (js/require "react-native"))

(def app-registry (.-AppRegistry ReactNative))
(def text (r/adapt-react-class (.-Text ReactNative)))
(def view (r/adapt-react-class (.-View ReactNative)))
(def image (r/adapt-react-class (.-Image ReactNative)))
(def touchable-highlight (r/adapt-react-class (.-TouchableHighlight ReactNative)))
(def Alert (.-Alert ReactNative))
(defonce Expo (js/require "expo"))
(defn ex-comp [name]
  (-> Expo
      (aget name)
      r/adapt-react-class))
(defonce camera (ex-comp "Camera"))
(defonce permissions (.-Permissions Expo))
(defonce notifications (.-Notifications Expo))
(defn alert [title]
  (.alert Alert title))
(defonce slice1 (js/require "./assets/images/slice1.png"))
(defonce slice2 (js/require "./assets/images/slice2.png"))
(defonce slice3 (js/require "./assets/images/slice3.png"))
(defonce slice4 (js/require "./assets/images/slice4.png"))
(defonce slice5 (js/require "./assets/images/slice5.png"))
(defonce slice6 (js/require "./assets/images/slice6.png"))
(def full-pizza (js/require "./assets/images/pizza.png"))

(defn get-camera-perm [permissions?]
  (-> (.askAsync permissions (.-CAMERA permissions))
      (.then (fn [resp]
               (println (str "GOT RESP: " (.-status resp)))
               (if (= (.-status resp) "granted")
                 (reset! permissions? true)
                 (reset! permissions? false))))
      (.catch (fn [err] (println (str "ERROR:" err)) (reset! permissions? false)))))

(defn get-notif-perm-then-expo-token [expo-token]
  (-> (.askAsync permissions (.-NOTIFICATIONS permissions))
      (.then (fn [resp]
               (println (str "GOT RESP: " (.-status resp)))
               (if (= (.-status resp) "granted")
                 (do (println "NOTIFICATIONS PERM GRANTED")
                     (-> (.getExpoPushTokenAsync notifications)
                         (.then (fn [token]
                                  (println token)
                                  (reset! expo-token token)))
                         (.catch #(println (str "ERROR >>>" %)))))
                 (do (println "NOTIFICATIONS PERM NOT GRANTED")
                     (reset! permissions? false)))))
      (.catch (fn [err] (println (str "ERROR:" err)) (reset! permissions? false)))))

#_(defn notification-handler [notification page-name]
  (let [status (((js->clj notification) "data") "status")]
    (condp = page-name
      "payment" (condp = status
                  ;;FIXME: pls enter the page name that you need in both the conditions
                  ;; TODO: Add CleverTap call to change user-status
                  "credit" (dispatch [:set-key-val :next-page "Order-Confirmed"])
                  "failed" (dispatch [:set-key-val :next-page "Videos"])))))

(defn receive-notification []
  (.addListener notifications #(println %)))


(defn app-root []
  (let [greeting (subscribe [:get-greeting])
        permissions? (r/atom nil)
        expo-token (r/atom nil)
        pizza (r/atom {:count 0
                       :slice1 true
                       :slice2 true
                       :slice3 true
                       :slice4 true
                       :slice5 true
                       :slice6 true
                       :pizza false})
        on-click (fn [slice]
                   (swap! pizza update-in [:count] inc)
                   (swap! pizza assoc slice false)
                   (when (= 6 (:count @pizza))
                     (swap! pizza assoc :visilbe true)))]
    (fn []
      (receive-notification)
      [view {:style {:flex 1 :justify-content "center"}}
       (println @pizza)
       (condp = @permissions?
         nil [view
              [text {:style {:text-align "center" :font-weight "bold"}} "No permissions"]
              [touchable-highlight {:style {:background-color "#999" :padding 10 :border-radius 5}
                                    :on-press #(get-notif-perm-then-expo-token expo-token)}
               [text {:style {:color "white" :text-align "center" :font-weight "bold"}}
                "GET NOTIF PERM THEN EXPO TOKEN"]]
              [touchable-highlight {:style {:background-color "#999" :padding 10 :border-radius 5}
                                    :on-press #(get-camera-perm permissions?)}
               [text {:style {:color "white" :text-align "center" :font-weight "bold"}} "GET CAMERA PERM"]]]
         true [camera {:style {:flex 1}}
               [view {:style {:flex 1}}
                (when (:slice1 @pizza)
                  [touchable-highlight {:on-press (fn []
                                                    (alert "Selected Slice 1")
                                                    (on-click :slice1))}
                   [image {:source slice1
                           :style {:height 50 :width 50 :align-self "flex-end"}}]])
                (when (:slice2 @pizza)
                  [touchable-highlight {:on-press (fn []
                                                    (alert "Selected Slice 2")
                                                    (on-click :slice2))}
                   [image {:source slice2
                           :style {:height 50 :width 50}}]])
                (when (:slice3 @pizza)
                  [touchable-highlight {:on-press (fn []
                                                    (alert "Selected Slice 3")
                                                    (on-click :slice3))}
                   [image {:source slice3
                           :style {:height 50 :width 50}}]])
                (when (:slice4 @pizza)
                  [touchable-highlight {:on-press (fn []
                                                    (alert "Selected Slice 4")
                                                    (on-click :slice4))}
                   [image {:source slice4
                           :style {:height 50 :width 50}}]])
                (when (:slice5 @pizza)
                  [touchable-highlight {:on-press (fn []
                                                    (alert "Selected Slice 5")
                                                    (on-click :slice5))}
                   [image {:source slice5
                           :style {:height 50 :width 50}}]])
                (when (:slice6 @pizza)
                  [touchable-highlight {:style {:background-color "#fff"}
                                        :on-press (fn []
                                                    (alert "Selected Slice 6")
                                                    (on-click :slice6))}
                   [image {:source slice6
                           :style {:height 50 :width 50}}]])
                (when (:visilbe @pizza)
                  [image {:source full-pizza
                          :style {:align-self "center" :height 100 :width 100}}])]]
         false [view [text {:style {:text-align "center" :font-weight "bold"}} "permissions not granted"]])])))

(defn init []
  (dispatch-sync [:initialize-db])
  (.registerComponent app-registry "main" #(r/reactify-component app-root)))
