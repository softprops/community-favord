package com.meetup.models

import javax.jdo.annotations._

@PersistenceCapable(
  identityType = IdentityType.APPLICATION,
  detachable = "true"
)
class User() {
  @PrimaryKey
  @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
  var id: String = _

  @Persistent
  var name: String = _

  @Persistent
  var photo: String = _
}
