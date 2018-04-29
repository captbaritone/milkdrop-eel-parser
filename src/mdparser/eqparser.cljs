(ns mdparser.eqparser
  (:require [clojure.set]
            [instaparse.core :as insta]))

(def funmap
  {
   ;unary
   :int "Math.floor"
   :abs "Math.abs"
   :sin "Math.sin"
   :cos "Math.cos"
   :tan "Math.tan"
   :asin "Math.asin"
   :acos "Math.acos"
   :atan "Math.atan"
   :exp "Math.exp"
   :sqr "sqr"
   :sqrt "sqrt"
   :log "Math.log"
   :log10 "log10"
   :sign "sign"
   :rand "rand"
   :bnot "bnot"
   ;binary
   :atan2 "Math.atan2"
   :pow "pow"
   :min "Math.min"
   :max "Math.max"
   :sigmoid "sigmoid"
   :bor "bor"
   :band "band"
   :equal "equal"
   :above "above"
   :below "below"
   ;ternary
   :if "ifcond"})

(def varmap
  {:frating :rating
   :fgammaadj :gammaadj
   :fdecay :decay
   :fvideoechozoom :echo_zoom
   :fvideoechoalpha :echo_alpha
   :nvideoechoorientation :echo_orient
   :nwavemode :wave_mode
   :badditivewaves :additivewave
   :bwavedots :wave_dots
   :bwavethick :wave_thick
   :bmodwavealphabyvolume :modwavealphabyvolume
   :bmaximizewavecolor :wave_brighten
   :btexwrap :wrap
   :bdarkencenter :darken_center
   :bredbluestereo :red_blue
   :bbrighten :brighten
   :bdarken :darken
   :bsolarize :solarize
   :binvert :invert
   :fwavealpha :wave_a
   :fwavescale :wave_scale
   :fwavesmoothing :wave_smoothing
   :fwaveparam :wave_mystery ;this var is different in base vals vs per frame eqs
   :wave_mystery :wave_mystery
   :fmodwavealphastart :modwavealphastart
   :fmodwavealphaend :modwavealphaend
   :fwarpanimspeed :warpanimspeed
   :fwarpscale :warpscale
   :fzoomexponent :zoomexp
   :fshader :fshader
   :zoom :zoom
   :rot :rot
   :cx :cx
   :cy :cy
   :dx :dx
   :dy :dy
   :warp :warp
   :sx :sx
   :sy :sy
   :wave_r :wave_r
   :wave_g :wave_g
   :wave_b :wave_b
   :wave_x :wave_x
   :wave_y :wave_y
   :ob_size :ob_size
   :ob_r :ob_r
   :ob_g :ob_g
   :ob_b :ob_b
   :ob_a :ob_a
   :ib_size :ib_size
   :ib_r :ib_r
   :ib_g :ib_g
   :ib_b :ib_b
   :ib_a :ib_a
   :nmotionvectorsx :mv_x ;this var is different in base vals vs per frame eqs
   :mv_x :mv_x
   :nmotionvectorsy :mv_y
   :mv_y :mv_y
   :mv_dx :mv_dx
   :mv_dy :mv_dy
   :mv_l :mv_l
   :mv_r :mv_r
   :mv_g :mv_g
   :mv_b :mv_b
   :mv_a :mv_a
   :r :r
   :g :g
   :b :b
   :a :a
   :r2 :r2
   :g2 :g2
   :b2 :b2
   :a2 :a2
   :border_r :border_r
   :border_g :border_g
   :border_b :border_b
   :border_a :border_a
   :thick :thickoutline
   :thickoutline :thickoutline
   :textured :textured
   :tex_zoom :tex_zoom
   :tex_ang :tex_ang
   :additive :additive
   :sides :sides
   :instance :instance
   :instances :num_inst
   :num_instances :num_inst
   :num_inst :num_inst
   :scaling :scaling
   :samples :samples
   :badditive :additive
   :busedots :usedots
   :bspectrum :spectrum
   :smoothing :smoothing
   :bdrawthick :thick
   :sample :sample
   :value1 :value1
   :value2 :value2
   :camera_x :camera_x
   :camera_y :camera_y
   :camera_z :camera_z
   :camera_look_x :camera_look_x
   :camera_look_y :camera_look_y
   :camera_look_z :camera_look_z
   :sep :sep
   :b1n :b1n
   :b2n :b2n
   :b3n :b3n
   :b1x :b1x
   :b2x :b2x
   :b3x :b3x
   :b1ed :b1ed
   :monitor :monitor
   :bass :bass
   :mid :mid
   :treb :treb
   :bass_att :bass_att
   :mid_att :mid_att
   :treb_att :treb_att})

