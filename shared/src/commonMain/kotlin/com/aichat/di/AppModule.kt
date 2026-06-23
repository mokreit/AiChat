package com.aichat.di

import org.koin.core.module.Module
import org.koin.dsl.module

val appModule: Module = module {
    // 后续 Phase 2-4 会在这里注入 Repository、AiProvider 等
}
