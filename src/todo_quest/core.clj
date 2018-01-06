(ns todo-quest.core
  (:require
   [org.httpkit.server :as server]
   [acknowledge.core :as handlers]

   [ring.middleware.session :refer [wrap-session]]

   [todo-quest.auth.core :as auth]
   [todo-quest.model :as db]
   [todo-quest.page :as pg]
   [todo-quest.util :as util]

   [todo-quest.front-end.template :as tmpl]))

(handlers/intern-static! "/static/" (handlers/resources "public/"))

(handlers/intern-handler-fn!
 "/" :login-page
 (fn [req]
   (if (auth/logged-in? req)
     (let [user (get-in req [:session :user])]
       (util/ok
        (pg/pg
         [:div {:id "todo-quest"}
          (tmpl/task-list (db/get-user-tasks user))]
         [:div {:id "toolbar"}
          [:form {:action "/api/classic/new-task"}
           [:input {:type "submit" :value "+"}] [:input {:type "text" :name "task-text"}]]
          [:a {:href "/oauth/log-out"} "Log Out"]]
         [:script {:src "/static/js/main.js" :type "text/javascript" :charset "utf-8"}])))
     (util/ok
      (pg/pg
       [:p "Hello there!"]
       [:a {:href auth/github-auth-url} "Login with Github"])))))

(defn -main
  []
  (server/run-server
   (->> handlers/routes-handler
        wrap-session)
   {:port 3000}))
