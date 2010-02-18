(ns conexp.graphics.scene-layouts
  (:use [conexp.util :only (update-ns-meta!, illegal-argument)]
	[conexp.base :only (defvar-)]
	[conexp.layout.base :only (make-layout, positions, connections, nodes)]
	[conexp.layout.util :only (edges-of-points)]
	conexp.graphics.nodes-and-connections
	conexp.graphics.scenes
	[clojure.contrib.swing-utils :only (do-swing)])
  (:import [javax.swing JFrame JButton JPanel JLabel]
	   [java.awt Dimension BorderLayout Color]
	   [no.geosoft.cc.graphics GWindow GScene GStyle]))

(update-ns-meta! conexp.graphics.scene-layouts
  :doc "Basic namespace for drawing lattice.")


;;; get diagram from scene

(defn get-diagram-from-scene
  "Returns nodes and lines of a scene."
  [#^GScene scene]
  (seq (.getChildren scene)))

;;; node and line iterators

(defmacro do-nodes
  "Do whatever with every node on the scene."
  [[node scene] & body]
  `(doseq [~node (filter node? (get-diagram-from-scene ~scene))]
     ~@body))

(defmacro do-lines
  "Do whatever with every connection on the scene."
  [[line scene] & body]
  `(doseq [~line (filter connection? (get-diagram-from-scene ~scene))]
     ~@body))

;;; manipulate layout of scene

(defn get-layout-from-scene
  "Returns layout from a scene."
  [scn]
  (let [[nodes connections] (loop [things (get-diagram-from-scene scn),
				   nodes [],
				   connections []]
			      (if (empty? things)
				[nodes connections]
				(let [thing (first things)]
				  (cond
				   (node? thing)
				   (recur (rest things) (conj nodes thing) connections),
				   (connection? thing)
				   (recur (rest things) nodes (conj connections thing)),
				   :else
				   (throw (IllegalStateException. "Invalid item in lattice diagram."))))))]
    (make-layout (reduce (fn [hash node]
			   (assoc hash (get-name node) (position node)))
			 {}
			 nodes),
		 (map #(vector (get-name (lower-node %))
			       (get-name (upper-node %)))
		      connections))))

(defn update-layout-of-scene
  "Updates layout according to new layout."
  [#^GScene scene, layout]
  (do-swing
   (let [pos (positions layout),
	 [x_min y_min x_max y_max] (edges-of-points (vals pos))]
     (do-nodes [node scene]
      (let [[x y] (pos (get-name node))]
	(move-node-unchecked-to node x y)))
     (.setWorldExtent scene
		      (double (- x_min (* 2 *default-node-radius*)))
		      (double (- y_min (* 2 *default-node-radius*)))
		      (double (- x_max x_min (* -4 *default-node-radius*)))
		      (double (- y_max y_min (* -4 *default-node-radius*))))
     (redraw-scene scene))))

(defn set-layout-of-scene
  "Sets given layout as current layout of scene."
  [#^GScene scene, layout]
  (let [[x_min y_min x_max y_max] (edges-of-points (vals (positions layout)))]
    (doto scene
      (.removeAll)
      (.setWorldExtent (double (- x_min (* 2 *default-node-radius*)))
		       (double (- y_min (* 2 *default-node-radius*)))
		       (double (- x_max x_min (* -4 *default-node-radius*)))
		       (double (- y_max y_min (* -4 *default-node-radius*))))
      (add-nodes-with-connections (positions layout) (connections layout))
      (.unzoom))))

;;; draw nodes with coordinates and connections on a scene

(defn draw-on-scene
  "Draws given layout on a GScene and returns it."
  [layout]
  (let [#^GWindow wnd (make-window),
	scn (make-scene wnd)]
    (doto scn
      (set-layout-of-scene layout))
    (doto wnd
      (.startInteraction (move-interaction scn)))
    scn))


;;;

nil