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
  (om/set-state! owner :tags resp));;put ajax response to the :tags field of app-state


(defn main-view [app owner]
  (reify
    om/IInitState
    (init-state [_]
      (GET "/tags"
           {:handler #(update-tags % app owner) ;;callback for ajax request which returns tags
            :response-format (json-response-format {:keywords? true})})
      {:tags []})
    om/IRenderState
    (render-state [this state]
        (dom/div nil 
                 (dom/h2 nil "Tag list")
                 (apply dom/ul #js {:className "nav nav-pills nav-stacked"}
                        (om/build-all tag-view (:tags state))))))) ;;will redraw on state change
                 

(om/root main-view app-state
         {:target (. js/document (getElementById "content"))})
