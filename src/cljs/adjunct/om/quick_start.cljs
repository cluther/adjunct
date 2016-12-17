(ns adjunct.om.quick-start
  (:require [goog.dom :as gdom]
            [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]))

(println "Loaded adjunct.om.quick-start.")

(def app-state
  (atom
    {:app/title "Animals"
     :animals/list
                [[1 "Ant"] [2 "Antelope"] [3 "Bird"] [4 "Cat"] [5 "Dog"]
                 [6 "Lion"] [7 "Mouse"] [8 "Monkey"] [9 "Snake"] [10 "Zebra"]]}))

(defmulti read (fn [env key params] key))

(defmethod read :default
  [{:keys [state]} key _]
  (let [st @state]
    (if-let [[_ value] (find st key)]
      {:value value}
      {:value :not-found})))

(defmethod read :animals/list
  [{:keys [state]} key {:keys [start end]}]
  {:value (subvec (:animals/list @state) start end)})

(defui AnimalsList
  static om/IQueryParams
  (params [this] {:start 0 :end 10})

  static om/IQuery
  (query [this] '[:app/title (:animals/list {:start ?start :end ?end})])

  Object
  (render [this]
    (let [{:keys [app/title animals/list]} (om/props this)]
      (dom/div
        nil
        (dom/h2 nil title)
        (apply
          dom/ul nil (map (fn [[i name]] (dom/li nil (str i ". " name))) list))))))

(def reconciler
  (om/reconciler {:state app-state :parser (om/parser {:read read})}))

(om/add-root! reconciler AnimalsList (gdom/getElement "app"))

(comment
  (def app-state (atom {:counter 0}))

  (defui Counter
         static om/IQuery
         (query [this] [:count])

         Object
         (render [this]
                 (let [{:keys [count]} (om/props this)]
                   (dom/div nil
                            (dom/span nil (str "Count: " count))
                            (dom/button
                              #js {:onClick (fn [e] (om/transact! this '[(increment)]))}
                              "Click me!")))))

  (defn read
    [{:keys [state] :as env} key params]
    (let [st @state]
      (if-let [[_ value] (find st key)]
        {:value value}
        {:value :not-found})))

  (defn mutate
    [{:keys [state] :as env} key params]
    (if (= 'increment key)
      {:value  {:keys [:count]}
       :action #(swap! state update-in [:count] inc)}
      {:value :not-found}))

  (def reconciler
    (om/reconciler {:state  app-state
                    :parser (om/parser {:read read :mutate mutate})}))

  (om/add-root! reconciler Counter (gdom/getElement "app")))


(comment
  (defui DivText
         Object
         (render [this]
                 (dom/div nil (get (om/props this) :text))))

  (def div-text (om/factory DivText))

  (js/ReactDOM.render
    (apply dom/div nil
           (map #(div-text {:react-key % :text (str "number " %)}) (range 3)))
    (gdom/getElement "app")))