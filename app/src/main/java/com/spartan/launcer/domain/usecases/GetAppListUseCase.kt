package com.spartan.launcer.domain.usecases

import com.spartan.launcer.data.AppRepository
import com.spartan.launcer.data.model.AppInfo
import kotlinx.coroutines.flow.Flow

class GetAppListUseCase(private val repository: AppRepository) {

    fun allApps(): Flow<List<AppInfo>> = repository.allApps

    fun favorites(): Flow<List<AppInfo>> = repository.favorites
}