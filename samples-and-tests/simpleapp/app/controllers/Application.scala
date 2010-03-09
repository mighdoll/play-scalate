package controllers

import play.mvc._
import models.User
object Application extends ScalateController{
    
    def index(name: String = "Guest user") = {
      val user2= new User("password", "email",name)
      val user3= new User("password", "email",name)
      user2.save()
      user3.save()
      val userlist = List(user2,user3)
      renderScalate(name,user2,userlist)
    }
}

