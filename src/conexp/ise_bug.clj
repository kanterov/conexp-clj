;; Copyright (c) Daniel Borchmann. All rights reserved.
;; The use and distribution terms for this software are covered by the
;; Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;; which can be found in the file LICENSE at the root of this distribution.
;; By using this software in any fashion, you are agreeing to be bound by
;; the terms of this license.
;; You must not remove this notice, or any other, from this software.

;;; This file is for specifically fixing a new bug with java event callbacks
;;; and undefined variables.

(ns conexp.ise-bug
  (:use conexp.base
    conexp.contrib.gui.util
    conexp.contrib.gui.editors.util
    conexp.util.multimethods))

(defn mk-btn
  "make button"
  []
  (let [  h (fn [] (message-box "MSG!")) ]
    (make-button "Klick" nil [ set-handler h ])))

(defn button-test
  "For debugging/testing buttons"
  []
  (let [btn (mk-btn) ]
    (show-in-frame (get-widget btn)) ))

nil