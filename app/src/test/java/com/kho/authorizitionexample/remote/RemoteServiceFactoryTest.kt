package com.kho.authorizitionexample.remote

import com.google.gson.Gson
import com.kho.authorizitionexample.data.Constants
import com.kho.authorizitionexample.data.exception.AuthorizationErrorException
import com.kho.authorizitionexample.data.local.TokenManager
import com.kho.authorizitionexample.data.model.authen.TokenResponse
import com.kho.authorizitionexample.data.model.list.SomeListResponse
import com.kho.authorizitionexample.data.remote.RemoteServiceFactory
import com.kho.authorizitionexample.data.remote.oauth_manager.BasicAuthInterceptor
import com.kho.authorizitionexample.data.remote.oauth_manager.BasicAuthenticator
import com.kho.authorizitionexample.data.remote.oauth_manager.OAuthTokenDelegateImpl
import com.kho.authorizitionexample.data.remote.service.AppService
import com.nhaarman.mockito_kotlin.*
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class RemoteServiceFactoryTest {
    private val baseUrl = "/"
    private val pathGetData = "/notification/api/types?name.equals="
    private val pathRefreshToken = "/uaa/oauth/token"
    private val testData = SomeListResponse(1, "hello!")
    private val testDataJson = "[{\"name\":\"${testData.name}\",\"id\":${testData.id}}]"

    private val mockWebServer = MockWebServer()
    private val httpUrl = mockWebServer.url(baseUrl)
    private lateinit var authenticator: BasicAuthenticator
    private lateinit var authInterceptor: BasicAuthInterceptor
    private lateinit var oAuth: OAuthTokenDelegateImpl
    private val tokenStorage = mock<TokenManager>()

    private lateinit var testApi: AppService

    @Before
    fun setUp() {
        oAuth = OAuthTokenDelegateImpl(tokenStorage)
        authenticator = BasicAuthenticator(oAuth)
        authInterceptor = BasicAuthInterceptor("", "")
        testApi = RemoteServiceFactory(
            tokenStorage, authenticator
            , authInterceptor, oAuth, httpUrl.toString()
        ).createAppService()

    }

    @Test
    fun `When succeed with valid data, Then response is parsed`() {
        //given
        val expect = listOf(testData)
        val successResponse = MockResponse().setBody(testDataJson)
        mockWebServer.enqueue(successResponse)

        //when
        val response = testApi.getSmsType("").test()

        //then
        mockWebServer.takeRequest()
        response.assertValue(expect)
    }

    @Test
    fun `When a call is done, Then auth header is added`() {
        //given
        val tokenReponse = TokenResponse(
            "Token", 0, "refreshToken"
            , Constants.httpHeaderBearerTokenPrefix
        )
        given(tokenStorage.get()).willReturn(tokenReponse)
        val successResponse = MockResponse().setBody(testDataJson)

        mockWebServer.enqueue(successResponse)

        //when
        testApi.getSmsType("").test()

        //then
        val recordedRequest = mockWebServer.takeRequest()
        val header = recordedRequest.getHeader("Authorization")
        assertThat(header, `is`("""${tokenReponse.tokenType} ${tokenReponse.accessToken}"""))
    }

    @Test
    fun `When fails with 401, Then authenticator refreshes token`() {
        //given
        val invalidTokenResponse = MockResponse().setResponseCode(401)

        val authResponse = TokenResponse(
            "newToken", 0, "refreshToken"
            , Constants.httpHeaderBearerTokenPrefix
        )
        val responseBody = Gson().toJson(authResponse, TokenResponse::class.java)
        val refreshResponse = MockResponse()
            .setResponseCode(200)
            .setBody(responseBody)

        given(tokenStorage.get()).willReturn(
            TokenResponse(
                "oldToken", 0, "refreshToken"
                , Constants.httpHeaderBearerTokenPrefix
            ),
            TokenResponse(
                "newToken", 0, "refreshToken"
                , Constants.httpHeaderBearerTokenPrefix
            )
        )

        val successResponse = MockResponse().setResponseCode(200).setBody(testDataJson)

        // Enqueue 401 response
        mockWebServer.enqueue(invalidTokenResponse)
        // Enqueue 200 refresh response
        mockWebServer.enqueue(refreshResponse)
        // Enqueue 200 original response
        mockWebServer.enqueue(successResponse)

        //when
        val response = testApi.getSmsType("").test()

        //then
        val request1 = mockWebServer.takeRequest()
        val request2 = mockWebServer.takeRequest()
        val request3 = mockWebServer.takeRequest()

        assertThat(request1.path, `is`(pathGetData))
        assertThat(request2.path, `is`(pathRefreshToken))
        assertThat(request3.path, `is`(pathGetData))
        verify(tokenStorage, times(2)).get()
        verify(tokenStorage).save(any())
        val header = request3.getHeader(Constants.httpHeaderAuthorization)
        assertThat(header, `is`("""${authResponse.tokenType} ${authResponse.accessToken}"""))
        response.assertComplete()
    }


    @Test
    fun `When fails with 401 2 times, Then not success cause retry count exceeded`() {

        //given
        val invalidTokenResponse = MockResponse().setResponseCode(401)

        val authResponse1 = TokenResponse(
            "newToken1", 0, "refreshToken"
            , Constants.httpHeaderBearerTokenPrefix
        )
        val authResponse2 = TokenResponse(
            "newToken2", 0, "refreshToken"
            , Constants.httpHeaderBearerTokenPrefix
        )

        val responseBody1 = Gson().toJson(authResponse1, TokenResponse::class.java)
        val refreshResponse1 = MockResponse()
            .setResponseCode(200)
            .setBody(responseBody1)
        val responseBody2 = Gson().toJson(authResponse2, TokenResponse::class.java)
        val refreshResponse2 = MockResponse()
            .setResponseCode(200)
            .setBody(responseBody2)

        given(tokenStorage.get()).willReturn(
            TokenResponse(
                "oldToken", 0, "refreshToken"
                , Constants.httpHeaderBearerTokenPrefix
            ),
            TokenResponse(
                "newToken1", 0, "refreshToken"
                , Constants.httpHeaderBearerTokenPrefix
            ),
            TokenResponse(
                "newToken2", 0, "refreshToken"
                , Constants.httpHeaderBearerTokenPrefix
            ),
            TokenResponse(
                "newToken3", 0, "refreshToken"
                , Constants.httpHeaderBearerTokenPrefix
            )
        )

        // Enqueue 401 response round 0
        mockWebServer.enqueue(invalidTokenResponse)
        // Enqueue 200 refresh response
        mockWebServer.enqueue(refreshResponse1)
        // Enqueue 401 response round 1
        mockWebServer.enqueue(invalidTokenResponse)
        // Enqueue 200 refresh response
        mockWebServer.enqueue(refreshResponse2)
        // Enqueue 401 response round 2
        mockWebServer.enqueue(invalidTokenResponse)

        //when
        val response = testApi.getSmsType("").test()

        //then
        val request1 = mockWebServer.takeRequest()
        val request2 = mockWebServer.takeRequest()
        val request3 = mockWebServer.takeRequest()
        val request4 = mockWebServer.takeRequest()
        val request5 = mockWebServer.takeRequest()


        assertThat(request1.path, `is`(pathGetData))
        assertThat(request2.path, `is`(pathRefreshToken))
        assertThat(request3.path, `is`(pathGetData))
        assertThat(request4.path, `is`(pathRefreshToken))
        assertThat(request5.path, `is`(pathGetData))

        assertThat(request3.getHeader(Constants.httpHeaderRetryCount), `is`("1"))
        assertThat(request5.getHeader(Constants.httpHeaderRetryCount), `is`("2"))
        assertThat(
            request3.getHeader(Constants.httpHeaderAuthorization),
            `is`("""${authResponse1.tokenType} ${authResponse1.accessToken}""")
        )
        assertThat(
            request5.getHeader(Constants.httpHeaderAuthorization),
            `is`("""${authResponse2.tokenType} ${authResponse2.accessToken}""")
        )

        response.assertError(AuthorizationErrorException::class.java)
        response.assertErrorMessage("Retry count exceeded")

    }
}