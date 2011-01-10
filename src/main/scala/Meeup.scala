package com.meetup

object Meetup {
  import net.liftweb.json._
  import net.liftweb.json.JsonDSL._
  import net.liftweb.json.JsonAST._

  import dispatch._
  import dispatch.oauth._
  import dispatch.oauth.OAuth._
  import dispatch.meetup._

  import com.meetup.Meetup._

  case class MeetupUser(id: String, name: String, photo: String)
  case class MeetupGroup(id: String, name: String, slug: String, photo: String, link: String)

  val DEFAULT_USER_IMG =
    "http://img1.meetupstatic.com/39194172310009655/img/noPhoto_50.gif"

  implicit def http = new dispatch.AppEngineHttp

  private val consumer = Consumer(
    Config("mu_consumer"), Config("mu_consumer_secret"))

  def user(token: Option[Token]): Option[MeetupUser] = token match {
    case Some(tok) =>
      val mu = OAuthClient(consumer, tok)
      val (res, _) = mu.call(Members.self)
      val me = res(0)
      val attrs =
        for {
          name <- Member.name(me)
          id <- Member.id(me)
          photo <- Member.photo_url(me)
        } yield {
          MeetupUser(id, name, if(photo.isEmpty) DEFAULT_USER_IMG else photo.replace("member_", "thumb_"))
        }
       attrs match {
         case List(user) => Some(user)
         case _ => None
       }
    case _ => None
  }

  def groups(memberId: String, token: Option[Token]): Option[List[MeetupGroup]] = token match {
    case Some(tok) =>
      val mu = OAuthClient(consumer, tok)
      val (res, _) = mu.call(Groups.member_id(memberId))
      val gps =
        for {
          g <- res
          id <- Group.id(g)
          name <- Group.name(g)
          slug <- Group.urlname(g)
          photo <- Group.photo_url(g)
          link <- Group.link(g)
        } yield {
          MeetupGroup(id, name, slug, photo, link)
        }
      Some(gps)
    case _ => None
  }
}
