package com.roro.storage.data.datasource

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import java.io.File
import java.util.UUID
import javax.inject.Inject

class RecorderLocalFileDataSource @Inject constructor(
    @ApplicationContext private val context: Context
) {


}