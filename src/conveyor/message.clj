(ns conveyor.message
  (:require
    [manifold.stream :as s]))

(defn form-message
  ([src-router msg-type]
   (form-message src-router msg-type {}))
  ([src-router msg-type addtional-fields]
   (merge
     {:type msg-type
      :router-stack [(:uuid src-router)]}
     addtional-fields)))

(defn route-message
  "Adds this router's UUID to the message router stack."
  [src-router message]
  (assoc
    message
    :router-stack
    (conj
      (:router-stack message)
      (:uuid src-router))))

(defn make-discovery
  [src-router]
  (form-message src-router :discovery))

(defn make-announcement
  [src-router]
  (form-message src-router :announcement))

(defn discovery!
  [source-router]
  @(s/put!
     (get-in source-router [:streams :broadcast])
     (make-discovery source-router)))

(defn announce!
  [source-router]
  @(s/put!
     (get-in source-router [:streams :broadcast])
     (make-announcement source-router)))

