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

  import com.google.appengine.api.datastore.{Key,KeyFactory}

  lazy val consumer = Consumer(
    Config("mu_consumer"), Config("mu_consumer_secret"))

  lazy val host = Config("host")

  def intent = {

    case GET(Path("/user.json")) & request =>
      CookieToken(request) match {
        case Some(ClientToken(v, s, Some(c))) =>
          Meetup.user(Some(Token(v,s))) match {
            case Some(MeetupUser(id, name, photo)) =>
              val groups =
                 Meetup.groupsByMember(id, Some(Token(v,s))) match {
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

    case GET(Path("/connect")) & request =>
      val callback = "%s/authenticated" format(host)
      val t = http(Auth.request_token(consumer, callback))
      ResponseCookies(
        Cookie("token", ClientToken(t.value, t.secret, None).toCookieString)) ~>
          Redirect(Auth.authenticate_url(t).to_uri.toString)

    case GET(Path("/disconnect")) & request =>
      ResponseCookies(Cookie("token", "")) ~> Redirect("/")

    case GET(Path("/authenticated") & Params(params)) & request =>
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

    case GET(Path("/polls.json") & Params(params)) & request =>
      CookieToken(request) match {
        case Some(ClientToken(v, s, Some(c))) =>
          Meetup.user(Some(Token(v,s))) match {
            case Some(muser@MeetupUser(id, name, _)) =>
              val expected = for {
                groupUrl <- lookup("group") is
                  required("group is required") is
                  nonempty("group can not be blank")
              } yield {
                Meetup.groupByMemberAndUrl(id, groupUrl.get, Some(Token(v,s))) match {
                   case Some(group) =>
                     val json = (com.meetup.stores.PollStore.byGroup(groupUrl.get) { polls =>
                       ("polls" ->
                         (polls.map { p =>
                           ("id" -> KeyFactory.keyToString(p.id)) ~
                           ("name" -> p.name) ~ ("content" -> p.content)
                         }).toList)
                     })
                     JsonContent ~> ResponseString(compact(render(json)))
                   case _ => ResponseString("""{"errors":["invalid group"]}""")
                }
              }

             expected(params) orFail { errors =>
               JsonContent ~> ResponseString("""{"errors":[%s]}""" format(errors.map { _.error } mkString("\"", "\",\"", "\"")))
             }
            case _ /* no user */ => ResponseString("""{"errors":["you are not authorized"]}""")
          }
        case _ /* no cookie */ => ResponseString("{}")
      }


    case POST(Path("/polls.json") & Params(params)) & request =>
      CookieToken(request) match {
        case Some(ClientToken(v, s, Some(c))) =>
          Meetup.user(Some(Token(v,s))) match {
            case Some(user@MeetupUser(id, name, _)) =>
               val expected = for {
                 name <- lookup("name") is
                   required("Poll name is required") is
                   nonempty("Poll name can not be blank")
                 desc <- lookup("description") is
                   required("Poll description is required") is
                   nonempty("Poll description can not be blank")
                 group <- lookup("group-urlname") is
                   required("This poll must be associated with a Meetup Group") is
                   nonempty("This poll must be associated with a Meetup Group")
                 choice <- lookup("choice") is
                   required("At least one choice is required") is
                   nonempty("At least one choice is required")
               } yield {
                 Meetup.groupByMemberAndUrl(id, group.get, Some(Token(v,s))) match {
                   case Some(group) =>
                     val poll = com.meetup.stores.PollStore + (
                       user, group, name.get, desc.get, params("choice")
                     )
                     println("created poll %s" format poll)
                     val json = ("poll" ->
                         ("id" ->  KeyFactory.keyToString(poll.id)) ~
                         ("name" -> poll.name) ~
                         ("description" -> poll.content))
                     JsonContent ~> ResponseString(compact(render(json)))
                   case _ => ResponseString("""{"errors":["invalid group"]}""")
                 }
               }

            expected(params) orFail { errors =>
              JsonContent ~> ResponseString("""{"errors":[%s]}""" format(errors.map { _.error } mkString("\"", "\",\"", "\"")))
            }
            case _ /* no user for this token */ => ResponseString("{}")
          }
        case _ /* no cookie */ => ResponseString("{}")
      }


  }
}
