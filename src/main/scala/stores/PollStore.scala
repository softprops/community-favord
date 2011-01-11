package com.meetup.stores

import com.meetup.models.{Choice, Poll}
import com.meetup.Meetup.{MeetupGroup, MeetupUser}

import scala.collection.JavaConversions._

object PollStore extends jdo.JdoStore[Poll] with jdo.DefaultManager {
  override val domainCls = classOf[Poll]
  type KeyClass = String

  def apply(key: String) = get(key)

  def + (creator: MeetupUser, group: MeetupGroup, name: String, content: String, choices: Seq[String]) = {
    val p = new Poll
    p.creator = creator.id
    p.name = name
    p.groupUrlname = group.slug
    p.content = content
    p.choices = choices.toList map { c =>
      val nc = new Choice
      nc.value = c
      nc
    }
    save(p)
    p
  }

  def byGroup[T](urlname: String)(f: Iterable[Poll] => T) =
    f(query("select from %s" format domainCls.getName) { q =>
    import scala.collection.JavaConversions._
      q.setFilter("groupUrlname == gurl")
      q.declareParameters("String gurl")
      val l = q.execute(urlname).asInstanceOf[java.util.List[Poll]]
      l.size
      l
    })
}
