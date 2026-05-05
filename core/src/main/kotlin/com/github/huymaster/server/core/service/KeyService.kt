package com.github.huymaster.server.core.service

import com.github.huymaster.server.core.database.table.UserDeviceTable
import com.github.huymaster.server.core.database.table.UserTable

class KeyService : BaseService() {
    companion object {
    }

    private val users by injectRepository(UserTable)
    private val devices by injectRepository(UserDeviceTable)
}