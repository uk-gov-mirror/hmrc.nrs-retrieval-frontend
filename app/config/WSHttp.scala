/*
 * Copyright 2021 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package config

/*
 * Copyright 2018 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import akka.actor.ActorSystem
import javax.inject.{Inject, Named, Singleton}
import com.google.inject.ImplementedBy
import com.typesafe.config.Config
import play.api.{Configuration, Environment}
import uk.gov.hmrc.http._
import uk.gov.hmrc.http.hooks.HttpHooks
import uk.gov.hmrc.play.audit.http.HttpAuditing
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import uk.gov.hmrc.play.audit.model.Audit
import uk.gov.hmrc.play.http.ws._
import play.api.libs.ws.WSClient

trait Hooks extends HttpHooks with HttpAuditing {
  val hooks = Seq(AuditingHook)
}

@ImplementedBy(classOf[WSHttp])
trait WSHttpT extends HttpGet with WSGet
  with HttpPut with WSPut
  with HttpPost with WSPost
  with HttpDelete with WSDelete
  with HttpPatch with WSPatch
  with HttpHead with WSHead

@Singleton
class WSHttp @Inject() (val environment: Environment, val runModeConfig: Configuration, val appNameConfig: Configuration, val wsClient: WSClient)
                       (implicit val actorSystem: ActorSystem) extends WSHttpT {
  override val hooks = NoneRequired

  override protected def configuration: Option[Config] = None
}

class MicroserviceAudit @Inject()(@Named("appName") val applicationName: String,
                                  val auditConnector: AuditConnector) extends Audit(applicationName, auditConnector)
