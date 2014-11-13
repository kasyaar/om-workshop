(ns gtd.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [ajax.core :refer [GET json-response-format]]
            [cljs.core.async :refer [put! chan <!]]
            [clojure.data :as data]
            [clojure.string :as string]))

(def app-state (atom))
;;view for tags
(defn tag-view "doc-string" [tag owner]
  (reify
    om/IRender
    (render [this]
      (dom/li nil  
              (dom/a #js {:onClick #(.log js/console (str tag))} 
                     (:name tag) 
                     (dom/span #js {:className "badge"} (:member_count tag)))))))

(defn update-tags "doc-string" [resp app owner]
  (om/set-state! owner :sidebar-title "Tags list")
  (om/set-state! owner :sidebar resp));;put ajax response to the :tags field of app-state

(defmulti list-entry-view  (fn [entry _] (:type entry)))
(defmethod list-entry-view "tag"
  [entry owner] (tag-view entry owner) )

(defn list-view "doc-string" [app owner]
  (reify
    om/IRender
    (render [this]
        (dom/div nil 
                 (dom/h2 nil (:title app))
                 (apply dom/ul #js {:className "nav nav-pills nav-stacked"}
                        (om/build-all list-entry-view (:items app)))
                 )))) ;;will redraw on state change

(defn sidebar-view "doc-string" [app owner]
  (reify
    om/IInitState
    (init-state [_]
      (GET "/tags"
           {:handler #(update-tags % app owner) ;;callback for ajax request which returns tags
            :response-format (json-response-format {:keywords? true})})
      {:sidebar []})
    om/IRenderState
    (render-state [app state]
      (om/build list-view {:items (:sidebar state) :title "Tags list"}))))


(om/root sidebar-view app-state
         {:target (. js/document (getElementById "tags"))})

(om/root (fn [app owner] 
           (reify 
           om/IRender
           (render [_]
                   (dom/h2 nil "List memebers")))) app-state
         {:target (. js/document (getElementById "members"))})
