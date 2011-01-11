package com.meetup.models

import javax.jdo.annotations._

@PersistenceCapable(
  identityType = IdentityType.APPLICATION,
  detachable = "true"
)
class Poll() {
  @PrimaryKey
  @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
  var id: com.google.appengine.api.datastore.Key = _

  @Persistent
  var name: String = _

  @Persistent
  var content: String = _

  @Persistent
  var groupUrlname: String = _

  /** meetup_id of creating user */
  @Persistent
  var creator: String = _

  @Persistent
  var maxVotes: Int = _

  @Persistent
  var choices: java.util.List[Choice] = _
}
