package com.github.huymaster.server.core.service

import com.github.huymaster.server.core.database.table.UserDeviceTable
import com.github.huymaster.server.core.database.table.UserInfoTable
import com.github.huymaster.server.core.database.table.UserRoleTable
import com.github.huymaster.server.core.database.table.UserTable

class UserService : BaseService() {
    private val users by injectRepository(UserTable)
    private val roles by injectRepository(UserRoleTable)
    private val infos by injectRepository(UserInfoTable)
    private val devices by injectRepository(UserDeviceTable)
}