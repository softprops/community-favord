package com.meetup.models

import javax.jdo.annotations._

@PersistenceCapable(
  identityType = IdentityType.APPLICATION,
  detachable = "true"
)
class Vote() {
  @PrimaryKey
  @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
  var id: String = _

  @Persistent
  var choice: Choice = _

  /* meetup_id of casting user */
  @Persistent
  var caster: String = _
}
