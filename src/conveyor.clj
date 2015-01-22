(ns conveyor
  (:require [manifold.stream :as s]))

(defn router-shell
  [router-name]
  {:name router-name
   :uuid-list (vec (repeatedly 3 #(str (java.util.UUID/randomUUID))))
   :streams
   {:incoming (s/stream 32)
    :announcement (s/stream 32)
    :discovery (s/stream 32)
    :broadcast (s/stream 32)
    :attached (atom {})}})

(defn connect-announcements!
  [router-map]
  (let
    [istream (get-in router-map [:streams :incoming])
     astream (get-in router-map [:streams :announcement])]
    (s/connect-via
      istream
      (fn [i]
        (when (= (:type i) :announcement)
          (s/put! astream i)))
      astream)
    router-map))

(defn connect-discoveries!
  [router-map]
  (let
    [istream (get-in router-map [:streams :incoming])
     dstream (get-in router-map [:streams :discovery])]
    (s/connect-via
      istream
      (fn [i]
        (when (= (:type i) :discovery)
          (s/put! dstream i)))
      dstream)
    router-map))

(defn make-router
  [router-name]
  (-> (router-shell router-name)
      connect-discoveries!
      connect-announcements!))

(defn make-message
  [source-router message-type message]
  {:type message-type
   :router-uuid-list (:uuid-list source-router)
   :router-stack []
   :message message})

(defn make-discovery
  [source-router]
  (make-message source-router :discovery nil))

(defn make-announcement
  [source-router]
  (make-message source-router :announcement nil))

(defn initiate-exchange
  [source-router]
  (make-message source-router :exchange-start))

(defn complete-exchange
  [source-router]
  (make-message source-router :exchange-end))

(defn discovery!
  [source-router]
  (s/put!
    (get-in source-router [:streams :broadcast])
    (make-discovery source-router)))

(defn announce!
  [source-router]
  (s/put!
    (get-in source-router [:streams :broadcast])
    (make-announcement source-router)))

(defn attach!
  [source-router destination-stream]
(s/put!
    destination-stream
    (make-announcement source-router))
  (s/connect
    (get-in source-router [:streams :broadcast])
    destination-stream)
  (swap!
    (get-in source-router [:streams :attached])
    conj [ destination-stream])
  true)

(comment

  (def router-one (make-router "r1"))
  (def router-two (make-router "r2"))
  (attach! router-one (get-in router-two [:streams :incoming]))


  )
