(ns om-react.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [clojure.string :as str]
            [cljs.core.async :refer [<! chan put! sliding-buffer]]))

(enable-console-print!)

(def app-state (atom {:products [{:category "Sporting Goods" :price "$49.99" :stocked true :name "Football"},
                                 {:category "Sporting Goods" :price "$9.99" :stocked true :name "Baseball"},
                                 {:category "Sporting Goods" :price "$29.99" :stocked false :name "Basketball"},
                                 {:category "Electronics" :price "$99.99" :stocked true :name "iPod Touch"},
                                 {:category "Electronics" :price "$399.99" :stocked false :name "iPhone 5"},
                                 {:category "Electronics" :price "$199.99" :stocked true :name "Nexus 7"}]}))

(defn product-row
  [product owner]
  (om/component
   (dom/tr nil
           (if (:stocked product)
             (dom/td nil (:name product))
             (dom/td nil
                     (dom/span #js {:style #js {:color "red"}} (:name product))))
           (dom/td nil (:price product)))))

(defn product-category-row
  [product owner]
  (om/component
   (dom/tr nil
           (dom/th #js {:colSpan 2} (:category product)))))

(defn build-product-rows
  [products]
  ;;a better structured product data structure would make this code cleaner
  (into-array
   (loop [products products
          last-category nil
          acc []]
     (if-let [{:keys [category price stocked name] :as product} (first products)]
       (recur (rest products)
              category
              (if-not (= category last-category)
                (conj acc
                      (om/build product-category-row product)
                      (om/build product-row product))
                (conj acc (om/build product-row product))))
       acc))))

(defn filter-products
  [filter-text in-stock-only products]
  (letfn [(by-text [product]
            (if-not (str/blank? filter-text)
              (>= (.indexOf (:name product) filter-text) 0)
              true))
          (by-stock [product]
            (if in-stock-only
              (:stocked product)
              true))]
    (->> products
         (filter by-stock)
         (filter by-text))))

(defn product-table
  [products owner]
  (reify
    om/IInitState
    (init-state [_]
      {:filter-text "" :in-stock-only false})
    om/IWillMount
    (will-mount [_]
      (if-let [event-chan (om/get-state owner :channel)]
        (go (while true
              (let [[-type -value] (<! event-chan)]
                (condp = -type
                  :filter (om/set-state! owner :filter-text -value)
                  :stock  (om/set-state! owner :in-stock-only -value)))))
        (.warn js/console "Event channel should have been given as initial state.")))
    om/IRenderState
    (render-state [_ {:keys [filter-text in-stock-only]}]
      (let [filtered (filter-products filter-text in-stock-only products)]
        (dom/table nil
                   (dom/thead nil
                              (dom/tr nil
                                      (dom/th nil "Name")
                                      (dom/th nil "Price")))
                   (dom/tbody nil (build-product-rows filtered)))))))

(defn search-bar
  [cursor owner]
  (reify
    om/IRenderState
    (render-state [_ {:keys [channel]}]
      (dom/form nil
                (dom/input #js {:type "text" :placeholder "Search..."
                                :onChange #(put! channel [:filter (.. % -target -value)])})
                (dom/p nil
                       (dom/input #js {:type "checkbox"
                                       :onChange #(put! channel [:stock (.. % -target -checked)])})
                       "Only show products in stock")))))

(defn filterable-product-table
  [cursor owner]
  (reify
    om/IInitState
    (init-state [_]
      {:channel (chan (sliding-buffer 1))})
    om/IRenderState
    (render-state [_ {:keys [channel]}]
      (dom/div nil
               (om/build search-bar cursor {:init-state {:channel channel}})
               (om/build product-table (:products cursor) {:init-state {:channel channel}})))))

(om/root app-state filterable-product-table (. js/document (getElementById "app")))
