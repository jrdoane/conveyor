(ns conveyor
  (:require
    [conveyor.message :as msg]
    [manifold.stream :as s]))

(defn router-shell
  [router-name]
  {:name router-name
   :uuid (str (java.util.UUID/randomUUID))
   :routing (atom {})
   :streams
   {:incoming (s/stream 32)
    :broadcast (s/stream 32)
    :attached (atom {})}})

(defn discovery-handler!
  "The fn that takes in a discovery message and handles it.
  2 things happen.
    A: The current router will update its routing table.
    B: All connected routers sans the one who sent the message will get the
       messaged passed on to them. The message will terminate when it hits a
       loop."
  [router message]
  (let [router-stack (:router-stack message)
        requesting-uuid (first router-stack)
        reverse-route (vec (reverse router-stack))
        router-set (set router-stack)
        routed-message (msg/route-message router message)]
    (swap!
      (:routing router)
      assoc
      requesting-uuid
      reverse-route)
    (doseq [i (get-in router [:streams :attached])]
      (when (contains? router-set (first i))
        (s/put! (second i) routed-message)))
    (s/put!
      (get-in router [:streams :attached
                      (first reverse-route)])
      (msg/make-announcement router))
    true))

(defn connect-message-type
  [router-map message-type]
  (let
    [a (get-in router-map [:streams :incoming])
     b (s/stream 32)]
    (s/connect-via
      a
      (fn [i]
        (when (= (:type i) message-type)
          (s/put! b i)))
      b)
    (assoc-in router-map [:streams message-type] b)))

(defn make-router
  [router-name]
  (-> (router-shell router-name)
      (connect-message-type :discovery)
      (connect-message-type :announcement)))

(defn add-remote-router!
  [src-router dest-uuid duplex-stream]
  (swap!
    (get-in src-router [:streams :attached])
    conj [dest-uuid duplex-stream])
  (s/connect duplex-stream (get-in src-router [:streams :incoming]))
  (s/connect (get-in src-router [:streams :broadcast]) duplex-stream))

(defn attach-local-routers!
  [src-router dest-router]
  (let [src-incoming-stream (s/stream 32)
        dest-incoming-stream (s/stream 32)
        src-duplex-stream (s/splice dest-incoming-stream src-incoming-stream)
        dest-duplex-stream (s/splice src-incoming-stream dest-incoming-stream)
        src-uuid (:uuid src-router) 
        dest-uuid (:uuid dest-router)]
    (add-remote-router! src-router dest-uuid src-duplex-stream)
    (add-remote-router! dest-router src-uuid dest-duplex-stream)
    (msg/discovery! src-router)
    (msg/discovery! dest-router)))

(comment

  (def router-one (make-router "r1"))
  (def router-two (make-router "r2"))
  (msg/discovery! router-one)
  (attach-local-routers! router-one router-two)

  )
