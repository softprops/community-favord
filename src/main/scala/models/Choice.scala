package com.meetup.models

import javax.jdo.annotations._

@PersistenceCapable(
  identityType = IdentityType.APPLICATION,
  detachable = "true"
)
class Choice() {
  @PrimaryKey
  @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
  var id: com.google.appengine.api.datastore.Key = _

  @Persistent
  var value: String = _
}