(def pool-vars
  {:per-pixel [:x :y :rad :ang]
   :per-shape-frame [:r :g :b :a :r2 :g2 :b2 :a2
                     :x :y :rad :ang
                     :border_r :border_g :border_b :border_a
                     :additive :thickoutline
                     :textured :tex_zoom :tex_ang
                     :sides :instance :num_inst]
   :per-wave-frame [:r :g :b :a
                    :samples :scaling :smoothing :sep
                    :additive :usedots :spectrum :thick]
   :per-wave-point [:x :y :r :g :b :a
                    :sample :value1 :value2]})

(def globalvarset
  #{:time
    :fps
    :frame
    :meshx
    :meshy
    :pixelsx
    :pixelsy
    :aspectx
    :aspecty
    :bass :mid :treb
    :bass_att :mid_att :treb_att})

(def basevarset
  (clojure.set/difference
    (into
     globalvarset
     (vals varmap))
    (reduce into #{} (map second pool-vars))))

(def builtin-symbols
  (into
   (into
    #{}
    (keys funmap))
   (keys varmap)))

(def comment-regex #"//[\s\S]*")

(defn strip-comments-from-line
  [s]
  (clojure.string/trim
   (clojure.string/replace s comment-regex "")))

(defn strip-all-comments
  [s]
  (reduce
   str
   (filter #(not (clojure.string/blank? %))
           (map strip-comments-from-line (seq (clojure.string/split-lines s))))))

(defn pre-parse
  [s]
  (map #(str % ";")
       (filter #(not (clojure.string/blank? %))
               (seq (clojure.string/split (strip-all-comments s) ";")))))

(def parser
  (insta/parser
   "
   PROGRAM     = STATEMENT+
   STATEMENT   = SPACE* ((lhs assign-op rhs) | loop | while | if) <';'>* SPACE*
   exec3       = <'exec3'> lparen STATEMENT+ <comma> STATEMENT+ <comma> STATEMENT* bitexpr* rparen
   exec2       = <'exec2'> lparen STATEMENT+ <comma> STATEMENT* bitexpr* rparen
   loop        = <'loop'> lparen bitexpr comma STATEMENT+ rparen
   while       = <'while'> lparen (exec3 | exec2) rparen
   <assign-op> = '=' | '+=' | '-=' | '*=' | '/=' | '%='
   <lhs>       = BUFFER | SYMBOL
   <rhs>       = SPACE* bitexpr SPACE*
   <bitexpr>   = loop | cond | BUFFER | expr | bitwise
   <expr>      = term | add-sub
   <term>      = factor | mult-div
   <factor>    = NUMBER | SYMBOL | BUFFER | if | funcall | SPACE* NEGATIVE* lparen bitexpr rparen
   bitwise     = bitexpr bitop expr
   add-sub     = expr addop term
   mult-div    = term multop factor
   <bitop>     = '&' | '|'
   <addop>     = '+' | '-'
   <multop>    = '/' | '*' | '%'
   if          = SPACE* NEGATIVE* SPACE* <'if'> lparen bitexpr comma (STATEMENT | exec3 | exec2 | bitexpr)+ comma (STATEMENT | exec3 | exec2 | bitexpr)+ rparen
   funcall     = SPACE* NEGATIVE* SYMBOL lparen bitexpr (<comma> bitexpr)* rparen
   cond        = lparen* bitexpr condop bitexpr rparen*
   condop      = '>' | '<' | '>=' | '<=' | '==' | '!='
   <lparen>    = SPACE* <'('> SPACE*
   <rparen>    = SPACE* <')'> SPACE*
   comma       = SPACE* <','> SPACE*
   NUMBER      = SPACE* NEGATIVE* (DECIMAL | INTEGER) SPACE*
   DECIMAL     = INTEGER? '.' INTEGER
   INTEGER     = #'[0-9]+'
   SYMBOL      = SPACE* NEGATIVE* SPACE* #'[A-Za-z][A-Za-z0-9_]*' SPACE*
   BUFFER      = SPACE* NEGATIVE* SPACE* ('gmegabuf' | 'megabuf') lparen bitexpr rparen SPACE*
   NEGATIVE    = <'-'>
   <SPACE>     = <#'[ \t\n]+'>
   "))

(defn parse-program
  [input]
  (let [parsed (parser input)]
    (if (insta/failure? parsed)
      (throw (ex-info (pr-str (insta/get-failure parsed)) {}))
      (insta/transform {:SYMBOL (fn [& xs] (into [:SYMBOL] (conj (vec (drop-last xs)) (.toLowerCase (last xs)))))}
                       parsed))))

(defn parse
  [input]
  (parse-program (reduce str (pre-parse input))))

(defn correct-basevar
  [x]
  (if-let [s (varmap (keyword x))] (name s) x))

(defn get-symbols
  [t]
  (map last
       (filter #(and (sequential? %) (= (first %) :SYMBOL))
          (tree-seq sequential? identity t))))

(defn remove-leading-negs
  [x]
  (let [[ns r] (split-with #(= % [:NEGATIVE]) x)]
    [(pos? (mod (count ns) 2)) r]))

(defn remove-trailing-negs
  [x]
  (let [ns (filter #(= % [:NEGATIVE]) x)
        r (filter #(not (= % [:NEGATIVE])) x)]
    [(pos? (mod (count ns) 2)) r]))

(defn analyze-line
  [l]
  (let [[f & r] l
        [lhs-neg r] (remove-leading-negs r)
        [rhs-neg r] (remove-trailing-negs r)]
    (if (> (count r) 1)
      (let [[lhs op rhs] r
            lhs-symbs (if (= (first lhs) :BUFFER)
                        []
                        [(correct-basevar (last lhs))])
            rhs-symbs (if (= (first lhs) :BUFFER)
                        (into (get-symbols rhs) (get-symbols lhs))
                        (get-symbols rhs))
            rhs-symbs (filter #(nil? (funmap (keyword %))) rhs-symbs) ;don't need function symbols
            rhs-symbs (set (map correct-basevar rhs-symbs))]
        {:lhs lhs-symbs
         :rhs rhs-symbs})
      (case (first (first r))
        :loop (let [[c comma & s] (rest (first r))]
                (reduce
                  (fn [coll a]
                    {:lhs (into (coll :lhs) (a :lhs))
                     :rhs (into (coll :rhs) (a :rhs))})
                  {:lhs []
                   :rhs (into #{} (get-symbols c))}
                  (map analyze-line s)))
        :while (reduce
                (fn [coll a]
                  {:lhs (into (coll :lhs) (a :lhs))
                   :rhs (into (coll :rhs) (a :rhs))})
                {:lhs []
                 :rhs #{}}
                (map #(if (= (first %) :STATEMENT)
                        (analyze-line %)
                        {:lhs [] :rhs (into #{} (get-symbols %))})
                     (rest (first (rest (first r))))))
        :if (let [[is-neg r] (remove-leading-negs (rest (first r)))
                  [c t f] (filterv #(not (= % '([:comma]))) (partition-by #(= % [:comma]) r))]
              (reduce
                (fn [coll a]
                  {:lhs (into (coll :lhs) (a :lhs))
                   :rhs (into (coll :rhs) (a :rhs))})
                {:lhs []
                 :rhs (into #{} (get-symbols c))}
                (map #(if (= (first %) :STATEMENT)
                        (analyze-line %)
                        {:lhs [] :rhs (into #{} (get-symbols %))})
                     (into t f))))))))

(def non-rkeys #{:x :y :rad :ang})

(defn get-rewrite-keys
  "Find keys that need to be rewritten between run of the per pixel equations.
  As an optimization we rewrite the keys that have been read from before being written
  to instead of cloning the whole object."
  [lhs-symbs rhs-symbss]
  (let [lhs-first-occur (reduce
                         (fn [c [ss i]]
                           (reduce
                            #(if (nil? (%1 %2))
                               (assoc %1 %2 i)
                               %1)
                            c
                            ss))
                         {}
                         (map vector lhs-symbs (range)))
        rhs-first-occur (reduce
                         (fn [c [ss i]]
                           (reduce
                            #(if (nil? (%1 %2))
                               (assoc %1 %2 i)
                               %1)
                            c
                            ss))
                         {}
                         (map vector rhs-symbss (range)))
        rkeys (reduce
               (fn [c [symb i]]
                 (let [rhs-occur (rhs-first-occur symb)]
                   (if (and
                        (not (nil? rhs-occur))
                        (>= i rhs-occur))
                     (conj c symb)
                     c)))
               []
               lhs-first-occur)
        rkeys (filter #(not (non-rkeys (keyword %))) rkeys)]
    rkeys))

(defn analyze
  ([p] (analyze p nil))
  ([p rkeys?]
   (let [[f & r] p
         symmaps (map analyze-line r)
         lhs-symbs (mapv :lhs symmaps)
         rhs-symbs (mapv :rhs symmaps)
         rkeys (when rkeys? (get-rewrite-keys lhs-symbs rhs-symbs))
         basevars (if rkeys?
                    (if (= :per-pixel rkeys?)
                      (into basevarset (pool-vars rkeys?))
                      (into globalvarset (pool-vars rkeys?)))
                    basevarset)
         user-vars (into
                     (reduce
                       #(into %1 (%2 :lhs))
                       #{}
                       symmaps)
                     (reduce
                       #(into %1 (%2 :rhs))
                       #{}
                       symmaps))
         user-vars (filter #(nil? (basevars (keyword %))) user-vars)]
     {:lhs lhs-symbs
      :rhs rhs-symbs
      :rkeys rkeys
      :user-vars user-vars})))

(defn emit
  ([l] (emit l ";"))
  ([l line-ending]
  (let [[f & r] l
        basic-op (fn [r]
                   (let [[lhs-neg r] (remove-leading-negs r)
                         [rhs-neg r] (remove-trailing-negs r)
                         [lhs op rhs] r]
                      (if (or (= op "/")
                              (= op "%")
                              (= op "|")
                              (= op "&"))
                     (str ({"/" "div"
                            "%" "mod"
                            "|" "bitor"
                            "&" "bitand"} op) "(" (when lhs-neg "-") (emit lhs) "," (when rhs-neg "-") (emit rhs) ")")
                        (str "(" (when lhs-neg "-") (emit lhs) op (when rhs-neg "-") (emit rhs) ")"))))]
    (case f
      :PROGRAM (map emit r)
      :STATEMENT (if (== (count r) 1)
                   (emit (first r))
                   (let [[rhs-neg r] (if (> (count r) 3)
                                       (remove-trailing-negs r)
                                       [nil r])
                         [lhs op rhs] r]
                     (str (emit lhs) " " op " " (when rhs-neg "-") (emit rhs) line-ending)))
      (:exec2 :exec3) (str
                        "("
                        (reduce str (map #(emit % ",") (drop-last r)))
                        (emit (last r) "")
                        ")")
      :while (let [idx-var (gensym "idx")
                   count-var (gensym "count")]
               (str
                 "var " idx-var ";"
                 "var " count-var  "=0;"
                 "do {"
                 count-var " += 1;"
                 idx-var " = " (reduce str (map #(emit % "") r))
                 "} while (" idx-var " !== 0 && " count-var " < 1048576);"))
      :loop (let [[c comma & s] r
                  idx-var (gensym "idx")
                  idx-max-var (gensym "idx_max")]
              (str
                "var " idx-max-var " = " (emit c) ";"
                "for (var " idx-var " = 0; " idx-var " < " idx-max-var "; "idx-var "++) {"
                (reduce str (map emit s))
                "}"))
      :bitwise (basic-op r)
      :add-sub (basic-op r)
      :mult-div (basic-op r)
      :NUMBER (let [[is-neg r] (remove-leading-negs r)]
                (str (when is-neg "-") (emit (last r))))
      :DECIMAL (if (== (count r) 3)
                 (let [[lhs _ rhs] r]
                   (str (emit lhs) "." (emit rhs)))
                 (let [[_ rhs] r]
                   (str "0." (emit rhs))))
      :INTEGER (first r)
      :SYMBOL (let [[is-neg r] (remove-leading-negs r)
                    sname (last r)
                    sname (correct-basevar sname)]
                (str (when is-neg "-") "m." sname))
      :BUFFER (let [[is-neg r] (remove-leading-negs r)]
                (str (when is-neg "-") "m." (first r) "[" (emit (second r)) "]"))
      :condop (last r)
      :cond (let [[lhs c rhs] r]
              (str
                "("
                (emit lhs)
                (emit c)
                (emit rhs)
                ")"))
      :if (let [[is-neg r] (remove-leading-negs r)
                [c t f] (filterv #(not (= % '([:comma]))) (partition-by #(= % [:comma]) r))]
            (str
              (when is-neg "-")
              "(" (emit (first c)) ") ? "
              "(" (reduce str (map emit t)) ") "
              ": "
              "(" (reduce str (map emit f)) ")"))
      :funcall (let [[is-neg-top r] (remove-leading-negs r)
                     [fname & args] r
                     [is-neg fname] (remove-leading-negs (rest fname))
                     f (funmap (keyword (.toLowerCase (first fname))))
                     as (reduce str (interpose ", " (map emit args)))]
                 (if (nil? f)
                   (throw (ex-info (str "No function matching: " (first fname)) {}))
                   (str (when (or is-neg-top is-neg) "-") f "(" as ")")))))))