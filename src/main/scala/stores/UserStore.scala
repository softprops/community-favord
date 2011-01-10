package com.meetup.stores

import com.meetup.models.User
import com.meetup.Meetup.MeetupUser

object UserStore extends jdo.JdoStore[User] with jdo.DefaultManager {
  override val domainCls = classOf[User]
  type KeyClass = String

  def apply(key: String) = get(key)

  def + (mu: MeetupUser) =
    apply(mu.id) match {
      case Some(user) => user
      case _ =>
        val newUser = new User
        newUser.id = mu.id
        newUser.photo = mu.photo
        newUser.name = mu.name
        save(newUser)
    }
}
