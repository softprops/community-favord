package com.meetup.models

import javax.jdo.annotations._

@PersistenceCapable(
  identityType = IdentityType.APPLICATION,
  detachable = "true"
)
class Poll() {
  @PrimaryKey
  @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
  var id: String = _

  var groupId: Long = _

  var groupName: String = _
}
