(ns conexp.graphics.draw
  (:use [conexp.util :only (update-ns-meta!)]
	[conexp.layout :only (*standard-layout-function*)]
	[conexp.layout.util :only (scale-layout, lattice-from-layout)]
	[conexp.layout.force :only (force-layout)]
	[conexp.graphics.nodes-and-connections :only (*default-node-radius*, move-interaction)]
	[conexp.graphics.base :only (draw-on-scene, get-layout-from-scene, set-layout-of-scene)])
  (:import [javax.swing JFrame JPanel JButton]
	   [java.awt Dimension BorderLayout FlowLayout]
	   [java.awt.event ActionListener]
	   [no.geosoft.cc.graphics ZoomInteraction GScene]))

(update-ns-meta! conexp.graphics.draw
  :doc "This namespace provides a lattice editor and a convenience function to draw lattices.")


;;; lattice editor -- a lot TODO

(declare make-button)

;; editor features

(defn- change-node-radius
  "Install node radius changer."
  [scn buttons]
  nil)

(defn- toggle-labels
  "Install label-toggler."
  [scn buttons]
  nil)

(defn- change-layout
  "Install lattice layout changer."
  [scn buttons]
  nil)

(defn- improve-layout-by-force
  "Improves layout on screen by force layout."
  [#^GScene scn, buttons]
  (let [button (make-button buttons "Force")]
    (.addActionListener
     button
     (proxy [ActionListener] []
       (actionPerformed [evt]
	 (let [layout (get-layout-from-scene scn),
	       new-layout (force-layout (lattice-from-layout layout)
					layout)]
	   (set-layout-of-scene scn (scale-layout [0.0 0.0] [400.0 400.0]
						  new-layout))))))))

(defn- toggle-zoom-move
  "Install zoom-move-toggler."
  [scn buttons]
  (let [button (make-button buttons "Zoom")]
    (.addActionListener button
			(proxy [ActionListener] []
			  (actionPerformed [evt]
			    (if (= "Zoom" (.getText button))
			      (do
				(.. scn getWindow (startInteraction (ZoomInteraction. scn)))
				(.setText button "Move"))
			      (do
				(.. scn getWindow (startInteraction (move-interaction)))
				(.setText button "Zoom"))))))))


(defn- export-as-file
  "Installs a file exporter."
  [scn buttons]
  nil)

;; technical helpers

(defmacro install-changers
  "Installs given methods to scene with buttons."
  [scene buttons & methods]
  `(do
     ~@(map (fn [method#] `(~method# ~scene ~buttons))
	    methods)))

(defn make-button
  "Uniformly create buttons for lattice editor."
  [buttons text]
  (let [button (JButton. text)]
    (.add buttons button)
    (.setPreferredSize button (Dimension. 100 20))
    button))

;; constructor

(defn make-lattice-editor
  "Creates a lattice editor for lattice with initial layout."
  [lattice layout-function]
  (let [#^JPanel main-panel (JPanel. (BorderLayout.)),

	#^GScene scn (draw-on-scene [-50.0 -50.0] [450.0 450.0]
			   (scale-layout [0.0 0.0] [400.0 400.0]
					 (layout-function lattice))),
	canvas (.. scn getWindow getCanvas),

	buttons (JPanel. (FlowLayout.))]
    (.setPreferredSize buttons (Dimension. 110 0))
    (install-changers scn buttons
      toggle-zoom-move
      change-node-radius
      toggle-labels
      change-layout
      improve-layout-by-force
      export-as-file)
    (doto main-panel
      (.add canvas BorderLayout/CENTER)
      (.add buttons BorderLayout/WEST))
    main-panel))


;;; drawing routine for the repl

(defn draw-lattice
  "Draws given lattice with given layout-function on a canvas and returns
  it. Uses *standard-layout-function* if no layout-function is given."
  ([lattice]
     (draw-lattice lattice *standard-layout-function*))
  ([lattice layout-function]
     (doto (JFrame. "conexp-clj Lattice")
       (.add (make-lattice-editor lattice layout-function))
       (.setSize (Dimension. 300 300))
       (.setVisible true))))


;;;

nil
