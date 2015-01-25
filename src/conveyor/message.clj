(ns conveyor.message
  (:require
    [manifold.stream :as s]))

(defn make-message
  [source-router message-type message]
  {:type message-type
   :router-stack [(:uuid source-router)]
   :message message})

(defn route-message
  [src-router message]
  (assoc
    message
    :router-stack
    (conj
      (:router-stack message)
      (:uuid src-router))))

(defn make-discovery
  [source-router]
  (make-message source-router :discovery nil))

(defn make-announcement
  [source-router]
  (make-message source-router :announcement nil))

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

