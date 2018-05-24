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

package model

import config.AppConfig
import models.SearchResultUtils
import play.api.{Configuration, Environment}
import support.fixtures.{NrsSearchFixture, SearchFixture}
import uk.gov.hmrc.play.test.UnitSpec

class SearchTest extends UnitSpec with SearchFixture with NrsSearchFixture {

  private val env = Environment.simple()
  private val configuration = Configuration.load(env)
  private val appConfig = new AppConfig(configuration, env)
  private val searchResultUtils: SearchResultUtils = new SearchResultUtils(appConfig)

  "fromNrsSearchResult" should {
    "create a SearchResult from an NrsSearchResult" in {
      searchResultUtils.fromNrsSearchResult(nrsSearchResult) shouldBe(searchResult)
    }
  }

  "RetrievalLink.linktext" should {
    "create retrieval link text" in {
      searchResult.retrievalLink shouldBe "Retrieve VAT return 18 January 1970"
    }
  }

  "DownloadLink.linktext" should {
    "create download link text" in {
      searchResult.downloadLink shouldBe "Download VAT return 18 January 1970"
    }
  }

  "FailedText.linktext" should {
    "create download link text" in {
      searchResult.failedText shouldBe "Download VAT return 18 January 1970"
    }
  }

}
