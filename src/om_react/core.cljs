(ns om-react.core
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]))

(enable-console-print!)

(def app-state (atom {:products [{:category "Sporting Goods" :price "$49.99" :stocked true :name "Football"},
                                 {:category "Sporting Goods" :price "$9.99" :stocked true :name "Baseball"},
                                 {:category "Sporting Goods" :price "$29.99" :stocked false :name "Basketball"},
                                 {:category "Electronics" :price "$99.99" :stocked true :name "iPod Touch"},
                                 {:category "Electronics" :price "$399.99" :stocked false :name "iPhone 5"},
                                 {:category "Electronics" :price "$199.99" :stocked true :name "Nexus 7"}]}))

(defn product-row
  [product owner]
  (reify
    om/IRender
    (render [obj]
      (dom/tr nil
              (if (:stocked product)
                (dom/td nil (:name product))
                (dom/td nil
                        (dom/span #js {:style #js {:color "red"}} (:name product))))
              (dom/td nil (:price product))))))

(defn product-category-row
  [product owner]
  (reify
    om/IRender
    (render [obj]
      (dom/tr nil
              (dom/th #js {:colSpan 2} (:category product))))))

(defn build-product-rows
  [products]
  (into-array
   (loop [products products
          last-category nil
          idx 0
          acc []]
     (if-let [{:keys [category price stocked name] :as product} (first products)]
       (if-not (= category last-category)
         (recur (rest products)
                category
                (inc (inc idx))
                (conj acc
                      (om/build product-category-row product {:om.core/index idx})
                      (om/build product-row product {:om.core/index (inc idx)})))
         (recur (rest products)
                category
                (inc idx)
                (conj acc (om/build product-row product {:om.core/index idx}))))
       acc))))

(defn product-table
  [products owner]
  (reify
    om/IRender
    (render [obj]
      (dom/table nil
                 (dom/thead nil
                            (dom/tr nil
                                    (dom/th nil "Name")
                                    (dom/th nil "Price")))
                 (dom/tbody nil (build-product-rows products))))))

(defn filterable-product-table
  [products owner]
  (reify
    om/IRender
    (render [obj]
      (dom/div nil
               (om/build product-table products)))))

(om/root
  app-state
  (fn [app owner]
    (om/build filterable-product-table (:products app))
    #_(dom/h1 nil (:text app)))
  (. js/document (getElementById "app")))
