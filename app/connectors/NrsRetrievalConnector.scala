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

package connectors

import javax.inject.{Inject, Singleton}
import play.api.{Environment, Logger}
import play.api.Mode.Mode
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import config.{AppConfig, Auditable, WSHttpT}
import models.NrsSearchResult
import models.audit.{NonRepudiationStoreRetrieve, NonRepudiationStoreSearch}
import uk.gov.hmrc.play.http.logging.MdcLoggingExecutionContext._

import scala.concurrent.Future

@Singleton
class NrsRetrievalConnector @Inject()(val environment: Environment,
                                      val http: WSHttpT,
                                      val auditable: Auditable,
                                      implicit val appConfig: AppConfig) {

  val logger: Logger = Logger(this.getClass)

  protected def mode: Mode = environment.mode

  def search(vrn: String)(implicit hc: HeaderCarrier): Future[Seq[NrsSearchResult]] = {
    logger.info(s"Search for VRN $vrn")

    // todo : these vavlues to come from stride-auth
    val authProviderId = "0"
    val name = "Name"

    val path = s"${appConfig.nrsRetrievalUrl}/submission-metadata?vrn=$vrn"

    auditable.sendDataEvent(NonRepudiationStoreSearch(authProviderId, name, vrn, path))

    http.GET[Seq[NrsSearchResult]](path)
  }

  def submitRetrievalRequest(vaultName: String, archiveId: String)(implicit hc: HeaderCarrier): Future[HttpResponse] = {
    logger.info(s"Submit a retrieval request for vault: $vaultName, archive: $archiveId")

    // todo : these vavlues to come from stride-auth
    val authProviderId = "0"
    val name = "Name"

    // todo : don't think we have this data item
    val vrn = ""

    val path = s"${appConfig.nrsRetrievalUrl}/submission-bundles/$vaultName/$archiveId/retrieval-requests"

    auditable.sendDataEvent(NonRepudiationStoreRetrieve(authProviderId, name, vrn, vatReturnPeriodKey, nrSubmissionId, vaultName, archiveId, path))

    http.doPostString(path, "", Seq.empty)
  }

  def statusSubmissionBundle(vaultName: String, archiveId: String)(implicit hc: HeaderCarrier): Future[HttpResponse] = {
    logger.info(s"Get submission bundle status for vault: $vaultName, archive: $archiveId")
    http.doHead(s"${appConfig.nrsRetrievalUrl}/submission-bundles/$vaultName/$archiveId")
  }

  def getSubmissionBundle(vaultName: String, archiveId: String)(implicit hc: HeaderCarrier): Future[HttpResponse] = {
    logger.info(s"Get submission bundle for vault: $vaultName, archive: $archiveId")
    http.doGet(s"${appConfig.nrsRetrievalUrl}/submission-bundles/$vaultName/$archiveId")
  }

}
