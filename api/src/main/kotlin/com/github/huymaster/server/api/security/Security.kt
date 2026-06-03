package com.github.huymaster.server.api.security

import org.bouncycastle.pqc.jcajce.provider.BouncyCastlePQCProvider
import java.security.Security

interface Security {
    companion object {
        init {
            if (Security.getProviders(BouncyCastlePQCProvider.PROVIDER_NAME) == null)
                Security.addProvider(BouncyCastlePQCProvider())
        }
    }
}