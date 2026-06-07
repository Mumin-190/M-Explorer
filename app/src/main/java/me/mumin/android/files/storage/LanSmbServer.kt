/*
 * Copyright (c) 2021 Mumin <mumin@example.com>
 * All Rights Reserved.
 */

package me.mumin.android.files.storage

import java.net.InetAddress

data class LanSmbServer(
    val host: String,
    val address: InetAddress
) : Comparable<LanSmbServer> {
    override fun compareTo(other: LanSmbServer): Int =
        compareValuesBy(this, other, { it.address.hostAddress }, { it.host })
}
