(ns pulina.login
  (:require [reagent.core :as reagent]
            [re-frame.core :refer [dispatch
                                   dispatch-sync]]
            [pulina.form-components :as fc]))

(defn create-user
  [create-account-atom]
  (let [form-data (reagent/atom {})]
    (fn create-user-render
      [create-account-atom]
      [:div.login-box
       [:h1 "Create new account"]
       [fc/label-input form-data [:name] {:type "text" :placeholder "Nickname"} "Nickname"]
       [fc/label-input form-data [:username] {:type "email" :placeholder "Username"} "Username"]
       [fc/label-input form-data [:password] {:type "password" :placeholder "Password"} "Password"]
       [:div.btns
        [:button
         {:on-click (fn [e] (.preventDefault e) (reset! create-account-atom false) e)}
         "Cancel"]
        [:button
         {:on-click
          (fn [e]
            (.preventDefault e)
            (dispatch-sync [:create-user @form-data])
            (reset! create-account-atom false)
            e)}
         "Create account"]]])))

(defn login-existing-user
  [create-account-atom]
  (let [form-data (reagent/atom {})]
    (fn login-existing-user-render
      [create-account-atom]
      [:form.login-box
       [:h1 "Login"]
       [fc/label-input form-data [:username] {:type "text" :placeholder "Username"} "Username"]
       [fc/label-input form-data [:password] {:type "password" :placeholder "Password"} "Password"]
       [:input.login-btn
        {:type "submit"
         :value "Login"
         :on-click (fn [e] (.preventDefault e) (dispatch [:login @form-data]) e)}]
       [:div.no-account
        [:a
         {:href "#"
          :on-click (fn [e] (.preventDefault e) (reset! create-account-atom true) e)}
         "No account yet?"]]])))

(defn login
  []
  (let [create-account? (reagent/atom false)]
    (fn login-render
      []
      [:div#login
       (if @create-account?
         [create-user create-account?]
         [login-existing-user create-account?])])))