package com.meetup

import unfiltered.request._
import unfiltered.response._

/** unfiltered plan */
class App extends unfiltered.filter.Plan {
  import QParams._

  import unfiltered.Cookie

  import dispatch._
  import dispatch.oauth._
  import dispatch.oauth.OAuth._
  import dispatch.meetup._

  import net.liftweb.json._
  import net.liftweb.json.JsonDSL._
  import net.liftweb.json.JsonAST._

  import com.meetup.Meetup._

  implicit def http = new dispatch.AppEngineHttp

  lazy val consumer = Consumer(
    Config("mu_consumer"), Config("mu_consumer_secret"))

  lazy val host = Config("host")

  val JsonResource = """(\S+).json""".r

  def intent = {

    case GET(Path("/user.json")) & request =>
      CookieToken(request) match {
        case Some(ClientToken(v, s, Some(c))) =>
          Meetup.user(Some(Token(v,s))) match {
            case Some(MeetupUser(id, name, photo)) =>
              val groups =
                 Meetup.groups(id, Some(Token(v,s))) match {
                   case Some(groups) => groups map { g =>
                     ("id" -> g.id) ~ ("name" -> g.name) ~
                     ("slug" -> g.slug) ~ ("photo" -> g.photo) ~
                     ("link" -> g.link)
                  }
                   case _ => Nil
                 }
              val json =
                ("user" ->
                   ("member" ->
                      ("id" -> id) ~
                      ("name" -> name) ~
                      ("photo" -> photo)) ~
                   ("groups" -> groups))
              ResponseString(compact(render(json)))
            case _ => /* no user */ ResponseString("{}")
          }
        case _ => /* no token */ ResponseString("{}")
      }


    //case GET(Path(Seg("groups" :: JsonResource(slug)))) & request =>
    //  ResponseString("{}")


    case GET(Path("/connect")) & request =>
      val callback = "%s/authenciated" format(host)
      val t = http(Auth.request_token(consumer, callback))
      ResponseCookies(
        Cookie("token", ClientToken(t.value, t.secret, None).toCookieString)) ~>
          Redirect(Auth.authorize_url(t).to_uri.toString)

    case GET(Path("/disconnect")) & request =>
      ResponseCookies(Cookie("token", "")) ~> Redirect("/")

    case GET(Path("/authenciated") & Params(params)) & request =>
      val expected = for {
        verifier <- lookup("oauth_verifier") is
          required("verifier is required") is
          nonempty("verifier can not be blank")
        token <- lookup("oauth_token") is
          required("token is required") is
          nonempty("token can not be blank")
      } yield {
        CookieToken(request) match {
          case Some(rt) =>
            val at = http(Auth.access_token(consumer, Token(rt.value, rt.sec), verifier.get))
            ResponseCookies(
               Cookie("token",
                      ClientToken(at.value, at.secret, verifier)
                      .toCookieString)) ~> Redirect("/")
          case _ => error("could not find request token")
        }
      }

      expected(params) orFail { errors =>
        BadRequest ~> ResponseString(errors.map { _.error } mkString(". "))
      }

  }
}
