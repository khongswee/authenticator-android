package com.kho.authorizitionexample.domain.usecase.list

import com.kho.authorizitionexample.data.local.TokenManager
import com.kho.authorizitionexample.data.model.list.SomeListResponse
import com.kho.authorizitionexample.data.repository.authen.AuthenRepository
import com.kho.authorizitionexample.data.repository.list.ListRespository
import com.kho.authorizitionexample.domain.base.BaseSingleUseCase
import com.kho.authorizitionexample.domain.thread.PostExecutionThread
import com.kho.authorizitionexample.data.thread.ThreadExecutor
import com.kho.authorizitionexample.domain.data.GetListUseCaseData
import io.reactivex.Single

class GetListUseCase(
    private val listRepository: ListRespository, private val loginRepository: AuthenRepository,private val tokenManager: TokenManager, threadExecutor: ThreadExecutor,
    postExecutionThread: PostExecutionThread
) : BaseSingleUseCase<List<SomeListResponse>, GetListUseCaseData>(threadExecutor, postExecutionThread) {
    override fun buildUseCaseObservable(params: GetListUseCaseData?): Single<List<SomeListResponse>> {
        params?.let {
            return loginRepository.login(params.accessTokenRequest).flatMap {
                tokenManager.save(it)
                listRepository.getList(params.typeId)
            }
        }?:run {
            return Single.error(NullPointerException("Params Null"))
        }

    }
}
