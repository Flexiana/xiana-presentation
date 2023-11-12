(ns ^:figwheel-hooks xiana-prezi.core
  (:require
    [clojure.string :as str]
    [goog.dom :as gdom]
    [reagent.core :as reagent :refer [atom]]
    [reagent.dom :as rdom]))

(def overview
  "-883 -292 2880 1620")

(defn states []
  [;; introduction
   "-316 -66 1124 632"
   ;; agenda
   "-316 393 1124 632"


   ;; what is xiana
   "732 38 349 196"
   ;; core concepts
   "660 220 519 292"

   ;; Configuration
   "693 510 426 239"

   ;;App startup
   "700 813 426 239"
   ;; qna #1
   "996 -56 249 140"

   ;; role definition
   "1089 70 426 239"

   ;; session handling
   "1089 296 426 239"
   ;; Migration
   "1089 535 426 239"

   ;;interceptors
   "1089 835 426 239"

   ;; qna #2
   "1394 -57 249 140"

   ;; request handler composition
   "1499 40 426 239"

   ;;routing
   "1499 244 426 239"

   ;;action
   "1499 418 426 239"
   ;;model
   "1501 614 426 239"
   ;;view
   "1501 793 426 239"

   ;; qna #3
   "1599 966 196 110"


   ;; thank you
   "1745 966 196 110"

   ;;overview
   #_"-883 -292 2880 1620"])




(defonce app-state (atom {:scale 1 :state 0 :steps 200 :target (first (states))}))

(defn get-app-element []
  (gdom/getElement "app"))


(add-watch app-state nil #(println :swap! @%2))

(defn main []
  [:<>
   [:div {:style {:display  :flex
                  :position :absolute
                  :left     0
                  :right    0
                  :top      0
                  :bottom   0
                  :flex     :row
                  :overflow :hidden}}
    [:div {:style {:overflow        :hidden
                   :position        :absolute
                   :background-size :cover
                   :left            0
                   :top             0
                   :height          :100%}}
     [:object {:data  "Prezi bg.svg"
               :type  "image/svg+xml"
               :id    "svg-bg-element"
               :style {:pointer-events :none
                       :overflow       :hidden}}]]
    [:div {:style {:overflow :hidden
                   ;:background-color "2b2b2b"
                   :height   :inherit}}
     [:object {:data  "drawing_2.svg"
               :type  "image/svg+xml"
               :id    "svg-element"
               :style {:pointer-events :none
                       :overflow       :hidden}}]]]])

(defn mount [el]
  (rdom/render [main] el))

(defn mount-app-element []
  (when-let [el (get-app-element)]
    (mount el)))


;; conditionally start your application based on the presence of an "app" element
;; this is particularly helpful for testing this ns without launching the app
(mount-app-element)

(defn on-dbl-click [e]
  (.stopPropagation e)
  (swap! app-state assoc :scale 1.5 :state 0)
  (let [svg (.. (:svg @app-state)
                getSVGDocument
                (querySelector "svg"))]
    (.. svg (setAttribute "viewBox" "-883 -292 2880 1620"))
    (.. svg (setAttribute "disabled" true))))

(defn on-mouse-move [e]
  (do                                                       ;(js/console.log e)
    (when (:mouse-down @app-state)
      (.stopPropagation e)

      (let [svg (:svg @app-state)
            [minx miny width height] (map js/parseFloat
                                          (str/split (.. svg
                                                         getSVGDocument
                                                         (querySelector "svg")
                                                         (getAttribute "viewBox"))
                                                     #" "))]
        (.. svg
            getSVGDocument
            (querySelector "svg")
            (setAttribute "viewBox"
                          (str/join " " [(- minx (* (.-movementX e) (/ (:scale @app-state) 1.1)))
                                         (- miny (* (.-movementY e) (/ (:scale @app-state) 1.1)))
                                         width
                                         height])))))))
(defn view-box []
  (.. (:svg @app-state)
      getSVGDocument
      (querySelector "svg")
      (getAttribute "viewBox")))


(defn parse-viewbox [vb]
  (map js/parseFloat (str/split vb #" ")))

(defn scale-fn [s]
  (let [svg (:svg @app-state)
        [x y w h] (parse-viewbox (view-box))
        orig-w (js/parseFloat (.. svg
                                  getSVGDocument
                                  (querySelector "svg")
                                  (getAttribute "width")))
        scale (/ w orig-w)
        orig-h (.. svg
                   getSVGDocument
                   (querySelector "svg")
                   (getAttribute "height"))
        rate (/ orig-h orig-w)
        new-scale (if (pos? (.-deltaY s))
                    (* scale 1.02)
                    (/ scale 1.02))
        scale (if (< 0.1 new-scale 1.5)
                (:scale (swap! app-state assoc :scale new-scale))
                scale)
        [minx miny width height]
        (map js/parseFloat
             (str/split (.. svg
                            getSVGDocument
                            (querySelector "svg")
                            (getAttribute "viewBox"))
                        #" "))
        n-width (min 3840 (max (* orig-w scale) 20))
        c-width (- width n-width)

        value [(+ minx (/ c-width 2))
               (+ miny (* (/ c-width 2) rate))
               n-width
               (* n-width rate)]]
    (.. svg
        getSVGDocument
        (querySelector "svg")
        (setAttribute "viewBox" (str/join " " value)))))

(defn abs [x]
  (if (pos? x) x (- x)))

(defn step [a b c d]
  #_(if (<= (abs (- b a)) 3)
      (/ (+ a b) 2)
      (float
        (-> (- b a)
            (/ c)
            (* d)
            (+ a))))
  (if (pos? d)
    (float
      (-> (- b a)
          (/ c)
          (* d)
          (+ a)))
    b))

(defn stop? []
  (let [target (parse-viewbox (:target @app-state))
        position (parse-viewbox (view-box))]
    (println :abs (map - target position))
    (every? #(< (abs %) 0.1) (map - target position))))

(defn animate []
  (if-let [svg (.. (:svg @app-state)
                   getSVGDocument
                   (querySelector "svg"))]
    (if (stop?)
      (do
        (.setAttribute svg "viewBox" (:target @app-state))
        (js/clearInterval (:animation-id @app-state))
        (swap! app-state dissoc :target))
      (let [[x y w h] (parse-viewbox (.getAttribute svg "viewBox"))
            [tx ty tw th] (parse-viewbox (:target @app-state))
            progress (:progress @app-state 100)
            steps (:steps @app-state 100)
            viewBox (str/join " " [(step x tx steps progress)
                                   (step y ty steps progress)
                                   (step w tw steps progress)
                                   (step h th steps progress)])]
        (.setAttribute svg "viewBox" viewBox)
        (swap! app-state update :progress dec)))
    (js/clearInterval (:animation-id @app-state))))

(defn key-nav [e]
  (case (.-key e)
    "s"
    (js/prompt "save" (str "\"" (str/join " " (map int (parse-viewbox (view-box)))) "\""))

    "0"
    (do
      (js/clearInterval (:animation-id @app-state))
      (swap! app-state
             merge
             {:target       overview
              :animation-id (js/setInterval animate 20)
              :progress     50}))

    (let [max-state (dec (count (states)))
          act (:state @app-state)
          s (case (.-key e)
              "ArrowRight" (min (inc act) max-state)
              "ArrowLeft" (max (dec act) 0)
              nil)]
      (when s
        (js/clearInterval (:animation-id @app-state))
        (swap! app-state
               merge
               {:target       (get (states) s)
                :state        s
                :animation-id (js/setInterval animate 20)
                :progress     50})))))

;; specify reload hook with ^:after-load metadata
(defn ^:after-load on-reload []
  (mount-app-element))

(.addEventListener
  (.getElementById js/document "svg-element")
  "load"
  (fn [e]
    (swap! app-state assoc :svg (.-target e))))

(.removeEventListener js/window "mousemove" on-mouse-move)
(.removeEventListener js/window "mousedown" #(swap! app-state assoc :mouse-down true))
(.removeEventListener js/window "mouseup" #(swap! app-state assoc :mouse-down false))
(.removeEventListener js/window "dblclick" on-dbl-click)
(.removeEventListener js/window "wheel" scale-fn)
(.removeEventListener js/window "keyup" key-nav)

(.addEventListener js/window "mousemove" on-mouse-move)
(.addEventListener js/window "mousedown" #(swap! app-state assoc :mouse-down true))
(.addEventListener js/window "mouseup" #(swap! app-state assoc :mouse-down false))
(.addEventListener js/window "dblclick" on-dbl-click)
(.addEventListener js/window "wheel" scale-fn)
(.addEventListener js/window "keyup" key-nav)


;; (.setAttribute svg "xmlns:xlink" "http://www.w3.org/1999/xlink")
;; (js/console.log (.-getAnimations svg))
;; (.setAttribute svg "animate" 
;;                "repeatCount=\"indefinite\" from=\"0 0 300 300\" to=\"100 100 500 500\" attributeName=\"viewBox\" begin=\"0s\" duration=\"1s\" fill=\"freeze\"")
;; (.setAttribute svg "height" "1080")
;; (.setAttribute svg "viewBox" "0 0 500 500")
;; optionally touch your app-state to force rerendering depending on
;; your application
;; (swap! app-state update-in [:__figwheel_counter] inc)

;;shape = document.getElementsByTagName("svg")[0];
;//shape.setAttribute("viewBox", "-250 -250 500 750");
;
;//shape = document.getElementsByTagName("h1")[0];
;//shape.innerHTML = "testing jscript";
;
;var mouseStartPosition = {x: 0, y: 0};
;var mousePosition = {x: 0, y: 0};
;var viewboxStartPosition = {x: 0, y: 0};
;var viewboxPosition = {x: 0, y: 0};
;var viewboxSize = {x: 480, y: 480};
;var viewboxScale = 1.0;
;
;var mouseDown = false;
;
;shape.addEventListener("mousemove", mousemove);
;shape.addEventListener("mousedown", mousedown);
;shape.addEventListener("wheel", wheel);
;
;function mousedown(e) {
;  mouseStartPosition.x = e.pageX;
;  mouseStartPosition.y = e.pageY;
;
;  viewboxStartPosition.x = viewboxPosition.x;
;  viewboxStartPosition.y = viewboxPosition.y;
;
;  window.addEventListener("mouseup", mouseup);
;
;  mouseDown = true;
;}
;
;function setviewbox()
;{
;  var vp = {x: 0, y: 0};
;  var vs = {x: 0, y: 0};
;
;  vp.x = viewboxPosition.x;
;  vp.y = viewboxPosition.y;
;
;  vs.x = viewboxSize.x * viewboxScale;
;  vs.y = viewboxSize.y * viewboxScale;
;
;  shape = document.getElementsByTagName("svg")[0];
;  shape.setAttribute("viewBox", vp.x + " " + vp.y + " " + vs.x + " " + vs.y);
;
;}
;
;function mousemove(e)
;{
;  mousePosition.x = e.offsetX;
;  mousePosition.y = e.offsetY;
;
;  if (mouseDown)
;  {
;    viewboxPosition.x = viewboxStartPosition.x + (mouseStartPosition.x - e.pageX) * viewboxScale;
;    viewboxPosition.y = viewboxStartPosition.y + (mouseStartPosition.y - e.pageY) * viewboxScale;
;
;    setviewbox();
;  }
;
;  var mpos = {x: mousePosition.x * viewboxScale, y: mousePosition.y * viewboxScale};
;  var vpos = {x: viewboxPosition.x, y: viewboxPosition.y};
;  var cpos = {x: mpos.x + vpos.x, y: mpos.y + vpos.y}
;
;  shape = document.getElementsByTagName("h1")[0];
;  shape.innerHTML = mpos.x + " " + mpos.y + " " + cpos.x + " " + cpos.y;
;}
;
;function mouseup(e) {
;  window.removeEventListener("mouseup", mouseup);
;
;  mouseDown = false;
;}
;
;function wheel(e) {
;  var scale = (e.deltaY < 0) ? 0.8 : 1.2;
;
;  if ((viewboxScale * scale < 8.) && (viewboxScale * scale > 1./256.))
;  {
;    var mpos = {x: mousePosition.x * viewboxScale, y: mousePosition.y * viewboxScale};
;    var vpos = {x: viewboxPosition.x, y: viewboxPosition.y};
;    var cpos = {x: mpos.x + vpos.x, y: mpos.y + vpos.y}
;
;    viewboxPosition.x = (viewboxPosition.x - cpos.x) * scale + cpos.x;
;    viewboxPosition.y = (viewboxPosition.y - cpos.y) * scale + cpos.y;
;    viewboxScale *= scale;
;
;    setviewbox();
;  }
;}