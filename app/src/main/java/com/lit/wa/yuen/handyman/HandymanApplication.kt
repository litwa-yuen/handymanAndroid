package com.lit.wa.yuen.handyman

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * The base class for your application, annotated with @HiltAndroidApp.
 * This triggers Hilt's code generation for the entire application component,
 * which is necessary to inject dependencies into activities and view models.
 */
@HiltAndroidApp
class HandymanApplication : Application()