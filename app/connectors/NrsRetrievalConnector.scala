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
import models.audit.{NonRepudiationStoreDownload, NonRepudiationStoreRetrieve, NonRepudiationStoreSearch}
import play.api.libs.ws.{WSClient, WSResponse}
import uk.gov.hmrc.play.http.logging.MdcLoggingExecutionContext._

import scala.concurrent.Future

@Singleton
class NrsRetrievalConnector @Inject()(val environment: Environment,
                                      val http: WSHttpT,
                                      val auditable: Auditable,
                                      ws: WSClient,
                                      implicit val appConfig: AppConfig) {

  val logger: Logger = Logger(this.getClass)

  def search(vrn: String)(implicit hc: HeaderCarrier): Future[Seq[NrsSearchResult]] = {
    logger.info(s"Search for VRN $vrn")

    val path = s"${appConfig.nrsRetrievalUrl}/submission-metadata?vrn=$vrn"

    // todo : these values need to come from stride-auth
    val authProviderId = "authProviderIdValue"
    val name = "nameValue"

    for{
      get <- http.GET[Seq[NrsSearchResult]](path)
        .map { r => r }
        .recover{
          case e if e.getMessage.contains("404") => Seq.empty[NrsSearchResult]
          case e if e.getMessage.contains("401") => {
            auditable.sendDataEvent(NonRepudiationStoreSearch(authProviderId, name, vrn, "Unauthorized", path))
            throw e
          }
        }
      _ <- auditable.sendDataEvent(NonRepudiationStoreSearch(authProviderId, name, vrn, get.seq.headOption.map(_.nrSubmissionId).getOrElse("(Empty)") ,path))
    } yield get
  }

  def submitRetrievalRequest(vaultName: String, archiveId: String)(implicit hc: HeaderCarrier): Future[HttpResponse] = {
    logger.info(s"Submit a retrieval request for vault: $vaultName, archive: $archiveId")

    val path = s"${appConfig.nrsRetrievalUrl}/submission-bundles/$vaultName/$archiveId/retrieval-requests"

    // todo : these values to come from stride-auth
    val authProviderId = "authProviderIdValue"
    val name = "nameValue"

    for {
      post <- http.POST(path, "", Seq.empty)
      _ <- auditable.sendDataEvent(NonRepudiationStoreRetrieve(authProviderId, name, vaultName, archiveId,
        if(post.allHeaders == null) "(Empty)" else post.header("nr-submission-id").getOrElse("(Empty)"), path))
    } yield post
  }

  def statusSubmissionBundle(vaultName: String, archiveId: String)(implicit hc: HeaderCarrier): Future[HttpResponse] = {
    logger.info(s"Get submission bundle status for vault: $vaultName, archive: $archiveId")
    val path = s"${appConfig.nrsRetrievalUrl}/submission-bundles/$vaultName/$archiveId"
    http.HEAD(path)
  }

  def getSubmissionBundle(vaultName: String, archiveId: String)(implicit hc: HeaderCarrier): Future[WSResponse] = {
    logger.info(s"Get submission bundle for vault: $vaultName, archive: $archiveId")
    val path = s"${appConfig.nrsRetrievalUrl}/submission-bundles/$vaultName/$archiveId"

    // todo : these values to come from stride-auth
    val authProviderId = "authProviderIdValue"
    val name = "nameValue"

    for{
      get <- ws.url(path).withHeaders(hc.headers ++ hc.extraHeaders ++ hc.otherHeaders: _*).get
      _ <- auditable.sendDataEvent(
        NonRepudiationStoreRetrieve(authProviderId, name, vaultName, archiveId, get.header("nr-submission-id").getOrElse("(Empty)"), path))
    }yield get
  }

  protected def mode: Mode = environment.mode

}
